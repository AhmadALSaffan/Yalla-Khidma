package com.yallakhedma.app.presentation.screens.profile

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

class ProviderEditProfileScreenModel(
    private val providerRepository: ProviderRepository,
    private val authRepository: AuthRepository,
    private val storageDataSource: StorageImagesDataSource,
) : ScreenModel {

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    init {
        val uid = authRepository.currentUid()
        if (uid != null) {
            screenModelScope.launch {
                val p = providerRepository.observeById(uid).firstOrNull()
                if (p != null) {
                    _state.update {
                        it.copy(
                            profession = p.profession,
                            bio = p.bio,
                            city = p.city,
                            phone = p.phone,
                            photoUrl = p.photoUrl,
                            years = p.yearsExperience.takeIf { y -> y > 0 }?.toString().orEmpty(),
                            completedJobs = p.completedJobs.takeIf { c -> c > 0 }?.toString().orEmpty(),
                            servicesText = p.services.joinToString("، "),
                            loaded = true,
                        )
                    }
                } else {
                    _state.update { it.copy(loaded = true) }
                }
            }
        }
    }

    fun onProfession(v: String) = _state.update { it.copy(profession = v, error = null) }
    fun onBio(v: String) = _state.update { it.copy(bio = v, error = null) }
    fun onCity(v: String) = _state.update { it.copy(city = v, error = null) }
    fun onPhone(v: String) = _state.update { it.copy(phone = v, error = null) }
    fun onYears(v: String) = _state.update { it.copy(years = v.filter { c -> c.isDigit() }, error = null) }
    fun onCompletedJobs(v: String) = _state.update { it.copy(completedJobs = v.filter { c -> c.isDigit() }, error = null) }
    fun onServices(v: String) = _state.update { it.copy(servicesText = v, error = null) }

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
        if (s.profession.isBlank()) { _state.update { it.copy(error = "المهنة مطلوبة") }; return }
        if (s.city.isBlank()) { _state.update { it.copy(error = "المدينة مطلوبة") }; return }
        screenModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            val uid = authRepository.currentUid()
            if (uid == null) {
                _state.update { it.copy(isSaving = false, error = "لا يوجد مستخدم") }
                return@launch
            }
            val existing = providerRepository.observeById(uid).firstOrNull()
            val name = existing?.name ?: authRepository.currentUser.firstOrNull()?.name.orEmpty()
            val services = s.servicesText.split('،', ',', '\n').map { it.trim() }.filter { it.isNotEmpty() }

            val provider = (existing ?: Provider(id = uid, name = name)).copy(
                profession = s.profession.trim(),
                bio = s.bio.trim(),
                city = s.city.trim(),
                phone = s.phone.trim(),
                photoUrl = s.photoUrl.trim(),
                yearsExperience = s.years.toIntOrNull() ?: existing?.yearsExperience ?: 0,
                completedJobs = s.completedJobs.toIntOrNull() ?: existing?.completedJobs ?: 0,
                services = services,
            )
            when (val r = providerRepository.saveProfile(provider)) {
                is DataResult.Success -> _state.update { it.copy(isSaving = false, saved = true) }
                is DataResult.Error -> _state.update { it.copy(isSaving = false, error = r.message) }
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
        val loaded: Boolean = false,
        val isSaving: Boolean = false,
        val saved: Boolean = false,
        val error: String? = null,
    )
}
