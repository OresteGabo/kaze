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
    val username: String? = null,
    val phoneNumber: String? = null,
    val privacyConsent: AuthPrivacyConsent = AuthPrivacyConsent(),
    val roles: Set<AuthRole>,
)

@Serializable
internal data class AuthPrivacyConsentDto(
    val mapAndVenueActivityEnabled: Boolean = true,
    val diagnosticsEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val analyticsEnabled: Boolean = false,
)

internal data class AuthPrivacyConsent(
    val mapAndVenueActivityEnabled: Boolean = true,
    val diagnosticsEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val analyticsEnabled: Boolean = false,
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
    val username: String? = null,
    val phoneNumber: String? = null,
)

@Serializable
internal data class AuthSigninRequest(
    val identifier: String? = null,
    val email: String? = null,
    val password: String,
)

@Serializable
internal data class SocialSigninRequest(
    val idToken: String? = null,
    val accessToken: String? = null,
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
    val username: String? = null,
    val phoneNumber: String? = null,
    val privacyConsent: AuthPrivacyConsentDto = AuthPrivacyConsentDto(),
    val roles: List<String>,
)

@Serializable
internal data class AuthProfileUpdateRequest(
    val displayName: String? = null,
    val username: String? = null,
    val phoneNumber: String? = null,
    val privacyConsent: AuthPrivacyConsentDto? = null,
)

@Serializable
internal data class AuthInvitationSummaryDto(
    val id: String,
    val eventId: String,
    val title: String,
    val subtitle: String,
    val code: String,
    val phoneLabel: String,
    val statusLabel: String,
    val state: String,
    val awaitingResponse: Boolean,
)

@Serializable
internal data class AuthEventSummaryDto(
    val id: String,
    val dayId: String,
    val dayLabel: String,
    val dateIso: String,
    val title: String,
    val description: String,
    val startIso: String,
    val endIso: String,
    val venueLabel: String,
    val hostLabel: String? = null,
)

@Serializable
internal data class AuthInvitationResponseRequest(
    val response: String,
)

internal fun AuthUser.toDto(): AuthUserDto =
    AuthUserDto(
        id = id,
        email = email,
        displayName = displayName,
        username = username,
        phoneNumber = phoneNumber,
        privacyConsent = privacyConsent.toDto(),
        roles = roles.map { it.name }.sorted(),
    )

internal fun AuthPrivacyConsent.toDto(): AuthPrivacyConsentDto =
    AuthPrivacyConsentDto(
        mapAndVenueActivityEnabled = mapAndVenueActivityEnabled,
        diagnosticsEnabled = diagnosticsEnabled,
        notificationsEnabled = notificationsEnabled,
        analyticsEnabled = analyticsEnabled,
    )

internal fun AuthPrivacyConsentDto.toDomain(): AuthPrivacyConsent =
    AuthPrivacyConsent(
        mapAndVenueActivityEnabled = mapAndVenueActivityEnabled,
        diagnosticsEnabled = diagnosticsEnabled,
        notificationsEnabled = notificationsEnabled,
        analyticsEnabled = analyticsEnabled,
    )
