package com.yallakhedma.app.presentation.screens.auth.signup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

            // Live password requirements — each turns green + checked as it's met.
            AnimatedVisibility(
                visible = state.password.isNotEmpty(),
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(150)),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(spacing.xs),
                ) {
                    RequirementRow("٨ أحرف على الأقل", state.password.length >= 8)
                    RequirementRow("تحتوي على حرف", state.password.any { it.isLetter() })
                    RequirementRow("تحتوي على رقم", state.password.any { it.isDigit() })
                    RequirementRow(
                        "كلمتا المرور متطابقتان",
                        state.confirmPassword.isNotEmpty() && state.password == state.confirmPassword,
                    )
                }
            }

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

private val RequirementMet = Color(0xFF10B981)

@Composable
private fun RequirementRow(label: String, met: Boolean) {
    val target = if (met) RequirementMet else MaterialTheme.colorScheme.onSurfaceVariant
    val color by animateColorAsState(targetValue = target, animationSpec = tween(250), label = "req-color")
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(if (met) RequirementMet else Color.Transparent)
                .border(BorderStroke(1.dp, color), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (met) Text("✓", color = Color.White, fontSize = 10.sp)
        }
        Text(text = label, color = color, style = MaterialTheme.typography.bodySmall)
    }
}
