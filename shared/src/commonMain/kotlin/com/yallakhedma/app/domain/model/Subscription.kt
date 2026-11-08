package com.yallakhedma.app.domain.model

import kotlinx.serialization.Serializable
import kotlin.math.roundToLong

/** Provider subscription plans. Prices are monthly, in USD. */
enum class PlanTier(val key: String, val monthlyUsd: Double, val monthlyRequests: Int?) {
    Basic("basic", 9.99, 10),
    Pro("pro", 24.99, 50),
    Vip("vip", 49.99, null);

    /** Price for one billing period; yearly = 12 months with 20% off. */
    fun price(yearly: Boolean): Double =
        if (yearly) ((monthlyUsd * 12 * 0.8) * 100).roundToLong() / 100.0 else monthlyUsd

    companion object {
        fun fromKey(key: String?): PlanTier? = entries.firstOrNull { it.key == key }
    }
}

/** One document per provider at `subscriptions/{providerId}`. */
@Serializable
data class Subscription(
    val providerId: String = "",
    val tier: String = "",
    val billing: String = BILLING_MONTHLY,
    val status: String = STATUS_TRIAL,
    val price: Double = 0.0,
    val tax: Double = 0.0,
    val currency: String = "USD",
    val startedAt: Long = 0L,
    val trialEndsAt: Long = 0L,
    val renewsAt: Long = 0L,
    val cardBrand: String = "",
    val cardLast4: String = "",
) {
    val plan: PlanTier? get() = PlanTier.fromKey(tier)
    val yearly: Boolean get() = billing == BILLING_YEARLY
    val isLive: Boolean get() = status == STATUS_TRIAL || status == STATUS_ACTIVE

    fun daysLeft(now: Long): Int = (((renewsAt - now).coerceAtLeast(0)) / DAY_MS).toInt()

    companion object {
        const val BILLING_MONTHLY = "monthly"
        const val BILLING_YEARLY = "yearly"
        const val STATUS_TRIAL = "trial"
        const val STATUS_ACTIVE = "active"
        const val STATUS_CANCELLED = "cancelled"
        const val TRIAL_DAYS = 7
        const val TAX_RATE = 0.15
        const val DAY_MS = 24L * 60 * 60 * 1000
    }
}
