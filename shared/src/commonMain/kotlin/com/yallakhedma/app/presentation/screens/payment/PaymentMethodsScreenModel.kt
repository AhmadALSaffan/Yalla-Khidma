package com.yallakhedma.app.presentation.screens.payment

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.yallakhedma.app.domain.model.PaymentMethod
import com.yallakhedma.app.domain.repository.AuthRepository
import com.yallakhedma.app.domain.repository.PaymentMethodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PaymentMethodsScreenModel(
    private val authRepository: AuthRepository,
    private val paymentRepository: PaymentMethodRepository,
) : ScreenModel {

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    init {
        val uid = authRepository.currentUid()
        if (uid != null) {
            screenModelScope.launch {
                paymentRepository.observe(uid)
                    .catch { _state.update { it.copy(loading = false) } }
                    .collect { list -> _state.update { it.copy(methods = list, loading = false) } }
            }
        } else {
            _state.update { it.copy(loading = false) }
        }
    }

    fun delete(id: String) {
        val uid = authRepository.currentUid() ?: return
        screenModelScope.launch { paymentRepository.delete(uid, id) }
    }

    data class State(
        val methods: List<PaymentMethod> = emptyList(),
        val loading: Boolean = true,
    )
}
