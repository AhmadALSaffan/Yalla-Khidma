package com.yallakhedma.app.util

import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private const val EARTH_RADIUS_KM = 6371.0

/** Great-circle distance between two lat/lng points in kilometres. */
fun haversineKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val dLat = (lat2 - lat1).toRadians()
    val dLng = (lng2 - lng1).toRadians()
    val a = sin(dLat / 2).pow(2) +
        cos(lat1.toRadians()) * cos(lat2.toRadians()) * sin(dLng / 2).pow(2)
    return 2 * EARTH_RADIUS_KM * asin(sqrt(a))
}

/** Friendly Arabic distance label, e.g. "750 م" / "2.4 كم". */
fun formatDistanceKm(km: Double): String =
    if (km < 1.0) "${(km * 1000).toInt()} م"
    else "${(km * 10).toInt() / 10.0} كم"

private fun Double.toRadians(): Double = this * PI / 180.0
