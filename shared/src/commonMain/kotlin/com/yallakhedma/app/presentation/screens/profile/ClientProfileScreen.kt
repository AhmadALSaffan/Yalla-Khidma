package com.yallakhedma.app.presentation.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.Image
import androidx.compose.material3.Icon
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.yallakhedma.app.presentation.screens.auth.login.LoginScreen
import com.yallakhedma.app.presentation.screens.bookings.MyBookingsScreen
import com.yallakhedma.app.presentation.screens.payment.PaymentMethodsScreen
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.visamaster
import yallakhidma.shared.generated.resources.ic_profile_about
import yallakhidma.shared.generated.resources.ic_profile_bookings
import yallakhidma.shared.generated.resources.ic_profile_edit
import yallakhidma.shared.generated.resources.ic_profile_language
import yallakhidma.shared.generated.resources.ic_profile_notifications
import yallakhidma.shared.generated.resources.ic_profile_support

private val Background = Color(0xFFFFFFFF)
private val SurfaceContainerLow = Color(0xFFF2F3FF)
private val OnSurface = Color(0xFF131B2E)
private val OnSurfaceVariant = Color(0xFF5B4137)
private val OutlineVariant = Color(0xFFE4BEB1)
private val LogoOrange = Color(0xFFFF6B35)
private val Primary = Color(0xFFA73A00)
private val PrimaryContainer = Color(0xFFFF5C00)
private val PrimaryFixed = Color(0xFFFFDBCE)
private val Danger = Color(0xFFEF4444)

object ClientProfileScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<ClientProfileScreenModel>()
        val state by screenModel.state.collectAsState()
        val user = state.user

        // After signing out the user goes null — route to login.
        var hadUser by remember { mutableStateOf(false) }
        LaunchedEffect(user) {
            if (user != null) hadUser = true
            else if (hadUser) navigator.replaceAll(LoginScreen)
        }

        var showAvatarPreview by remember { mutableStateOf(false) }
        val photoUrl = user?.photoUrl?.takeIf { it.isNotBlank() }
            ?.let { com.yallakhedma.app.presentation.screens.home.HomeImageAssets.imageUrl(it) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp),
        ) {
            // Orange hero header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .background(LogoOrange)
                    .padding(start = 20.dp, end = 20.dp, top = 46.dp, bottom = 30.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
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
                    Text("حسابي", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.size(40.dp))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .pointerInput(photoUrl) {
                                if (photoUrl != null) {
                                    detectTapGestures(onLongPress = { showAvatarPreview = true })
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = user?.name?.firstOrNull()?.toString() ?: "؟",
                            color = Primary,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        if (photoUrl != null) {
                            coil3.compose.AsyncImage(
                                model = photoUrl,
                                contentDescription = user?.name,
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                            )
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(user?.name ?: "—", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        user?.email?.takeIf { it.isNotBlank() }?.let {
                            Text(it, color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                        }
                        user?.phone?.takeIf { it.isNotBlank() }?.let {
                            Text(it, color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("الإعدادات", color = OnSurface, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                MenuRow(Res.drawable.ic_profile_bookings, "حجوزاتي") { navigator.push(MyBookingsScreen) }
                MenuRow(Res.drawable.ic_profile_edit, "تعديل الملف الشخصي") { navigator.push(ClientEditProfileScreen) }
                MenuRow(Res.drawable.visamaster, "طرق الدفع", colored = true) {
                    navigator.push(PaymentMethodsScreen)
                }
                MenuRow(Res.drawable.ic_profile_notifications, "الإشعارات") { }
                MenuRow(Res.drawable.ic_profile_language, "اللغة") { }
                MenuRow(Res.drawable.ic_profile_support, "الدعم والمساعدة") { }
                MenuRow(Res.drawable.ic_profile_about, "حول التطبيق") { }

                Spacer(Modifier.height(24.dp))
                SignOutButton(onClick = screenModel::signOut)
            }
        }

        if (showAvatarPreview && photoUrl != null) {
            AvatarPreviewDialog(
                photoUrl = photoUrl,
                contentDescription = user?.name,
                onDismiss = { showAvatarPreview = false },
            )
        }
    }
}

@Composable
private fun AvatarPreviewDialog(
    photoUrl: String,
    contentDescription: String?,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                coil3.compose.AsyncImage(
                    model = photoUrl,
                    contentDescription = contentDescription,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                )
            }
        }
    }
}

@Composable
private fun MenuRow(icon: DrawableResource, label: String, colored: Boolean = false, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceContainerLow)
            .border(BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.3f)), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(38.dp).clip(CircleShape).background(PrimaryFixed.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center,
        ) {
            if (colored) {
                // Multi-color icon (e.g. Visa + Mastercard) — keep original colors.
                Image(
                    painter = painterResource(icon),
                    contentDescription = label,
                    modifier = Modifier.size(22.dp),
                )
            } else {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = label,
                    tint = Primary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Text(label, color = OnSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        Text("‹", color = OnSurfaceVariant, fontSize = 20.sp)
    }
}

@Composable
private fun SignOutButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Danger.copy(alpha = 0.1f))
            .border(BorderStroke(1.dp, Danger.copy(alpha = 0.4f)), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text("تسجيل الخروج", color = Danger, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}
