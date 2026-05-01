package dev.orestegabo.kaze.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.HttpStatusCode
import org.mindrot.jbcrypt.BCrypt
import java.time.Instant
import java.util.Date
import java.util.UUID

internal class AuthService(
    private val repositoryProvider: () -> AuthRepository,
    private val jwtConfig: JwtConfig,
    private val tokenVerifier: ExternalTokenVerifier = ExternalTokenVerifier(),
    private val oauthStateFactory: OAuthStateFactory = OAuthStateFactory(),
    private val socialProviders: SocialOAuthProviders? = null,
) {
    private val signingAlgorithm = Algorithm.HMAC256(jwtConfig.secret)
    private val repository: AuthRepository
        get() = repositoryProvider()

    fun signup(request: AuthSignupRequest): AuthResponseDto {
        val email = normalizeEmail(request.email)
        val username = request.username?.trim()?.takeIf { it.isNotEmpty() }?.let(::normalizeUsername)
        val phoneNumber = request.phoneNumber?.trim()?.takeIf { it.isNotEmpty() }?.let(::normalizePhoneNumber)
        validatePassword(request.password)
        if (repository.findByEmail(email) != null) {
            throw AuthProblemException(HttpStatusCode.Conflict, "email_already_registered", "This email is already registered.")
        }
        if (username != null && repository.findByUsername(username) != null) {
            throw AuthProblemException(HttpStatusCode.Conflict, "username_already_registered", "This username is already taken.")
        }
        if (phoneNumber != null && repository.findByPhoneNumber(phoneNumber) != null) {
            throw AuthProblemException(HttpStatusCode.Conflict, "phone_number_already_registered", "This phone number is already registered.")
        }

        val user = repository.createPasswordUser(
            email = email,
            passwordHash = BCrypt.hashpw(request.password, BCrypt.gensalt(BCRYPT_COST)),
            displayName = request.displayName?.trim()?.takeIf { it.isNotEmpty() } ?: username,
            username = username,
            phoneNumber = phoneNumber,
        ).user
        return user.toAuthResponse()
    }

    fun signin(request: AuthSigninRequest): AuthResponseDto {
        val identifier = request.identifier?.trim()?.takeIf { it.isNotEmpty() }
            ?: request.email?.trim()?.takeIf { it.isNotEmpty() }
            ?: throw invalidCredentials()
        val storedUser = findUserByIdentifier(identifier)
            ?: throw invalidCredentials()
        val passwordHash = storedUser.passwordHash ?: throw invalidCredentials()
        if (!BCrypt.checkpw(request.password, passwordHash)) throw invalidCredentials()

        return storedUser.user.toAuthResponse()
    }

    fun signinWithGoogle(request: SocialSigninRequest): AuthResponseDto {
        if (jwtConfig.googleClientIds.isEmpty()) {
            throw AuthProblemException(
                status = HttpStatusCode.BadRequest,
                code = "google_auth_not_configured",
                message = "Google sign-in needs KAZE_GOOGLE_CLIENT_IDS or kaze.security.oauth.googleClientIds.",
            )
        }
        val idToken = request.idToken?.trim()?.takeIf { it.isNotEmpty() }
            ?: throw AuthProblemException(HttpStatusCode.BadRequest, "missing_id_token", "Google sign-in requires an id token.")
        val identity = tokenVerifier.verifyGoogle(idToken, jwtConfig.googleClientIds.first(), nonce = null)
            .withRequestedDisplayName(request.displayName)
        return repository.upsertExternalUser(identity).user.toAuthResponse()
    }

    fun signinWithApple(request: SocialSigninRequest): AuthResponseDto {
        if (jwtConfig.appleClientIds.isEmpty()) {
            throw AuthProblemException(
                status = HttpStatusCode.BadRequest,
                code = "apple_auth_not_configured",
                message = "Apple sign-in needs KAZE_APPLE_CLIENT_IDS or kaze.security.oauth.appleClientIds.",
            )
        }
        val idToken = request.idToken?.trim()?.takeIf { it.isNotEmpty() }
            ?: throw AuthProblemException(HttpStatusCode.BadRequest, "missing_id_token", "Apple sign-in requires an id token.")
        val identity = tokenVerifier.verifyApple(idToken, jwtConfig.appleClientIds.first(), nonce = null)
            .withRequestedDisplayName(request.displayName)
        return repository.upsertExternalUser(identity).user.toAuthResponse()
    }

    suspend fun signinWithFacebook(request: SocialSigninRequest): AuthResponseDto {
        val provider = requireSocialProviders().require(AuthProvider.FACEBOOK) as? FacebookOAuthProvider
            ?: throw AuthProblemException(
                status = HttpStatusCode.BadRequest,
                code = "facebook_auth_not_configured",
                message = "Facebook sign-in is not configured.",
            )
        if (!provider.isConfigured()) {
            throw AuthProblemException(
                status = HttpStatusCode.BadRequest,
                code = "facebook_auth_not_configured",
                message = "Facebook sign-in needs FACEBOOK_APP_ID, FACEBOOK_APP_SECRET, and FACEBOOK_REDIRECT_URI.",
            )
        }
        val accessToken = request.accessToken?.trim()?.takeIf { it.isNotEmpty() }
            ?: throw AuthProblemException(HttpStatusCode.BadRequest, "missing_access_token", "Facebook sign-in requires an access token.")
        val identity = provider.verifyAccessToken(accessToken)
            .withRequestedDisplayName(request.displayName)
        return repository.upsertExternalUser(identity).user.toAuthResponse()
    }

    fun createAuthorizationRequest(
        providerName: String,
        appRedirectUri: String?,
    ): AuthStartResponseDto {
        val provider = providerName.toAuthProvider()
        val socialProvider = requireSocialProviders().require(provider)
        if (!socialProvider.isConfigured()) {
            throw AuthProblemException(
                status = HttpStatusCode.BadRequest,
                code = "${provider.name.lowercase()}_auth_not_configured",
                message = "${provider.name.lowercase()} social login is not configured.",
            )
        }
        val attempt = oauthStateFactory.createAttempt(
            provider = provider,
            appRedirectUri = appRedirectUri?.trim()?.takeIf { it.isNotEmpty() }
                ?: jwtConfig.socialAuth.appDeepLinkRedirect,
        )
        repository.createOAuthAttempt(
            attempt = attempt,
            expiresAt = Instant.now().plusSeconds(OAUTH_ATTEMPT_TTL_SECONDS),
        )
        return AuthStartResponseDto(
            authorizationUrl = socialProvider.authorizationUrl(attempt),
            state = attempt.state,
        )
    }

    suspend fun completeOAuthCallback(
        provider: AuthProvider,
        code: String?,
        state: String?,
    ): String {
        if (code.isNullOrBlank() || state.isNullOrBlank()) {
            throw AuthProblemException(HttpStatusCode.BadRequest, "invalid_oauth_callback", "OAuth callback is missing code or state.")
        }
        val attempt = repository.consumeOAuthAttempt(provider, state)
            ?: throw AuthProblemException(HttpStatusCode.Unauthorized, "invalid_oauth_state", "OAuth state is invalid or expired.")
        val socialProvider = requireSocialProviders().require(provider)
        val identity = socialProvider.exchangeAndVerify(code, attempt)
        val user = repository.upsertExternalUser(identity).user
        val oneTimeToken = randomUrlSafeToken()
        repository.createOneTimeLoginToken(
            userId = user.id,
            tokenHash = secureHash(oneTimeToken),
            expiresAt = Instant.now().plusSeconds(jwtConfig.oneTimeLoginTokenTtlSeconds),
        )
        return appendQueryParams(
            baseUrl = attempt.appRedirectUri,
            params = mapOf(
                "login_token" to oneTimeToken,
                "state" to attempt.state,
            ),
        )
    }

    fun claimOneTimeLoginToken(request: AuthSessionClaimRequest): AuthResponseDto {
        val storedUser = repository.claimOneTimeLoginToken(secureHash(request.loginToken))
            ?: throw AuthProblemException(HttpStatusCode.Unauthorized, "invalid_login_token", "Login token is invalid or expired.")
        return storedUser.user.toAuthResponse(
            deviceId = request.deviceId,
            deviceLabel = request.deviceLabel,
        )
    }

    fun refresh(request: AuthRefreshRequest): AuthResponseDto {
        val incomingHash = secureHash(request.refreshToken)
        val existing = repository.findActiveRefreshToken(incomingHash)
            ?: throw AuthProblemException(HttpStatusCode.Unauthorized, "invalid_refresh_token", "Refresh token is invalid or expired.")
        val user = repository.findById(existing.userId)
            ?: throw AuthProblemException(HttpStatusCode.Unauthorized, "invalid_refresh_token", "Refresh token user no longer exists.")
        val rawRefreshToken = randomUrlSafeToken(48)
        val replacement = repository.createRefreshToken(
            userId = user.user.id,
            tokenHash = secureHash(rawRefreshToken),
            familyId = existing.familyId,
            deviceId = request.deviceId,
            deviceLabel = request.deviceLabel,
            expiresAt = Instant.now().plusSeconds(jwtConfig.refreshTokenTtlSeconds),
        )
        repository.revokeRefreshToken(existing.id, replacement.id)
        return user.user.toAccessResponse(refreshToken = rawRefreshToken)
    }

    fun logout(refreshToken: String?) {
        refreshToken?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let(::secureHash)
            ?.let(repository::findActiveRefreshToken)
            ?.let { repository.revokeRefreshToken(it.id) }
    }

    fun currentUser(userId: String): AuthUserDto =
        repository.findById(userId)?.user?.toDto()
            ?: throw AuthProblemException(HttpStatusCode.NotFound, "user_not_found", "The signed-in user could not be found.")

    fun updateProfile(userId: String, request: AuthProfileUpdateRequest): AuthUserDto {
        val currentUser = repository.findById(userId)
            ?: throw AuthProblemException(HttpStatusCode.NotFound, "user_not_found", "The signed-in user could not be found.")

        val normalizedDisplayName = request.displayName?.trim()?.takeIf { it.isNotEmpty() }
        val normalizedUsername = request.username?.trim()?.takeIf { it.isNotEmpty() }?.let(::normalizeUsername)
        val normalizedPhoneNumber = request.phoneNumber?.trim()?.takeIf { it.isNotEmpty() }?.let(::normalizePhoneNumber)

        if (normalizedUsername != null) {
            repository.findByUsername(normalizedUsername)
                ?.takeIf { it.user.id != userId }
                ?.let {
                    throw AuthProblemException(HttpStatusCode.Conflict, "username_already_registered", "This username is already taken.")
                }
        }

        if (normalizedPhoneNumber != null) {
            repository.findByPhoneNumber(normalizedPhoneNumber)
                ?.takeIf { it.user.id != userId }
                ?.let {
                    throw AuthProblemException(HttpStatusCode.Conflict, "phone_number_already_registered", "This phone number is already registered.")
                }
        }

        return repository.updateProfile(
            userId = userId,
            displayName = normalizedDisplayName ?: currentUser.user.displayName,
            username = normalizedUsername ?: currentUser.user.username,
            phoneNumber = normalizedPhoneNumber ?: currentUser.user.phoneNumber,
            privacyConsent = request.privacyConsent?.toDomain() ?: currentUser.user.privacyConsent,
        )?.user?.toDto()
            ?: throw AuthProblemException(HttpStatusCode.InternalServerError, "profile_update_failed", "Could not update the profile right now.")
    }

    fun currentUserInvitations(userId: String): List<AuthInvitationSummaryDto> =
        repository.listInvitationsForUser(userId)

    fun currentUserEvents(userId: String): List<AuthEventSummaryDto> =
        repository.listEventsForUser(userId)

    fun respondToInvitation(userId: String, invitationId: String, request: AuthInvitationResponseRequest): AuthInvitationSummaryDto {
        val accepted = when (request.response.trim().uppercase()) {
            "ACCEPT", "ACCEPTED" -> true
            "DECLINE", "DECLINED", "REJECT", "REJECTED" -> false
            else -> throw AuthProblemException(HttpStatusCode.BadRequest, "invalid_invitation_response", "Invitation response must be accept or decline.")
        }
        return repository.respondToInvitation(userId, invitationId, accepted)
            ?: throw AuthProblemException(HttpStatusCode.NotFound, "invitation_not_found", "That invitation could not be updated.")
    }

    fun verifier(): JWTVerifier =
        JWT.require(signingAlgorithm)
            .withIssuer(jwtConfig.issuer)
            .withAudience(jwtConfig.audience)
            .build()

    fun issueAccessToken(user: AuthUser): String {
        val now = Instant.now()
        val expiresAt = now.plusSeconds(jwtConfig.accessTokenTtlSeconds)
        return JWT.create()
            .withIssuer(jwtConfig.issuer)
            .withAudience(jwtConfig.audience)
            .withSubject(user.id)
            .withClaim("email", user.email)
            .withClaim("roles", user.roles.map { it.name })
            .withIssuedAt(Date.from(now))
            .withExpiresAt(Date.from(expiresAt))
            .withJWTId(UUID.randomUUID().toString())
            .sign(signingAlgorithm)
    }

    private fun AuthUser.toAuthResponse(
        deviceId: String? = null,
        deviceLabel: String? = null,
    ): AuthResponseDto {
        val rawRefreshToken = randomUrlSafeToken(48)
        repository.createRefreshToken(
            userId = id,
            tokenHash = secureHash(rawRefreshToken),
            familyId = UUID.randomUUID().toString(),
            deviceId = deviceId,
            deviceLabel = deviceLabel,
            expiresAt = Instant.now().plusSeconds(jwtConfig.refreshTokenTtlSeconds),
        )
        return toAccessResponse(rawRefreshToken)
    }

    private fun AuthUser.toAccessResponse(refreshToken: String): AuthResponseDto =
        AuthResponseDto(
            accessToken = issueAccessToken(this),
            refreshToken = refreshToken,
            expiresInSeconds = jwtConfig.accessTokenTtlSeconds,
            user = toDto(),
        )

    private fun requireSocialProviders(): SocialOAuthProviders =
        socialProviders ?: throw AuthProblemException(
            status = HttpStatusCode.BadRequest,
            code = "social_auth_not_configured",
            message = "Social auth providers are not configured.",
        )

    private fun invalidCredentials(): AuthProblemException =
        AuthProblemException(HttpStatusCode.Unauthorized, "invalid_credentials", "Identifier or password is incorrect.")

    private fun findUserByIdentifier(identifier: String): StoredAuthUser? =
        when {
            '@' in identifier -> repository.findByEmail(normalizeEmail(identifier))
            looksLikePhoneNumber(identifier) -> repository.findByPhoneNumber(normalizePhoneNumber(identifier))
            else -> repository.findByUsername(normalizeUsername(identifier))
        }

    private fun normalizeEmail(email: String): String {
        val normalized = email.trim().lowercase()
        require(EMAIL_REGEX.matches(normalized)) { "A valid email address is required." }
        return normalized
    }

    private fun normalizeUsername(username: String): String {
        val normalized = username.trim().lowercase()
        require(USERNAME_REGEX.matches(normalized)) {
            "Username must be 3 to 32 characters and use only letters, numbers, dots, and underscores."
        }
        return normalized
    }

    private fun normalizePhoneNumber(phoneNumber: String): String {
        val trimmed = phoneNumber.trim()
        val compact = buildString(trimmed.length) {
            trimmed.forEachIndexed { index, char ->
                when {
                    char.isDigit() -> append(char)
                    char == '+' && index == 0 -> append(char)
                }
            }
        }
        val normalized = when {
            compact.startsWith("+") -> compact
            compact.startsWith("250") && compact.length == 12 -> "+$compact"
            compact.startsWith("07") && compact.length == 10 -> "+250${compact.drop(1)}"
            compact.startsWith("7") && compact.length == 9 -> "+250$compact"
            else -> compact
        }
        require(PHONE_NUMBER_REGEX.matches(normalized)) {
            "Phone number must be a valid international number."
        }
        return normalized
    }

    private fun looksLikePhoneNumber(identifier: String): Boolean {
        val compact = identifier.filter { it.isDigit() || it == '+' }
        val digitCount = compact.count { it.isDigit() }
        return digitCount in 8..15 && compact.any { it.isDigit() }
    }

    private fun validatePassword(password: String) {
        require(password.length >= MIN_PASSWORD_LENGTH) { "Password must be at least $MIN_PASSWORD_LENGTH characters." }
    }
}

