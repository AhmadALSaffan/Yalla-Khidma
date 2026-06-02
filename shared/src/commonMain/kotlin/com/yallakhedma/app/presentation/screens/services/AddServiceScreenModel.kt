package com.yallakhedma.app.presentation.screens.services

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.yallakhedma.app.data.datasource.StorageImagesDataSource
import com.yallakhedma.app.domain.model.Service
import com.yallakhedma.app.domain.repository.AuthRepository
import com.yallakhedma.app.domain.repository.ServiceRepository
import com.yallakhedma.app.domain.util.DataResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class AddServiceScreenModel(
    private val serviceRepository: ServiceRepository,
    private val authRepository: AuthRepository,
    private val storageDataSource: StorageImagesDataSource,
) : ScreenModel {

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    fun onTitle(v: String) = _state.update { it.copy(title = v, error = null) }
    fun onCategory(v: String) = _state.update { it.copy(category = v, error = null) }
    fun onDescription(v: String) = _state.update { it.copy(description = v, error = null) }
    fun onPrice(v: String) = _state.update { it.copy(price = v, error = null) }
    fun onDuration(v: String) = _state.update { it.copy(duration = v, error = null) }

    // Manual edit clears any GPS coordinates (the typed text no longer matches them).
    fun onDistance(v: String) = _state.update { it.copy(distance = v, latitude = 0.0, longitude = 0.0, error = null) }

    /** Sets the location from a GPS pick: label + coordinates. */
    fun onLocationPicked(label: String, lat: Double, lng: Double) =
        _state.update { it.copy(distance = label, latitude = lat, longitude = lng, error = null) }

    fun uploadImage(bytes: ByteArray) {
        screenModelScope.launch {
            _state.update { it.copy(imageUploading = true, error = null) }
            val uid = authRepository.currentUid()
            if (uid == null) {
                _state.update { it.copy(imageUploading = false, error = "لا يوجد مستخدم") }
                return@launch
            }
            val key = Clock.System.now().toEpochMilliseconds().toString()
            val url = runCatching { storageDataSource.uploadServiceImage(uid, key, bytes) }.getOrNull()
            if (url == null) {
                _state.update { it.copy(imageUploading = false, error = "تعذّر رفع الصورة") }
            } else {
                _state.update { it.copy(imageUploading = false, imageUrl = url) }
            }
        }
    }

    fun submit() {
        val s = state.value
        if (s.title.isBlank()) { _state.update { it.copy(error = "عنوان الخدمة مطلوب") }; return }
        if (s.price.isBlank()) { _state.update { it.copy(error = "السعر مطلوب") }; return }
        screenModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            val uid = authRepository.currentUid()
            if (uid == null) {
                _state.update { it.copy(isSaving = false, error = "لا يوجد مستخدم") }
                return@launch
            }
            val providerName = authRepository.currentUser.firstOrNull()?.name.orEmpty()
            val service = Service(
                title = s.title.trim(),
                categoryTag = s.category.trim(),
                description = s.description.trim(),
                priceFrom = s.price.trim(),
                distance = s.distance.trim(),
                imageUrl = s.imageUrl,
                providerId = uid,
                providerName = providerName,
                durationText = s.duration.trim(),
                latitude = s.latitude,
                longitude = s.longitude,
            )
            when (val r = serviceRepository.addService(service)) {
                is DataResult.Success -> _state.update { it.copy(isSaving = false, saved = true) }
                is DataResult.Error -> _state.update { it.copy(isSaving = false, error = r.message) }
                DataResult.Loading -> Unit
            }
        }
    }

    data class State(
        val title: String = "",
        val category: String = "",
        val description: String = "",
        val price: String = "",
        val duration: String = "",
        val distance: String = "",
        val latitude: Double = 0.0,
        val longitude: Double = 0.0,
        val imageUrl: String = "",
        val imageUploading: Boolean = false,
        val isSaving: Boolean = false,
        val error: String? = null,
        val saved: Boolean = false,
    )
}
