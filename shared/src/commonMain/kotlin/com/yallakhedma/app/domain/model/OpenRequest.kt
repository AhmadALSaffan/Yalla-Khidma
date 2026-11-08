package com.yallakhedma.app.domain.model

import kotlinx.serialization.Serializable

/**
 * A client's open job post ("I need X") that providers answer with offers.
 * Accepting an offer turns it into a regular [ServiceRequest] booking.
 */
@Serializable
data class OpenRequest(
    val id: String = "",
    val clientId: String = "",
    val clientName: String = "",
    val clientPhoto: String = "",
    val categoryId: String = "",
    val categoryNameAr: String = "",
    val categoryNameEn: String = "",
    val description: String = "",
    val images: List<String> = emptyList(),
    val locationLabel: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val budgetMin: Double = 0.0,
    val budgetMax: Double = 0.0,
    val currency: String = "ر.س",
    val timing: String = TIMING_WEEK,
    val preferredDate: Long = 0L,
    val status: String = STATUS_OPEN,
    val offersCount: Int = 0,
    val acceptedOfferId: String = "",
    val createdAt: Long = 0L,
) {
    fun categoryName(arabic: Boolean) = if (arabic) categoryNameAr.ifBlank { categoryNameEn } else categoryNameEn.ifBlank { categoryNameAr }

    companion object {
        const val STATUS_OPEN = "open"
        const val STATUS_ASSIGNED = "assigned"
        const val STATUS_CLOSED = "closed"

        const val TIMING_TODAY = "today"
        const val TIMING_TOMORROW = "tomorrow"
        const val TIMING_WEEK = "week"
        const val TIMING_DATE = "date"
    }
}

/** A provider's quote on an [OpenRequest]. */
@Serializable
data class Offer(
    val id: String = "",
    val requestId: String = "",
    val requestSummary: String = "",
    val clientId: String = "",
    val providerId: String = "",
    val providerName: String = "",
    val providerPhoto: String = "",
    val providerProfession: String = "",
    val providerRating: Double = 0.0,
    val providerVerified: Boolean = false,
    val price: Double = 0.0,
    val currency: String = "ر.س",
    val eta: String = "",
    val message: String = "",
    val status: String = STATUS_PENDING,
    val createdAt: Long = 0L,
) {
    companion object {
        const val STATUS_PENDING = "pending"
        const val STATUS_ACCEPTED = "accepted"
        const val STATUS_DECLINED = "declined"
    }
}
