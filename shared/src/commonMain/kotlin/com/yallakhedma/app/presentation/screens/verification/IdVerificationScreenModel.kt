package com.yallakhedma.app.presentation.screens.verification

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.yallakhedma.app.data.datasource.FirebaseAuthDataSource
import com.yallakhedma.app.domain.model.DocumentType
import com.yallakhedma.app.domain.repository.VerificationRepository
import com.yallakhedma.app.domain.util.DataResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class IdVerificationScreenModel(
    private val verificationRepository: VerificationRepository,
    private val authDataSource: FirebaseAuthDataSource,
) : ScreenModel {

    private val _state = MutableStateFlow(IdVerificationState())
    val state = _state.asStateFlow()

    fun onDocumentTypeChange(type: DocumentType) =
        _state.update { it.copy(documentType = type, error = null) }

    fun onImagePicked(bytes: ByteArray, mimeType: String) =
        _state.update {
            it.copy(pickedBytes = bytes, pickedMimeType = mimeType, error = null)
        }

    fun onClearImage() =
        _state.update { it.copy(pickedBytes = null, pickedMimeType = null) }

    fun submit() {
        val s = state.value
        val bytes = s.pickedBytes
        val mime = s.pickedMimeType ?: "image/jpeg"
        if (bytes == null) {
            _state.update { it.copy(error = "اختر صورة الوثيقة أولاً") }
            return
        }
        val uid = authDataSource.currentUid()
        if (uid == null) {
            _state.update { it.copy(error = "لا يوجد مستخدم مسجّل") }
            return
        }
        screenModelScope.launch {
            _state.update { it.copy(isUploading = true, error = null) }
            val result = verificationRepository.submitIdDocument(
                userId = uid,
                documentType = s.documentType,
                bytes = bytes,
                mimeType = mime,
            )
            when (result) {
                is DataResult.Success -> _state.update {
                    it.copy(isUploading = false, submitted = true)
                }
                is DataResult.Error -> _state.update {
                    it.copy(isUploading = false, error = result.message)
                }
                DataResult.Loading -> Unit
            }
        }
    }
}
