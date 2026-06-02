package com.yallakhedma.app.presentation.screens.bookings

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.yallakhedma.app.domain.model.Coupon
import com.yallakhedma.app.domain.model.PaymentMethod
import com.yallakhedma.app.domain.repository.AuthRepository
import com.yallakhedma.app.domain.repository.CouponRepository
import com.yallakhedma.app.domain.repository.PaymentMethodRepository
import com.yallakhedma.app.domain.repository.RequestRepository
import com.yallakhedma.app.domain.util.DataResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Suppress("OPT_IN_USAGE")
class PayBookingScreenModel(
    private val authRepository: AuthRepository,
    private val paymentRepository: PaymentMethodRepository,
    private val requestRepository: RequestRepository,
    private val couponRepository: CouponRepository,
) : ScreenModel {

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    init {
        screenModelScope.launch {
            authRepository.currentUser
                .flatMapLatest { user ->
                    if (user == null) flowOf(emptyList())
                    else paymentRepository.observe(user.id)
                }
                .collectLatest { methods ->
                    _state.update {
                        it.copy(
                            methods = methods,
                            loading = false,
                            // Auto-select first method if none chosen yet.
                            selectedId = it.selectedId ?: methods.firstOrNull()?.id,
                        )
                    }
                }
        }
    }

    fun select(id: String) = _state.update { it.copy(selectedId = id, error = null) }

    fun onCouponCode(v: String) = _state.update {
        // Allow letters/digits, uppercase. Clearing the input also clears any
        // applied coupon (forces re-apply).
        val cleaned = v.filter { c -> c.isLetterOrDigit() }.uppercase().take(20)
        it.copy(couponCode = cleaned, appliedCoupon = null, couponError = null)
    }

    fun applyCoupon(providerId: String) {
        val s = state.value
        if (s.couponApplying) return
        val code = s.couponCode.trim()
        if (code.isBlank()) {
            _state.update { it.copy(couponError = "أدخل رمز الكوبون") }
            return
        }
        if (providerId.isBlank()) {
            _state.update { it.copy(couponError = "تعذّر التحقق من الكوبون") }
            return
        }
        screenModelScope.launch {
            _state.update { it.copy(couponApplying = true, couponError = null) }
            val coupon = couponRepository.findByCode(providerId, code)
            if (coupon == null) {
                _state.update {
                    it.copy(couponApplying = false, appliedCoupon = null, couponError = "كوبون غير صالح")
                }
            } else {
                _state.update { it.copy(couponApplying = false, appliedCoupon = coupon, couponError = null) }
            }
        }
    }

    /** Returns the price after applying the currently-applied coupon (if any). */
    fun finalAmount(originalAmount: Double): Double =
        state.value.appliedCoupon?.apply(originalAmount) ?: originalAmount

    fun pay(requestId: String, originalAmount: Double, currency: String) {
        val s = state.value
        if (s.paying || s.paid) return
        val method = s.methods.firstOrNull { it.id == s.selectedId }
        if (method == null) {
            _state.update { it.copy(error = "اختر طريقة دفع") }
            return
        }
        val coupon = s.appliedCoupon
        val paidAmount = coupon?.apply(originalAmount) ?: originalAmount
        val couponLabel = coupon?.label(currency).orEmpty()
        val couponCode = coupon?.code.orEmpty()

        screenModelScope.launch {
            _state.update { it.copy(paying = true, error = null) }
            val r = requestRepository.markPaid(
                requestId = requestId,
                brand = method.brand,
                last4 = method.last4,
                paidAmount = paidAmount,
                couponCode = couponCode,
                couponLabel = couponLabel,
            )
            when (r) {
                is DataResult.Success -> _state.update { it.copy(paying = false, paid = true) }
                is DataResult.Error -> _state.update { it.copy(paying = false, error = r.message) }
                DataResult.Loading -> Unit
            }
        }
    }

    data class State(
        val methods: List<PaymentMethod> = emptyList(),
        val selectedId: String? = null,
        val loading: Boolean = true,
        val paying: Boolean = false,
        val paid: Boolean = false,
        val error: String? = null,
        val couponCode: String = "",
        val couponApplying: Boolean = false,
        val appliedCoupon: Coupon? = null,
        val couponError: String? = null,
    )
}
