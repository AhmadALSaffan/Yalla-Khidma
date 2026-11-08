package com.yallakhedma.app.presentation.screens.provider_setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.yallakhedma.app.domain.repository.AuthRepository
import com.yallakhedma.app.presentation.components.ErrorBanner
import com.yallakhedma.app.presentation.components.YkButton
import com.yallakhedma.app.presentation.components.YkFormScreen
import com.yallakhedma.app.presentation.components.YkIcons
import com.yallakhedma.app.presentation.i18n.asString
import com.yallakhedma.app.presentation.routing.destinationForUser
import com.yallakhedma.app.presentation.screens.common.ProviderProfileForm
import com.yallakhedma.app.presentation.screens.common.ProviderProfileFormActions
import com.yallakhedma.app.presentation.screens.common.ProviderProfileFormValues
import com.yallakhedma.app.presentation.theme.Yk
import com.yallakhedma.app.presentation.theme.YkRadius
import com.yallakhedma.app.util.rememberImagePicker
import kotlinx.coroutines.flow.first
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.psetup_submit
import yallakhidma.shared.generated.resources.psetup_subtitle
import yallakhidma.shared.generated.resources.psetup_title

object ProviderProfileSetupScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val model = koinScreenModel<ProviderProfileSetupScreenModel>()
        val authRepository = koinInject<AuthRepository>()
        val state by model.state.collectAsState()
        val user by authRepository.currentUser.collectAsState(null)
        val pickImage = rememberImagePicker { bytes, mime -> model.uploadPhoto(bytes, mime) }

        LaunchedEffect(state.saved) {
            if (state.saved) navigator.replaceAll(destinationForUser(authRepository.currentUser.first()))
        }

        YkFormScreen(
            title = stringResource(Res.string.psetup_title),
            onBack = null,
            bottomBar = {
                ErrorBanner(state.error?.asString(), Modifier.padding(bottom = 10.dp))
                YkButton(
                    text = stringResource(Res.string.psetup_submit),
                    onClick = model::submit,
                    loading = state.isLoading,
                    enabled = !state.photoUploading,
                )
            },
        ) {
            Row(
                Modifier.fillMaxWidth().clip(YkRadius.sm).background(Yk.SuccessSoft).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(YkIcons.ShieldCheck, null, tint = Yk.Success, modifier = Modifier.size(20.dp))
                Text(stringResource(Res.string.psetup_subtitle), style = MaterialTheme.typography.bodyMedium, color = Yk.Ink)
            }
            Spacer(Modifier.height(20.dp))
            ProviderProfileForm(
                values = ProviderProfileFormValues(
                    name = user?.name.orEmpty(),
                    photoUrl = state.photoUrl,
                    photoUploading = state.photoUploading,
                    profession = state.profession,
                    city = state.city,
                    bio = state.bio,
                    phone = state.phone,
                    years = state.years,
                    jobs = state.completedJobs,
                    services = state.servicesText,
                ),
                actions = ProviderProfileFormActions(
                    onPickPhoto = pickImage,
                    onProfession = model::onProfession,
                    onCity = model::onCity,
                    onBio = model::onBio,
                    onPhone = model::onPhone,
                    onYears = model::onYears,
                    onJobs = model::onCompletedJobs,
                    onServices = model::onServices,
                ),
            )
        }
    }
}
