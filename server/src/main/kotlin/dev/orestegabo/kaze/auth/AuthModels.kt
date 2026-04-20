package dev.orestegabo.kaze.auth

import kotlinx.serialization.Serializable

internal enum class AuthProvider {
    PASSWORD,
    GOOGLE,
    APPLE,
    FACEBOOK,
}

internal enum class AuthRole {
    CUSTOMER,
    STAFF,
    ADMIN,
}

internal data class AuthUser(
    val id: String,
    val email: String,
    val displayName: String?,
    val roles: Set<AuthRole>,
)

internal data class ExternalIdentity(
    val provider: AuthProvider,
    val providerSubject: String,
    val email: String,
    val emailVerified: Boolean,
    val displayName: String?,
    val avatarUrl: String? = null,
)

internal data class OAuthLoginAttempt(
    val id: String,
    val provider: AuthProvider,
    val state: String,
    val codeVerifier: String,
    val nonce: String,
    val appRedirectUri: String,
)

internal data class StoredRefreshToken(
    val id: String,
    val userId: String,
    val tokenHash: String,
    val familyId: String,
)

@Serializable
internal data class AuthSignupRequest(
    val email: String,
    val password: String,
    val displayName: String? = null,
)

@Serializable
internal data class AuthSigninRequest(
    val email: String,
    val password: String,
)

@Serializable
internal data class SocialSigninRequest(
    val idToken: String,
    val displayName: String? = null,
)

@Serializable
internal data class AuthStartResponseDto(
    val authorizationUrl: String,
    val state: String,
)

@Serializable
internal data class AuthSessionClaimRequest(
    val loginToken: String,
    val deviceId: String? = null,
    val deviceLabel: String? = null,
)

@Serializable
internal data class AuthRefreshRequest(
    val refreshToken: String,
    val deviceId: String? = null,
    val deviceLabel: String? = null,
)

@Serializable
internal data class AuthResponseDto(
    val accessToken: String,
    val refreshToken: String? = null,
    val tokenType: String = "Bearer",
    val expiresInSeconds: Long,
    val user: AuthUserDto,
)

@Serializable
internal data class AuthLogoutResponseDto(
    val status: String = "signed_out",
    val message: String = "Token cleared on this device. Discard the stored JWT to complete logout.",
)

@Serializable
internal data class AuthUserDto(
    val id: String,
    val email: String,
    val displayName: String? = null,
    val roles: List<String>,
)

internal fun AuthUser.toDto(): AuthUserDto =
    AuthUserDto(
        id = id,
        email = email,
        displayName = displayName,
        roles = roles.map { it.name }.sorted(),
    )
