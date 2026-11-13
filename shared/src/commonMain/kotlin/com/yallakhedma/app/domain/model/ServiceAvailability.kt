package com.yallakhedma.app.domain.model

import kotlinx.serialization.Serializable

/**
 * Structured weekly availability of a listing, evaluated in [timeZone].
 * A listing without this object has **unknown** availability — the app must
 * say so rather than guess.
 *
 * - [days]: ISO day numbers (1 = Monday … 7 = Sunday) the provider works.
 * - [startMinute] / [endMinute]: working window in minutes from local midnight.
 *   `0..1440` means around the clock.
 * - [bookedDates]: ISO dates ("2026-11-13") the provider is fully booked.
 */
@Serializable
data class ServiceAvailability(
    val timeZone: String = "",
    val days: List<Int> = emptyList(),
    val startMinute: Int = 0,
    val endMinute: Int = 0,
    val bookedDates: List<String> = emptyList(),
) {
    val isAroundTheClock: Boolean get() = startMinute <= 0 && endMinute >= MINUTES_PER_DAY

    companion object {
        const val MINUTES_PER_DAY = 24 * 60
    }
}
