package dev.orestegabo.kaze.auth

import kotlinx.serialization.Serializable

internal enum class AuthProvider {
    PASSWORD,
    GOOGLE,
    APPLE,
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
internal data class AuthResponseDto(
    val accessToken: String,
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
