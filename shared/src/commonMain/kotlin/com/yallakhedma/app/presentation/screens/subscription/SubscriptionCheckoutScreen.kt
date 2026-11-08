package com.yallakhedma.app.presentation.screens.subscription

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.yallakhedma.app.domain.model.PlanTier
import com.yallakhedma.app.domain.model.Subscription
import com.yallakhedma.app.presentation.components.ErrorBanner
import com.yallakhedma.app.presentation.components.YkButton
import com.yallakhedma.app.presentation.components.YkButtonStyle
import com.yallakhedma.app.presentation.components.YkFormScreen
import com.yallakhedma.app.presentation.components.YkIcons
import com.yallakhedma.app.presentation.components.safeTopPadding
import com.yallakhedma.app.presentation.components.shimmer
import com.yallakhedma.app.presentation.i18n.asString
import com.yallakhedma.app.presentation.screens.chats.shortDate
import com.yallakhedma.app.presentation.screens.payment.AddPaymentMethodScreen
import com.yallakhedma.app.presentation.screens.payment.SavedCardRow
import com.yallakhedma.app.presentation.theme.Yk
import com.yallakhedma.app.presentation.theme.YkRadius
import org.jetbrains.compose.resources.stringResource
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.breq_done
import yallakhidma.shared.generated.resources.pay_add_card
import yallakhidma.shared.generated.resources.pay_no_cards
import yallakhidma.shared.generated.resources.sub_monthly
import yallakhidma.shared.generated.resources.sub_per_month
import yallakhidma.shared.generated.resources.sub_per_year
import yallakhidma.shared.generated.resources.sub_yearly
import yallakhidma.shared.generated.resources.subpay_done_body
import yallakhidma.shared.generated.resources.subpay_done_title
import yallakhidma.shared.generated.resources.subpay_method
import yallakhidma.shared.generated.resources.subpay_pci
import yallakhidma.shared.generated.resources.subpay_plan
import yallakhidma.shared.generated.resources.subpay_price
import yallakhidma.shared.generated.resources.subpay_renews
import yallakhidma.shared.generated.resources.subpay_ssl
import yallakhidma.shared.generated.resources.subpay_submit
import yallakhidma.shared.generated.resources.subpay_tax
import yallakhidma.shared.generated.resources.subpay_title
import yallakhidma.shared.generated.resources.subpay_today
import yallakhidma.shared.generated.resources.subpay_total
import yallakhidma.shared.generated.resources.subpay_trial

data class SubscriptionCheckoutScreen(private val tierKey: String, private val yearly: Boolean) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val model = koinScreenModel<SubscriptionCheckoutScreenModel>()
        val state by model.state.collectAsState()
        val tier = PlanTier.fromKey(tierKey) ?: PlanTier.Pro

        AnimatedContent(
            state.done,
            transitionSpec = { (fadeIn(tween(260)) + scaleIn(initialScale = 0.94f)) togetherWith fadeOut(tween(160)) },
            contentKey = { it != null },
            label = "checkout",
        ) { done ->
            if (done != null) Done(done, onFinish = { navigator.popUntilRoot() })
            else Checkout(tier, yearly, state, model, onAddCard = { navigator.push(AddPaymentMethodScreen) }, onBack = { navigator.pop() })
        }
    }
}

