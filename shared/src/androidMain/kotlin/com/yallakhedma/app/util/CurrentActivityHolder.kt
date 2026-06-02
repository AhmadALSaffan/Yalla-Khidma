package com.yallakhedma.app.util

import androidx.activity.ComponentActivity

/**
 * Holds the currently visible Activity. Set in [MainActivity.onCreate], cleared in onDestroy.
 * Used by [SocialAuthClient] because Credential Manager needs an Activity context to present UI.
 */
object CurrentActivityHolder {
    @Volatile
    var activity: ComponentActivity? = null
}
