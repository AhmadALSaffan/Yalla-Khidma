package com.yallakhedma.app.presentation.screens.profile

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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

private val Background = Color(0xFFFFFFFF)
private val SurfaceContainerLow = Color(0xFFF2F3FF)
private val OnSurface = Color(0xFF131B2E)
private val OnSurfaceVariant = Color(0xFF5B4137)
private val Primary = Color(0xFFFF6B35)
private val PrimaryFixed = Color(0xFFFFDBCE)
private val Danger = Color(0xFFEF4444)

object ClientEditProfileScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<ClientEditProfileScreenModel>()
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
                Text("تعديل الملف", color = OnSurface, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            Text("حدّث بياناتك الشخصية", color = OnSurfaceVariant, fontSize = 13.sp)

            Spacer(Modifier.height(24.dp))
            // Photo uploader
            val pickImage = rememberImagePicker { bytes, _ -> screenModel.uploadPhoto(bytes) }
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainerLow)
                        .border(BorderStroke(2.dp, PrimaryFixed), CircleShape)
                        .clickable(enabled = !state.photoUploading) { pickImage() },
                    contentAlignment = Alignment.Center,
                ) {
                    when {
                        state.photoUploading -> CircularProgressIndicator(color = Primary, strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
                        state.photoUrl.isNotBlank() -> AsyncImage(
                            model = HomeImageAssets.imageUrl(state.photoUrl),
                            contentDescription = "صورة الملف",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                        )
                        else -> Text("📷", fontSize = 30.sp)
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (state.photoUrl.isBlank()) "أضف صورة" else "تغيير الصورة",
                    color = Primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable(enabled = !state.photoUploading) { pickImage() },
                )
            }

            Spacer(Modifier.height(20.dp))
            AppTextField(value = state.name, onValueChange = screenModel::onName, label = "الاسم الكامل")
            Spacer(Modifier.height(16.dp))
            AppTextField(value = state.phone, onValueChange = screenModel::onPhone, label = "رقم الجوال", keyboardType = KeyboardType.Phone)
            Spacer(Modifier.height(16.dp))
            AppTextField(value = state.city, onValueChange = screenModel::onCity, label = "المدينة")
            Spacer(Modifier.height(16.dp))
            AppTextField(value = state.country, onValueChange = screenModel::onCountry, label = "الدولة")

            state.error?.let { msg ->
                Spacer(Modifier.height(12.dp))
                Text(msg, color = Danger, fontSize = 13.sp)
            }

            Spacer(Modifier.height(24.dp))
            AppPrimaryButton(text = "حفظ", onClick = screenModel::submit, loading = state.saving)
        }
    }
}
