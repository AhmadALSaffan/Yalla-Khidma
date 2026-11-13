package com.yallakhedma.app.discovery

import com.yallakhedma.app.data.demo.DemoDiscoveryCatalog
import com.yallakhedma.app.domain.discovery.ArabicText
import com.yallakhedma.app.domain.discovery.AvailabilityNote
import com.yallakhedma.app.domain.discovery.DiscoveryEngine
import com.yallakhedma.app.domain.discovery.ExclusionReason
import com.yallakhedma.app.domain.discovery.KeywordRetriever
import com.yallakhedma.app.domain.discovery.QueryParser
import com.yallakhedma.app.domain.discovery.SearchContext
import com.yallakhedma.app.domain.discovery.TimeIntent
import com.yallakhedma.app.domain.model.Service
import com.yallakhedma.app.domain.model.ServiceAvailability
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DiscoveryEngineTest {

    private val engine = DiscoveryEngine()
    private val listings = DiscoveryEval.listings
    private val ctx = DiscoveryEval.context
    private val story = "عندي مي عم تنزل من تحت المجلى وبدي حدا اليوم"

    @Test
    fun normalizesArabicVariants() {
        assertEquals(ArabicText.stems("المَجْلى"), ArabicText.stems("مجلي"))
        assertEquals(ArabicText.stems("الحنفيّة"), ArabicText.stems("حنفيات"))
        assertEquals(listOf("2026"), ArabicText.stems("٢٠٢٦"))
    }

    @Test
    fun parsesDialectTimeWords() {
        assertEquals(TimeIntent.Today, QueryParser.parse(story).time)
        assertEquals(TimeIntent.Today, QueryParser.parse("عايز كهربائي النهارده").time)
        assertEquals(TimeIntent.Tomorrow, QueryParser.parse("ابغى سباك بكرة").time)
        assertEquals(TimeIntent.Now, QueryParser.parse("بدي سباك هلق").time)
        assertEquals(TimeIntent.None, QueryParser.parse("تنظيف كنب").time)
    }

    @Test
    fun userStoryReturnsPlumbingWithGroundedReasons() {
        val result = engine.search(story, listings, ctx)
        val ids = result.results.map { it.service.id }
        assertTrue(ids.isNotEmpty(), "expected matches")
        assertEquals("demo-p01", ids.first())
        assertTrue("demo-p02" in ids.take(3), "24h emergency plumber should rank high: $ids")
        // Top results are plumbing.
        result.results.take(3).forEach { assertEquals("سباكة", it.service.categoryTag) }
        // Grounding: every quoted term is copied from the listing; hours come from its schedule.
        for (r in result.results) {
            val text = r.service.title + " " + r.service.description + " " + r.service.categoryTag
            r.reason.matchedTerms.forEach { term -> assertTrue(term in text, "'$term' not in listing ${r.service.id}") }
            val a = r.service.availability!!
            assertEquals(AvailabilityNote.Kind.Today, r.reason.availability.kind)
            assertEquals(a.startMinute, r.reason.availability.fromMinute)
            assertEquals(a.endMinute, r.reason.availability.untilMinute)
            assertTrue(r.reason.distanceKm <= r.reason.radiusKm)
        }
    }

    @Test
    fun strongMatchesOutsideAreaOrUnavailableAreExcluded() {
        val result = engine.search(story, listings, ctx)
        val ids = result.results.map { it.service.id }
        assertTrue("demo-p09" !in ids && "demo-p10" !in ids && "demo-p03" !in ids)
        val reasons = result.excluded.associate { it.service.id to it.reason }
        assertEquals(ExclusionReason.OutsideArea, reasons["demo-p09"])
        assertEquals(ExclusionReason.Unavailable, reasons["demo-p10"]) // fully booked today
        assertEquals(ExclusionReason.Unavailable, reasons["demo-p03"]) // closed on Fridays
        // p09 would otherwise be a top match: it is retrieved with a high score.
        assertTrue(result.excluded.first { it.service.id == "demo-p09" }.score >= result.results.last().score)
    }

    @Test
    fun resolvesTodayInListingTimeZone() {
        // 21:30 UTC Friday = 00:30 Saturday in Amman (UTC+3).
        val late = Instant.parse("2026-11-13T21:30:00Z")
        val saturdayOnly = ServiceAvailability("Asia/Amman", days = listOf(6), startMinute = 0, endMinute = 1440)
        val fridayOnly = saturdayOnly.copy(days = listOf(5))
        assertEquals(AvailabilityNote.Kind.Today, DiscoveryEngine.availabilityFor(saturdayOnly, TimeIntent.Today, late).kind)
        assertEquals(AvailabilityNote.Kind.ClosedToday, DiscoveryEngine.availabilityFor(fridayOnly, TimeIntent.Today, late).kind)
        // Past today's closing time → not available "today".
        val dayShift = ServiceAvailability("Asia/Amman", days = (1..7).toList(), startMinute = 8 * 60, endMinute = 18 * 60)
        val evening = Instant.parse("2026-11-13T16:00:00Z") // 19:00 Amman
        assertEquals(AvailabilityNote.Kind.ClosedToday, DiscoveryEngine.availabilityFor(dayShift, TimeIntent.Today, evening).kind)
        assertEquals(AvailabilityNote.Kind.Tomorrow, DiscoveryEngine.availabilityFor(dayShift, TimeIntent.Tomorrow, evening).kind)
    }

    @Test
    fun unknownAvailabilityIsExcludedForTimedRequestsAndLabeledOtherwise() {
        val live = Service(
            id = "live-1", title = "تصليح تسريب المجلى", categoryTag = "سباكة",
            latitude = 31.955, longitude = 35.920, availability = null,
        )
        val timed = engine.search(story, listOf(live), ctx)
        assertTrue(timed.results.isEmpty())
        assertEquals(ExclusionReason.UnknownAvailability, timed.excluded.single().reason)

        val untimed = engine.search("مي عم تنزل من تحت المجلى", listOf(live), ctx)
        assertEquals(AvailabilityNote.Kind.Unknown, untimed.results.single().reason.availability.kind)
        assertTrue(untimed.results.single().reason.radiusIsDefault)
    }

    @Test
    fun listingWithoutCoordinatesNeverPassesTheAreaFilter() {
        val noGeo = Service(id = "x", title = "تصليح تسريب المجلى", categoryTag = "سباكة")
        val r = engine.search("تسريب المجلى", listOf(noGeo), ctx)
        assertTrue(r.results.isEmpty())
        assertEquals(ExclusionReason.UnknownLocation, r.excluded.single().reason)
    }

    @Test
    fun unrelatedRequestReturnsNothing() {
        val r = engine.search("بدي حدا يصلح فرامل السيارة", listings, ctx)
        assertTrue(r.results.isEmpty(), "unexpected: ${r.results.map { it.service.id }}")
    }

    @Test
    fun englishRequestMatchesArabicListingsOnlySemantically() {
        val q = "my kitchen sink is leaking"
        assertTrue(DiscoveryEngine(KeywordRetriever()).search(q, listings, ctx).results.isEmpty())
        // No time requested, so the fully-booked p10 is eligible too.
        val top = engine.search(q, listings, ctx).results.first().service.id
        assertTrue(top in setOf("demo-p01", "demo-p10"), "top was $top")
    }

    @Test
    fun demoCatalogHasThirtyListingsInThreeCategories() {
        assertEquals(30, listings.size)
        assertEquals(setOf("سباكة", "كهرباء", "تنظيف"), listings.map { it.categoryTag }.toSet())
        assertTrue(listings.all { it.isDemo && DemoDiscoveryCatalog.isDemoId(it.id) })
    }

    @Test
    fun keywordAndSemanticNeverReturnFilteredListings() {
        assertEquals(0, DiscoveryEval.run(KeywordRetriever()).filterLeaks)
        assertEquals(0, DiscoveryEval.run(com.yallakhedma.app.domain.discovery.SemanticRetriever()).filterLeaks)
    }
}
