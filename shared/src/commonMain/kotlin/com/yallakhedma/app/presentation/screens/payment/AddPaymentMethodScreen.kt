package com.yallakhedma.app.presentation.screens.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.yallakhedma.app.presentation.components.ErrorBanner
import com.yallakhedma.app.presentation.components.YkButton
import com.yallakhedma.app.presentation.components.YkFormScreen
import com.yallakhedma.app.presentation.components.YkIcons
import com.yallakhedma.app.presentation.components.YkTextField
import com.yallakhedma.app.presentation.i18n.asString
import com.yallakhedma.app.presentation.theme.Yk
import com.yallakhedma.app.presentation.theme.YkRadius
import com.yallakhedma.app.util.CardBrand
import org.jetbrains.compose.resources.stringResource
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.add_card_note
import yallakhidma.shared.generated.resources.add_card_submit
import yallakhidma.shared.generated.resources.add_card_title
import yallakhidma.shared.generated.resources.card_holder_ph
import yallakhidma.shared.generated.resources.field_card_number
import yallakhidma.shared.generated.resources.field_cardholder
import yallakhidma.shared.generated.resources.field_cvv
import yallakhidma.shared.generated.resources.field_expiry

object AddPaymentMethodScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val model = koinScreenModel<AddPaymentMethodScreenModel>()
        val state by model.state.collectAsState()
        val numberTransform = remember { CardNumberVisualTransformation() }
        val expiryTransform = remember { ExpiryVisualTransformation() }
        LaunchedEffect(state.saved) { if (state.saved) navigator.pop() }

        YkFormScreen(
            title = stringResource(Res.string.add_card_title),
            onBack = { navigator.pop() },
            bottomBar = {
                ErrorBanner(state.error?.asString(), Modifier.padding(bottom = 10.dp))
                YkButton(
                    text = stringResource(Res.string.add_card_submit),
                    onClick = model::submit,
                    loading = state.saving,
                    leading = { Icon(YkIcons.Lock, null, tint = Color.White, modifier = Modifier.size(16.dp)) },
                )
            },
        ) {
            CardPreview(
                brand = state.brand,
                number = state.cardNumber,
                holder = state.cardholder,
                expiry = state.expiry,
            )
            Spacer(Modifier.height(24.dp))
            YkTextField(
                value = state.cardNumber,
                onValueChange = model::onCardNumber,
                label = stringResource(Res.string.field_card_number),
                placeholder = "0000 0000 0000 0000",
                leadingIcon = YkIcons.CreditCard,
                keyboardType = KeyboardType.Number,
                forceLtr = true,
                visualTransformation = numberTransform,
                trailing = if (state.brand != CardBrand.OTHER) ({ BrandBadge(state.brand) }) else null,
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                YkTextField(
                    value = state.expiry,
                    onValueChange = model::onExpiry,
                    label = stringResource(Res.string.field_expiry),
                    placeholder = "MM/YY",
                    keyboardType = KeyboardType.Number,
                    forceLtr = true,
                    visualTransformation = expiryTransform,
                    modifier = Modifier.weight(1f),
                )
                YkTextField(
                    value = state.cvv,
                    onValueChange = model::onCvv,
                    label = stringResource(Res.string.field_cvv),
                    placeholder = if (state.brand == CardBrand.AMEX) "••••" else "•••",
                    keyboardType = KeyboardType.NumberPassword,
                    isPassword = true,
                    forceLtr = true,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(16.dp))
            YkTextField(
                value = state.cardholder,
                onValueChange = model::onCardholder,
                label = stringResource(Res.string.field_cardholder),
                placeholder = stringResource(Res.string.card_holder_ph),
                leadingIcon = YkIcons.User,
                capitalization = KeyboardCapitalization.Characters,
                forceLtr = true,
            )
            Spacer(Modifier.height(16.dp))
            Row(
                Modifier.fillMaxWidth().clip(YkRadius.sm).background(Yk.Fill).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(YkIcons.ShieldCheck, null, tint = Yk.Success, modifier = Modifier.size(18.dp))
                Text(stringResource(Res.string.add_card_note), style = MaterialTheme.typography.bodySmall, color = Yk.Ink2)
            }
        }
    }
}
