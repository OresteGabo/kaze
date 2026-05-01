package dev.orestegabo.kaze.presentation.auth

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import platform.AuthenticationServices.ASAuthorization
import platform.AuthenticationServices.ASAuthorizationAppleIDCredential
import platform.AuthenticationServices.ASAuthorizationAppleIDProvider
import platform.AuthenticationServices.ASAuthorizationController
import platform.AuthenticationServices.ASAuthorizationControllerDelegateProtocol
import platform.AuthenticationServices.ASAuthorizationControllerPresentationContextProvidingProtocol
import platform.AuthenticationServices.ASAuthorizationScopeEmail
import platform.AuthenticationServices.ASAuthorizationScopeFullName
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.UIKit.UIWindow
import platform.UIKit.UIDevice
import platform.UIKit.UIApplication
import platform.darwin.NSObject
import platform.posix.memcpy
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal actual fun createPlatformAuthHttpClient(json: Json): HttpClient =
    HttpClient(Darwin) {
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
    (NSBundle.mainBundle.objectForInfoDictionaryKey("KAZE_API_BASE_URL") as? String)
        ?.trim()
        ?.takeIf(::isUsableApiBaseUrl)
        ?: DEFAULT_AUTH_API_BASE_URL

internal actual fun defaultDeviceLabel(): String =
    UIDevice.currentDevice.name

internal actual fun createExternalUrlLauncher(): ExternalUrlLauncher =
    IosExternalUrlLauncher()

internal actual fun createNativeSocialAuthLauncher(): NativeSocialAuthLauncher =
    AppleNativeSocialAuthLauncher()

private class IosExternalUrlLauncher : ExternalUrlLauncher {
    override fun open(url: String): Boolean {
        val nsUrl = NSURL.URLWithString(url) ?: return false
        UIApplication.sharedApplication.openURL(
            url = nsUrl,
            options = emptyMap<Any?, Any>(),
            completionHandler = null,
        )
        return true
    }
}

private class AppleNativeSocialAuthLauncher : NativeSocialAuthLauncher {
    override suspend fun signIn(provider: SocialAuthProvider): NativeSocialAuthResult? {
        if (provider != SocialAuthProvider.APPLE) return null
        return suspendCancellableCoroutine { continuation ->
            val request = ASAuthorizationAppleIDProvider().createRequest().apply {
                requestedScopes = listOf(ASAuthorizationScopeFullName, ASAuthorizationScopeEmail)
            }
            val controller = ASAuthorizationController(authorizationRequests = listOf(request))
            val delegate = AppleAuthorizationDelegate(
                onSuccess = { result ->
                    AppleAuthorizationRetainer.delegate = null
                    if (continuation.isActive) continuation.resume(result)
                },
                onFailure = { errorMessage ->
                    AppleAuthorizationRetainer.delegate = null
                    if (continuation.isActive) continuation.resumeWithException(IllegalStateException(errorMessage))
                },
            )
            AppleAuthorizationRetainer.delegate = delegate
            controller.delegate = delegate
            controller.presentationContextProvider = delegate
            continuation.invokeOnCancellation {
                AppleAuthorizationRetainer.delegate = null
            }
            controller.performRequests()
        }
    }
}

private object AppleAuthorizationRetainer {
    var delegate: AppleAuthorizationDelegate? = null
}

private class AppleAuthorizationDelegate(
    private val onSuccess: (NativeSocialAuthResult) -> Unit,
    private val onFailure: (String) -> Unit,
) : NSObject(), ASAuthorizationControllerDelegateProtocol, ASAuthorizationControllerPresentationContextProvidingProtocol {

    override fun authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithAuthorization: ASAuthorization,
    ) {
        val credential = didCompleteWithAuthorization.credential as? ASAuthorizationAppleIDCredential
        if (credential == null) {
            onFailure("Apple sign-in returned an unsupported credential.")
            return
        }

        val idToken = credential.identityToken?.let(::decodeAppleIdentityToken)
        if (idToken.isNullOrBlank()) {
            onFailure("Apple sign-in did not return an identity token.")
            return
        }

        onSuccess(
            NativeSocialAuthResult(
                credential = idToken,
                displayName = credential.fullName?.let(::formatAppleDisplayName),
            ),
        )
    }

    override fun authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithError: NSError,
    ) {
        onFailure(didCompleteWithError.localizedDescription)
    }

    override fun presentationAnchorForAuthorizationController(controller: ASAuthorizationController): UIWindow =
        activePresentationWindow()
}

private fun decodeAppleIdentityToken(data: NSData): String? =
    data.toByteArray().decodeToString().takeIf { it.isNotBlank() }

private fun formatAppleDisplayName(fullName: platform.Foundation.NSPersonNameComponents): String? =
    listOfNotNull(fullName.givenName, fullName.familyName)
        .joinToString(" ")
        .trim()
        .ifBlank { null }

private fun activePresentationWindow(): UIWindow =
    UIApplication.sharedApplication.keyWindow ?: UIWindow()

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray =
    ByteArray(length.toInt()).apply {
        usePinned { pinned ->
            memcpy(pinned.addressOf(0), bytes, length)
        }
    }

private const val AUTH_CONNECT_TIMEOUT_MS = 5_000L
private const val AUTH_REQUEST_TIMEOUT_MS = 8_000L
private const val DEFAULT_AUTH_API_BASE_URL = "https://kaze-api-338266348516.europe-west1.run.app/api/v1"

private fun isUsableApiBaseUrl(value: String): Boolean {
    if (value.isBlank()) return false
    if ("\$(" in value) return false
    if (value.contains("localhost", ignoreCase = true)) return false
    if (value.contains("127.0.0.1")) return false
    return value.startsWith("https://") || value.startsWith("http://")
}
