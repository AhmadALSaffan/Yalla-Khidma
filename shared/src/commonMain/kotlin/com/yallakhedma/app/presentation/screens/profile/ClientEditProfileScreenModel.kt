package com.yallakhedma.app.presentation.screens.profile

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.yallakhedma.app.data.datasource.StorageImagesDataSource
import com.yallakhedma.app.domain.repository.AuthRepository
import com.yallakhedma.app.domain.util.DataResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ClientEditProfileScreenModel(
    private val authRepository: AuthRepository,
    private val storageDataSource: StorageImagesDataSource,
) : ScreenModel {

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    init {
        screenModelScope.launch {
            val u = authRepository.currentUser.firstOrNull() ?: return@launch
            _state.update {
                it.copy(
                    name = u.name,
                    phone = u.phone.orEmpty(),
                    city = u.city.orEmpty(),
                    country = u.country.orEmpty(),
                    photoUrl = u.photoUrl,
                    loaded = true,
                )
            }
        }
    }

    fun onName(v: String) = _state.update { it.copy(name = v, error = null) }
    fun onPhone(v: String) = _state.update { it.copy(phone = v, error = null) }
    fun onCity(v: String) = _state.update { it.copy(city = v, error = null) }
    fun onCountry(v: String) = _state.update { it.copy(country = v, error = null) }

    fun uploadPhoto(bytes: ByteArray) {
        screenModelScope.launch {
            _state.update { it.copy(photoUploading = true, error = null) }
            val uid = authRepository.currentUid()
            if (uid == null) {
                _state.update { it.copy(photoUploading = false, error = "لا يوجد مستخدم") }
                return@launch
            }
            val url = runCatching { storageDataSource.uploadUserPhoto(uid, bytes) }.getOrNull()
            if (url == null) {
                _state.update { it.copy(photoUploading = false, error = "تعذّر رفع الصورة") }
            } else {
                _state.update { it.copy(photoUploading = false, photoUrl = url) }
            }
        }
    }

    fun submit() {
        val s = state.value
        if (s.name.isBlank()) { _state.update { it.copy(error = "الاسم مطلوب") }; return }
        screenModelScope.launch {
            _state.update { it.copy(saving = true, error = null) }
            val existing = authRepository.currentUser.firstOrNull()
            if (existing == null) {
                _state.update { it.copy(saving = false, error = "لا يوجد مستخدم") }
                return@launch
            }
            val updated = existing.copy(
                name = s.name.trim(),
                phone = s.phone.trim().ifBlank { null },
                city = s.city.trim().ifBlank { null },
                country = s.country.trim().ifBlank { null },
                photoUrl = s.photoUrl,
            )
            when (val r = authRepository.updateUserProfile(updated)) {
                is DataResult.Success -> _state.update { it.copy(saving = false, saved = true) }
                is DataResult.Error -> _state.update { it.copy(saving = false, error = r.message) }
                DataResult.Loading -> Unit
            }
        }
    }

    data class State(
        val name: String = "",
        val phone: String = "",
        val city: String = "",
        val country: String = "",
        val photoUrl: String = "",
        val photoUploading: Boolean = false,
        val loaded: Boolean = false,
        val saving: Boolean = false,
        val saved: Boolean = false,
        val error: String? = null,
    )
}
