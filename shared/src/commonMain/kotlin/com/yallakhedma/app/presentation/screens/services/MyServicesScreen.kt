package com.yallakhedma.app.presentation.screens.services

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import com.yallakhedma.app.domain.model.Service
import com.yallakhedma.app.presentation.components.AppPrimaryButton
import com.yallakhedma.app.presentation.screens.home.HomeImageAssets

private val Background = Color(0xFFFFFFFF)
private val SurfaceContainerLow = Color(0xFFF2F3FF)
private val OnSurface = Color(0xFF131B2E)
private val OnSurfaceVariant = Color(0xFF5B4137)
private val OutlineVariant = Color(0xFFE4BEB1)
private val LogoOrange = Color(0xFFFF6B35)
private val Primary = Color(0xFFA73A00)
private val PrimaryFixed = Color(0xFFFFDBCE)

object MyServicesScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<MyServicesScreenModel>()
        val state by screenModel.state.collectAsState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp),
        ) {
            // Orange header with back on the right
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .background(LogoOrange)
                    .padding(start = 20.dp, end = 20.dp, top = 46.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
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
                    Text("خدماتي", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.size(40.dp))
                }
                Text(
                    text = "الخدمات التي نشرتها (${state.services.size})",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AppPrimaryButton(
                    text = "+ إضافة خدمة جديدة",
                    onClick = { navigator.push(AddServiceScreen) },
                )
                Spacer(Modifier.height(4.dp))
                when {
                    state.loading && state.services.isEmpty() ->
                        Text("جارٍ التحميل...", color = OnSurfaceVariant, fontSize = 13.sp)
                    state.services.isEmpty() -> Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center,
                    ) { Text("ما نشرت أي خدمة بعد", color = OnSurfaceVariant, fontSize = 14.sp) }
                    else -> state.services.forEach { svc ->
                        MyServiceRow(svc, onClick = { navigator.push(ServiceDetailsScreen(svc)) })
                    }
                }
            }
        }
    }
}

@Composable
private fun MyServiceRow(service: Service, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Background)
            .border(BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.4f)), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(modifier = Modifier.size(60.dp).clip(RoundedCornerShape(12.dp)).background(PrimaryFixed)) {
            val url = HomeImageAssets.imageUrl(service.imageUrl)
            if (url != null) {
                AsyncImage(
                    model = url,
                    contentDescription = service.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(service.title, color = OnSurface, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (service.categoryTag.isNotBlank()) {
                Text(service.categoryTag, color = OnSurfaceVariant, fontSize = 12.sp)
            }
            Text(service.priceFrom, color = Primary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Text("‹", color = OnSurfaceVariant, fontSize = 20.sp)
    }
}
