package com.yallakhedma.app.presentation.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.yallakhedma.app.domain.repository.AuthRepository
import com.yallakhedma.app.presentation.components.ErrorBanner
import com.yallakhedma.app.presentation.components.YkButton
import com.yallakhedma.app.presentation.components.YkFormScreen
import com.yallakhedma.app.presentation.components.shimmer
import com.yallakhedma.app.presentation.i18n.asString
import com.yallakhedma.app.presentation.screens.common.ProviderProfileForm
import com.yallakhedma.app.presentation.screens.common.ProviderProfileFormActions
import com.yallakhedma.app.presentation.screens.common.ProviderProfileFormValues
import com.yallakhedma.app.presentation.theme.YkRadius
import com.yallakhedma.app.util.rememberImagePicker
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.common_save
import yallakhidma.shared.generated.resources.pedit_subtitle
import yallakhidma.shared.generated.resources.pedit_title

object ProviderEditProfileScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val model = koinScreenModel<ProviderEditProfileScreenModel>()
        val authRepository = koinInject<AuthRepository>()
        val state by model.state.collectAsState()
        val user by authRepository.currentUser.collectAsState(null)
        val pickImage = rememberImagePicker { bytes, mime -> model.uploadPhoto(bytes, mime) }

        LaunchedEffect(state.saved) { if (state.saved) navigator.pop() }

        YkFormScreen(
            title = stringResource(Res.string.pedit_title),
            subtitle = stringResource(Res.string.pedit_subtitle),
            onBack = { navigator.pop() },
            bottomBar = {
                ErrorBanner(state.error?.asString(), Modifier.padding(bottom = 10.dp))
                YkButton(
                    text = stringResource(Res.string.common_save),
                    onClick = model::submit,
                    loading = state.isSaving,
                    enabled = state.loaded && !state.photoUploading,
                )
            },
        ) {
            if (!state.loaded) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(Modifier.size(112.dp).align(Alignment.CenterHorizontally).shimmer(CircleShape))
                    repeat(5) { Box(Modifier.fillMaxWidth().height(56.dp).shimmer(YkRadius.field)) }
                }
            } else {
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
}
