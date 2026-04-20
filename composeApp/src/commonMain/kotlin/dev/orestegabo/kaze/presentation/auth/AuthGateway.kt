package dev.orestegabo.kaze.presentation.auth

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json

internal interface AuthGateway {
    suspend fun signIn(email: String, password: String): AuthSession
    suspend fun createAccount(email: String, password: String): AuthSession
    suspend fun startSocialLogin(provider: SocialAuthProvider): AuthStartResponse
    suspend fun claimSession(loginToken: String): AuthSession
    suspend fun refresh(refreshToken: String): AuthSession
    suspend fun logout(refreshToken: String?)
}

internal class KazeAuthGateway(
    private val client: HttpClient,
    private val baseUrl: String,
    private val deviceId: String,
    private val deviceLabel: String,
) : AuthGateway {
    private val authBaseUrl = baseUrl.trimEnd('/')

    override suspend fun signIn(email: String, password: String): AuthSession =
        client.post("$authBaseUrl/auth/signin") {
            contentType(ContentType.Application.Json)
            setBody(AuthSigninRequest(email = email, password = password))
        }.body<AuthResponse>().toSession()

    override suspend fun createAccount(email: String, password: String): AuthSession =
        client.post("$authBaseUrl/auth/signup") {
            contentType(ContentType.Application.Json)
            setBody(AuthSignupRequest(email = email, password = password))
        }.body<AuthResponse>().toSession()

    override suspend fun startSocialLogin(provider: SocialAuthProvider): AuthStartResponse =
        client.get("$authBaseUrl/auth/${provider.routeName}/start") {
            parameter("appRedirectUri", AuthDeepLinks.CALLBACK_URI)
        }.body()

    override suspend fun claimSession(loginToken: String): AuthSession =
        client.post("$authBaseUrl/auth/session/claim") {
            contentType(ContentType.Application.Json)
            setBody(
                AuthSessionClaimRequest(
                    loginToken = loginToken,
                    deviceId = deviceId,
                    deviceLabel = deviceLabel,
                ),
            )
        }.body<AuthResponse>().toSession()

    override suspend fun refresh(refreshToken: String): AuthSession =
        client.post("$authBaseUrl/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(
                AuthRefreshRequest(
                    refreshToken = refreshToken,
                    deviceId = deviceId,
                    deviceLabel = deviceLabel,
                ),
            )
        }.body<AuthResponse>().toSession()

    override suspend fun logout(refreshToken: String?) {
        client.post("$authBaseUrl/auth/logout") {
            contentType(ContentType.Application.Json)
            if (!refreshToken.isNullOrBlank()) {
                setBody(AuthRefreshRequest(refreshToken = refreshToken))
            }
        }
    }

    private fun AuthResponse.toSession(): AuthSession =
        AuthSession(
            accessToken = accessToken,
            refreshToken = refreshToken,
            email = user.email,
        )
}

internal object DemoAuthGateway : AuthGateway {
    override suspend fun signIn(email: String, password: String): AuthSession =
        AuthSession(accessToken = "demo-local-session", refreshToken = null, email = email.trim().lowercase())

    override suspend fun createAccount(email: String, password: String): AuthSession =
        AuthSession(accessToken = "demo-local-session", refreshToken = null, email = email.trim().lowercase())

    override suspend fun startSocialLogin(provider: SocialAuthProvider): AuthStartResponse =
        AuthStartResponse(authorizationUrl = "", state = "")

    override suspend fun claimSession(loginToken: String): AuthSession =
        AuthSession(accessToken = "demo-local-session", refreshToken = null, email = "demo@kaze.local")

    override suspend fun refresh(refreshToken: String): AuthSession =
        AuthSession(accessToken = "demo-local-session", refreshToken = refreshToken, email = "demo@kaze.local")

    override suspend fun logout(refreshToken: String?) = Unit
}

internal object NoopExternalUrlLauncher : ExternalUrlLauncher {
    override fun open(url: String): Boolean = false
}

internal interface ExternalUrlLauncher {
    fun open(url: String): Boolean
}

internal fun createAuthHttpClient(): HttpClient {
    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    return createPlatformAuthHttpClient(json)
}

internal expect fun createPlatformAuthHttpClient(json: Json): HttpClient

internal expect fun defaultAuthApiBaseUrl(): String

internal expect fun defaultDeviceLabel(): String

internal expect fun createExternalUrlLauncher(): ExternalUrlLauncher

internal fun Throwable.toAuthMessage(): String =
    when (this) {
        is HttpRequestTimeoutException -> {
            "Kaze could not reach the backend quickly. Check that the API is running and this device is on the same network as your Mac."
        }
        is ClientRequestException -> when (response.status.value) {
            400 -> "This sign-in option is not configured yet on the backend."
            401 -> "The sign-in session expired or was rejected. Please try again."
            409 -> "This email is already registered. Try logging in instead."
            else -> "Could not complete sign-in. Please check your details and try again."
        }
        is ServerResponseException -> "Kaze is having trouble signing you in. Please try again."
        else -> when {
            message?.contains("Connection refused", ignoreCase = true) == true -> {
                "Kaze cannot reach the backend. Make sure your phone and Mac are on the same network and the API is running."
            }
            message?.contains("Failed to connect", ignoreCase = true) == true -> {
                "Kaze cannot reach the backend. Make sure your phone and Mac are on the same network and the API is running."
            }
            else -> message?.takeIf { it.isNotBlank() } ?: "Could not complete sign-in. Please try again."
        }
    }
