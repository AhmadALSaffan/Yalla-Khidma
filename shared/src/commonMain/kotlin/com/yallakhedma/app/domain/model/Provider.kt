package com.yallakhedma.app.domain.model

import kotlinx.serialization.Serializable

/**
 * A service provider profile shown on the home screen and in search results.
 * `photoUrl` may be either a Firebase Storage path (e.g. "providers/ahmed.jpg")
 * or a full http(s) URL — the UI resolves either form.
 */
@Serializable
data class Provider(
    val id: String = "",
    val name: String = "",
    val profession: String = "",
    val photoUrl: String = "",
    val rating: Double = 0.0,
    val reviewsCount: Int = 0,
    val isFeatured: Boolean = false,
    val category: String = "",
    // Incremented each time one of this provider's services is booked.
    val bookingsCount: Int = 0,

    // Richer profile details (all optional — safe defaults for old docs).
    val bio: String = "",
    val city: String = "",
    val yearsExperience: Int = 0,
    val completedJobs: Int = 0,
    val verified: Boolean = false,
    val phone: String = "",
    val services: List<String> = emptyList(),
)
