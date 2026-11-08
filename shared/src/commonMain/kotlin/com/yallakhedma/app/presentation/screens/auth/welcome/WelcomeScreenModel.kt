package com.yallakhedma.app.presentation.screens.auth.welcome

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.yallakhedma.app.data.auth.SocialAuthClient
import com.yallakhedma.app.domain.model.User
import com.yallakhedma.app.domain.model.UserType
import com.yallakhedma.app.domain.repository.AuthRepository
import com.yallakhedma.app.domain.util.DataResult
import com.yallakhedma.app.domain.util.ErrorCode
import com.yallakhedma.app.presentation.i18n.UiText
import com.yallakhedma.app.presentation.i18n.toUiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Google-first auth. Works for both new and returning users: a brand-new
 * Google account is created with `userTypeChosen = false`, and routing then
 * asks for client vs provider. If [presetType] is given (sign-up screen),
 * that choice is applied immediately instead.
 */
class GoogleAuthDelegate(
    private val authRepository: AuthRepository,
    private val socialAuthClient: SocialAuthClient,
) {
    suspend fun signIn(presetType: UserType? = null): DataResult<User> {
        val idToken = socialAuthClient.signInWithGoogle()
            ?: return DataResult.Error("Google sign-in cancelled", code = ErrorCode.SocialCancelled)
        val result = authRepository.signInWithGoogleCredential(idToken)
        if (result is DataResult.Success && presetType != null && !result.data.userTypeChosen) {
            return authRepository.chooseUserType(presetType)
        }
        return result
    }
}

data class WelcomeState(
    val googleLoading: Boolean = false,
    val error: UiText? = null,
    val signedIn: Boolean = false,
)

class WelcomeScreenModel(
    authRepository: AuthRepository,
    socialAuthClient: SocialAuthClient,
) : ScreenModel {

    private val google = GoogleAuthDelegate(authRepository, socialAuthClient)
    private val _state = MutableStateFlow(WelcomeState())
    val state = _state.asStateFlow()

    fun continueWithGoogle() {
        if (_state.value.googleLoading) return
        screenModelScope.launch {
            _state.update { it.copy(googleLoading = true, error = null) }
            when (val result = google.signIn()) {
                is DataResult.Success -> _state.update { it.copy(googleLoading = false, signedIn = true) }
                is DataResult.Error -> _state.update { it.copy(googleLoading = false, error = result.toUiText()) }
                DataResult.Loading -> Unit
            }
        }
    }

    fun clearError() = _state.update { it.copy(error = null) }
}
