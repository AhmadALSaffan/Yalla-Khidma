package com.yallakhedma.app.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
actual fun rememberLocationPicker(
    onResult: (PickedLocation?) -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) scope.launch { onResult(resolveLocation(context)) }
        else onResult(null)
    }

    return {
        if (hasLocationPermission(context)) {
            scope.launch { onResult(resolveLocation(context)) }
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
}

private fun hasLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED

private suspend fun resolveLocation(context: Context): PickedLocation? =
    withContext(Dispatchers.IO) {
        runCatching {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val location = lastKnownLocation(lm) ?: return@runCatching null
            val label = geocodeLabel(context, location.latitude, location.longitude)
                ?: "${"%.4f".format(location.latitude)}, ${"%.4f".format(location.longitude)}"
            PickedLocation(label, location.latitude, location.longitude)
        }.getOrNull()
    }

@Suppress("MissingPermission")
private fun lastKnownLocation(lm: LocationManager): Location? {
    val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
    return providers
        .mapNotNull { provider -> runCatching { lm.getLastKnownLocation(provider) }.getOrNull() }
        .maxByOrNull { it.time }
}

private fun geocodeLabel(context: Context, lat: Double, lng: Double): String? = runCatching {
    @Suppress("DEPRECATION")
    val results = Geocoder(context).getFromLocation(lat, lng, 1)
    val address = results?.firstOrNull() ?: return null
    // Prefer a "city, district" style label; fall back to whatever is available.
    listOfNotNull(address.subAdminArea ?: address.locality, address.adminArea)
        .distinct()
        .joinToString("، ")
        .ifBlank { address.getAddressLine(0) }
}.getOrNull()
