package com.yallakhedma.app.di

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
    // OTP send/verify now runs server-side (Cloud Functions). No SMTP secret
    // ships in the app anymore — see functions/src/index.ts.
}

const val KOIN_PROP_GOOGLE_WEB_CLIENT_ID = "google_web_client_id"
