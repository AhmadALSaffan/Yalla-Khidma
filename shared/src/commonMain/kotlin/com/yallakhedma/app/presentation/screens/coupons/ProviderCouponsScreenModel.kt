package com.yallakhedma.app.presentation.screens.coupons

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.yallakhedma.app.domain.model.Coupon
import com.yallakhedma.app.domain.repository.AuthRepository
import com.yallakhedma.app.domain.repository.CouponRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Suppress("OPT_IN_USAGE")
class ProviderCouponsScreenModel(
    private val authRepository: AuthRepository,
    private val couponRepository: CouponRepository,
) : ScreenModel {

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    init {
        screenModelScope.launch {
            authRepository.currentUser
                .flatMapLatest { user ->
                    if (user == null) flowOf(emptyList())
                    else couponRepository.observeForProvider(user.id)
                }
                .collectLatest { list -> _state.update { it.copy(coupons = list, loading = false) } }
        }
    }

    fun delete(id: String) {
        screenModelScope.launch { couponRepository.delete(id) }
    }

    data class State(
        val coupons: List<Coupon> = emptyList(),
        val loading: Boolean = true,
    )
}
