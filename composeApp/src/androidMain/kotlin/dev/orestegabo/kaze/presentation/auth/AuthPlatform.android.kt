package dev.orestegabo.kaze.presentation.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal actual fun createPlatformAuthHttpClient(json: Json): HttpClient =
    HttpClient(Android) {
        install(HttpTimeout) {
            requestTimeoutMillis = AUTH_REQUEST_TIMEOUT_MS
            connectTimeoutMillis = AUTH_CONNECT_TIMEOUT_MS
            socketTimeoutMillis = AUTH_REQUEST_TIMEOUT_MS
        }
        install(ContentNegotiation) {
            json(json)
        }
    }

internal actual fun defaultAuthApiBaseUrl(): String =
    if (isAndroidEmulator()) {
        "http://10.0.2.2:8080/api/v1"
    } else {
        "http://172.20.10.2:8080/api/v1"
    }

internal actual fun defaultDeviceLabel(): String = "Android"

internal actual fun createExternalUrlLauncher(): ExternalUrlLauncher =
    AndroidExternalUrlLauncher()

internal object KazeAuthAndroidPlatform {
    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    fun open(url: String): Boolean {
        val context = appContext ?: return false
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return runCatching {
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }
}

private class AndroidExternalUrlLauncher : ExternalUrlLauncher {
    override fun open(url: String): Boolean = KazeAuthAndroidPlatform.open(url)
}

private fun isAndroidEmulator(): Boolean {
    val fingerprint = Build.FINGERPRINT.lowercase()
    val model = Build.MODEL.lowercase()
    val manufacturer = Build.MANUFACTURER.lowercase()
    val brand = Build.BRAND.lowercase()
    val device = Build.DEVICE.lowercase()
    val product = Build.PRODUCT.lowercase()
    return fingerprint.startsWith("generic") ||
        fingerprint.contains("emulator") ||
        model.contains("emulator") ||
        model.contains("android sdk built for") ||
        manufacturer.contains("genymotion") ||
        brand.startsWith("generic") && device.startsWith("generic") ||
        product.contains("sdk_gphone") ||
        product.contains("emulator")
}

private const val AUTH_CONNECT_TIMEOUT_MS = 5_000L
private const val AUTH_REQUEST_TIMEOUT_MS = 8_000L
