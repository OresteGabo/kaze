package dev.orestegabo.kaze.presentation.auth

import kotlinx.serialization.Serializable

internal enum class SocialAuthProvider(val routeName: String, val displayName: String) {
    GOOGLE("google", "Google"),
    APPLE("apple", "Apple"),
    FACEBOOK("facebook", "Facebook"),
}

internal data class AuthSession(
    val userId: String,
    val accessToken: String,
    val refreshToken: String?,
    val email: String,
    val displayName: String? = null,
    val username: String? = null,
    val phoneNumber: String? = null,
)

@Serializable
internal data class AuthStartResponse(
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
internal data class AuthSigninRequest(
    val email: String,
    val password: String,
)

@Serializable
internal data class AuthSignupRequest(
    val email: String,
    val password: String,
    val displayName: String? = null,
)

@Serializable
internal data class AuthProfileUpdateRequest(
    val displayName: String? = null,
    val username: String? = null,
    val phoneNumber: String? = null,
)

@Serializable
internal data class AuthResponse(
    val accessToken: String,
    val refreshToken: String? = null,
    val user: AuthUser,
)

@Serializable
internal data class AuthUser(
    val id: String,
    val email: String,
    val displayName: String? = null,
    val username: String? = null,
    val phoneNumber: String? = null,
    val roles: List<String> = emptyList(),
)
