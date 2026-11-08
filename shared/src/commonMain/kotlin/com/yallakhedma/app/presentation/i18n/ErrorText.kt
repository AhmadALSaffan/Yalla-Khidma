package com.yallakhedma.app.presentation.i18n

import com.yallakhedma.app.domain.util.DataResult
import com.yallakhedma.app.domain.util.ErrorCode
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.error_email_in_use
import yallakhidma.shared.generated.resources.error_generic
import yallakhidma.shared.generated.resources.error_google_failed
import yallakhidma.shared.generated.resources.error_invalid_credentials
import yallakhidma.shared.generated.resources.error_network
import yallakhidma.shared.generated.resources.error_not_signed_in
import yallakhidma.shared.generated.resources.error_reset_failed
import yallakhidma.shared.generated.resources.error_save_failed
import yallakhidma.shared.generated.resources.error_signin_failed
import yallakhidma.shared.generated.resources.error_signup_failed
import yallakhidma.shared.generated.resources.error_too_many_requests
import yallakhidma.shared.generated.resources.error_weak_password

/** Language-neutral [ErrorCode] → localized message. */
fun ErrorCode.toUiText(): UiText = UiText.of(
    when (this) {
        ErrorCode.Network -> Res.string.error_network
        ErrorCode.TooManyRequests -> Res.string.error_too_many_requests
        ErrorCode.InvalidCredentials -> Res.string.error_invalid_credentials
        ErrorCode.EmailInUse -> Res.string.error_email_in_use
        ErrorCode.WeakPassword -> Res.string.error_weak_password
        ErrorCode.SignUpFailed -> Res.string.error_signup_failed
        ErrorCode.SignInFailed -> Res.string.error_signin_failed
        ErrorCode.ResetFailed -> Res.string.error_reset_failed
        ErrorCode.NotSignedIn -> Res.string.error_not_signed_in
        ErrorCode.SaveFailed -> Res.string.error_save_failed
        ErrorCode.SocialCancelled -> Res.string.error_google_failed
        ErrorCode.Unknown -> Res.string.error_generic
    },
)

fun DataResult.Error.toUiText(): UiText = code.toUiText()
