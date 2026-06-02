package com.yallakhedma.app.domain.repository

import com.yallakhedma.app.domain.model.DocumentType
import com.yallakhedma.app.domain.util.DataResult

interface VerificationRepository {

    /**
     * Uploads the ID document to Firebase Storage and marks the user's profile as Pending review.
     * @return the download URL on success.
     */
    suspend fun submitIdDocument(
        userId: String,
        documentType: DocumentType,
        bytes: ByteArray,
        mimeType: String,
    ): DataResult<String>
}
