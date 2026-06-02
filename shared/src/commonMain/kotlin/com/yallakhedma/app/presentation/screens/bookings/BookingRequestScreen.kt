package com.yallakhedma.app.presentation.screens.bookings

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
import com.yallakhedma.app.domain.model.Service
import com.yallakhedma.app.presentation.components.AppPrimaryButton
import com.yallakhedma.app.presentation.components.AppTextField

private val Background = Color(0xFFFFFFFF)
private val SurfaceContainerLow = Color(0xFFF2F3FF)
private val OnSurface = Color(0xFF131B2E)
private val OnSurfaceVariant = Color(0xFF5B4137)
private val LogoOrange = Color(0xFFFF6B35)
private val Primary = Color(0xFFA73A00)
private val Success = Color(0xFF10B981)
private val Danger = Color(0xFFEF4444)

data class BookingRequestScreen(private val service: Service) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<BookingRequestScreenModel>()
        val state by screenModel.state.collectAsState()
        LaunchedEffect(service.providerId) { screenModel.loadProvider(service.providerId) }

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
                Text("طلب حجز", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.size(40.dp))
            }

            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                // Service summary
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceContainerLow)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(service.title, color = OnSurface, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    if (service.priceFrom.isNotBlank()) {
                        Text(
                            "السعر المقترح من مقدم الخدمة: ${service.priceFrom}",
                            color = OnSurfaceVariant,
                            fontSize = 12.sp,
                        )
                    }
                }

                // Headline
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "السعر اللي تقدر تدفعه",
                        color = OnSurface,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        "اقترح سعراً لمقدم الخدمة، وبعد ما يقبل تقدر تدفع.",
                        color = OnSurfaceVariant,
                        fontSize = 12.sp,
                    )
                }

                AppTextField(
                    value = state.price,
                    onValueChange = screenModel::onPrice,
                    label = "السعر (ر.س)",
                    keyboardType = KeyboardType.Decimal,
                )

                state.error?.let {
                    Text(it, color = Danger, fontSize = 13.sp)
                }

                if (state.submitted) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Success),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "تم إرسال الطلب — استنى رد مقدم الخدمة ✓",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Primary)
                            .clickable { navigator.pop() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("رجوع", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    AppPrimaryButton(
                        text = "يلّا أرسل الطلب",
                        onClick = { screenModel.submit(service) },
                        loading = state.submitting,
                    )
                }
            }
        }
    }
}
