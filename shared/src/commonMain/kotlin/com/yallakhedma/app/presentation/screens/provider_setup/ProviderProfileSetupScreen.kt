package com.yallakhedma.app.presentation.screens.provider_setup

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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import com.yallakhedma.app.domain.repository.AuthRepository
import com.yallakhedma.app.presentation.components.AppPrimaryButton
import com.yallakhedma.app.presentation.components.AppTextField
import com.yallakhedma.app.presentation.routing.destinationForUser
import com.yallakhedma.app.presentation.screens.home.HomeImageAssets
import com.yallakhedma.app.util.rememberImagePicker
import com.yallakhedma.app.util.rememberLocationPicker
import kotlinx.coroutines.flow.first
import org.koin.compose.koinInject

private val Background = Color(0xFFFFFFFF)
private val SurfaceContainerLow = Color(0xFFF2F3FF)
private val LogoOrange = Color(0xFFFF6B35)
private val OnSurface = Color(0xFF131B2E)
private val OnSurfaceVariant = Color(0xFF5B4137)
private val PrimaryFixed = Color(0xFFFFDBCE)
private val Danger = Color(0xFFEF4444)

object ProviderProfileSetupScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<ProviderProfileSetupScreenModel>()
        val authRepository = koinInject<AuthRepository>()
        val state by screenModel.state.collectAsState()

        LaunchedEffect(state.saved) {
            if (state.saved) {
                val user = authRepository.currentUser.first()
                navigator.replaceAll(destinationForUser(user))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 46.dp, bottom = 32.dp),
        ) {
            Text(
                text = "أكمل ملفك المهني",
                color = OnSurface,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "تم توثيق حسابك ✓ — عبّي بياناتك عشان يشوفها العملاء",
                color = OnSurfaceVariant,
                fontSize = 14.sp,
            )

            Spacer(Modifier.height(24.dp))
            // Profile photo uploader
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
                    val photo = state.photoUrl
                    when {
                        state.photoUploading -> CircularProgressIndicator(
                            color = LogoOrange,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(28.dp),
                        )
                        photo.isNotBlank() -> AsyncImage(
                            model = HomeImageAssets.imageUrl(photo),
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
                    text = if (state.photoUrl.isBlank()) "أضف صورة الملف" else "تغيير الصورة",
                    color = LogoOrange,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable(enabled = !state.photoUploading) { pickImage() },
                )
            }

            Spacer(Modifier.height(20.dp))
            AppTextField(
                value = state.profession,
                onValueChange = screenModel::onProfession,
                label = "المهنة (مثال: سباك محترف)",
            )
            Spacer(Modifier.height(16.dp))
            AppTextField(
                value = state.city,
                onValueChange = screenModel::onCity,
                label = "المدينة",
            )
            Spacer(Modifier.height(8.dp))
            var locatingCity by remember { mutableStateOf(false) }
            val cityPicker = rememberLocationPicker { picked ->
                locatingCity = false
                if (picked != null) screenModel.onCity(picked.label)
            }
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (locatingCity) SurfaceContainerLow else PrimaryFixed.copy(alpha = 0.4f))
                    .border(BorderStroke(1.dp, LogoOrange.copy(alpha = 0.4f)), RoundedCornerShape(50))
                    .clickable(enabled = !locatingCity) { locatingCity = true; cityPicker() }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (locatingCity) {
                    CircularProgressIndicator(color = LogoOrange, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                    Text("جاري تحديد موقعك...", color = LogoOrange, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                } else {
                    Text("📍", fontSize = 14.sp)
                    Text("استخدم موقعي الحالي", color = LogoOrange, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(16.dp))
            AppTextField(
                value = state.bio,
                onValueChange = screenModel::onBio,
                label = "نبذة عنك",
            )
            Spacer(Modifier.height(16.dp))
            AppTextField(
                value = state.phone,
                onValueChange = screenModel::onPhone,
                label = "رقم الجوال",
                keyboardType = KeyboardType.Phone,
            )
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    AppTextField(
                        value = state.years,
                        onValueChange = screenModel::onYears,
                        label = "سنوات الخبرة",
                        keyboardType = KeyboardType.Number,
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    AppTextField(
                        value = state.completedJobs,
                        onValueChange = screenModel::onCompletedJobs,
                        label = "أعمال مكتملة",
                        keyboardType = KeyboardType.Number,
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            AppTextField(
                value = state.servicesText,
                onValueChange = screenModel::onServices,
                label = "الخدمات (افصل بينها بفاصلة)",
            )

            state.error?.let { msg ->
                Spacer(Modifier.height(12.dp))
                Text(msg, color = Danger, fontSize = 13.sp)
            }

            Spacer(Modifier.height(24.dp))
            AppPrimaryButton(
                text = "حفظ ومتابعة",
                onClick = screenModel::submit,
                loading = state.isLoading,
            )
        }
    }
}
