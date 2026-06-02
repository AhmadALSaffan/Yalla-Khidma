package com.yallakhedma.app.di

import com.yallakhedma.app.domain.repository.AuthRepository
import org.koin.core.context.GlobalContext

// Swift-callable bootstrap. Call once from iOSApp.init().
fun doInitKoin() {
    initKoin()
}

// Swift accessors. Generated Objective-C class: KoinKt.
fun getAuthRepository(): AuthRepository = GlobalContext.get().get()
