package com.yallakhedma.app.presentation.screens.provider_setup

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.yallakhedma.app.data.datasource.StorageImagesDataSource
import com.yallakhedma.app.domain.model.Provider
import com.yallakhedma.app.domain.repository.AuthRepository
import com.yallakhedma.app.domain.repository.ProviderRepository
import com.yallakhedma.app.domain.util.DataResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProviderProfileSetupScreenModel(
    private val providerRepository: ProviderRepository,
    private val authRepository: AuthRepository,
    private val storageDataSource: StorageImagesDataSource,
) : ScreenModel {

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    fun onProfession(v: String) = _state.update { it.copy(profession = v, error = null) }
    fun onBio(v: String) = _state.update { it.copy(bio = v, error = null) }
    fun onCity(v: String) = _state.update { it.copy(city = v, error = null) }
    fun onPhone(v: String) = _state.update { it.copy(phone = v, error = null) }
    fun onPhotoUrl(v: String) = _state.update { it.copy(photoUrl = v, error = null) }
    fun onYears(v: String) = _state.update { it.copy(years = v.filter { c -> c.isDigit() }, error = null) }
    fun onCompletedJobs(v: String) =
        _state.update { it.copy(completedJobs = v.filter { c -> c.isDigit() }, error = null) }
    fun onServices(v: String) = _state.update { it.copy(servicesText = v, error = null) }

    /** Uploads the picked image to Storage and stores its URL in state. */
    fun uploadPhoto(bytes: ByteArray) {
        screenModelScope.launch {
            _state.update { it.copy(photoUploading = true, error = null) }
            val uid = authRepository.currentUid()
            if (uid == null) {
                _state.update { it.copy(photoUploading = false, error = "لا يوجد مستخدم") }
                return@launch
            }
            val url = runCatching { storageDataSource.uploadProviderPhoto(uid, bytes) }.getOrNull()
            if (url == null) {
                _state.update { it.copy(photoUploading = false, error = "تعذّر رفع الصورة") }
            } else {
                _state.update { it.copy(photoUploading = false, photoUrl = url) }
            }
        }
    }

    fun submit() {
        val s = state.value
        if (s.profession.isBlank()) {
            _state.update { it.copy(error = "المهنة مطلوبة") }
            return
        }
        if (s.city.isBlank()) {
            _state.update { it.copy(error = "المدينة مطلوبة") }
            return
        }
        screenModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val uid = authRepository.currentUid()
            if (uid == null) {
                _state.update { it.copy(isLoading = false, error = "لا يوجد مستخدم") }
                return@launch
            }
            val name = authRepository.currentUser.firstOrNull()?.name.orEmpty()
            val services = s.servicesText
                .split('،', ',', '\n')
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            val provider = Provider(
                id = uid,
                name = name,
                profession = s.profession.trim(),
                photoUrl = s.photoUrl.trim(),
                bio = s.bio.trim(),
                city = s.city.trim(),
                phone = s.phone.trim(),
                yearsExperience = s.years.toIntOrNull() ?: 0,
                completedJobs = s.completedJobs.toIntOrNull() ?: 0,
                services = services,
                verified = true, // reached here only after ID verification was approved
                bookingsCount = 0,
                isFeatured = false,
            )

            val saveResult = providerRepository.saveProfile(provider)
            if (saveResult is DataResult.Error) {
                _state.update { it.copy(isLoading = false, error = saveResult.message) }
                return@launch
            }
            when (val markResult = authRepository.markProviderProfileCompleted()) {
                is DataResult.Success -> _state.update { it.copy(isLoading = false, saved = true) }
                is DataResult.Error -> _state.update { it.copy(isLoading = false, error = markResult.message) }
                DataResult.Loading -> Unit
            }
        }
    }

    data class State(
        val profession: String = "",
        val bio: String = "",
        val city: String = "",
        val phone: String = "",
        val photoUrl: String = "",
        val photoUploading: Boolean = false,
        val years: String = "",
        val completedJobs: String = "",
        val servicesText: String = "",
        val isLoading: Boolean = false,
        val error: String? = null,
        val saved: Boolean = false,
    )
}
