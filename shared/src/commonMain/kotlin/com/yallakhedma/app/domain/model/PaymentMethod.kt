package com.yallakhedma.app.domain.model

import kotlinx.serialization.Serializable

/**
 * A saved payment card for a user. We store the **brand**, **last 4 digits**,
 * the cardholder's name and expiry, and the PAN **encrypted** (AES-GCM, see
 * `PaymentCardCrypto`). The CVV is NEVER stored — PCI-DSS forbids it even
 * encrypted, and this MVP doesn't need it.
 *
 * Lives under `users/{uid}/payment_methods/{methodId}`.
 */
@Serializable
data class PaymentMethod(
    val id: String = "",
    val brand: String = "",          // "visa" | "mastercard" | "discover" | "amex" | "other"
    val last4: String = "",
    val cardholderName: String = "",
    val expMonth: Int = 0,
    val expYear: Int = 0,
    val encryptedPan: String = "",   // Base64(ciphertext)
    val iv: String = "",             // Base64(nonce)
    val createdAt: Long = 0L,
)
