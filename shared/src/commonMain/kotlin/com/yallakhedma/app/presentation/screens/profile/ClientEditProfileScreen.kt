package com.yallakhedma.app.presentation.screens.profile

import com.yallakhedma.app.presentation.components.safeTopPadding
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.yallakhedma.app.presentation.components.Avatar
import com.yallakhedma.app.presentation.components.ErrorBanner
import com.yallakhedma.app.presentation.components.YkButton
import com.yallakhedma.app.presentation.components.YkIcons
import com.yallakhedma.app.presentation.components.YkTextField
import com.yallakhedma.app.presentation.components.YkTopBar
import com.yallakhedma.app.presentation.i18n.asString
import com.yallakhedma.app.presentation.theme.Yk
import com.yallakhedma.app.util.rememberImagePicker
import org.jetbrains.compose.resources.stringResource
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.common_save
import yallakhidma.shared.generated.resources.edit_photo_add
import yallakhidma.shared.generated.resources.edit_photo_change
import yallakhidma.shared.generated.resources.edit_subtitle
import yallakhidma.shared.generated.resources.edit_title
import yallakhidma.shared.generated.resources.field_city
import yallakhidma.shared.generated.resources.field_country
import yallakhidma.shared.generated.resources.field_full_name
import yallakhidma.shared.generated.resources.field_phone

object ClientEditProfileScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val model = koinScreenModel<ClientEditProfileScreenModel>()
        val state by model.state.collectAsState()
        val pickImage = rememberImagePicker { bytes, _ -> model.uploadPhoto(bytes) }

        LaunchedEffect(state.saved) { if (state.saved) navigator.pop() }

        Column(
            Modifier
                .fillMaxSize()
                .background(Yk.Canvas)
                .safeTopPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            YkTopBar(onBack = { navigator.pop() }, title = stringResource(Res.string.edit_title), modifier = Modifier.padding(top = 8.dp))
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(Res.string.edit_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = Yk.Ink2,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Spacer(Modifier.height(24.dp))

            // Photo with camera badge
            Box(Modifier.align(Alignment.CenterHorizontally)) {
                Box(
                    Modifier
                        .size(112.dp)
                        .clip(CircleShape)
                        .border(3.dp, Yk.OrangeTint, CircleShape)
                        .clickable(enabled = !state.photoUploading, role = Role.Button) { pickImage() },
                    contentAlignment = Alignment.Center,
                ) {
                    Avatar(state.name, state.photoUrl.takeIf { it.isNotBlank() }, 112.dp, textSize = MaterialTheme.typography.displaySmall.fontSize)
                    if (state.photoUploading) {
                        Box(Modifier.size(112.dp).background(Color.Black.copy(alpha = 0.35f)), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color.White, strokeWidth = 2.5.dp, modifier = Modifier.size(28.dp))
                        }
                    }
                }
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-2).dp, y = (-2).dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Yk.Orange)
                        .border(3.dp, Yk.Canvas, CircleShape),
                    contentAlignment = Alignment.Center,
                ) { Icon(YkIcons.Camera, null, tint = Color.White, modifier = Modifier.size(16.dp)) }
            }
            Text(
                stringResource(if (state.photoUrl.isBlank()) Res.string.edit_photo_add else Res.string.edit_photo_change),
                style = MaterialTheme.typography.titleSmall,
                color = Yk.Orange,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 10.dp)
                    .clickable(enabled = !state.photoUploading, role = Role.Button) { pickImage() }
                    .padding(6.dp),
            )

            Spacer(Modifier.height(20.dp))
            YkTextField(
                value = state.name, onValueChange = model::onName,
                label = stringResource(Res.string.field_full_name), leadingIcon = YkIcons.User,
                capitalization = KeyboardCapitalization.Words,
            )
            Spacer(Modifier.height(16.dp))
            YkTextField(
                value = state.phone, onValueChange = model::onPhone,
                label = stringResource(Res.string.field_phone), leadingIcon = YkIcons.Phone,
                keyboardType = KeyboardType.Phone, forceLtr = true,
            )
            Spacer(Modifier.height(16.dp))
            YkTextField(value = state.city, onValueChange = model::onCity, label = stringResource(Res.string.field_city), leadingIcon = YkIcons.MapPin)
            Spacer(Modifier.height(16.dp))
            YkTextField(value = state.country, onValueChange = model::onCountry, label = stringResource(Res.string.field_country), leadingIcon = YkIcons.Globe)

            Spacer(Modifier.height(20.dp))
            ErrorBanner(state.error?.asString())
            Spacer(Modifier.height(12.dp))
            YkButton(text = stringResource(Res.string.common_save), onClick = model::submit, loading = state.saving)
            Spacer(Modifier.height(24.dp))
        }
    }
}
