package com.yallakhedma.app.domain.discovery

import com.yallakhedma.app.domain.model.Service
import com.yallakhedma.app.domain.model.ServiceAvailability
import com.yallakhedma.app.util.haversineKm
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

data class GeoPoint(val latitude: Double, val longitude: Double)

/** Everything the hard filters need. The same context is used for every retriever. */
data class SearchContext(
    val clientLocation: GeoPoint,
    val now: Instant,
    /** Applied when a listing has coordinates but no radius of its own. */
    val defaultRadiusKm: Double = DEFAULT_RADIUS_KM,
) {
    companion object { const val DEFAULT_RADIUS_KM = 10.0 }
}

/** What the listing's own schedule says for the requested time. All values come from its data. */
data class AvailabilityNote(
    val kind: Kind,
    val fromMinute: Int = 0,
    val untilMinute: Int = 0,
    val aroundTheClock: Boolean = false,
) {
    enum class Kind {
        /** Requested now and the listing is inside its working window. */
        Now,
        /** Requested today and the listing still has working time left today. */
        Today,
        Tomorrow,
        /** No time requested; the listing works today. */
        OpenToday,
        /** No time requested; the listing does not work today. */
        ClosedToday,
        /** The listing has no usable schedule. */
        Unknown,
    }
}

/** Grounded explanation parts — rendered per language in the UI, never free text. */
data class MatchReason(
    val matchedTerms: List<String>,
    /** Set when the match rests on the category rather than on quoted words. */
    val categoryTag: String?,
    val availability: AvailabilityNote,
    val distanceKm: Double,
    val radiusKm: Double,
    val radiusIsDefault: Boolean,
)

data class RankedListing(val service: Service, val score: Double, val reason: MatchReason)

enum class ExclusionReason { OutsideArea, UnknownLocation, Unavailable, UnknownAvailability }

data class ExcludedListing(
    val service: Service,
    val score: Double,
    val reason: ExclusionReason,
    val distanceKm: Double?,
    val radiusKm: Double?,
)

data class DiscoveryResult(
    val query: ParsedQuery,
    val results: List<RankedListing>,
    /** Relevant listings removed by a hard filter, strongest first. */
    val excluded: List<ExcludedListing>,
)

/**
 * Retrieve → hard-filter (service area, availability) → rank → explain.
 * The retriever is swappable so keyword and semantic search share identical
 * filters and ranking.
 */
