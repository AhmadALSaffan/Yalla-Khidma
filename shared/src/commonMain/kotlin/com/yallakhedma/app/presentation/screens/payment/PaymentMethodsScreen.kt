package com.yallakhedma.app.presentation.screens.payment

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.yallakhedma.app.domain.model.PaymentMethod
import com.yallakhedma.app.presentation.components.AppPrimaryButton
import com.yallakhedma.app.util.CardBrand
import org.jetbrains.compose.resources.painterResource

private val Background = Color(0xFFFFFFFF)
private val SurfaceContainerLow = Color(0xFFF2F3FF)
private val OnSurface = Color(0xFF131B2E)
private val OnSurfaceVariant = Color(0xFF5B4137)
private val OutlineVariant = Color(0xFFE4BEB1)
private val LogoOrange = Color(0xFFFF6B35)
private val Primary = Color(0xFFFF6B35)
private val Danger = Color(0xFFEF4444)

object PaymentMethodsScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<PaymentMethodsScreenModel>()
        val state by screenModel.state.collectAsState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp),
        ) {
            // Orange header (back on the right, RTL)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .background(LogoOrange)
                    .padding(start = 20.dp, end = 20.dp, top = 46.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).clickable { navigator.pop() },
                        contentAlignment = Alignment.Center,
                    ) { Text("→", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold) }
                    Text("طرق الدفع", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.size(40.dp))
                }
                Text(
                    text = "إدارة بطاقاتك المحفوظة بأمان",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                )
            }

            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AppPrimaryButton(
                    text = "+ إضافة بطاقة",
                    onClick = { navigator.push(AddPaymentMethodScreen) },
                )
                Spacer(Modifier.height(4.dp))
                when {
                    state.loading && state.methods.isEmpty() ->
                        Text("جارٍ التحميل...", color = OnSurfaceVariant, fontSize = 13.sp)
                    state.methods.isEmpty() -> Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center,
                    ) { Text("ما عندك بطاقات محفوظة بعد", color = OnSurfaceVariant, fontSize = 14.sp) }
                    else -> state.methods.forEach { method ->
                        PaymentMethodRow(method, onDelete = { screenModel.delete(method.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodRow(method: PaymentMethod, onDelete: () -> Unit) {
    val brand = CardBrand.fromKey(method.brand)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceContainerLow)
            .border(BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.3f)), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Brand logo
        Image(
            painter = painterResource(brand.drawable),
            contentDescription = brand.displayName,
            modifier = Modifier.size(width = 42.dp, height = 28.dp),
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(brand.displayName, color = OnSurface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(
                text = "•••• ${method.last4}",
                color = OnSurfaceVariant,
                fontSize = 13.sp,
            )
            if (method.expMonth > 0 && method.expYear > 0) {
                Text(
                    text = "${method.expMonth.toString().padStart(2, '0')}/${method.expYear % 100}",
                    color = OnSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                )
            }
        }
        Text(
            text = "حذف",
            color = Danger,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .clickable(onClick = onDelete)
                .padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}
