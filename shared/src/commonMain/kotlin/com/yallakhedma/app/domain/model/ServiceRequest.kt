package com.yallakhedma.app.domain.model

import kotlinx.serialization.Serializable

/**
 * A booking request created when a client books a service. The provider sees
 * pending requests on their dashboard and accepts/rejects them.
 */
@Serializable
data class ServiceRequest(
    val id: String = "",
    val serviceId: String = "",
    val serviceTitle: String = "",
    val providerId: String = "",
    val clientId: String = "",
    val clientName: String = "",
    val clientPhotoUrl: String = "",
    val status: String = STATUS_PENDING,
    val createdAt: Long = 0L,

    // Snapshot of the service + provider at booking time so the bookings
    // list/detail can render rich info without extra reads. All optional with
    // safe defaults so legacy docs keep deserialising cleanly.
    val serviceImageUrl: String = "",
    val serviceDescription: String = "",
    val servicePrice: String = "",
    val serviceCategoryTag: String = "",
    val providerName: String = "",
    val providerPhotoUrl: String = "",
    val providerProfession: String = "",

    // The price the client offered when sending the booking request, and the
    // currency it's in (defaulting to Saudi Riyal). Payment fields capture
    // when/how the client paid after the provider accepted.
    val proposedPrice: Double = 0.0,
    val currency: String = "ر.س",
    val paid: Boolean = false,
    val paidAt: Long = 0L,
    val paymentMethodBrand: String = "",
    val paymentMethodLast4: String = "",

    // Discount snapshot captured at payment time. `paidAmount` is what the
    // client actually charged (= proposedPrice if no coupon). `couponCode` +
    // `couponLabel` are stored verbatim so the receipt stays correct even if
    // the coupon doc is later edited or deleted.
    val paidAmount: Double = 0.0,
    val couponCode: String = "",
    val couponLabel: String = "",
) {
    companion object {
        const val STATUS_PENDING = "pending"
        const val STATUS_ACCEPTED = "accepted"
        const val STATUS_REJECTED = "rejected"
    }
}
