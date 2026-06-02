package com.yallakhedma.app.presentation.screens.auth.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.yallakhedma.app.domain.repository.AuthRepository
import com.yallakhedma.app.presentation.components.AppPrimaryButton
import com.yallakhedma.app.presentation.components.AppTextField
import com.yallakhedma.app.presentation.routing.destinationForUser
import com.yallakhedma.app.presentation.screens.auth.usertype.UserTypeSelectionScreen
import com.yallakhedma.app.presentation.theme.LocalSpacing
import kotlinx.coroutines.flow.first
import org.koin.compose.koinInject
import org.jetbrains.compose.resources.painterResource
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.icon

object LoginScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<LoginScreenModel>()
        val authRepository = koinInject<AuthRepository>()
        val state by screenModel.state.collectAsState()
        val spacing = LocalSpacing.current

        LaunchedEffect(state.signedIn) {
            if (state.signedIn) {
                val user = authRepository.currentUser.first()
                navigator.replaceAll(destinationForUser(user))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.screenHorizontal)
                .padding(top = spacing.screenTop, bottom = spacing.xxxl),
        ) {
            Spacer(Modifier.height(spacing.xxl))
            Image(
                painter = painterResource(Res.drawable.icon),
                contentDescription = "شعار يلّا خِدمة",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(160.dp),
            )
            Spacer(Modifier.height(spacing.xxl))
            Text(
                text = "أهلاً بعودتك",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(spacing.xs))
            Text(
                text = "سجّل دخولك وكمّل من حيث وقفت",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(spacing.xxxl))

            AppTextField(
                value = state.email,
                onValueChange = screenModel::onEmailChange,
                label = "الإيميل",
                keyboardType = KeyboardType.Email,
                isError = state.error != null,
            )
            Spacer(Modifier.height(spacing.lg))
            AppTextField(
                value = state.password,
                onValueChange = screenModel::onPasswordChange,
                label = "كلمة المرور",
                keyboardType = KeyboardType.Password,
                isPassword = true,
                passwordVisible = state.passwordVisible,
                isError = state.error != null,
                trailingIcon = {
                    TextButton(onClick = screenModel::togglePasswordVisibility) {
                        Text(
                            text = if (state.passwordVisible) "إخفاء" else "إظهار",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                },
            )

            state.error?.let { msg ->
                Spacer(Modifier.height(spacing.sm))
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Spacer(Modifier.height(spacing.sm))
            TextButton(
                onClick = { /* TODO: forgot password screen */ },
                modifier = Modifier.align(Alignment.End),
            ) {
                Text("نسيت كلمة المرور؟", color = MaterialTheme.colorScheme.primary)
            }

            Spacer(Modifier.height(spacing.lg))
            AppPrimaryButton(
                text = "تسجيل الدخول",
                onClick = screenModel::submit,
                loading = state.isLoading,
            )

            // Divider with "أو"
            Spacer(Modifier.height(spacing.xxl))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline)
                Text(
                    text = "أو",
                    modifier = Modifier.padding(horizontal = spacing.md),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline)
            }
            Spacer(Modifier.height(spacing.lg))

            // Google (always shown — works on Android via Credential Manager; iOS routes via Swift)
            OutlinedButton(
                onClick = screenModel::signInWithGoogle,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.small,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Text(
                    text = "متابعة بحساب Google",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            Spacer(Modifier.height(spacing.xxxl))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("ما عندك حساب؟", color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = { navigator.push(UserTypeSelectionScreen) }) {
                    Text("إنشاء حساب", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
