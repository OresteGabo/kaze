package dev.orestegabo.kaze.presentation.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import dev.orestegabo.kaze.R
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

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
    "https://kaze-api-338266348516.europe-west1.run.app/api/v1"

internal actual fun defaultDeviceLabel(): String = "Android"

internal actual fun createExternalUrlLauncher(): ExternalUrlLauncher =
    AndroidExternalUrlLauncher()

internal actual fun createNativeSocialAuthLauncher(): NativeSocialAuthLauncher =
    AndroidNativeSocialAuthLauncher()

internal object KazeAuthAndroidPlatform {
    private var appContext: Context? = null
    private var activity: ComponentActivity? = null
    private var callbackManager: CallbackManager? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    fun attachActivity(activity: ComponentActivity) {
        this.activity = activity
    }

    fun detachActivity(activity: ComponentActivity) {
        if (this.activity === activity) {
            this.activity = null
        }
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

    fun currentActivity(): ComponentActivity? = activity

    fun isFacebookLoginConfigured(): Boolean {
        val context = appContext ?: return false
        val appId = runCatching { context.getString(R.string.facebook_app_id) }.getOrDefault("")
            .removePrefix("fb")
            .trim()
        return appId.isNotBlank()
    }

    fun setFacebookCallbackManager(manager: CallbackManager?) {
        callbackManager = manager
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean =
        callbackManager?.onActivityResult(requestCode, resultCode, data) ?: false
}

private class AndroidExternalUrlLauncher : ExternalUrlLauncher {
    override fun open(url: String): Boolean = KazeAuthAndroidPlatform.open(url)
}

private class AndroidNativeSocialAuthLauncher : NativeSocialAuthLauncher {
    override suspend fun signIn(provider: SocialAuthProvider): NativeSocialAuthResult? {
        if (provider != SocialAuthProvider.FACEBOOK) return null
        if (!KazeAuthAndroidPlatform.isFacebookLoginConfigured()) return null
        val activity = KazeAuthAndroidPlatform.currentActivity() ?: return null

        return suspendCancellableCoroutine { continuation ->
            val callbackManager = CallbackManager.Factory.create()
            val loginManager = LoginManager.getInstance()
            KazeAuthAndroidPlatform.setFacebookCallbackManager(callbackManager)
            loginManager.registerCallback(
                callbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(result: LoginResult) {
                        loginManager.unregisterCallback(callbackManager)
                        KazeAuthAndroidPlatform.setFacebookCallbackManager(null)
                        val accessToken = result.accessToken.token
                        if (continuation.isActive) {
                            continuation.resume(
                                NativeSocialAuthResult(
                                    credential = accessToken,
                                    credentialType = SocialAuthCredentialType.ACCESS_TOKEN,
                                ),
                            )
                        }
                    }

                    override fun onCancel() {
                        loginManager.unregisterCallback(callbackManager)
                        KazeAuthAndroidPlatform.setFacebookCallbackManager(null)
                        if (continuation.isActive) {
                            continuation.resumeWithException(IllegalStateException("Facebook sign-in was cancelled."))
                        }
                    }

                    override fun onError(error: FacebookException) {
                        loginManager.unregisterCallback(callbackManager)
                        KazeAuthAndroidPlatform.setFacebookCallbackManager(null)
                        if (continuation.isActive) {
                            continuation.resumeWithException(error)
                        }
                    }
                },
            )
            continuation.invokeOnCancellation {
                loginManager.unregisterCallback(callbackManager)
                KazeAuthAndroidPlatform.setFacebookCallbackManager(null)
            }
            loginManager.logInWithReadPermissions(activity, listOf("email", "public_profile"))
        }
    }
}

private const val AUTH_CONNECT_TIMEOUT_MS = 5_000L
private const val AUTH_REQUEST_TIMEOUT_MS = 8_000L
