package com.yallakhedma.app.presentation.routing

import cafe.adriel.voyager.core.screen.Screen
import com.yallakhedma.app.domain.model.User
import com.yallakhedma.app.domain.model.UserType
import com.yallakhedma.app.domain.model.VerificationStatus
import com.yallakhedma.app.presentation.screens.auth.login.LoginScreen
import com.yallakhedma.app.presentation.screens.auth.otp.OtpScreen
import com.yallakhedma.app.presentation.screens.dashboard.ProviderDashboardScreen
import com.yallakhedma.app.presentation.screens.home.ClientHomeScreen
import com.yallakhedma.app.presentation.screens.provider_setup.ProviderProfileSetupScreen
import com.yallakhedma.app.presentation.screens.verification.IdVerificationScreen
import com.yallakhedma.app.presentation.screens.verification.WaitingForVerificationScreen

/**
 * Single decision point for "given this auth state, where should the user be?".
 * Used by Splash (cold start), Login (after sign-in), SignUp (after sign-up),
 * and the verification screens (after submission).
 */
fun destinationForUser(user: User?): Screen = when {
    user == null -> LoginScreen
    // Email accounts must confirm the OTP first. Google/Apple users are
    // created with emailVerified = true, so they skip straight past this.
    !user.emailVerified -> OtpScreen
    user.userType == UserType.Provider -> when (user.idVerificationStatus) {
        VerificationStatus.NotSubmitted,
        VerificationStatus.Rejected -> IdVerificationScreen
        VerificationStatus.Pending -> WaitingForVerificationScreen
        // Verified providers fill their profile once, then reach their dashboard.
        VerificationStatus.Approved ->
            if (user.providerProfileCompleted) ProviderDashboardScreen
            else ProviderProfileSetupScreen
    }
    else -> ClientHomeScreen
}
