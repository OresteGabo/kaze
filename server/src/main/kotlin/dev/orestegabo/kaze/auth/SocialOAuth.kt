package dev.orestegabo.kaze.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.http.URLBuilder
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.SecureRandom
import java.security.interfaces.ECPrivateKey
import java.time.Instant
import java.util.Base64
import java.util.Date

internal class OAuthStateFactory {
    fun createAttempt(provider: AuthProvider, appRedirectUri: String): OAuthLoginAttempt =
        OAuthLoginAttempt(
            id = randomUrlSafeToken(18),
            provider = provider,
            state = randomUrlSafeToken(),
            codeVerifier = randomUrlSafeToken(48),
            nonce = randomUrlSafeToken(),
            appRedirectUri = appRedirectUri,
        )
}

internal interface SocialOAuthProvider {
    val provider: AuthProvider
    fun authorizationUrl(attempt: OAuthLoginAttempt): String
    suspend fun exchangeAndVerify(code: String, attempt: OAuthLoginAttempt): ExternalIdentity
    fun isConfigured(): Boolean
}

internal class SocialOAuthProviders(
    providers: Set<SocialOAuthProvider>,
) {
    private val byProvider = providers.associateBy { it.provider }

    fun require(provider: AuthProvider): SocialOAuthProvider =
        byProvider[provider] ?: throw AuthProblemException(
            status = io.ktor.http.HttpStatusCode.BadRequest,
            code = "unsupported_auth_provider",
            message = "Unsupported social auth provider: ${provider.name.lowercase()}",
        )
}

internal class GoogleOAuthProvider(
    private val config: OAuthProviderConfig,
    private val tokenVerifier: ExternalTokenVerifier,
    private val httpClient: HttpClient,
) : SocialOAuthProvider {
    override val provider: AuthProvider = AuthProvider.GOOGLE

    override fun isConfigured(): Boolean = config.isConfigured && config.clientSecret.isNotBlank()

    override fun authorizationUrl(attempt: OAuthLoginAttempt): String =
        URLBuilder(config.authorizeUrl).apply {
            parameters.append("client_id", config.clientId)
            parameters.append("redirect_uri", config.redirectUri)
            parameters.append("response_type", "code")
            parameters.append("scope", config.scopes.joinToString(" "))
            parameters.append("state", attempt.state)
            parameters.append("nonce", attempt.nonce)
            parameters.append("code_challenge", sha256Base64Url(attempt.codeVerifier))
            parameters.append("code_challenge_method", "S256")
            parameters.append("prompt", "select_account")
        }.buildString()

    override suspend fun exchangeAndVerify(code: String, attempt: OAuthLoginAttempt): ExternalIdentity {
        val token = exchangeToken(
            tokenUrl = config.tokenUrl,
            parameters = Parameters.build {
                append("grant_type", "authorization_code")
                append("client_id", config.clientId)
                append("client_secret", config.clientSecret)
                append("code", code)
                append("redirect_uri", config.redirectUri)
                append("code_verifier", attempt.codeVerifier)
            },
        )
        val idToken = token.idToken ?: throw invalidProviderToken("Google did not return an id_token.")
        return tokenVerifier.verifyGoogle(idToken, config.clientId, attempt.nonce)
    }

    private suspend fun exchangeToken(tokenUrl: String, parameters: Parameters): OAuthTokenResponse =
        httpClient.post(tokenUrl) {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(FormDataContent(parameters))
        }.body()
}

internal class AppleOAuthProvider(
    private val config: AppleOAuthProviderConfig,
    private val tokenVerifier: ExternalTokenVerifier,
    private val httpClient: HttpClient,
) : SocialOAuthProvider {
    override val provider: AuthProvider = AuthProvider.APPLE

    override fun isConfigured(): Boolean = config.isConfigured

    override fun authorizationUrl(attempt: OAuthLoginAttempt): String =
        URLBuilder(config.authorizeUrl).apply {
            parameters.append("client_id", config.clientId)
            parameters.append("redirect_uri", config.redirectUri)
            parameters.append("response_type", "code")
            parameters.append("response_mode", "form_post")
            parameters.append("scope", config.scopes.joinToString(" "))
            parameters.append("state", attempt.state)
            parameters.append("nonce", attempt.nonce)
            parameters.append("code_challenge", sha256Base64Url(attempt.codeVerifier))
            parameters.append("code_challenge_method", "S256")
        }.buildString()

    override suspend fun exchangeAndVerify(code: String, attempt: OAuthLoginAttempt): ExternalIdentity {
        val token = httpClient.post(config.tokenUrl) {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(
                FormDataContent(
                    Parameters.build {
                        append("grant_type", "authorization_code")
                        append("client_id", config.clientId)
                        append("client_secret", appleClientSecretJwt())
                        append("code", code)
                        append("redirect_uri", config.redirectUri)
                        append("code_verifier", attempt.codeVerifier)
                    },
                ),
            )
        }.body<OAuthTokenResponse>()
        val idToken = token.idToken ?: throw invalidProviderToken("Apple did not return an id_token.")
        return tokenVerifier.verifyApple(idToken, config.clientId, attempt.nonce)
    }

    private fun appleClientSecretJwt(): String {
        val now = Instant.now()
        val privateKey = parseEcPrivateKey(config.privateKeyPem)
        return JWT.create()
            .withKeyId(config.keyId)
            .withIssuer(config.teamId)
            .withAudience("https://appleid.apple.com")
            .withSubject(config.clientId)
            .withIssuedAt(Date.from(now))
            .withExpiresAt(Date.from(now.plusSeconds(APPLE_CLIENT_SECRET_TTL_SECONDS)))
            .sign(Algorithm.ECDSA256(null, privateKey))
    }
}

