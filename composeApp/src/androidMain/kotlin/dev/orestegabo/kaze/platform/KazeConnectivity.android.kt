package dev.orestegabo.kaze.platform

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

internal object KazeConnectivityContext {
    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    fun currentContext(): Context? = appContext
}

internal actual fun isDeviceOnline(): Boolean {
    val context = KazeConnectivityContext.currentContext() ?: return true
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}
