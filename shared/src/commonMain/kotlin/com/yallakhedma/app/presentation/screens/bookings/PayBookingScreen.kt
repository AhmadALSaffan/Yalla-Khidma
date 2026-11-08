package com.yallakhedma.app.presentation.screens.bookings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.yallakhedma.app.presentation.components.ErrorBanner
import com.yallakhedma.app.presentation.components.YkButton
import com.yallakhedma.app.presentation.components.YkButtonStyle
import com.yallakhedma.app.presentation.components.YkFormScreen
import com.yallakhedma.app.presentation.components.YkIcons
import com.yallakhedma.app.presentation.components.YkTextField
import com.yallakhedma.app.presentation.components.shimmer
import com.yallakhedma.app.presentation.i18n.asString
import com.yallakhedma.app.presentation.screens.common.formatMoney
import com.yallakhedma.app.presentation.screens.payment.AddPaymentMethodScreen
import com.yallakhedma.app.presentation.screens.payment.SavedCardRow
import com.yallakhedma.app.presentation.theme.Yk
import com.yallakhedma.app.presentation.theme.YkRadius
import org.jetbrains.compose.resources.stringResource
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.bdet_original
import yallakhidma.shared.generated.resources.pay_add_card
import yallakhidma.shared.generated.resources.pay_apply
import yallakhidma.shared.generated.resources.pay_coupon
import yallakhidma.shared.generated.resources.pay_coupon_applied
import yallakhidma.shared.generated.resources.pay_coupon_ph
import yallakhidma.shared.generated.resources.pay_method
import yallakhidma.shared.generated.resources.pay_no_cards
import yallakhidma.shared.generated.resources.pay_secure
import yallakhidma.shared.generated.resources.pay_submit
import yallakhidma.shared.generated.resources.pay_title
import yallakhidma.shared.generated.resources.pay_total

data class PayBookingScreen(
    private val requestId: String,
    private val amount: Double,
    private val currency: String,
    private val serviceTitle: String,
    private val providerId: String = "",
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val model = koinScreenModel<PayBookingScreenModel>()
        val state by model.state.collectAsState()
        LaunchedEffect(state.paid) { if (state.paid) navigator.pop() }

        val coupon = state.appliedCoupon
        val total = coupon?.apply(amount) ?: amount
        val money = { v: Double -> "${formatMoney(v)} $currency" }

        YkFormScreen(
            title = stringResource(Res.string.pay_title),
            subtitle = serviceTitle,
            onBack = { navigator.pop() },
            bottomBar = {
                ErrorBanner(state.error?.asString(), Modifier.padding(bottom = 10.dp))
                YkButton(
                    text = stringResource(Res.string.pay_submit, money(total)),
                    onClick = { model.pay(requestId, amount, currency) },
                    loading = state.paying,
                    enabled = state.methods.isNotEmpty(),
                    leading = { Icon(YkIcons.Lock, null, tint = Color.White, modifier = Modifier.size(16.dp)) },
                )
                Row(
                    Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(YkIcons.ShieldCheck, null, tint = Yk.Ink3, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(Res.string.pay_secure), style = MaterialTheme.typography.labelMedium, color = Yk.Ink3)
                }
            },
        ) {
            // Total card
            Column(
                Modifier.fillMaxWidth().clip(YkRadius.card).background(Yk.BrandGradient).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(stringResource(Res.string.pay_total), style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.85f))
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(money(total), style = MaterialTheme.typography.displaySmall, color = Color.White)
                    if (coupon != null) {
                        Text(
                            money(amount),
                            style = MaterialTheme.typography.bodyLarge.copy(textDecoration = TextDecoration.LineThrough),
                            color = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 6.dp),
                        )
                    }
                }
                if (coupon != null) {
                    Text(
                        "${stringResource(Res.string.bdet_original)}: ${money(amount)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                    )
                }
            }

            // Payment methods
            Spacer(Modifier.height(24.dp))
            Text(stringResource(Res.string.pay_method), style = MaterialTheme.typography.headlineSmall, color = Yk.Ink)
            Spacer(Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                when {
                    state.loading -> repeat(2) { Box(Modifier.fillMaxWidth().height(68.dp).shimmer(YkRadius.card)) }
                    state.methods.isEmpty() -> Text(stringResource(Res.string.pay_no_cards), style = MaterialTheme.typography.bodyMedium, color = Yk.Ink2)
                    else -> state.methods.forEach { m ->
                        SavedCardRow(m, selected = m.id == state.selectedId, onClick = { model.select(m.id) })
                    }
                }
                YkButton(
                    text = stringResource(Res.string.pay_add_card),
                    onClick = { navigator.push(AddPaymentMethodScreen) },
                    style = YkButtonStyle.Outline,
                    height = 50.dp,
                    leading = { Icon(YkIcons.Plus, null, tint = Yk.Ink, modifier = Modifier.size(18.dp)) },
                )
            }

            // Coupon
            Spacer(Modifier.height(24.dp))
            Text(stringResource(Res.string.pay_coupon), style = MaterialTheme.typography.headlineSmall, color = Yk.Ink)
            Spacer(Modifier.height(12.dp))
            val canApply = state.couponCode.isNotBlank() && !state.couponApplying
            YkTextField(
                value = state.couponCode,
                onValueChange = model::onCouponCode,
                placeholder = stringResource(Res.string.pay_coupon_ph),
                leadingIcon = YkIcons.Ticket,
                capitalization = KeyboardCapitalization.Characters,
                forceLtr = true,
                isError = state.couponError != null,
                supportingText = state.couponError?.asString(),
                trailing = {
                    Box(
                        Modifier
                            .clip(YkRadius.chip)
                            .background(if (state.couponCode.isBlank()) Yk.Line else Yk.Night)
                            .clickable(enabled = canApply, role = Role.Button) { model.applyCoupon(providerId) }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (state.couponApplying) {
                            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                        } else {
                            Text(
                                stringResource(Res.string.pay_apply),
                                style = MaterialTheme.typography.labelLarge,
                                color = if (state.couponCode.isBlank()) Yk.Ink3 else Color.White,
                            )
                        }
                    }
                },
            )
            AnimatedVisibility(coupon != null, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                if (coupon != null) {
                    Row(
                        Modifier.fillMaxWidth().padding(top = 10.dp).clip(YkRadius.sm).background(Yk.SuccessSoft).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(YkIcons.Check, null, tint = Yk.Success, modifier = Modifier.size(18.dp))
                        Text(
                            stringResource(Res.string.pay_coupon_applied, coupon.code, coupon.label(currency)),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Yk.Success,
                        )
                    }
                }
            }
        }
    }
}
