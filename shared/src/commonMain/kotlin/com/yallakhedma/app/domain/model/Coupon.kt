package com.yallakhedma.app.domain.model

import kotlinx.serialization.Serializable

/**
 * A discount coupon a provider creates. Either [discountPercent] (0-100) OR
 * [discountAmount] is used — the non-zero one wins, percent wins ties.
 *
 * Codes are stored as upper-case in [code]; lookup must normalise inputs.
 * Coupons are scoped to their [providerId] — a code from provider A cannot
 * be redeemed against a booking with provider B.
 */
@Serializable
data class Coupon(
    val id: String = "",
    val providerId: String = "",
    val code: String = "",
    val discountPercent: Double = 0.0,
    val discountAmount: Double = 0.0,
    val active: Boolean = true,
    val createdAt: Long = 0L,
) {
    /** Returns the new amount after applying this coupon (floored at 0). */
    fun apply(amount: Double): Double {
        val reduced = when {
            discountPercent > 0.0 -> amount - amount * (discountPercent / 100.0)
            discountAmount > 0.0 -> amount - discountAmount
            else -> amount
        }
        return if (reduced < 0.0) 0.0 else reduced
    }

    /** Human-readable label like "20%" or "50 ر.س". */
    fun label(currency: String = "ر.س"): String = when {
        discountPercent > 0.0 -> {
            val v = discountPercent
            if (v % 1.0 == 0.0) "${v.toLong()}%" else "$v%"
        }
        discountAmount > 0.0 -> {
            val v = discountAmount
            val s = if (v % 1.0 == 0.0) v.toLong().toString() else v.toString()
            "$s $currency"
        }
        else -> "—"
    }
}
