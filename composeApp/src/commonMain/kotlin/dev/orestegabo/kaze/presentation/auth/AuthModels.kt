package dev.orestegabo.kaze.presentation.auth

import kotlinx.serialization.Serializable

internal enum class SocialAuthProvider(val routeName: String, val displayName: String) {
    GOOGLE("google", "Google"),
    APPLE("apple", "Apple"),
    FACEBOOK("facebook", "Facebook"),
}

internal enum class SocialAuthCredentialType {
    ID_TOKEN,
    ACCESS_TOKEN,
}

internal data class AuthSession(
    val userId: String,
    val accessToken: String,
    val refreshToken: String?,
    val email: String,
    val displayName: String? = null,
    val username: String? = null,
    val phoneNumber: String? = null,
    val privacyConsent: AuthPrivacyConsent = AuthPrivacyConsent(),
)

internal data class NativeSocialAuthResult(
    val credential: String,
    val credentialType: SocialAuthCredentialType = SocialAuthCredentialType.ID_TOKEN,
    val displayName: String? = null,
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
    val privacyConsent: AuthPrivacyConsent? = null,
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
    val privacyConsent: AuthPrivacyConsent = AuthPrivacyConsent(),
    val roles: List<String> = emptyList(),
)

@Serializable
internal data class AuthPrivacyConsent(
    val mapAndVenueActivityEnabled: Boolean = true,
    val diagnosticsEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val analyticsEnabled: Boolean = false,
)

@Serializable
internal data class AuthInvitationSummary(
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
internal data class AuthEventSummary(
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
internal data class AuthActiveStay(
    val hotelId: String,
    val hotelDisplayName: String,
    val guestId: String,
    val guestName: String,
    val stayId: String,
    val roomId: String? = null,
    val stayStatus: String,
    val startsAtIso: String,
    val endsAtIso: String,
)

@Serializable
internal data class AuthActiveStayResponse(
    val activeStay: AuthActiveStay? = null,
)

@Serializable
internal data class AuthSessionBootstrap(
    val user: AuthUser,
    val invitations: List<AuthInvitationSummary> = emptyList(),
    val events: List<AuthEventSummary> = emptyList(),
    val activeStay: AuthActiveStay? = null,
)

@Serializable
internal data class AuthInvitationResponseRequest(
    val response: String,
)

@Serializable
internal data class ReservationDraftSubmissionRequest(
    val placeId: String,
    val serviceId: String? = null,
    val eventName: String,
    val preferredDateLabel: String,
    val guestCount: Int,
    val packageLabel: String,
    val addOns: List<String> = emptyList(),
    val paymentMethod: String,
    val note: String? = null,
)

@Serializable
internal data class ReservationResponse(
    val id: String,
    val reservationCode: String,
    val eventId: String,
    val placeId: String,
    val placeName: String,
    val serviceId: String? = null,
    val status: String,
    val eventName: String,
    val preferredDateLabel: String,
    val guestCount: Int,
    val packageLabel: String,
    val addOns: List<String>,
    val paymentMethod: String,
    val createdAtIso: String,
)

@Serializable
internal data class SocialSigninRequest(
    val idToken: String? = null,
    val accessToken: String? = null,
    val displayName: String? = null,
)

@Serializable
internal data class AuthApiProblem(
    val code: String,
    val message: String,
)

internal data class AuthGatewayProblemException(
    val statusCode: Int,
    val problemCode: String? = null,
    override val message: String,
) : RuntimeException(message)
