package com.yallakhedma.app.presentation.screens.payment

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.yallakhedma.app.data.crypto.PaymentCardCrypto
import com.yallakhedma.app.domain.model.PaymentMethod
import com.yallakhedma.app.domain.repository.AuthRepository
import com.yallakhedma.app.domain.repository.PaymentMethodRepository
import com.yallakhedma.app.domain.util.DataResult
import com.yallakhedma.app.util.CardBrand
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class AddPaymentMethodScreenModel(
    private val authRepository: AuthRepository,
    private val paymentRepository: PaymentMethodRepository,
    private val crypto: PaymentCardCrypto,
) : ScreenModel {

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    fun onCardNumber(raw: String) {
        // First pass: detect brand from the (uncapped) digits so we know the
        // correct max length for this card scheme.
        val allDigits = raw.filter { it.isDigit() }.take(19)
        val brand = CardBrand.detect(allDigits)
        val maxLen = when (brand) {
            CardBrand.AMEX -> 15
            CardBrand.VISA, CardBrand.MASTERCARD, CardBrand.DISCOVER, CardBrand.MADA -> 16
            CardBrand.OTHER -> 19 // unknown — leave the widest possible cap
        }
        val overflow = allDigits.length > maxLen
        val digits = allDigits.take(maxLen)

        // CVV: Amex allows 4 digits, all others 3. Trim if shrinking.
        val maxCvv = if (brand == CardBrand.AMEX) 4 else 3
        val trimmedCvv = _state.value.cvv.take(maxCvv)

        _state.update {
            it.copy(
                cardNumber = digits,
                brand = brand,
                cvv = trimmedCvv,
                error = if (overflow) "تم بلوغ الحد الأقصى لرقم البطاقة ($maxLen رقم)" else null,
            )
        }
    }

    fun onExpiry(raw: String) {
        // Store raw digits only — the slash is added by ExpiryVisualTransformation
        // at display time. Keeps the caret well-behaved for LTR input.
        val digits = raw.filter { it.isDigit() }.take(4)
        _state.update { it.copy(expiry = digits, error = null) }
    }

    fun onCardholder(v: String) = _state.update { it.copy(cardholder = v, error = null) }
    fun onCvv(v: String) {
        val max = if (_state.value.brand == CardBrand.AMEX) 4 else 3
        _state.update { it.copy(cvv = v.filter { c -> c.isDigit() }.take(max), error = null) }
    }

    fun submit() {
        val s = state.value
        val pan = s.cardNumber.filter { it.isDigit() }
        if (pan.length < 13 || pan.length > 19) {
            _state.update { it.copy(error = "رقم البطاقة غير صحيح") }
            return
        }
        if (s.brand == CardBrand.OTHER) {
            _state.update {
                it.copy(error = "نوع البطاقة غير مدعوم. نقبل فقط: Visa أو Mastercard أو American Express أو Discover أو مدى")
            }
            return
        }
        val expDigits = s.expiry
        if (expDigits.length != 4) {
            _state.update { it.copy(error = "تاريخ الانتهاء غير صحيح") }
            return
        }
        val mm = expDigits.substring(0, 2).toIntOrNull()
        val yy = expDigits.substring(2, 4).toIntOrNull()
        if (mm == null || yy == null || mm !in 1..12) {
            _state.update { it.copy(error = "تاريخ الانتهاء غير صحيح") }
            return
        }
        val cvvLen = if (s.brand == CardBrand.AMEX) 4 else 3
        if (s.cvv.length != cvvLen) {
            val msg = if (s.brand == CardBrand.AMEX)
                "رمز الأمان لبطاقات أمريكان إكسبريس يتكوّن من 4 أرقام"
            else
                "رمز الأمان يتكوّن من 3 أرقام"
            _state.update { it.copy(error = msg) }
            return
        }
        if (s.cardholder.isBlank()) {
            _state.update { it.copy(error = "اسم حامل البطاقة مطلوب") }
            return
        }

        screenModelScope.launch {
            _state.update { it.copy(saving = true, error = null) }
            val uid = authRepository.currentUid()
            if (uid == null) {
                _state.update { it.copy(saving = false, error = "لا يوجد مستخدم") }
                return@launch
            }
            val encrypted = runCatching { crypto.encrypt(pan) }.getOrNull()
            if (encrypted == null || encrypted.ciphertextBase64.isBlank()) {
                _state.update { it.copy(saving = false, error = "تعذّر تشفير البطاقة") }
                return@launch
            }

            val method = PaymentMethod(
                brand = s.brand.key,
                last4 = pan.takeLast(4),
                cardholderName = s.cardholder.trim(),
                expMonth = mm,
                expYear = 2000 + yy,
                encryptedPan = encrypted.ciphertextBase64,
                iv = encrypted.ivBase64,
                createdAt = Clock.System.now().toEpochMilliseconds(),
            )
            when (val r = paymentRepository.add(uid, method)) {
                is DataResult.Success -> _state.update { it.copy(saving = false, saved = true) }
                is DataResult.Error -> _state.update { it.copy(saving = false, error = r.message) }
                DataResult.Loading -> Unit
            }
        }
    }

    data class State(
        val cardNumber: String = "",
        val brand: CardBrand = CardBrand.OTHER,
        val expiry: String = "",
        val cardholder: String = "",
        val cvv: String = "",
        val saving: Boolean = false,
        val saved: Boolean = false,
        val error: String? = null,
    )
}
