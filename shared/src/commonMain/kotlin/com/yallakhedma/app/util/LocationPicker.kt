package com.yallakhedma.app.util

import androidx.compose.runtime.Composable

/** A resolved device location: a human-readable label + raw coordinates. */
data class PickedLocation(
    val label: String,
    val latitude: Double,
    val longitude: Double,
)

/**
 * Returns a launcher function. Calling it requests location permission (if
 * needed), reads the device GPS location, reverse-geocodes it to a readable
 * label, and invokes [onResult]. [onResult] gets null if the permission was
 * denied or no location could be determined.
 *
 * - Android: LocationManager + Geocoder via the Activity Result permission API.
 * - iOS: stub (returns null) until CoreLocation is bridged.
 */
@Composable
expect fun rememberLocationPicker(
    onResult: (PickedLocation?) -> Unit,
): () -> Unit
