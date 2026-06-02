package com.yallakhedma.app.presentation.screens.verification

import com.yallakhedma.app.domain.model.DocumentType

data class IdVerificationState(
    val documentType: DocumentType = DocumentType.NationalId,
    val pickedBytes: ByteArray? = null,
    val pickedMimeType: String? = null,
    val isUploading: Boolean = false,
    val error: String? = null,
    val submitted: Boolean = false,
) {
    val hasFile: Boolean get() = pickedBytes != null

    // ByteArray needs custom equals/hashCode to avoid stale-state recomposition bugs.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IdVerificationState) return false
        return documentType == other.documentType &&
            pickedMimeType == other.pickedMimeType &&
            (pickedBytes?.contentEquals(other.pickedBytes) ?: (other.pickedBytes == null)) &&
            isUploading == other.isUploading &&
            error == other.error &&
            submitted == other.submitted
    }

    override fun hashCode(): Int {
        var result = documentType.hashCode()
        result = 31 * result + (pickedBytes?.contentHashCode() ?: 0)
        result = 31 * result + (pickedMimeType?.hashCode() ?: 0)
        result = 31 * result + isUploading.hashCode()
        result = 31 * result + (error?.hashCode() ?: 0)
        result = 31 * result + submitted.hashCode()
        return result
    }
}
