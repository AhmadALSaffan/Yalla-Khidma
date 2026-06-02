package com.yallakhedma.app.presentation.screens.coupons

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.yallakhedma.app.presentation.screens.coupons.AddCouponScreenModel.Kind

private val Background = Color(0xFFFFFFFF)
private val SurfaceContainerLow = Color(0xFFF2F3FF)
private val OnSurface = Color(0xFF131B2E)
private val OnSurfaceVariant = Color(0xFF5B4137)
private val OutlineVariant = Color(0xFFE4BEB1)
private val LogoOrange = Color(0xFFFF6B35)
private val Primary = Color(0xFFA73A00)
private val Danger = Color(0xFFEF4444)

object AddCouponScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<AddCouponScreenModel>()
        val state by screenModel.state.collectAsState()

        LaunchedEffect(state.saved) { if (state.saved) navigator.pop() }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .verticalScroll(rememberScrollState()),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .background(LogoOrange)
                    .padding(start = 20.dp, end = 20.dp, top = 46.dp, bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).clickable { navigator.pop() },
                    contentAlignment = Alignment.Center,
                ) { Text("→", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold) }
                Text("إضافة كوبون", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.size(40.dp))
            }

            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                AppTextField(
                    value = state.code,
                    onValueChange = screenModel::onCode,
                    label = "رمز الكوبون (أحرف وأرقام)",
                )

                Text("نوع الخصم", color = OnSurface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    KindChip("نسبة %", selected = state.kind == Kind.Percent, modifier = Modifier.weight(1f)) {
                        screenModel.onKind(Kind.Percent)
                    }
                    KindChip("مبلغ ثابت", selected = state.kind == Kind.Amount, modifier = Modifier.weight(1f)) {
                        screenModel.onKind(Kind.Amount)
                    }
                }

                AppTextField(
                    value = state.value,
                    onValueChange = screenModel::onValue,
                    label = if (state.kind == Kind.Percent) "نسبة الخصم (0-100)" else "قيمة الخصم (ر.س)",
                    keyboardType = KeyboardType.Decimal,
                )

                state.error?.let { Text(it, color = Danger, fontSize = 13.sp) }

                Spacer(Modifier.height(4.dp))
                AppPrimaryButton(
                    text = "حفظ الكوبون",
                    onClick = screenModel::submit,
                    loading = state.saving,
                )
            }
        }
    }
}

@Composable
private fun KindChip(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) LogoOrange else SurfaceContainerLow)
            .border(
                BorderStroke(1.dp, if (selected) Primary else OutlineVariant.copy(alpha = 0.4f)),
                RoundedCornerShape(10.dp),
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = if (selected) Color.White else OnSurface,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
