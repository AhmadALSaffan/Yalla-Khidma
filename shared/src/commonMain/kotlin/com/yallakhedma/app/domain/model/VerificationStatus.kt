package com.yallakhedma.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class VerificationStatus { NotSubmitted, Pending, Approved, Rejected }

@Serializable
enum class DocumentType { NationalId, Passport }
