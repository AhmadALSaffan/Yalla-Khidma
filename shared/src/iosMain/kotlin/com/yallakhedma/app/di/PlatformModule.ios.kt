package com.yallakhedma.app.di

import com.yallakhedma.app.data.auth.EmailOtpSender
import com.yallakhedma.app.data.auth.SocialAuthClient
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { SocialAuthClient() }
    single { EmailOtpSender() }
}
