package com.yallakhedma.app

import android.app.Application
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import com.yallakhedma.app.di.KOIN_PROP_GOOGLE_WEB_CLIENT_ID
import com.yallakhedma.app.di.KOIN_PROP_SMTP_APP_PASSWORD
import com.yallakhedma.app.di.KOIN_PROP_SMTP_EMAIL
import com.yallakhedma.app.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.logger.Level

class YallaKhedmaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidLogger(Level.INFO)
            androidContext(this@YallaKhedmaApp)
            properties(
                mapOf(
                    KOIN_PROP_GOOGLE_WEB_CLIENT_ID to getString(R.string.google_web_client_id),
                    KOIN_PROP_SMTP_EMAIL to getString(R.string.smtp_email),
                    KOIN_PROP_SMTP_APP_PASSWORD to getString(R.string.smtp_app_password),
                ),
            )
        }
        // Register Coil's network image loader synchronously, before any
        // Compose code (and any AsyncImage) gets a chance to run. If we did
        // this from inside a @Composable, the first AsyncImage could try to
        // load while the singleton was still the default no-op factory.
        SingletonImageLoader.setSafe { context ->
            ImageLoader.Builder(context)
                .components { add(KtorNetworkFetcherFactory()) }
                .crossfade(true)
                .build()
        }
    }
}