internal class FacebookOAuthProvider(
    private val config: OAuthProviderConfig,
    private val httpClient: HttpClient,
    private val json: Json,
) : SocialOAuthProvider {
    override val provider: AuthProvider = AuthProvider.FACEBOOK

    override fun isConfigured(): Boolean = config.isConfigured && config.clientSecret.isNotBlank()

    override fun authorizationUrl(attempt: OAuthLoginAttempt): String =
        URLBuilder(config.authorizeUrl).apply {
            parameters.append("client_id", config.clientId)
            parameters.append("redirect_uri", config.redirectUri)
            parameters.append("response_type", "code")
            parameters.append("scope", config.scopes.joinToString(","))
            parameters.append("state", attempt.state)
        }.buildString()

    override suspend fun exchangeAndVerify(code: String, attempt: OAuthLoginAttempt): ExternalIdentity {
        val token = httpClient.get(config.tokenUrl) {
            url {
                parameters.append("client_id", config.clientId)
                parameters.append("client_secret", config.clientSecret)
                parameters.append("redirect_uri", config.redirectUri)
                parameters.append("code", code)
            }
        }.body<OAuthTokenResponse>()
        val accessToken = token.accessToken ?: throw invalidProviderToken("Facebook did not return an access token.")
        validateFacebookAccessToken(accessToken)
        val profile = fetchFacebookProfile(accessToken)
        val email = profile.email?.let(::normalizeEmailOrNull)
            ?: throw AuthProblemException(
                status = io.ktor.http.HttpStatusCode.Unauthorized,
                code = "identity_email_missing",
                message = "Facebook did not return an email. Ask the user to authorize email access or use another login method.",
            )
        return ExternalIdentity(
            provider = AuthProvider.FACEBOOK,
            providerSubject = profile.id,
            email = email,
            emailVerified = true,
            displayName = profile.name,
            avatarUrl = profile.picture?.data?.url,
        )
    }

    private suspend fun validateFacebookAccessToken(accessToken: String) {
        val appAccessToken = "${config.clientId}|${config.clientSecret}"
        val response = httpClient.get("https://graph.facebook.com/debug_token") {
            url {
                parameters.append("input_token", accessToken)
                parameters.append("access_token", appAccessToken)
            }
        }.bodyAsText()
        val data = json.parseToJsonElement(response).jsonObject["data"]?.jsonObject
            ?: throw invalidProviderToken("Facebook debug_token response was invalid.")
        val isValid = data["is_valid"]?.jsonPrimitive?.booleanOrNull ?: false
        val appId = data["app_id"]?.jsonPrimitive?.content
        if (!isValid || appId != config.clientId) {
            throw invalidProviderToken("Facebook access token is invalid.")
        }
    }

    private suspend fun fetchFacebookProfile(accessToken: String): FacebookProfileResponse =
        httpClient.get("https://graph.facebook.com/me") {
            url {
                parameters.append("fields", "id,name,email,picture")
                parameters.append("access_token", accessToken)
            }
        }.body()
}

internal class ExternalTokenVerifier {
    fun verifyGoogle(idToken: String, audience: String, nonce: String?): ExternalIdentity =
        verifyOpenIdToken(
            idToken = idToken,
            provider = AuthProvider.GOOGLE,
            issuer = GOOGLE_ISSUER,
            alternateIssuer = GOOGLE_ALT_ISSUER,
            audience = audience,
            jwksUrl = GOOGLE_JWKS_URL,
            nonce = nonce,
        )

