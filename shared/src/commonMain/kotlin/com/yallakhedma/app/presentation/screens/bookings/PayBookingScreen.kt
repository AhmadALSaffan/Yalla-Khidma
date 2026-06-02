package com.yallakhedma.app.presentation.screens.bookings

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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.width
import androidx.compose.ui.text.style.TextDecoration
import com.yallakhedma.app.domain.model.PaymentMethod
import com.yallakhedma.app.presentation.components.AppPrimaryButton
import com.yallakhedma.app.presentation.components.AppTextField
import com.yallakhedma.app.presentation.screens.payment.AddPaymentMethodScreen
import com.yallakhedma.app.util.CardBrand
import org.jetbrains.compose.resources.painterResource

private val Background = Color(0xFFFFFFFF)
private val SurfaceContainerLow = Color(0xFFF2F3FF)
private val OnSurface = Color(0xFF131B2E)
private val OnSurfaceVariant = Color(0xFF5B4137)
private val OutlineVariant = Color(0xFFE4BEB1)
private val LogoOrange = Color(0xFFFF6B35)
private val Primary = Color(0xFFA73A00)
private val Success = Color(0xFF10B981)
private val Danger = Color(0xFFEF4444)

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
        val screenModel = koinScreenModel<PayBookingScreenModel>()
        val state by screenModel.state.collectAsState()

        LaunchedEffect(state.paid) { if (state.paid) navigator.pop() }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .verticalScroll(rememberScrollState()),
        ) {
            // Orange header
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
                Text("الدفع", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.size(40.dp))
            }

            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                // Amount summary — strikes through the original price once a coupon applies
                val applied = state.appliedCoupon
                val finalAmount = applied?.apply(amount) ?: amount
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceContainerLow)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(serviceTitle, color = OnSurface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    if (applied != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text(
                                "${formatMoney(amount)} $currency",
                                color = OnSurfaceVariant,
                                fontSize = 14.sp,
                                textDecoration = TextDecoration.LineThrough,
                            )
                            Text(
                                "خصم ${applied.label(currency)}",
                                color = Success,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                    Text(
                        "المبلغ: ${formatMoney(finalAmount)} $currency",
                        color = Primary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }

                // Coupon section
                Text("كوبون خصم (اختياري)", color = OnSurface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        AppTextField(
                            value = state.couponCode,
                            onValueChange = screenModel::onCouponCode,
                            label = "رمز الكوبون",
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(96.dp)
                            .height(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(LogoOrange)
                            .clickable { screenModel.applyCoupon(providerId) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            if (state.couponApplying) "..." else "تطبيق",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                AnimatedVisibility(
                    visible = state.appliedCoupon != null,
                    enter = fadeIn(tween(200)),
                    exit = fadeOut(tween(160)),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Success.copy(alpha = 0.10f))
                            .border(BorderStroke(1.dp, Success.copy(alpha = 0.4f)), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            "تم تطبيق الكوبون ${state.appliedCoupon?.code.orEmpty()} ✓",
                            color = Success,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            "خصم ${state.appliedCoupon?.label(currency).orEmpty()}",
                            color = Success,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                state.couponError?.let { Text(it, color = Danger, fontSize = 12.sp) }

                Text(
                    "اختر بطاقة الدفع",
                    color = OnSurface,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                )

                when {
                    state.loading -> Box(
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator(color = LogoOrange) }

                    state.methods.isEmpty() -> Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            "ما عندك بطاقات محفوظة. أضف بطاقة عشان تقدر تدفع.",
                            color = OnSurfaceVariant,
                            fontSize = 13.sp,
                        )
                        AppPrimaryButton(
                            text = "إضافة بطاقة",
                            onClick = { navigator.push(AddPaymentMethodScreen) },
                        )
                    }

                    else -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        state.methods.forEach { method ->
                            MethodRow(
                                method = method,
                                selected = method.id == state.selectedId,
                                onClick = { screenModel.select(method.id) },
                            )
                        }
                    }
                }

                state.error?.let { Text(it, color = Danger, fontSize = 13.sp) }

                if (state.methods.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    AppPrimaryButton(
                        text = "ادفع ${formatMoney(finalAmount)} $currency",
                        onClick = { screenModel.pay(requestId, amount, currency) },
                        loading = state.paying,
                    )
                }

                if (state.paid) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Success),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "تم الدفع بنجاح ✓",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MethodRow(method: PaymentMethod, selected: Boolean, onClick: () -> Unit) {
    val brand = CardBrand.fromKey(method.brand)
    val borderColor = if (selected) Primary else OutlineVariant.copy(alpha = 0.4f)
    val borderWidth = if (selected) 2.dp else 1.dp
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceContainerLow)
            .border(BorderStroke(borderWidth, borderColor), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Image(
            painter = painterResource(brand.drawable),
            contentDescription = brand.displayName,
            modifier = Modifier.size(width = 44.dp, height = 28.dp),
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("•••• ${method.last4}", color = OnSurface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(brand.displayName, color = OnSurfaceVariant, fontSize = 12.sp)
        }
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(if (selected) Primary else Color.Transparent)
                .border(BorderStroke(1.5.dp, if (selected) Primary else OutlineVariant), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) Text("✓", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

private fun formatMoney(amount: Double): String {
    // Strip trailing .0 for whole numbers, otherwise show two decimals.
    val rounded = (amount * 100).toLong() / 100.0
    return if (rounded % 1.0 == 0.0) rounded.toLong().toString() else String_format(rounded)
}

/** Cross-platform two-decimal format without depending on java.text. */
private fun String_format(value: Double): String {
    val whole = value.toLong()
    val fractionTimes100 = ((value - whole) * 100).let { if (it < 0) -it else it }
    val frac = fractionTimes100.toLong().toString().padStart(2, '0')
    return "$whole.$frac"
}
