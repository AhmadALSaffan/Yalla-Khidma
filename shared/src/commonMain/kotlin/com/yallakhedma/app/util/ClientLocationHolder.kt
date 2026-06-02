package com.yallakhedma.app.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Process-wide cache of the client's last picked GPS location. Lets the home,
 * all-services and any other screen share the same fix without re-prompting.
 */
class ClientLocationHolder {
    private val _location = MutableStateFlow<PickedLocation?>(null)
    val location: StateFlow<PickedLocation?> = _location.asStateFlow()

    fun set(value: PickedLocation) { _location.value = value }
}
