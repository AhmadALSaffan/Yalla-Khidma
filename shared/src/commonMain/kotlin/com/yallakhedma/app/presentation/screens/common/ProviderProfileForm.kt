package com.yallakhedma.app.presentation.screens.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yallakhedma.app.presentation.components.Avatar
import com.yallakhedma.app.presentation.components.YkIcons
import com.yallakhedma.app.presentation.components.YkTextField
import com.yallakhedma.app.presentation.theme.Yk
import com.yallakhedma.app.presentation.theme.YkRadius
import com.yallakhedma.app.util.rememberLocationPicker
import org.jetbrains.compose.resources.stringResource
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.edit_photo_add
import yallakhidma.shared.generated.resources.edit_photo_change
import yallakhidma.shared.generated.resources.field_bio
import yallakhidma.shared.generated.resources.field_bio_ph
import yallakhidma.shared.generated.resources.field_city
import yallakhidma.shared.generated.resources.field_jobs
import yallakhidma.shared.generated.resources.field_phone
import yallakhidma.shared.generated.resources.field_profession
import yallakhidma.shared.generated.resources.field_profession_ph
import yallakhidma.shared.generated.resources.field_specialties
import yallakhidma.shared.generated.resources.field_specialties_ph
import yallakhidma.shared.generated.resources.field_years
import yallakhidma.shared.generated.resources.locating
import yallakhidma.shared.generated.resources.use_my_location

/** Field values + callbacks shared by provider profile setup and edit. */
data class ProviderProfileFormValues(
    val name: String,
    val photoUrl: String,
    val photoUploading: Boolean,
    val profession: String,
    val city: String,
    val bio: String,
    val phone: String,
    val years: String,
    val jobs: String,
    val services: String,
)

class ProviderProfileFormActions(
    val onPickPhoto: () -> Unit,
    val onProfession: (String) -> Unit,
    val onCity: (String) -> Unit,
    val onBio: (String) -> Unit,
    val onPhone: (String) -> Unit,
    val onYears: (String) -> Unit,
    val onJobs: (String) -> Unit,
    val onServices: (String) -> Unit,
)

@Composable
fun ColumnScope.ProviderProfileForm(values: ProviderProfileFormValues, actions: ProviderProfileFormActions) {
    // Photo with camera badge
    Box(Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp)) {
        Box(
            Modifier
                .size(112.dp)
                .clip(CircleShape)
                .border(3.dp, Yk.OrangeTint, CircleShape)
                .clickable(enabled = !values.photoUploading, role = Role.Button, onClick = actions.onPickPhoto),
            contentAlignment = Alignment.Center,
        ) {
            if (values.photoUrl.isBlank() && values.name.isBlank()) {
                Box(Modifier.size(112.dp).background(Yk.OrangeSoft), contentAlignment = Alignment.Center) {
                    Icon(YkIcons.Camera, null, tint = Yk.Orange, modifier = Modifier.size(32.dp))
                }
            } else {
                Avatar(values.name, values.photoUrl.takeIf { it.isNotBlank() }, 112.dp, textSize = MaterialTheme.typography.displaySmall.fontSize)
            }
            if (values.photoUploading) {
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
        stringResource(if (values.photoUrl.isBlank()) Res.string.edit_photo_add else Res.string.edit_photo_change),
        style = MaterialTheme.typography.titleSmall,
        color = Yk.Orange,
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .padding(top = 10.dp)
            .clip(YkRadius.chip)
            .clickable(enabled = !values.photoUploading, role = Role.Button, onClick = actions.onPickPhoto)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    )

    Spacer(Modifier.height(20.dp))
    YkTextField(
        value = values.profession,
        onValueChange = actions.onProfession,
        label = stringResource(Res.string.field_profession),
        placeholder = stringResource(Res.string.field_profession_ph),
        leadingIcon = YkIcons.Briefcase,
        capitalization = KeyboardCapitalization.Sentences,
    )

    Spacer(Modifier.height(16.dp))
    YkTextField(
        value = values.city,
        onValueChange = actions.onCity,
        label = stringResource(Res.string.field_city),
        leadingIcon = YkIcons.MapPin,
    )
    Spacer(Modifier.height(10.dp))
    var locating by remember { mutableStateOf(false) }
    val pickCity = rememberLocationPicker { picked ->
        locating = false
        if (picked != null) actions.onCity(picked.label)
    }
    Row(
        Modifier
            .clip(YkRadius.chip)
            .background(Yk.OrangeSoft)
            .clickable(enabled = !locating, role = Role.Button) { locating = true; pickCity() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (locating) {
            CircularProgressIndicator(color = Yk.Orange, strokeWidth = 2.dp, modifier = Modifier.size(14.dp))
        } else {
            Icon(YkIcons.MapPin, null, tint = Yk.Orange, modifier = Modifier.size(16.dp))
        }
        Text(
            stringResource(if (locating) Res.string.locating else Res.string.use_my_location),
            style = MaterialTheme.typography.labelLarge,
            color = Yk.OrangeDeep,
        )
    }

    Spacer(Modifier.height(16.dp))
    YkTextField(
        value = values.phone,
        onValueChange = actions.onPhone,
        label = stringResource(Res.string.field_phone),
        leadingIcon = YkIcons.Phone,
        keyboardType = KeyboardType.Phone,
        forceLtr = true,
    )

    Spacer(Modifier.height(16.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        YkTextField(
            value = values.years,
            onValueChange = actions.onYears,
            label = stringResource(Res.string.field_years),
            leadingIcon = YkIcons.Clock,
            keyboardType = KeyboardType.Number,
            modifier = Modifier.weight(1f),
        )
        YkTextField(
            value = values.jobs,
            onValueChange = actions.onJobs,
            label = stringResource(Res.string.field_jobs),
            leadingIcon = YkIcons.Check,
            keyboardType = KeyboardType.Number,
            modifier = Modifier.weight(1f),
        )
    }

    Spacer(Modifier.height(16.dp))
    YkTextField(
        value = values.bio,
        onValueChange = actions.onBio,
        label = stringResource(Res.string.field_bio),
        placeholder = stringResource(Res.string.field_bio_ph),
        singleLine = false,
        capitalization = KeyboardCapitalization.Sentences,
    )

    Spacer(Modifier.height(16.dp))
    YkTextField(
        value = values.services,
        onValueChange = actions.onServices,
        label = stringResource(Res.string.field_specialties),
        placeholder = stringResource(Res.string.field_specialties_ph),
        leadingIcon = YkIcons.Wrench,
    )
    val tags = remember(values.services) {
        values.services.split('،', ',', '\n').map { it.trim() }.filter { it.isNotEmpty() }.distinct()
    }
    AnimatedVisibility(tags.isNotEmpty(), enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
        FlowRow(
            Modifier.fillMaxWidth().padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            tags.forEach { tag ->
                Text(
                    tag,
                    style = MaterialTheme.typography.labelLarge,
                    color = Yk.OrangeDeep,
                    modifier = Modifier.clip(YkRadius.chip).background(Yk.OrangeSoft).padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
        }
    }
}
