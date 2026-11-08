package com.yallakhedma.app.di

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import com.yallakhedma.app.data.auth.SocialAuthClient
import platform.Foundation.NSUserDefaults
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<Settings> { NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults) }
    single { SocialAuthClient() }
    // OTP runs server-side now (Cloud Functions) — no platform sender needed.
}
