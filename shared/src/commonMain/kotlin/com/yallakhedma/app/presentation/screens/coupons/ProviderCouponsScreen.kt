package com.yallakhedma.app.presentation.screens.coupons

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import com.yallakhedma.app.domain.model.Coupon
import com.yallakhedma.app.presentation.components.AppPrimaryButton

private val Background = Color(0xFFFFFFFF)
private val SurfaceContainerLow = Color(0xFFF2F3FF)
private val OnSurface = Color(0xFF131B2E)
private val OnSurfaceVariant = Color(0xFF5B4137)
private val OutlineVariant = Color(0xFFE4BEB1)
private val LogoOrange = Color(0xFFFF6B35)
private val Primary = Color(0xFFA73A00)
private val Danger = Color(0xFFEF4444)

object ProviderCouponsScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<ProviderCouponsScreenModel>()
        val state by screenModel.state.collectAsState()

        Column(modifier = Modifier.fillMaxSize().background(Background)) {
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
                Text("الكوبونات", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.size(40.dp))
            }

            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                AppPrimaryButton(
                    text = "+ إضافة كوبون",
                    onClick = { navigator.push(AddCouponScreen) },
                )
            }

            val kind = when {
                state.loading -> "loading"
                state.coupons.isEmpty() -> "empty"
                else -> "content"
            }
            AnimatedContent(
                targetState = kind,
                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(160)) },
                label = "coupons-state",
                modifier = Modifier.fillMaxSize(),
            ) { k ->
                when (k) {
                    "loading" -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = LogoOrange)
                    }
                    "empty" -> Box(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "ما عندك كوبونات",
                                color = OnSurface,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(Modifier.size(6.dp))
                            Text(
                                "أنشئ كوبون يستخدمه عملاؤك للحصول على خصم.",
                                color = OnSurfaceVariant,
                                fontSize = 13.sp,
                            )
                        }
                    }
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(state.coupons, key = { it.id }) { coupon ->
                            CouponRow(
                                coupon = coupon,
                                onDelete = { screenModel.delete(coupon.id) },
                                modifier = Modifier.animateItem(
                                    fadeInSpec = tween(300),
                                    fadeOutSpec = tween(200),
                                    placementSpec = tween(350),
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CouponRow(coupon: Coupon, onDelete: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceContainerLow)
            .border(BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.3f)), RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(coupon.code, color = OnSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("خصم ${coupon.label()}", color = Primary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Danger.copy(alpha = 0.10f))
                .clickable(onClick = onDelete)
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Text("حذف", color = Danger, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