@Composable
private fun Checkout(
    tier: PlanTier,
    yearly: Boolean,
    state: SubscriptionCheckoutScreenModel.State,
    model: SubscriptionCheckoutScreenModel,
    onAddCard: () -> Unit,
    onBack: () -> Unit,
) {
    val price = tier.price(yearly)
    val tax = kotlin.math.round(price * Subscription.TAX_RATE * 100) / 100.0
    val period = stringResource(if (yearly) Res.string.sub_per_year else Res.string.sub_per_month)

    YkFormScreen(
        title = stringResource(Res.string.subpay_title),
        onBack = onBack,
        bottomBar = {
            ErrorBanner(state.error?.asString(), Modifier.padding(bottom = 10.dp))
            YkButton(
                text = stringResource(Res.string.subpay_submit),
                onClick = { model.subscribe(tier, yearly) },
                loading = state.paying,
                enabled = state.methods.isNotEmpty(),
                leading = { Icon(YkIcons.Lock, null, tint = Color.White, modifier = Modifier.size(16.dp)) },
            )
            Row(
                Modifier.fillMaxWidth().padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            ) {
                TrustBadge(YkIcons.Lock, stringResource(Res.string.subpay_ssl))
                TrustBadge(YkIcons.ShieldCheck, stringResource(Res.string.subpay_pci))
            }
        },
    ) {
        // Summary
        Column(
            Modifier.fillMaxWidth().clip(YkRadius.card).background(Yk.BrandGradient).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                stringResource(
                    Res.string.subpay_plan,
                    stringResource(tier.nameRes),
                    stringResource(if (yearly) Res.string.sub_yearly else Res.string.sub_monthly),
                ),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.9f),
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(usd(price), style = MaterialTheme.typography.displaySmall, color = Color.White)
                Spacer(Modifier.width(6.dp))
                Text(period, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f), modifier = Modifier.padding(bottom = 6.dp))
            }
            Row(
                Modifier.clip(YkRadius.pill).background(Color.White.copy(alpha = 0.2f)).padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(YkIcons.Sparkles, null, tint = Color.White, modifier = Modifier.size(14.dp))
                Text(stringResource(Res.string.subpay_trial, "${usd(price)} $period"), style = MaterialTheme.typography.labelMedium, color = Color.White)
            }
        }

        // Method
        Spacer(Modifier.height(24.dp))
        Text(stringResource(Res.string.subpay_method), style = MaterialTheme.typography.headlineSmall, color = Yk.Ink)
        Spacer(Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            when {
                state.loading -> repeat(2) { Box(Modifier.fillMaxWidth().height(68.dp).shimmer(YkRadius.card)) }
                state.methods.isEmpty() -> Text(stringResource(Res.string.pay_no_cards), style = MaterialTheme.typography.bodyMedium, color = Yk.Ink2)
                else -> state.methods.forEach { m -> SavedCardRow(m, selected = m.id == state.selectedId, onClick = { model.select(m.id) }) }
            }
            YkButton(
                text = stringResource(Res.string.pay_add_card),
                onClick = onAddCard,
                style = YkButtonStyle.Outline,
                height = 50.dp,
                leading = { Icon(YkIcons.Plus, null, tint = Yk.Ink, modifier = Modifier.size(18.dp)) },
            )
        }

        // Totals
        Spacer(Modifier.height(24.dp))
        Column(
            Modifier.fillMaxWidth().clip(YkRadius.card).background(Yk.Fill).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            TotalRow(stringResource(Res.string.subpay_price), usd(price))
            TotalRow(stringResource(Res.string.subpay_tax), usd(tax))
            TotalRow(stringResource(Res.string.subpay_total), usd(price + tax), strong = true)
            Box(Modifier.fillMaxWidth().height(1.dp).background(Yk.Line))
            TotalRow(stringResource(Res.string.subpay_today), usd(0.0), strong = true, accent = true)
        }
    }
}

@Composable
private fun TotalRow(label: String, value: String, strong: Boolean = false, accent: Boolean = false) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            style = if (strong) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            color = if (strong) Yk.Ink else Yk.Ink2,
            modifier = Modifier.weight(1f),
        )
        Text(
            value,
            style = if (strong) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            color = if (accent) Yk.Success else Yk.Ink,
        )
    }
}

@Composable
private fun TrustBadge(icon: ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, null, tint = Yk.Ink3, modifier = Modifier.size(14.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = Yk.Ink3)
    }
}

@Composable
private fun Done(sub: Subscription, onFinish: () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(Yk.Canvas).background(Yk.WarmGlow).safeTopPadding().navigationBarsPadding().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(Modifier.size(104.dp).clip(CircleShape).background(Yk.BrandGradient), contentAlignment = Alignment.Center) {
            Icon(YkIcons.Check, null, tint = Color.White, modifier = Modifier.size(48.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text(stringResource(Res.string.subpay_done_title), style = MaterialTheme.typography.displaySmall, color = Yk.Ink, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(stringResource(Res.string.subpay_done_body), style = MaterialTheme.typography.bodyLarge, color = Yk.Ink2, textAlign = TextAlign.Center)
        Spacer(Modifier.height(14.dp))
        Text(
            stringResource(Res.string.subpay_renews, shortDate(sub.renewsAt)),
            style = MaterialTheme.typography.labelLarge,
            color = Yk.OrangeDeep,
            modifier = Modifier.clip(YkRadius.pill).background(Yk.OrangeSoft).padding(horizontal = 14.dp, vertical = 7.dp),
        )
        Spacer(Modifier.height(32.dp))
        YkButton(text = stringResource(Res.string.breq_done), onClick = onFinish)
    }
}
