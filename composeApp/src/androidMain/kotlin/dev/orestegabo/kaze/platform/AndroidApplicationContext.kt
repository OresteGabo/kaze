package dev.orestegabo.kaze.platform

import android.content.Context

internal object AndroidApplicationContext {
    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    fun require(): Context =
        requireNotNull(appContext) { "AndroidApplicationContext must be initialized before creating Android AI services." }
}
