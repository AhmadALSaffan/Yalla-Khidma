package com.yallakhedma.app.presentation.screens.subscription

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.State as ComposeState
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.yallakhedma.app.domain.model.PaymentMethod
import com.yallakhedma.app.domain.model.PlanTier
import com.yallakhedma.app.domain.model.Subscription
import com.yallakhedma.app.domain.repository.AuthRepository
import com.yallakhedma.app.domain.repository.PaymentMethodRepository
import com.yallakhedma.app.domain.repository.SubscriptionRepository
import com.yallakhedma.app.domain.util.DataResult
import com.yallakhedma.app.presentation.i18n.AppLanguage
import com.yallakhedma.app.presentation.i18n.LocalAppLanguage
import com.yallakhedma.app.presentation.i18n.UiText
import com.yallakhedma.app.util.nowMillis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.koin.compose.koinInject
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.error_choose_method
import yallakhidma.shared.generated.resources.error_sub_failed
import yallakhidma.shared.generated.resources.plan_basic
import yallakhidma.shared.generated.resources.plan_basic_tag
import yallakhidma.shared.generated.resources.plan_pro
import yallakhidma.shared.generated.resources.plan_pro_tag
import yallakhidma.shared.generated.resources.plan_vip
import yallakhidma.shared.generated.resources.plan_vip_tag

val PlanTier.nameRes: StringResource
    get() = when (this) {
        PlanTier.Basic -> Res.string.plan_basic
        PlanTier.Pro -> Res.string.plan_pro
        PlanTier.Vip -> Res.string.plan_vip
    }

val PlanTier.tagRes: StringResource
    get() = when (this) {
        PlanTier.Basic -> Res.string.plan_basic_tag
        PlanTier.Pro -> Res.string.plan_pro_tag
        PlanTier.Vip -> Res.string.plan_vip_tag
    }

/** "$9.99" in English, "9.99$" in Arabic (matches the Stitch design). */
fun usd(amount: Double, language: AppLanguage): String {
    val cents = kotlin.math.round(amount * 100).toLong()
    val s = "${cents / 100}.${(cents % 100).toString().padStart(2, '0')}"
    return if (language == AppLanguage.Arabic) "$s$" else "$$s"
}

@Composable
fun usd(amount: Double): String = usd(amount, LocalAppLanguage.current)

/** Live subscription of the signed-in provider (null = none). */
@Composable
@Suppress("OPT_IN_USAGE")
fun rememberMySubscription(): ComposeState<Subscription?> {
    val auth = koinInject<AuthRepository>()
    val repo = koinInject<SubscriptionRepository>()
    val flow = remember {
        auth.currentUser.flatMapLatest { u -> if (u == null) flowOf(null) else repo.observe(u.id) }
    }
    return flow.collectAsState(initial = null)
}

@Suppress("OPT_IN_USAGE")
class SubscriptionCheckoutScreenModel(
    private val auth: AuthRepository,
    private val payments: PaymentMethodRepository,
    private val subscriptions: SubscriptionRepository,
) : ScreenModel {

    data class State(
        val methods: List<PaymentMethod> = emptyList(),
        val selectedId: String? = null,
        val loading: Boolean = true,
        val paying: Boolean = false,
        val done: Subscription? = null,
        val error: UiText? = null,
    )

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    init {
        screenModelScope.launch {
            auth.currentUser
                .flatMapLatest { u -> if (u == null) emptyFlow() else payments.observe(u.id) }
                .collectLatest { methods ->
                    _state.update {
                        it.copy(methods = methods, loading = false, selectedId = it.selectedId ?: methods.firstOrNull()?.id)
                    }
                }
        }
    }

    fun select(id: String) = _state.update { it.copy(selectedId = id, error = null) }

    fun subscribe(tier: PlanTier, yearly: Boolean) {
        val s = state.value
        if (s.paying || s.done != null) return
        val card = s.methods.firstOrNull { it.id == s.selectedId }
        if (card == null) {
            _state.update { it.copy(error = UiText.of(Res.string.error_choose_method)) }
            return
        }
        val uid = auth.currentUid() ?: return
        val now = nowMillis()
        val trialEnds = now + Subscription.TRIAL_DAYS * Subscription.DAY_MS
        val price = tier.price(yearly)
        val sub = Subscription(
            providerId = uid,
            tier = tier.key,
            billing = if (yearly) Subscription.BILLING_YEARLY else Subscription.BILLING_MONTHLY,
            status = Subscription.STATUS_TRIAL,
            price = price,
            tax = kotlin.math.round(price * Subscription.TAX_RATE * 100) / 100.0,
            startedAt = now,
            trialEndsAt = trialEnds,
            renewsAt = trialEnds,
            cardBrand = card.brand,
            cardLast4 = card.last4,
        )
        screenModelScope.launch {
            _state.update { it.copy(paying = true, error = null) }
            when (subscriptions.save(sub)) {
                is DataResult.Success -> _state.update { it.copy(paying = false, done = sub) }
                else -> _state.update { it.copy(paying = false, error = UiText.of(Res.string.error_sub_failed)) }
            }
        }
    }
}
