package com.yallakhedma.app.presentation.screens.verification

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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.yallakhedma.app.domain.model.DocumentType
import com.yallakhedma.app.domain.repository.AuthRepository
import com.yallakhedma.app.presentation.components.AppPrimaryButton
import com.yallakhedma.app.presentation.screens.auth.login.LoginScreen
import com.yallakhedma.app.presentation.theme.LocalSpacing
import com.yallakhedma.app.util.rememberImagePicker
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

object IdVerificationScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<IdVerificationScreenModel>()
        val authRepository = koinInject<AuthRepository>()
        val state by screenModel.state.collectAsState()
        val currentUser by authRepository.currentUser.collectAsState(initial = null)
        val scope = rememberCoroutineScope()
        val spacing = LocalSpacing.current

        LaunchedEffect(state.submitted) {
            if (state.submitted) navigator.replaceAll(WaitingForVerificationScreen)
        }

        val launchPicker = rememberImagePicker(onPicked = screenModel::onImagePicked)

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    tonalElevation = 6.dp,
                ) {
                    Box(Modifier.padding(spacing.lg)) {
                        AppPrimaryButton(
                            text = if (state.hasFile) "إرسال للتدقيق" else "اختر صورة أولاً",
                            onClick = screenModel::submit,
                            loading = state.isUploading,
                            enabled = state.hasFile,
                        )
                    }
                }
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = spacing.screenHorizontal)
                    .padding(top = spacing.screenTop, bottom = spacing.lg),
            ) {
                // "Signed in as X" banner so the user knows which account is being verified
                SignedInBanner(
                    name = currentUser?.name,
                    email = currentUser?.email ?: currentUser?.phone,
                    onSignOut = {
                        scope.launch {
                            authRepository.signOut()
                            navigator.replaceAll(LoginScreen)
                        }
                    },
                )
                Spacer(Modifier.height(spacing.xl))

                // Header
                Text(
                    text = "توثيق الهوية",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(spacing.sm))
                Text(
                    text = "يرجى رفع صورة واضحة للهوية الوطنية أو جواز السفر لتوثيق حسابك والبدء في استقبال الطلبات.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(spacing.xxl))

                // Document selection + upload card (with orange left stripe)
                StripedCard {
                    Text(
                        text = "اختر نوع الوثيقة",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(spacing.lg))

                    // Two doc type cards (in RTL, first child appears on the right)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing.md),
                    ) {
                        DocumentTypeCard(
                            label = "هوية وطنية",
                            selected = state.documentType == DocumentType.NationalId,
                            onClick = { screenModel.onDocumentTypeChange(DocumentType.NationalId) },
                            modifier = Modifier.weight(1f),
                        )
                        DocumentTypeCard(
                            label = "جواز سفر",
                            selected = state.documentType == DocumentType.Passport,
                            onClick = { screenModel.onDocumentTypeChange(DocumentType.Passport) },
                            modifier = Modifier.weight(1f),
                        )
                    }

                    Spacer(Modifier.height(spacing.lg))

                    UploadZone(
                        hasFile = state.hasFile,
                        fileSizeKb = state.pickedBytes?.size?.div(1024),
                        onClick = launchPicker,
                        onClear = screenModel::onClearImage,
                    )

                    Spacer(Modifier.height(spacing.lg))

                    InfoNote(
                        text = "يجب أن تكون الصورة واضحة، غير مقصوصة الحواف، وتظهر جميع البيانات بوضوح لتجنب رفض الطلب.",
                    )
                }

                state.error?.let { msg ->
                    Spacer(Modifier.height(spacing.md))
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

/** White card with a vertical orange stripe on its leading edge (right side in RTL). */
@Composable
private fun StripedCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Max)) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primary),
            )
            Column(modifier = Modifier.padding(20.dp)) { content() }
        }
    }
}

@Composable
private fun DocumentTypeCard(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val bg = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(if (selected) 2.dp else 1.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Simple icon stand-in (text emoji) — replace with Compose Resources drawable when available.
            Text(
                text = if (label.contains("جواز")) "📕" else "🪪",
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun UploadZone(
    hasFile: Boolean,
    fileSizeKb: Int?,
    onClick: () -> Unit,
    onClear: () -> Unit,
) {
    val borderColor = if (hasFile) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val bg = if (hasFile) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else Color.Transparent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 28.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "📷", style = MaterialTheme.typography.headlineMedium)
            }
            Spacer(Modifier.height(12.dp))
            if (hasFile) {
                Text(
                    text = "تم اختيار الصورة ✓",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "الحجم: ${fileSizeKb ?: 0} KB · اضغط لتغيير الصورة",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "إزالة",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.clickable(onClick = onClear),
                )
            } else {
                Text(
                    text = "اضغط هنا لرفع الصورة",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "أو قم بسحب وإفلات الملف هنا",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "يدعم: JPG, PNG, PDF (الحد الأقصى 5 ميجابايت)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Top banner: "تسجل الدخول كـ {name} ({email}) — ليس أنت؟ تسجيل خروج". */
@Composable
private fun SignedInBanner(
    name: String?,
    email: String?,
    onSignOut: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "تسجّلت الدخول كـ ${name ?: "..."}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold,
                )
                if (!email.isNullOrBlank()) {
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(
                text = "تغيير الحساب",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.clickable(onClick = onSignOut),
            )
        }
    }
}

@Composable
private fun InfoNote(text: String) {
    val blueBg = Color(0xFFE0EBFF)
    val blueText = Color(0xFF1E3A8A)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = blueBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Text(text = "ℹ️", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    text = "ملاحظة هامة:",
                    style = MaterialTheme.typography.labelLarge,
                    color = blueText,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    color = blueText,
                )
            }
        }
    }
}