    fun verifyApple(idToken: String, audience: String, nonce: String?): ExternalIdentity =
        verifyOpenIdToken(
            idToken = idToken,
            provider = AuthProvider.APPLE,
            issuer = APPLE_ISSUER,
            alternateIssuer = null,
            audience = audience,
            jwksUrl = APPLE_JWKS_URL,
            nonce = nonce,
        )

    private fun verifyOpenIdToken(
        idToken: String,
        provider: AuthProvider,
        issuer: String,
        alternateIssuer: String?,
        audience: String,
        jwksUrl: String,
        nonce: String?,
    ): ExternalIdentity {
        val decoded = JWT.decode(idToken)
        val verifier = com.auth0.jwk.JwkProviderBuilder(java.net.URI(jwksUrl).toURL())
            .cached(10, 24, java.util.concurrent.TimeUnit.HOURS)
            .rateLimited(10, 1, java.util.concurrent.TimeUnit.MINUTES)
            .build()
            .get(decoded.keyId)
            .let { jwk ->
                JWT.require(Algorithm.RSA256(jwk.publicKey as java.security.interfaces.RSAPublicKey, null))
                    .withIssuer(*listOfNotNull(issuer, alternateIssuer).toTypedArray())
                    .withAudience(audience)
                    .build()
            }
        val token = try {
            verifier.verify(idToken)
        } catch (cause: Throwable) {
            throw invalidProviderToken("The $provider identity token is invalid.")
        }
        if (nonce != null && token.getClaim("nonce").asString() != nonce) {
            throw invalidProviderToken("The $provider identity token nonce is invalid.")
        }
        val email = token.getClaim("email").asString()?.let(::normalizeEmailOrNull)
            ?: throw AuthProblemException(
                status = io.ktor.http.HttpStatusCode.Unauthorized,
                code = "identity_email_missing",
                message = "The identity token does not include an email.",
            )
        return ExternalIdentity(
            provider = provider,
            providerSubject = token.subject,
            email = email,
            emailVerified = token.getClaim("email_verified").asBoolean()
                ?: token.getClaim("email_verified").asString()?.toBooleanStrictOrNull()
                ?: provider == AuthProvider.APPLE,
            displayName = token.getClaim("name").asString()?.trim()?.takeIf { it.isNotEmpty() },
        )
    }
}

@Serializable
internal data class OAuthTokenResponse(
    @SerialName("access_token") val accessToken: String? = null,
    @SerialName("token_type") val tokenType: String? = null,
    @SerialName("expires_in") val expiresIn: Long? = null,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("id_token") val idToken: String? = null,
)

@Serializable
private data class FacebookProfileResponse(
    val id: String,
    val name: String? = null,
    val email: String? = null,
    val picture: FacebookPicture? = null,
)

@Serializable
private data class FacebookPicture(val data: FacebookPictureData? = null)

@Serializable
private data class FacebookPictureData(val url: String? = null)

internal fun randomUrlSafeToken(bytes: Int = 32): String {
    val data = ByteArray(bytes)
    SecureRandom().nextBytes(data)
    return Base64.getUrlEncoder().withoutPadding().encodeToString(data)
}

internal fun sha256Base64Url(value: String): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
    return Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
}

internal fun secureHash(value: String): String = sha256Base64Url(value)

private fun parseEcPrivateKey(pem: String): ECPrivateKey {
    val cleaned = pem
        .replace("-----BEGIN PRIVATE KEY-----", "")
        .replace("-----END PRIVATE KEY-----", "")
        .replace("\\s".toRegex(), "")
    val bytes = Base64.getDecoder().decode(cleaned)
    return KeyFactory.getInstance("EC")
        .generatePrivate(java.security.spec.PKCS8EncodedKeySpec(bytes)) as ECPrivateKey
}

private fun invalidProviderToken(message: String): AuthProblemException =
    AuthProblemException(io.ktor.http.HttpStatusCode.Unauthorized, "invalid_provider_token", message)

private fun normalizeEmailOrNull(email: String): String? =
    email.trim().lowercase().takeIf { EMAIL_REGEX.matches(it) }

private const val APPLE_CLIENT_SECRET_TTL_SECONDS = 180L * 24L * 60L * 60L
private const val GOOGLE_ISSUER = "https://accounts.google.com"
private const val GOOGLE_ALT_ISSUER = "accounts.google.com"
private const val GOOGLE_JWKS_URL = "https://www.googleapis.com/oauth2/v3/certs"
private const val APPLE_ISSUER = "https://appleid.apple.com"
private const val APPLE_JWKS_URL = "https://appleid.apple.com/auth/keys"
private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
