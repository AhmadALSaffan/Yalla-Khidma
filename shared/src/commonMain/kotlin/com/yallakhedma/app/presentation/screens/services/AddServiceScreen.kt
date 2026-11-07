package com.yallakhedma.app.presentation.screens.services

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.yallakhedma.app.presentation.components.ErrorBanner
import com.yallakhedma.app.presentation.components.RemoteImage
import com.yallakhedma.app.presentation.components.YkButton
import com.yallakhedma.app.presentation.components.YkChip
import com.yallakhedma.app.presentation.components.YkIcons
import com.yallakhedma.app.presentation.components.YkTextField
import com.yallakhedma.app.presentation.components.YkTopBar
import com.yallakhedma.app.presentation.components.safeTopPadding
import com.yallakhedma.app.presentation.i18n.LocalAppLanguage
import com.yallakhedma.app.presentation.i18n.asString
import com.yallakhedma.app.presentation.i18n.displayName
import com.yallakhedma.app.presentation.theme.Yk
import com.yallakhedma.app.presentation.theme.YkRadius
import com.yallakhedma.app.util.rememberImagePicker
import com.yallakhedma.app.util.rememberLocationPicker
import org.jetbrains.compose.resources.stringResource
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.add_service_photo
import yallakhidma.shared.generated.resources.add_service_photo_hint
import yallakhidma.shared.generated.resources.add_service_submit
import yallakhidma.shared.generated.resources.add_service_subtitle
import yallakhidma.shared.generated.resources.add_service_title
import yallakhidma.shared.generated.resources.field_category
import yallakhidma.shared.generated.resources.field_category_ph
import yallakhidma.shared.generated.resources.field_description
import yallakhidma.shared.generated.resources.field_description_ph
import yallakhidma.shared.generated.resources.field_duration
import yallakhidma.shared.generated.resources.field_duration_ph
import yallakhidma.shared.generated.resources.field_location
import yallakhidma.shared.generated.resources.field_location_ph
import yallakhidma.shared.generated.resources.field_price
import yallakhidma.shared.generated.resources.field_price_ph
import yallakhidma.shared.generated.resources.field_service_title
import yallakhidma.shared.generated.resources.field_service_title_ph
import yallakhidma.shared.generated.resources.locating
import yallakhidma.shared.generated.resources.use_my_location

object AddServiceScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val model = koinScreenModel<AddServiceScreenModel>()
        val state by model.state.collectAsState()
        val language = LocalAppLanguage.current
        val pickImage = rememberImagePicker { bytes, _ -> model.uploadImage(bytes) }
        var locating by remember { mutableStateOf(false) }
        val pickLocation = rememberLocationPicker { picked ->
            locating = false
            if (picked != null) model.onLocationPicked(picked.label, picked.latitude, picked.longitude)
        }

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
        ) {
            YkTopBar(onBack = { navigator.pop() }, modifier = Modifier.padding(top = 8.dp))
            Spacer(Modifier.height(12.dp))
            Text(stringResource(Res.string.add_service_title), style = MaterialTheme.typography.displaySmall, color = Yk.Ink)
            Spacer(Modifier.height(4.dp))
            Text(stringResource(Res.string.add_service_subtitle), style = MaterialTheme.typography.bodyLarge, color = Yk.Ink2)
            Spacer(Modifier.height(24.dp))

            // Photo picker
            Text(stringResource(Res.string.add_service_photo), style = MaterialTheme.typography.labelLarge, color = Yk.Ink2)
            Spacer(Modifier.height(8.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(YkRadius.card)
                    .background(Yk.Fill)
                    .border(1.dp, Yk.LineStrong, YkRadius.card)
                    .clickable(enabled = !state.imageUploading, role = Role.Button) { pickImage() },
                contentAlignment = Alignment.Center,
            ) {
                if (state.imageUrl.isNotBlank()) {
                    RemoteImage(state.imageUrl, null, Modifier.fillMaxSize())
                    Box(
                        Modifier.align(Alignment.BottomEnd).padding(10.dp).size(36.dp).clip(YkRadius.pill).background(Yk.Night.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center,
                    ) { Icon(YkIcons.Camera, null, tint = Color.White, modifier = Modifier.size(16.dp)) }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.size(52.dp).clip(YkRadius.pill).background(Yk.OrangeSoft), contentAlignment = Alignment.Center) {
                            Icon(YkIcons.Camera, null, tint = Yk.Orange, modifier = Modifier.size(24.dp))
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(stringResource(Res.string.add_service_photo_hint), style = MaterialTheme.typography.bodyMedium, color = Yk.Ink3)
                    }
                }
                if (state.imageUploading) {
                    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.5.dp, modifier = Modifier.size(30.dp))
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            YkTextField(
                value = state.title, onValueChange = model::onTitle,
                label = stringResource(Res.string.field_service_title),
                placeholder = stringResource(Res.string.field_service_title_ph),
                leadingIcon = YkIcons.Wrench,
            )
            Spacer(Modifier.height(16.dp))
            YkTextField(
                value = state.category, onValueChange = model::onCategory,
                label = stringResource(Res.string.field_category),
                placeholder = stringResource(Res.string.field_category_ph),
                leadingIcon = YkIcons.Grid,
            )
            if (state.categories.isNotEmpty()) {
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    state.categories.forEach { c ->
                        val name = c.displayName(language)
                        YkChip(name, state.category == name, { model.onCategory(name) })
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            YkTextField(
                value = state.description, onValueChange = model::onDescription,
                label = stringResource(Res.string.field_description),
                placeholder = stringResource(Res.string.field_description_ph),
                singleLine = false,
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                YkTextField(
                    value = state.price, onValueChange = model::onPrice,
                    label = stringResource(Res.string.field_price),
                    placeholder = stringResource(Res.string.field_price_ph),
                    leadingIcon = YkIcons.CreditCard,
                    modifier = Modifier.weight(1f),
                )
                YkTextField(
                    value = state.duration, onValueChange = model::onDuration,
                    label = stringResource(Res.string.field_duration),
                    placeholder = stringResource(Res.string.field_duration_ph),
                    leadingIcon = YkIcons.Clock,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(16.dp))
            YkTextField(
                value = state.distance, onValueChange = model::onDistance,
                label = stringResource(Res.string.field_location),
                placeholder = stringResource(Res.string.field_location_ph),
                leadingIcon = YkIcons.MapPin,
            )
            Row(
                Modifier
                    .padding(top = 10.dp)
                    .clip(YkRadius.pill)
                    .background(Yk.OrangeSoft)
                    .clickable(enabled = !locating, role = Role.Button) { locating = true; pickLocation() }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (locating) {
                    CircularProgressIndicator(color = Yk.Orange, strokeWidth = 2.dp, modifier = Modifier.size(14.dp))
                } else {
                    Icon(YkIcons.MapPin, null, tint = Yk.Orange, modifier = Modifier.size(14.dp))
                }
                Text(
                    stringResource(if (locating) Res.string.locating else Res.string.use_my_location),
                    style = MaterialTheme.typography.labelLarge,
                    color = Yk.OrangeDeep,
                )
            }

            Spacer(Modifier.height(24.dp))
            ErrorBanner(state.error?.asString(), Modifier.padding(bottom = 12.dp))
            YkButton(
                text = stringResource(Res.string.add_service_submit),
                onClick = model::submit,
                loading = state.isSaving,
                enabled = !state.imageUploading,
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
