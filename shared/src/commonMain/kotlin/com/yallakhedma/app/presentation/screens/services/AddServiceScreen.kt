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
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import com.yallakhedma.app.presentation.components.AppPrimaryButton
import com.yallakhedma.app.presentation.components.AppTextField
import com.yallakhedma.app.presentation.screens.home.HomeImageAssets
import com.yallakhedma.app.util.rememberImagePicker
import com.yallakhedma.app.util.rememberLocationPicker

private val Background = Color(0xFFFFFFFF)
private val SurfaceContainerLow = Color(0xFFF2F3FF)
private val OnSurface = Color(0xFF131B2E)
private val OnSurfaceVariant = Color(0xFF5B4137)
private val OutlineVariant = Color(0xFFE4BEB1)
private val LogoOrange = Color(0xFFFF6B35)
private val Primary = Color(0xFFA73A00)
private val PrimaryFixed = Color(0xFFFFDBCE)
private val Success = Color(0xFF10B981)
private val Danger = Color(0xFFEF4444)

object AddServiceScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<AddServiceScreenModel>()
        val state by screenModel.state.collectAsState()
        val pickImage = rememberImagePicker { bytes, _ -> screenModel.uploadImage(bytes) }

        LaunchedEffect(state.saved) {
            if (state.saved) navigator.pop()
        }

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
                Text("إضافة خدمة", color = OnSurface, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(20.dp))
            // Image uploader (landscape)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceContainerLow)
                    .border(BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.5f)), RoundedCornerShape(16.dp))
                    .clickable(enabled = !state.imageUploading) { pickImage() },
                contentAlignment = Alignment.Center,
            ) {
                when {
                    state.imageUploading -> CircularProgressIndicator(color = LogoOrange, strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
                    state.imageUrl.isNotBlank() -> AsyncImage(
                        model = HomeImageAssets.imageUrl(state.imageUrl),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📷", fontSize = 30.sp)
                        Spacer(Modifier.height(6.dp))
                        Text("أضف صورة للخدمة", color = OnSurfaceVariant, fontSize = 13.sp)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            AppTextField(value = state.title, onValueChange = screenModel::onTitle, label = "عنوان الخدمة")
            Spacer(Modifier.height(16.dp))
            AppTextField(value = state.category, onValueChange = screenModel::onCategory, label = "الفئة (مثال: سباكة)")
            Spacer(Modifier.height(16.dp))
            AppTextField(value = state.description, onValueChange = screenModel::onDescription, label = "وصف الخدمة")
            Spacer(Modifier.height(16.dp))
            AppTextField(value = state.price, onValueChange = screenModel::onPrice, label = "السعر (مثال: تبدأ من 50 ر.س)")
            Spacer(Modifier.height(16.dp))
            AppTextField(value = state.duration, onValueChange = screenModel::onDuration, label = "المدة (مثال: ساعة - ساعتين)")

            Spacer(Modifier.height(16.dp))
            // Location: type it OR use GPS.
            var locating by remember { mutableStateOf(false) }
            val locationPicker = rememberLocationPicker { picked ->
                locating = false
                if (picked != null) {
                    screenModel.onLocationPicked(picked.label, picked.latitude, picked.longitude)
                }
            }
            AppTextField(value = state.distance, onValueChange = screenModel::onDistance, label = "الموقع")
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (locating) SurfaceContainerLow else PrimaryFixed.copy(alpha = 0.4f))
                    .border(BorderStroke(1.dp, LogoOrange.copy(alpha = 0.4f)), RoundedCornerShape(50))
                    .clickable(enabled = !locating) { locating = true; locationPicker() }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (locating) {
                    CircularProgressIndicator(color = LogoOrange, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                    Text("جاري تحديد موقعك...", color = Primary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                } else {
                    Text("📍", fontSize = 14.sp)
                    Text("استخدم موقعي الحالي", color = Primary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            state.error?.let { msg ->
                Spacer(Modifier.height(12.dp))
                Text(msg, color = Danger, fontSize = 13.sp)
            }

            Spacer(Modifier.height(24.dp))
            AppPrimaryButton(
                text = "نشر الخدمة",
                onClick = screenModel::submit,
                loading = state.isSaving || state.imageUploading,
            )
        }
    }
}