class DiscoveryEngine(
    private val retriever: Retriever = SemanticRetriever(),
    private val maxResults: Int = 10,
) {

    fun search(rawQuery: String, listings: List<Service>, context: SearchContext): DiscoveryResult {
        val query = QueryParser.parse(rawQuery)
        val candidates = retriever.retrieve(query, listings)
        val ranked = ArrayList<RankedListing>()
        val excluded = ArrayList<ExcludedListing>()

        for (c in candidates) {
            val s = c.service
            when (val outcome = applyFilters(s, query.time, context)) {
                is FilterOutcome.Rejected ->
                    excluded += ExcludedListing(s, c.score, outcome.reason, outcome.distanceKm, outcome.radiusKm)
                is FilterOutcome.Passed -> ranked += RankedListing(
                    service = s,
                    score = c.score,
                    reason = MatchReason(
                        matchedTerms = c.matchedTerms,
                        categoryTag = s.categoryTag.takeIf { c.matchedTerms.isEmpty() && c.categoryFit && it.isNotBlank() },
                        availability = outcome.availability,
                        distanceKm = outcome.distanceKm,
                        radiusKm = outcome.radiusKm,
                        radiusIsDefault = outcome.radiusIsDefault,
                    ),
                )
            }
        }

        val results = ranked
            .sortedWith(
                compareByDescending<RankedListing> { it.score }
                    .thenByDescending { it.service.rating }
                    .thenBy { it.reason.distanceKm },
            )
            .take(maxResults)
        return DiscoveryResult(query, results, excluded.sortedByDescending { it.score })
    }

    sealed interface FilterOutcome {
        data class Passed(
            val distanceKm: Double,
            val radiusKm: Double,
            val radiusIsDefault: Boolean,
            val availability: AvailabilityNote,
        ) : FilterOutcome
        data class Rejected(val reason: ExclusionReason, val distanceKm: Double?, val radiusKm: Double?) : FilterOutcome
    }

    companion object {

        /**
         * The hard filters, from structured data only: (1) the client must be inside
         * the listing's service area; (2) if a time was requested, the listing's
         * schedule must confirm it — unknown availability does not pass.
         */
        fun applyFilters(s: Service, time: TimeIntent, context: SearchContext): FilterOutcome {
            if (s.latitude == 0.0 && s.longitude == 0.0) {
                return FilterOutcome.Rejected(ExclusionReason.UnknownLocation, null, null)
            }
            val distance = haversineKm(context.clientLocation.latitude, context.clientLocation.longitude, s.latitude, s.longitude)
            val radiusIsDefault = s.serviceRadiusKm <= 0.0
            val radius = if (radiusIsDefault) context.defaultRadiusKm else s.serviceRadiusKm
            if (distance > radius) return FilterOutcome.Rejected(ExclusionReason.OutsideArea, distance, radius)

            val note = availabilityFor(s.availability, time, context.now)
            if (time != TimeIntent.None) {
                when (note.kind) {
                    AvailabilityNote.Kind.Unknown -> return FilterOutcome.Rejected(ExclusionReason.UnknownAvailability, distance, radius)
                    AvailabilityNote.Kind.ClosedToday -> return FilterOutcome.Rejected(ExclusionReason.Unavailable, distance, radius)
                    else -> Unit
                }
            }
            return FilterOutcome.Passed(distance, radius, radiusIsDefault, note)
        }

        /**
         * Evaluates [availability] for [time], resolving "today"/"tomorrow" in the
         * listing's own time zone (the service area's local calendar). Returns
         * [AvailabilityNote.Kind.ClosedToday] for "not available" so callers can filter.
         */
        fun availabilityFor(availability: ServiceAvailability?, time: TimeIntent, now: Instant): AvailabilityNote {
            val a = availability ?: return AvailabilityNote(AvailabilityNote.Kind.Unknown)
            if (a.days.isEmpty() || a.endMinute <= a.startMinute) return AvailabilityNote(AvailabilityNote.Kind.Unknown)
            val zone = runCatching { TimeZone.of(a.timeZone) }.getOrNull()
                ?: return AvailabilityNote(AvailabilityNote.Kind.Unknown)
            val local = now.toLocalDateTime(zone)
            val minuteNow = local.hour * 60 + local.minute
            val today = local.date
            val tomorrow = today.plus(1, DateTimeUnit.DAY)
            val h24 = a.isAroundTheClock

            fun worksOn(date: LocalDate) = date.dayOfWeek.isoDayNumber in a.days && date.toString() !in a.bookedDates
            fun note(kind: AvailabilityNote.Kind) = AvailabilityNote(kind, a.startMinute, a.endMinute, h24)
            val closed = note(AvailabilityNote.Kind.ClosedToday)

            return when (time) {
                TimeIntent.Now ->
                    if (worksOn(today) && minuteNow >= a.startMinute && minuteNow < a.endMinute) note(AvailabilityNote.Kind.Now) else closed
                TimeIntent.Today ->
                    if (worksOn(today) && minuteNow < a.endMinute) note(AvailabilityNote.Kind.Today) else closed
                TimeIntent.Tomorrow ->
                    if (worksOn(tomorrow)) note(AvailabilityNote.Kind.Tomorrow) else closed
                TimeIntent.None ->
                    if (worksOn(today)) note(AvailabilityNote.Kind.OpenToday) else closed
            }
        }
    }
}
