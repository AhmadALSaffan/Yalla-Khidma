package com.yallakhedma.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String = "",
    val email: String? = null,
    val phone: String? = null,
    val name: String = "",
    val userType: UserType = UserType.Client,
    val country: String? = null,
    val city: String? = null,
    val language: String = "ar",
    val createdAt: Long = 0L,
    val isVerified: Boolean = false,

    // User's profile photo (Storage URL or path). Shown on the provider's
    // booking request card when this user books a service.
    val photoUrl: String = "",

    // Email OTP — true once the user confirmed the 4-digit code we emailed.
    // Google sign-ins are set true automatically (Google verifies the email).
    val emailVerified: Boolean = false,

    // ID verification (Providers only — review process)
    val idVerificationStatus: VerificationStatus = VerificationStatus.NotSubmitted,
    val idDocumentType: DocumentType? = null,
    val idDocumentUrl: String? = null,
    val idDocumentSubmittedAt: Long? = null,

    // Providers only — true once they've filled their profile after verification.
    val providerProfileCompleted: Boolean = false,
)
