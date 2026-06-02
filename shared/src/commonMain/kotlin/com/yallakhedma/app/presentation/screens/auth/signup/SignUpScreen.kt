package com.yallakhedma.app.presentation.screens.auth.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.yallakhedma.app.domain.model.UserType
import com.yallakhedma.app.domain.repository.AuthRepository
import com.yallakhedma.app.presentation.components.AppPrimaryButton
import com.yallakhedma.app.presentation.components.AppTextField
import com.yallakhedma.app.presentation.routing.destinationForUser
import com.yallakhedma.app.presentation.theme.LocalSpacing
import kotlinx.coroutines.flow.first
import org.koin.compose.koinInject

class SignUpScreen(private val initialType: UserType = UserType.Client) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<SignUpScreenModel>()
        val authRepository = koinInject<AuthRepository>()
        val state by screenModel.state.collectAsState()
        val spacing = LocalSpacing.current

        LaunchedEffect(Unit) { screenModel.setInitialUserType(initialType) }

        LaunchedEffect(state.signedUp) {
            if (state.signedUp) {
                val user = authRepository.currentUser.first()
                navigator.replaceAll(destinationForUser(user))
            }
        }

        val typeLabel = if (initialType == UserType.Provider) "مقدّم خدمة" else "باحث عن خدمة"

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.screenHorizontal)
                .padding(top = spacing.screenTop, bottom = spacing.xxxl),
        ) {
            Text(
                text = "أنشئ حسابك",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(spacing.xs))
            Text(
                text = "تسجّل كـ $typeLabel",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(spacing.xxl))

            AppTextField(
                value = state.name,
                onValueChange = screenModel::onNameChange,
                label = "الاسم الكامل",
                keyboardType = KeyboardType.Text,
            )
            Spacer(Modifier.height(spacing.lg))
            AppTextField(
                value = state.email,
                onValueChange = screenModel::onEmailChange,
                label = "الإيميل",
                keyboardType = KeyboardType.Email,
            )
            Spacer(Modifier.height(spacing.lg))
            AppTextField(
                value = state.password,
                onValueChange = screenModel::onPasswordChange,
                label = "كلمة المرور",
                keyboardType = KeyboardType.Password,
                isPassword = true,
                passwordVisible = state.passwordVisible,
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
            Spacer(Modifier.height(spacing.lg))
            AppTextField(
                value = state.confirmPassword,
                onValueChange = screenModel::onConfirmPasswordChange,
                label = "تأكيد كلمة المرور",
                keyboardType = KeyboardType.Password,
                isPassword = true,
                passwordVisible = state.passwordVisible,
            )

            Spacer(Modifier.height(spacing.lg))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = state.termsAccepted,
                    onCheckedChange = { screenModel.onTermsToggle() },
                )
                Text(
                    text = "أوافق على شروط الاستخدام وسياسة الخصوصية",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            state.error?.let { msg ->
                Spacer(Modifier.height(spacing.sm))
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Spacer(Modifier.height(spacing.lg))
            AppPrimaryButton(
                text = "يلّا نسجّل",
                onClick = screenModel::submit,
                loading = state.isLoading,
            )

            Spacer(Modifier.height(spacing.xxxl))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("عندك حساب؟", color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = { navigator.pop() }) {
                    Text("سجّل دخول", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
