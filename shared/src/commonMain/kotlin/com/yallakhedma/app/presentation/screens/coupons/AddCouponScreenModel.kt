package com.yallakhedma.app.presentation.screens.coupons

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.yallakhedma.app.domain.model.Coupon
import com.yallakhedma.app.domain.repository.AuthRepository
import com.yallakhedma.app.domain.repository.CouponRepository
import com.yallakhedma.app.domain.util.DataResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class AddCouponScreenModel(
    private val authRepository: AuthRepository,
    private val couponRepository: CouponRepository,
) : ScreenModel {

    enum class Kind { Percent, Amount }

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    fun onCode(v: String) = _state.update {
        // Allow A-Z, a-z, 0-9. Uppercase + cap at 20 chars.
        val cleaned = v.filter { c -> c.isLetterOrDigit() }.uppercase().take(20)
        it.copy(code = cleaned, error = null)
    }

    fun onKind(k: Kind) = _state.update { it.copy(kind = k, error = null) }

    fun onValue(v: String) = _state.update {
        val cleaned = buildString {
            var dot = false
            for (c in v) {
                if (c.isDigit()) append(c)
                else if ((c == '.' || c == ',') && !dot) { append('.'); dot = true }
            }
        }.take(8)
        it.copy(value = cleaned, error = null)
    }

    fun submit() {
        val s = state.value
        if (s.saving || s.saved) return
        if (s.code.length < 3) {
            _state.update { it.copy(error = "اكتب رمز كوبون من 3 أحرف فأكثر") }
            return
        }
        val number = s.value.toDoubleOrNull()
        if (number == null || number <= 0.0) {
            _state.update { it.copy(error = "أدخل قيمة خصم صحيحة") }
            return
        }
        if (s.kind == Kind.Percent && number > 100.0) {
            _state.update { it.copy(error = "نسبة الخصم لا تتجاوز 100%") }
            return
        }
        screenModelScope.launch {
            _state.update { it.copy(saving = true, error = null) }
            val uid = authRepository.currentUser.firstOrNull()?.id.orEmpty()
            if (uid.isBlank()) {
                _state.update { it.copy(saving = false, error = "لا يوجد مستخدم") }
                return@launch
            }
            val coupon = Coupon(
                providerId = uid,
                code = s.code,
                discountPercent = if (s.kind == Kind.Percent) number else 0.0,
                discountAmount = if (s.kind == Kind.Amount) number else 0.0,
                createdAt = Clock.System.now().toEpochMilliseconds(),
            )
            when (val r = couponRepository.add(coupon)) {
                is DataResult.Success -> _state.update { it.copy(saving = false, saved = true) }
                is DataResult.Error -> _state.update { it.copy(saving = false, error = r.message) }
                DataResult.Loading -> Unit
            }
        }
    }

    data class State(
        val code: String = "",
        val kind: Kind = Kind.Percent,
        val value: String = "",
        val saving: Boolean = false,
        val saved: Boolean = false,
        val error: String? = null,
    )
}
