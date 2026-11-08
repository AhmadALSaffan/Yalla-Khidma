package com.yallakhedma.app.presentation.screens.verification

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.yallakhedma.app.domain.model.DocumentType
import com.yallakhedma.app.domain.repository.AuthRepository
import com.yallakhedma.app.presentation.components.Avatar
import com.yallakhedma.app.presentation.components.ErrorBanner
import com.yallakhedma.app.presentation.components.YkButton
import com.yallakhedma.app.presentation.components.YkChip
import com.yallakhedma.app.presentation.components.YkFormScreen
import com.yallakhedma.app.presentation.components.YkIcons
import com.yallakhedma.app.presentation.i18n.asString
import com.yallakhedma.app.presentation.routing.SignedOutDestination
import com.yallakhedma.app.presentation.theme.Yk
import com.yallakhedma.app.presentation.theme.YkRadius
import com.yallakhedma.app.util.rememberImagePicker
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.idv_change
import yallakhidma.shared.generated.resources.idv_doc_type
import yallakhidma.shared.generated.resources.idv_national_id
import yallakhidma.shared.generated.resources.idv_passport
import yallakhidma.shared.generated.resources.idv_pick_first
import yallakhidma.shared.generated.resources.idv_remove
import yallakhidma.shared.generated.resources.idv_selected
import yallakhidma.shared.generated.resources.idv_signed_in
import yallakhidma.shared.generated.resources.idv_submit
import yallakhidma.shared.generated.resources.idv_subtitle
import yallakhidma.shared.generated.resources.idv_switch
import yallakhidma.shared.generated.resources.idv_tip
import yallakhidma.shared.generated.resources.idv_title
import yallakhidma.shared.generated.resources.idv_upload
import yallakhidma.shared.generated.resources.idv_upload_hint

object IdVerificationScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val model = koinScreenModel<IdVerificationScreenModel>()
        val authRepository = koinInject<AuthRepository>()
        val state by model.state.collectAsState()
        val user by authRepository.currentUser.collectAsState(initial = null)
        val scope = rememberCoroutineScope()
        val pickImage = rememberImagePicker(onPicked = model::onImagePicked)

        LaunchedEffect(state.submitted) {
            if (state.submitted) navigator.replaceAll(WaitingForVerificationScreen)
        }

        YkFormScreen(
            title = stringResource(Res.string.idv_title),
            subtitle = stringResource(Res.string.idv_subtitle),
            onBack = null,
            bottomBar = {
                ErrorBanner(state.error?.asString(), Modifier.padding(bottom = 10.dp))
                YkButton(
                    text = stringResource(if (state.hasFile) Res.string.idv_submit else Res.string.idv_pick_first),
                    onClick = model::submit,
                    loading = state.isUploading,
                    enabled = state.hasFile,
                    leading = { Icon(YkIcons.ShieldCheck, null, tint = Color.White, modifier = Modifier.size(18.dp)) },
                )
            },
        ) {
            // Signed-in account
            val label = user?.email?.takeIf { it.isNotBlank() } ?: user?.phone.orEmpty()
            Row(
                Modifier.fillMaxWidth().clip(YkRadius.card).background(Yk.Fill).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Avatar(user?.name.orEmpty(), null, 40.dp)
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(Res.string.idv_signed_in, user?.name.orEmpty()),
                        style = MaterialTheme.typography.titleSmall,
                        color = Yk.Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (label.isNotBlank()) {
                        Text(label, style = MaterialTheme.typography.bodySmall, color = Yk.Ink3, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                Text(
                    stringResource(Res.string.idv_switch),
                    style = MaterialTheme.typography.labelLarge,
                    color = Yk.OrangeDeep,
                    modifier = Modifier
                        .clip(YkRadius.chip)
                        .clickable(role = Role.Button) {
                            scope.launch {
                                authRepository.signOut()
                                navigator.replaceAll(SignedOutDestination)
                            }
                        }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                )
            }

            Spacer(Modifier.height(24.dp))
            Text(stringResource(Res.string.idv_doc_type), style = MaterialTheme.typography.headlineSmall, color = Yk.Ink)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                YkChip(
                    label = stringResource(Res.string.idv_national_id),
                    selected = state.documentType == DocumentType.NationalId,
                    onClick = { model.onDocumentTypeChange(DocumentType.NationalId) },
                    leading = YkIcons.IdCard,
                )
                YkChip(
                    label = stringResource(Res.string.idv_passport),
                    selected = state.documentType == DocumentType.Passport,
                    onClick = { model.onDocumentTypeChange(DocumentType.Passport) },
                    leading = YkIcons.Book,
                )
            }

            Spacer(Modifier.height(20.dp))
            AnimatedContent(
                targetState = state.pickedBytes,
                transitionSpec = { (fadeIn(tween(220)) + scaleIn(initialScale = 0.96f)) togetherWith fadeOut(tween(150)) },
                contentKey = { it != null },
                label = "doc",
            ) { bytes ->
                if (bytes == null) {
                    UploadDropZone(onClick = pickImage)
                } else {
                    PickedDocument(
                        bytes = bytes,
                        onChange = pickImage,
                        onRemove = model::onClearImage,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(
                Modifier.fillMaxWidth().clip(YkRadius.sm).background(Yk.WarningSoft).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(YkIcons.Info, null, tint = Yk.Warning, modifier = Modifier.size(18.dp))
                Text(stringResource(Res.string.idv_tip), style = MaterialTheme.typography.bodySmall, color = Yk.Ink2)
            }
        }
    }
}

@Composable
private fun UploadDropZone(onClick: () -> Unit) {
    val dash = Yk.Orange.copy(alpha = 0.55f)
    Column(
        Modifier
            .fillMaxWidth()
            .aspectRatio(1.6f)
            .clip(YkRadius.card)
            .background(Yk.OrangeSoft)
            .drawBehind {
                drawRoundRect(
                    color = dash,
                    style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))),
                    cornerRadius = CornerRadius(20.dp.toPx()),
                )
            }
            .clickable(role = Role.Button, onClick = onClick)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier.size(64.dp).clip(CircleShape).background(Yk.Canvas).border(1.dp, Yk.OrangeTint, CircleShape),
            contentAlignment = Alignment.Center,
        ) { Icon(YkIcons.Camera, null, tint = Yk.Orange, modifier = Modifier.size(28.dp)) }
        Spacer(Modifier.height(14.dp))
        Text(stringResource(Res.string.idv_upload), style = MaterialTheme.typography.titleMedium, color = Yk.Ink, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        Text(stringResource(Res.string.idv_upload_hint), style = MaterialTheme.typography.bodySmall, color = Yk.Ink3)
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun PickedDocument(bytes: ByteArray, onChange: () -> Unit, onRemove: () -> Unit) {
    val bitmap = remember(bytes) { runCatching { bytes.decodeToImageBitmap() }.getOrNull() }
    Column(Modifier.fillMaxWidth().clip(YkRadius.card).border(1.dp, Yk.Line, YkRadius.card)) {
        Box(Modifier.fillMaxWidth().aspectRatio(1.6f).background(Yk.Fill)) {
            if (bitmap != null) {
                Image(bitmap, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            } else {
                Icon(YkIcons.IdCard, null, tint = Yk.Ink3, modifier = Modifier.size(48.dp).align(Alignment.Center))
            }
            Box(
                Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Yk.Success),
                contentAlignment = Alignment.Center,
            ) { Icon(YkIcons.Check, null, tint = Color.White, modifier = Modifier.size(18.dp)) }
        }
        Row(
            Modifier.fillMaxWidth().background(Yk.Canvas).padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(Res.string.idv_selected, (bytes.size / 1024).coerceAtLeast(1)),
                style = MaterialTheme.typography.bodyMedium,
                color = Yk.Ink2,
                modifier = Modifier.weight(1f),
            )
            Text(
                stringResource(Res.string.idv_change),
                style = MaterialTheme.typography.labelLarge,
                color = Yk.OrangeDeep,
                modifier = Modifier.clip(YkRadius.chip).clickable(role = Role.Button, onClick = onChange).padding(horizontal = 8.dp, vertical = 6.dp),
            )
            Text(
                stringResource(Res.string.idv_remove),
                style = MaterialTheme.typography.labelLarge,
                color = Yk.Danger,
                modifier = Modifier.clip(YkRadius.chip).clickable(role = Role.Button, onClick = onRemove).padding(horizontal = 8.dp, vertical = 6.dp),
            )
        }
    }
}
