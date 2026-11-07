package com.yallakhedma.app.presentation.screens.auth.signup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.yallakhedma.app.domain.model.UserType
import com.yallakhedma.app.domain.repository.AuthRepository
import com.yallakhedma.app.presentation.components.ErrorBanner
import com.yallakhedma.app.presentation.components.GoogleButton
import com.yallakhedma.app.presentation.components.LabeledDivider
import com.yallakhedma.app.presentation.components.PasswordToggle
import com.yallakhedma.app.presentation.components.PromptLink
import com.yallakhedma.app.presentation.components.YkButton
import com.yallakhedma.app.presentation.components.YkIcons
import com.yallakhedma.app.presentation.components.YkTextField
import com.yallakhedma.app.presentation.components.YkTopBar
import com.yallakhedma.app.presentation.i18n.asString
import com.yallakhedma.app.presentation.routing.destinationForUser
import com.yallakhedma.app.presentation.screens.auth.login.LoginScreen
import com.yallakhedma.app.presentation.theme.Yk
import com.yallakhedma.app.presentation.theme.YkRadius
import kotlinx.coroutines.flow.first
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.auth_continue_google
import yallakhidma.shared.generated.resources.common_change
import yallakhidma.shared.generated.resources.common_hide
import yallakhidma.shared.generated.resources.common_show
import yallakhidma.shared.generated.resources.field_confirm_password
import yallakhidma.shared.generated.resources.field_email
import yallakhidma.shared.generated.resources.field_email_placeholder
import yallakhidma.shared.generated.resources.field_full_name
import yallakhidma.shared.generated.resources.field_full_name_placeholder
import yallakhidma.shared.generated.resources.field_password
import yallakhidma.shared.generated.resources.field_password_placeholder
import yallakhidma.shared.generated.resources.signup_as_client
import yallakhidma.shared.generated.resources.signup_as_provider
import yallakhidma.shared.generated.resources.signup_have_account
import yallakhidma.shared.generated.resources.signup_login
import yallakhidma.shared.generated.resources.signup_or_email
import yallakhidma.shared.generated.resources.signup_req_digit
import yallakhidma.shared.generated.resources.signup_req_length
import yallakhidma.shared.generated.resources.signup_req_letter
import yallakhidma.shared.generated.resources.signup_req_match
import yallakhidma.shared.generated.resources.signup_submit
import yallakhidma.shared.generated.resources.signup_subtitle_client
import yallakhidma.shared.generated.resources.signup_subtitle_provider
import yallakhidma.shared.generated.resources.signup_terms
import yallakhidma.shared.generated.resources.signup_title

