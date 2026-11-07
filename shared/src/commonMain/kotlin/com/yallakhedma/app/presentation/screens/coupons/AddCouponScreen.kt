package com.yallakhedma.app.presentation.screens.coupons

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.yallakhedma.app.domain.model.Coupon
import com.yallakhedma.app.presentation.components.ErrorBanner
import com.yallakhedma.app.presentation.components.YkButton
import com.yallakhedma.app.presentation.components.YkIcons
import com.yallakhedma.app.presentation.components.YkTextField
import com.yallakhedma.app.presentation.components.YkTopBar
import com.yallakhedma.app.presentation.components.safeTopPadding
import com.yallakhedma.app.presentation.i18n.asString
import com.yallakhedma.app.presentation.theme.Yk
import com.yallakhedma.app.presentation.theme.YkRadius
import org.jetbrains.compose.resources.stringResource
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.add_coupon_submit
import yallakhidma.shared.generated.resources.add_coupon_subtitle
import yallakhidma.shared.generated.resources.add_coupon_title
import yallakhidma.shared.generated.resources.coupon_kind_amount
import yallakhidma.shared.generated.resources.coupon_kind_percent
import yallakhidma.shared.generated.resources.coupon_preview
import yallakhidma.shared.generated.resources.field_coupon_code
import yallakhidma.shared.generated.resources.field_coupon_code_ph
import yallakhidma.shared.generated.resources.field_discount_value

object AddCouponScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val model = koinScreenModel<AddCouponScreenModel>()
        val state by model.state.collectAsState()

        LaunchedEffect(state.saved) { if (state.saved) navigator.pop() }

        Column(
            Modifier
                .fillMaxSize()
                .background(Yk.Canvas)
                .safeTopPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            YkTopBar(onBack = { navigator.pop() }, modifier = Modifier.padding(top = 8.dp))
            Spacer(Modifier.height(12.dp))
            Text(stringResource(Res.string.add_coupon_title), style = MaterialTheme.typography.displaySmall, color = Yk.Ink)
            Spacer(Modifier.height(4.dp))
            Text(stringResource(Res.string.add_coupon_subtitle), style = MaterialTheme.typography.bodyLarge, color = Yk.Ink2)
            Spacer(Modifier.height(24.dp))

            YkTextField(
                value = state.code,
                onValueChange = model::onCode,
                label = stringResource(Res.string.field_coupon_code),
                placeholder = stringResource(Res.string.field_coupon_code_ph),
                leadingIcon = YkIcons.Ticket,
                capitalization = KeyboardCapitalization.Characters,
                forceLtr = true,
            )
            Spacer(Modifier.height(16.dp))

            // Percent / amount toggle
            Row(
                Modifier.fillMaxWidth().height(46.dp).clip(YkRadius.pill).background(Yk.Fill).padding(4.dp),
            ) {
                listOf(
                    AddCouponScreenModel.Kind.Percent to stringResource(Res.string.coupon_kind_percent),
                    AddCouponScreenModel.Kind.Amount to stringResource(Res.string.coupon_kind_amount),
                ).forEach { (kind, label) ->
                    val selected = state.kind == kind
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clip(YkRadius.pill)
                            .background(if (selected) Yk.Canvas else Color.Transparent)
                            .clickable(role = Role.Tab) { model.onKind(kind) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(label, style = MaterialTheme.typography.titleSmall, color = if (selected) Yk.Ink else Yk.Ink3)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            YkTextField(
                value = state.value,
                onValueChange = model::onValue,
                label = stringResource(Res.string.field_discount_value),
                placeholder = if (state.kind == AddCouponScreenModel.Kind.Percent) "20" else "50",
                keyboardType = KeyboardType.Decimal,
                forceLtr = true,
            )

            // Live preview
            val number = state.value.toDoubleOrNull() ?: 0.0
            if (state.code.isNotBlank() && number > 0) {
                Spacer(Modifier.height(20.dp))
                Text(stringResource(Res.string.coupon_preview), style = MaterialTheme.typography.labelLarge, color = Yk.Ink2)
                Spacer(Modifier.height(8.dp))
                CouponTicket(
                    coupon = Coupon(
                        code = state.code,
                        discountPercent = if (state.kind == AddCouponScreenModel.Kind.Percent) number else 0.0,
                        discountAmount = if (state.kind == AddCouponScreenModel.Kind.Amount) number else 0.0,
                    ),
                    onDelete = null,
                )
            }

            Spacer(Modifier.height(24.dp))
            ErrorBanner(state.error?.asString(), Modifier.padding(bottom = 12.dp))
            YkButton(text = stringResource(Res.string.add_coupon_submit), onClick = model::submit, loading = state.saving)
            Spacer(Modifier.height(24.dp))
        }
    }
}
