package com.yallakhedma.app.presentation.screens.auth.login

import com.yallakhedma.app.presentation.components.safeTopPadding
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.yallakhedma.app.domain.repository.AuthRepository
import com.yallakhedma.app.presentation.components.ErrorBanner
import com.yallakhedma.app.presentation.components.PasswordToggle
import com.yallakhedma.app.presentation.components.PromptLink
import com.yallakhedma.app.presentation.components.YkButton
import com.yallakhedma.app.presentation.components.YkIcons
import com.yallakhedma.app.presentation.components.YkTextField
import com.yallakhedma.app.presentation.components.YkTopBar
import com.yallakhedma.app.presentation.i18n.asString
import com.yallakhedma.app.presentation.routing.destinationForUser
import com.yallakhedma.app.presentation.screens.auth.forgot.ForgotPasswordScreen
import com.yallakhedma.app.presentation.screens.auth.usertype.UserTypeSelectionScreen
import com.yallakhedma.app.presentation.theme.Yk
import com.yallakhedma.app.presentation.theme.YkRadius
import kotlinx.coroutines.flow.first
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.common_hide
import yallakhidma.shared.generated.resources.common_show
import yallakhidma.shared.generated.resources.field_email
import yallakhidma.shared.generated.resources.field_email_placeholder
import yallakhidma.shared.generated.resources.field_password
import yallakhidma.shared.generated.resources.field_password_placeholder
import yallakhidma.shared.generated.resources.login_create
import yallakhidma.shared.generated.resources.login_forgot
import yallakhidma.shared.generated.resources.login_no_account
import yallakhidma.shared.generated.resources.login_submit
import yallakhidma.shared.generated.resources.login_subtitle
import yallakhidma.shared.generated.resources.login_title

/** Email sign-in. Reached from Welcome → "Continue with email". */
object LoginScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val model = koinScreenModel<LoginScreenModel>()
        val auth = koinInject<AuthRepository>()
        val state by model.state.collectAsState()
        val focus = LocalFocusManager.current

        LaunchedEffect(state.signedIn) {
            if (state.signedIn) navigator.replaceAll(destinationForUser(auth.currentUser.first()))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Yk.Canvas)
                .safeTopPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            YkTopBar(onBack = { navigator.pop() }, modifier = Modifier.padding(top = 8.dp))
            Spacer(Modifier.height(24.dp))
            Text(stringResource(Res.string.login_title), style = MaterialTheme.typography.displaySmall, color = Yk.Ink)
            Spacer(Modifier.height(6.dp))
            Text(stringResource(Res.string.login_subtitle), style = MaterialTheme.typography.bodyLarge, color = Yk.Ink2)
            Spacer(Modifier.height(32.dp))

            YkTextField(
                value = state.email,
                onValueChange = model::onEmailChange,
                label = stringResource(Res.string.field_email),
                placeholder = stringResource(Res.string.field_email_placeholder),
                leadingIcon = YkIcons.Mail,
                keyboardType = KeyboardType.Email,
                forceLtr = true,
                isError = state.emailError != null,
                supportingText = state.emailError?.asString(),
            )
            Spacer(Modifier.height(18.dp))
            YkTextField(
                value = state.password,
                onValueChange = model::onPasswordChange,
                label = stringResource(Res.string.field_password),
                placeholder = stringResource(Res.string.field_password_placeholder),
                leadingIcon = YkIcons.Lock,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(onDone = { focus.clearFocus(); model.submit() }),
                isPassword = true,
                passwordVisible = state.passwordVisible,
                forceLtr = true,
                isError = state.passwordError != null,
                supportingText = state.passwordError?.asString(),
                trailing = {
                    PasswordToggle(
                        visible = state.passwordVisible,
                        onToggle = model::togglePasswordVisibility,
                        contentDescription = stringResource(if (state.passwordVisible) Res.string.common_hide else Res.string.common_show),
                    )
                },
            )

            Text(
                text = stringResource(Res.string.login_forgot),
                style = MaterialTheme.typography.titleSmall,
                color = Yk.Orange,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 8.dp)
                    .clip(YkRadius.chip)
                    .clickable(role = Role.Button) { navigator.push(ForgotPasswordScreen(state.email.trim())) }
                    .padding(horizontal = 6.dp, vertical = 8.dp),
            )

            Spacer(Modifier.height(16.dp))
            ErrorBanner(state.error?.asString(), Modifier.padding(bottom = 12.dp))
            YkButton(
                text = stringResource(Res.string.login_submit),
                onClick = { focus.clearFocus(); model.submit() },
                loading = state.isLoading,
            )

            Spacer(Modifier.height(32.dp))
            PromptLink(
                prompt = stringResource(Res.string.login_no_account),
                action = stringResource(Res.string.login_create),
                onClick = { navigator.push(UserTypeSelectionScreen(UserTypeSelectionScreen.Mode.SignUp)) },
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}
