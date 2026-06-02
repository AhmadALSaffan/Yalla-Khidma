package com.yallakhedma.app.util

import androidx.compose.runtime.Composable

@Composable
actual fun rememberLocationPicker(
    onResult: (PickedLocation?) -> Unit,
): () -> Unit = {
    // TODO: bridge CoreLocation (CLLocationManager) + reverse geocoding from Swift.
    onResult(null)
}
