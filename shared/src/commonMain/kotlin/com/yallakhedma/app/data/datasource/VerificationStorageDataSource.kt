package com.yallakhedma.app.data.datasource

import com.yallakhedma.app.data.remote.OpeninaryClient

class VerificationStorageDataSource(private val openinary: OpeninaryClient) {

    /**
     * Uploads the ID document image and returns its URL. The file name is a
     * random UUID so the (public) Openinary URL can't be guessed.
     */
    suspend fun uploadIdDocument(userId: String, bytes: ByteArray, mimeType: String = "image/jpeg"): String =
        openinary.uploadImage(bytes, folder = "verifications/$userId", mimeType = mimeType)
}
