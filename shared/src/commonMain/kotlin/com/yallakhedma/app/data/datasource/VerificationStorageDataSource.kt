package com.yallakhedma.app.data.datasource

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.storage.Data
import dev.gitlive.firebase.storage.storage

class VerificationStorageDataSource {

    /**
     * Uploads bytes to `verifications/{userId}/id_document` and returns the download URL.
     */
    suspend fun uploadIdDocument(userId: String, bytes: ByteArray): String {
        val ref = Firebase.storage.reference("verifications/$userId/id_document")
        ref.putData(Data(bytes))
        return ref.getDownloadUrl()
    }
}