class SignUpScreen(private val initialType: UserType = UserType.Client) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val model = koinScreenModel<SignUpScreenModel>()
        val auth = koinInject<AuthRepository>()
        val state by model.state.collectAsState()
        val focus = LocalFocusManager.current
        val isProvider = initialType == UserType.Provider

        LaunchedEffect(Unit) { model.setInitialUserType(initialType) }
        LaunchedEffect(state.signedUp) {
            if (state.signedUp) navigator.replaceAll(destinationForUser(auth.currentUser.first()))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Yk.Canvas)
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            YkTopBar(onBack = { navigator.pop() }, modifier = Modifier.padding(top = 8.dp))
            Spacer(Modifier.height(20.dp))

            // Account-type chip with "Change" → back to the selection step.
            Row(
                modifier = Modifier
                    .clip(YkRadius.pill)
                    .background(Yk.OrangeSoft)
                    .clickable(role = Role.Button) { navigator.pop() }
                    .padding(start = 10.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(if (isProvider) YkIcons.Briefcase else YkIcons.User, null, tint = Yk.OrangeDeep, modifier = Modifier.size(16.dp))
                Text(
                    stringResource(if (isProvider) Res.string.signup_as_provider else Res.string.signup_as_client),
                    style = MaterialTheme.typography.labelLarge,
                    color = Yk.OrangeDeep,
                )
                Text("·", color = Yk.OrangeDeep.copy(alpha = 0.5f))
                Text(stringResource(Res.string.common_change), style = MaterialTheme.typography.labelLarge, color = Yk.Orange)
            }
            Spacer(Modifier.height(16.dp))
            Text(stringResource(Res.string.signup_title), style = MaterialTheme.typography.displaySmall, color = Yk.Ink)
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(if (isProvider) Res.string.signup_subtitle_provider else Res.string.signup_subtitle_client),
                style = MaterialTheme.typography.bodyLarge,
                color = Yk.Ink2,
            )
            Spacer(Modifier.height(24.dp))

            GoogleButton(
                text = stringResource(Res.string.auth_continue_google),
                onClick = model::continueWithGoogle,
                loading = state.googleLoading,
                enabled = !state.isLoading,
            )
            Spacer(Modifier.height(24.dp))
            LabeledDivider(stringResource(Res.string.signup_or_email))
            Spacer(Modifier.height(20.dp))

            YkTextField(
                value = state.name,
                onValueChange = model::onNameChange,
                label = stringResource(Res.string.field_full_name),
                placeholder = stringResource(Res.string.field_full_name_placeholder),
                leadingIcon = YkIcons.User,
                capitalization = KeyboardCapitalization.Words,
                isError = state.nameError != null,
                supportingText = state.nameError?.asString(),
            )
            Spacer(Modifier.height(16.dp))
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
            Spacer(Modifier.height(16.dp))
            val toggle: @Composable () -> Unit = {
                PasswordToggle(
                    visible = state.passwordVisible,
                    onToggle = model::togglePasswordVisibility,
                    contentDescription = stringResource(if (state.passwordVisible) Res.string.common_hide else Res.string.common_show),
                )
            }
            YkTextField(
                value = state.password,
                onValueChange = model::onPasswordChange,
                label = stringResource(Res.string.field_password),
                placeholder = stringResource(Res.string.field_password_placeholder),
                leadingIcon = YkIcons.Lock,
                keyboardType = KeyboardType.Password,
                isPassword = true,
                passwordVisible = state.passwordVisible,
                forceLtr = true,
                isError = state.passwordError != null,
                supportingText = state.passwordError?.asString(),
                trailing = toggle,
            )
            Spacer(Modifier.height(16.dp))
            YkTextField(
                value = state.confirmPassword,
                onValueChange = model::onConfirmPasswordChange,
                label = stringResource(Res.string.field_confirm_password),
                placeholder = stringResource(Res.string.field_password_placeholder),
                leadingIcon = YkIcons.Lock,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(onDone = { focus.clearFocus() }),
                isPassword = true,
                passwordVisible = state.passwordVisible,
                forceLtr = true,
                isError = state.confirmError != null,
                supportingText = state.confirmError?.asString(),
            )

            // Live password checklist
            AnimatedVisibility(
                visible = state.password.isNotEmpty(),
                enter = fadeIn(tween(200)) + expandVertically(),
                exit = fadeOut(tween(150)) + shrinkVertically(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                        .clip(YkRadius.sm)
                        .background(Yk.Fill)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    RequirementRow(stringResource(Res.string.signup_req_length), state.password.length >= 8)
                    RequirementRow(stringResource(Res.string.signup_req_letter), state.password.any { it.isLetter() })
                    RequirementRow(stringResource(Res.string.signup_req_digit), state.password.any { it.isDigit() })
                    RequirementRow(
                        stringResource(Res.string.signup_req_match),
                        state.confirmPassword.isNotEmpty() && state.password == state.confirmPassword,
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
            TermsRow(checked = state.termsAccepted, onToggle = model::onTermsToggle)

            Spacer(Modifier.height(18.dp))
            ErrorBanner(state.error?.asString(), Modifier.padding(bottom = 12.dp))
            YkButton(
                text = stringResource(Res.string.signup_submit),
                onClick = { focus.clearFocus(); model.submit() },
                loading = state.isLoading,
                enabled = !state.googleLoading,
            )

            Spacer(Modifier.height(24.dp))
            PromptLink(
                prompt = stringResource(Res.string.signup_have_account),
                action = stringResource(Res.string.signup_login),
                onClick = {
                    // Reuse the login screen if it's already under us; otherwise open it.
                    if (navigator.items.any { it is LoginScreen }) navigator.popUntil { it is LoginScreen }
                    else navigator.push(LoginScreen)
                },
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun RequirementRow(label: String, met: Boolean) {
    val color by animateColorAsState(if (met) Yk.Success else Yk.Ink3, tween(220), label = "req-color")
    val fill by animateFloatAsState(if (met) 1f else 0f, tween(220), label = "req-fill")
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            modifier = Modifier.size(18.dp).clip(CircleShape).border(1.5.dp, color, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier.size(18.dp).graphicsLayer { scaleX = fill; scaleY = fill; alpha = fill }.background(Yk.Success, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(YkIcons.Check, null, tint = Color.White, modifier = Modifier.size(12.dp))
            }
        }
        Text(label, style = MaterialTheme.typography.bodyMedium, color = color)
    }
}

@Composable
private fun TermsRow(checked: Boolean, onToggle: () -> Unit) {
    val box by animateColorAsState(if (checked) Yk.Orange else Yk.Canvas, tween(160), label = "terms-bg")
    val border by animateColorAsState(if (checked) Yk.Orange else Yk.LineStrong, tween(160), label = "terms-border")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(YkRadius.sm)
            .clickable(role = Role.Checkbox, onClick = onToggle)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(22.dp).clip(YkRadius.chip).background(box).border(1.5.dp, border, YkRadius.chip),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) Icon(YkIcons.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
        }
        Text(
            stringResource(Res.string.signup_terms),
            style = MaterialTheme.typography.bodyMedium,
            color = Yk.Ink2,
        )
    }
}
