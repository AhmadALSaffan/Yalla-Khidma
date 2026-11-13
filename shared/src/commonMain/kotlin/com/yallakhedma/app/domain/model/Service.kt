package com.yallakhedma.app.domain.model

import kotlinx.serialization.Serializable

/**
 * A bookable service offering shown in the "Nearby Services" rail. We store
 * `priceFrom` and `distance` as pre-formatted display strings to keep the test
 * data simple; production will switch to numeric currency + km values once we
 * have the formatting/geo machinery wired up.
 *
 * `imageUrl` accepts either a Firebase Storage path or a full http(s) URL.
 */
@Serializable
data class Service(
    val id: String = "",
    val title: String = "",
    val categoryTag: String = "",
    val description: String = "",
    val priceFrom: String = "",
    val distance: String = "",
    val imageUrl: String = "",
    val providerId: String = "",

    // Richer details (optional — safe defaults for old docs).
    val providerName: String = "",
    val durationText: String = "",
    val rating: Double = 0.0,

    // Coordinates of the service location (0.0 = unset), for distance sorting later.
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,

    // Service area radius around (latitude, longitude) in km. 0.0 = not provided.
    val serviceRadiusKm: Double = 0.0,
    // Structured schedule. null = availability unknown (shown as such in the UI).
    val availability: ServiceAvailability? = null,
    // True for the fictional listings bundled for the discovery demo.
    val isDemo: Boolean = false,
)