internal class AuthProblemException(
    val status: HttpStatusCode,
    val code: String,
    override val message: String,
) : RuntimeException(message)

private fun String.toAuthProvider(): AuthProvider =
    when (lowercase()) {
        "google" -> AuthProvider.GOOGLE
        "apple" -> AuthProvider.APPLE
        "facebook", "meta" -> AuthProvider.FACEBOOK
        else -> throw AuthProblemException(HttpStatusCode.BadRequest, "unsupported_auth_provider", "Unsupported auth provider: $this")
    }

private fun ExternalIdentity.withRequestedDisplayName(displayName: String?): ExternalIdentity {
    val requestedDisplayName = displayName?.trim()?.takeIf { it.isNotEmpty() }
    return if (requestedDisplayName == null) this else copy(displayName = requestedDisplayName)
}

private fun appendQueryParams(baseUrl: String, params: Map<String, String>): String {
    val separator = if ("?" in baseUrl) "&" else "?"
    return baseUrl + separator + params.entries.joinToString("&") { (key, value) ->
        "${java.net.URLEncoder.encode(key, Charsets.UTF_8)}=${java.net.URLEncoder.encode(value, Charsets.UTF_8)}"
    }
}

private const val MIN_PASSWORD_LENGTH = 8
private const val BCRYPT_COST = 12
private const val OAUTH_ATTEMPT_TTL_SECONDS = 600L
private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
private val USERNAME_REGEX = Regex("^[a-z0-9._]{3,32}$")
private val PHONE_NUMBER_REGEX = Regex("^\\+?[1-9][0-9]{7,14}$")
