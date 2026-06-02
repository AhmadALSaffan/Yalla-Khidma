package com.yallakhedma.app.data.repository

import com.yallakhedma.app.data.datasource.UserFirestoreDataSource
import com.yallakhedma.app.data.datasource.VerificationStorageDataSource
import com.yallakhedma.app.domain.model.DocumentType
import com.yallakhedma.app.domain.model.VerificationStatus
import com.yallakhedma.app.domain.repository.VerificationRepository
import com.yallakhedma.app.domain.util.DataResult
import kotlinx.datetime.Clock

class VerificationRepositoryImpl(
    private val userDs: UserFirestoreDataSource,
    private val storageDs: VerificationStorageDataSource,
) : VerificationRepository {

    override suspend fun submitIdDocument(
        userId: String,
        documentType: DocumentType,
        bytes: ByteArray,
        mimeType: String,
    ): DataResult<String> = runCatching {
        if (bytes.size > MAX_BYTES) {
            return DataResult.Error("الملف أكبر من 5 ميجابايت")
        }
        val url = storageDs.uploadIdDocument(userId, bytes)

        val current = userDs.getUser(userId)
            ?: return DataResult.Error("ملف المستخدم غير موجود")

        userDs.upsertUser(
            current.copy(
                idVerificationStatus = VerificationStatus.Pending,
                idDocumentType = documentType,
                idDocumentUrl = url,
                idDocumentSubmittedAt = Clock.System.now().toEpochMilliseconds(),
            ),
        )
        DataResult.Success(url)
    }.getOrElse { DataResult.Error(it.message ?: "تعذّر رفع الوثيقة", it) }

    private companion object {
        const val MAX_BYTES = 5 * 1024 * 1024 // 5 MB per the UI spec
    }
}
