package com.yallakhedma.app.di

import com.yallakhedma.app.data.auth.EmailOtpSender
import com.yallakhedma.app.data.auth.SocialAuthClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single {
        SocialAuthClient(
            appContext = androidContext(),
            webClientId = getProperty<String>(KOIN_PROP_GOOGLE_WEB_CLIENT_ID),
        )
    }
    single {
        EmailOtpSender(
            senderEmail = getProperty<String>(KOIN_PROP_SMTP_EMAIL),
            senderAppPassword = getProperty<String>(KOIN_PROP_SMTP_APP_PASSWORD),
        )
    }
}

const val KOIN_PROP_GOOGLE_WEB_CLIENT_ID = "google_web_client_id"
const val KOIN_PROP_SMTP_EMAIL = "smtp_email"
const val KOIN_PROP_SMTP_APP_PASSWORD = "smtp_app_password"
