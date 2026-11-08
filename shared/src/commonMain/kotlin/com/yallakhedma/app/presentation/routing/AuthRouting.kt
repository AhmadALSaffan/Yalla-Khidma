package com.yallakhedma.app.presentation.routing

import cafe.adriel.voyager.core.screen.Screen
import com.yallakhedma.app.domain.model.User
import com.yallakhedma.app.domain.model.UserType
import com.yallakhedma.app.domain.model.VerificationStatus
import com.yallakhedma.app.presentation.screens.auth.verify.EmailVerificationScreen
import com.yallakhedma.app.presentation.screens.auth.usertype.UserTypeSelectionScreen
import com.yallakhedma.app.presentation.screens.auth.welcome.WelcomeScreen
import com.yallakhedma.app.presentation.screens.main.ProviderMainScreen
import com.yallakhedma.app.presentation.screens.main.ClientMainScreen
import com.yallakhedma.app.presentation.screens.provider_setup.ProviderProfileSetupScreen
import com.yallakhedma.app.presentation.screens.verification.IdVerificationScreen
import com.yallakhedma.app.presentation.screens.verification.WaitingForVerificationScreen

/**
 * Single decision point for "given this auth state, where should the user be?".
 * Used by Splash (cold start), Welcome/Login/SignUp (after auth), the account-type
 * step, and the verification screens (after submission).
 */
fun destinationForUser(user: User?): Screen = when {
    user == null -> WelcomeScreen
    // First Google/Apple sign-in: we don't know client vs provider yet.
    !user.userTypeChosen -> UserTypeSelectionScreen(UserTypeSelectionScreen.Mode.CompleteAccount)
    // Email accounts must confirm the OTP first. Google/Apple users are
    // created with emailVerified = true, so they skip straight past this.
    !user.emailVerified -> EmailVerificationScreen
    user.userType == UserType.Provider -> when (user.idVerificationStatus) {
        VerificationStatus.NotSubmitted,
        VerificationStatus.Rejected -> IdVerificationScreen
        VerificationStatus.Pending -> WaitingForVerificationScreen
        // Verified providers fill their profile once, then reach their dashboard.
        VerificationStatus.Approved ->
            if (user.providerProfileCompleted) ProviderMainScreen
            else ProviderProfileSetupScreen
    }
    else -> ClientMainScreen
}

/** Where a signed-out user lands (logout, cancelled verification, …). */
val SignedOutDestination: Screen get() = WelcomeScreen
