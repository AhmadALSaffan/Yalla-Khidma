package com.yallakhedma.app.presentation.screens.payment

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.yallakhedma.app.presentation.components.AppPrimaryButton
import com.yallakhedma.app.presentation.components.AppTextField
import org.jetbrains.compose.resources.painterResource

private val Background = Color(0xFFFFFFFF)
private val OnSurface = Color(0xFF131B2E)
private val OnSurfaceVariant = Color(0xFF5B4137)
private val Primary = Color(0xFFFF6B35)
private val Danger = Color(0xFFEF4444)

object AddPaymentMethodScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<AddPaymentMethodScreenModel>()
        val state by screenModel.state.collectAsState()

        LaunchedEffect(state.saved) { if (state.saved) navigator.pop() }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 46.dp, bottom = 32.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).clickable { navigator.pop() },
                    contentAlignment = Alignment.Center,
                ) { Text("→", color = Primary, fontSize = 24.sp, fontWeight = FontWeight.Bold) }
                Text("إضافة بطاقة", color = OnSurface, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = "🔒 يتم تشفير البطاقة قبل حفظها. لا نخزّن رمز CVV.",
                color = OnSurfaceVariant,
                fontSize = 12.sp,
            )

            Spacer(Modifier.height(20.dp))
            // Card number + live brand badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    AppTextField(
                        value = state.cardNumber,
                        onValueChange = screenModel::onCardNumber,
                        label = "رقم البطاقة",
                        keyboardType = KeyboardType.Number,
                        visualTransformation = remember { CardNumberVisualTransformation() },
                    )
                }
                Image(
                    painter = painterResource(state.brand.drawable),
                    contentDescription = state.brand.displayName,
                    modifier = Modifier.size(width = 44.dp, height = 28.dp),
                )
            }

            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    AppTextField(
                        value = state.expiry,
                        onValueChange = screenModel::onExpiry,
                        label = "MM/YY",
                        keyboardType = KeyboardType.Number,
                        visualTransformation = remember { ExpiryVisualTransformation() },
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    val cvvLen = if (state.brand == com.yallakhedma.app.util.CardBrand.AMEX) 4 else 3
                    AppTextField(
                        value = state.cvv,
                        onValueChange = screenModel::onCvv,
                        label = "CVV ($cvvLen أرقام)",
                        keyboardType = KeyboardType.NumberPassword,
                        isPassword = true,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            AppTextField(
                value = state.cardholder,
                onValueChange = screenModel::onCardholder,
                label = "اسم حامل البطاقة",
            )

            state.error?.let { msg ->
                Spacer(Modifier.height(12.dp))
                Text(msg, color = Danger, fontSize = 13.sp)
            }

            Spacer(Modifier.height(24.dp))
            AppPrimaryButton(
                text = "حفظ البطاقة",
                onClick = screenModel::submit,
                loading = state.saving,
            )
        }
    }
}
