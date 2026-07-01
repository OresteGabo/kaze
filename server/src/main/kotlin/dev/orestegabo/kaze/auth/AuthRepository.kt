package dev.orestegabo.kaze.auth

import java.sql.ResultSet
import java.sql.Timestamp
import java.time.Instant
import javax.sql.DataSource

internal interface AuthRepository {
    fun findByEmail(email: String): StoredAuthUser?
    fun findByUsername(username: String): StoredAuthUser?
    fun findByPhoneNumber(phoneNumber: String): StoredAuthUser?
    fun findSignupConflicts(email: String, username: String?, phoneNumber: String?): SignupConflicts
    fun findByProvider(provider: AuthProvider, providerSubject: String): StoredAuthUser?
    fun findUserBySocialProvider(provider: String, subject: String): AppUser?
    fun createPasswordUser(
        email: String,
        passwordHash: String,
        displayName: String?,
        username: String?,
        phoneNumber: String?,
        roles: Set<AuthRole> = setOf(AuthRole.CUSTOMER),
    ): StoredAuthUser

    fun setPasswordLogin(userId: String, passwordHash: String): StoredAuthUser?

    fun upsertExternalUser(identity: ExternalIdentity): StoredAuthUser
    fun linkSocialProviderToUser(
        userId: String,
        provider: String,
        subject: String,
        email: String,
        emailVerified: Boolean = false,
        displayName: String? = null,
        avatarUrl: String? = null,
    ): UserAuthProvider
    fun createOAuthAttempt(attempt: OAuthLoginAttempt, expiresAt: Instant)
    fun consumeOAuthAttempt(provider: AuthProvider, state: String): OAuthLoginAttempt?
    fun createOneTimeLoginToken(userId: String, tokenHash: String, expiresAt: Instant)
    fun claimOneTimeLoginToken(tokenHash: String): StoredAuthUser?
    fun createRefreshToken(
        userId: String,
        tokenHash: String,
        familyId: String,
        deviceId: String?,
        deviceLabel: String?,
        expiresAt: Instant,
    ): StoredRefreshToken

    fun findRefreshToken(tokenHash: String): StoredRefreshToken?
    fun findActiveRefreshToken(tokenHash: String): StoredRefreshToken?
    fun rotateRefreshToken(
        currentTokenHash: String,
        replacementTokenHash: String,
        deviceId: String?,
        deviceLabel: String?,
        replacementExpiresAt: Instant,
    ): RefreshTokenRotation?
    fun revokeRefreshToken(tokenId: String, replacedByTokenId: String? = null)
    fun revokeRefreshTokenFamily(familyId: String)
    fun isGuestLinkedToUser(userId: String, hotelId: String, guestId: String): Boolean
    fun findById(userId: String): StoredAuthUser?
    fun updateProfile(
        userId: String,
        displayName: String?,
        username: String?,
        phoneNumber: String?,
        privacyConsent: AuthPrivacyConsent,
    ): StoredAuthUser?
    fun listInvitationsForUser(userId: String): List<AuthInvitationSummaryDto>
    fun listEventsForUser(userId: String): List<AuthEventSummaryDto>
    fun listPublicEvents(): List<AuthEventSummaryDto>
    fun listSuggestedEventsForUser(userId: String): List<AuthEventSummaryDto>
    fun updateEventFollow(userId: String, eventId: String, status: String)
    fun createEventNotice(userId: String, eventId: String, request: EventNoticeCreateRequest)
    fun reviewVenueReservation(reviewerUserId: String, reservationId: String, request: VenueReservationReviewRequest): VenueReservationReviewDto?
    fun findActiveStayForUser(userId: String): AuthActiveStayDto?
    fun respondToInvitation(userId: String, invitationId: String, accepted: Boolean): AuthInvitationSummaryDto?
    fun createEvent(userId: String, request: EventCreateRequest): AuthEventSummaryDto
}

internal data class StoredAuthUser(
    val user: AuthUser,
    val passwordHash: String?,
)

internal data class SignupConflicts(
    val emailExists: Boolean,
    val usernameExists: Boolean,
    val phoneNumberExists: Boolean,
)

internal data class RefreshTokenRotation(
    val consumed: StoredRefreshToken,
    val replacement: StoredRefreshToken,
)

internal class JdbcAuthRepository(
    private val dataSource: DataSource,
) : AuthRepository {

    override fun findByEmail(email: String): StoredAuthUser? =
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                SELECT id, email, display_name, username, phone_number,
                       map_and_venue_activity_enabled, diagnostics_enabled, notifications_enabled, analytics_enabled,
                       password_hash, roles
                FROM app_users
                WHERE lower(email) = lower(?)
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, email.trim())
                statement.executeQuery().use { result ->
                    result.singleUserOrNull()
                }
            }
        }

    override fun findByUsername(username: String): StoredAuthUser? =
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                SELECT id, email, display_name, username, phone_number,
                       map_and_venue_activity_enabled, diagnostics_enabled, notifications_enabled, analytics_enabled,
                       password_hash, roles
                FROM app_users
                WHERE username = ?
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, username.trim().lowercase())
                statement.executeQuery().use { result ->
                    result.singleUserOrNull()
                }
            }
        }

    override fun findByPhoneNumber(phoneNumber: String): StoredAuthUser? =
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                SELECT id, email, display_name, username, phone_number,
                       map_and_venue_activity_enabled, diagnostics_enabled, notifications_enabled, analytics_enabled,
                       password_hash, roles
                FROM app_users
                WHERE phone_number = ?
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, phoneNumber.trim())
                statement.executeQuery().use { result ->
                    result.singleUserOrNull()
                }
            }
        }

    override fun findSignupConflicts(email: String, username: String?, phoneNumber: String?): SignupConflicts =
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                SELECT
                    EXISTS(
                        SELECT 1
                        FROM app_users
                        WHERE lower(email) = lower(?)
                    ) AS email_exists,
                    CASE
                        WHEN ? IS NULL THEN false
                        ELSE EXISTS(
                            SELECT 1
                            FROM app_users
                            WHERE username = ?
                        )
                    END AS username_exists,
                    CASE
                        WHEN ? IS NULL THEN false
                        ELSE EXISTS(
                            SELECT 1
                            FROM app_users
                            WHERE phone_number = ?
                        )
                    END AS phone_number_exists
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, email.trim())
                statement.setString(2, username?.trim()?.lowercase())
                statement.setString(3, username?.trim()?.lowercase())
                statement.setString(4, phoneNumber?.trim())
                statement.setString(5, phoneNumber?.trim())
                statement.executeQuery().use { result ->
                    check(result.next()) { "Signup conflict lookup did not return a row" }
                    SignupConflicts(
                        emailExists = result.getBoolean("email_exists"),
                        usernameExists = result.getBoolean("username_exists"),
                        phoneNumberExists = result.getBoolean("phone_number_exists"),
                    )
                }
            }
        }

    override fun findByProvider(provider: AuthProvider, providerSubject: String): StoredAuthUser? =
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                SELECT u.id, u.email, u.display_name, u.username, u.phone_number,
                       u.map_and_venue_activity_enabled, u.diagnostics_enabled, u.notifications_enabled, u.analytics_enabled,
                       u.password_hash, u.roles
                FROM app_users u
                INNER JOIN user_auth_providers p ON p.user_id = u.id
                WHERE p.provider = ? AND p.provider_subject = ?
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, provider.name)
                statement.setString(2, providerSubject)
                statement.executeQuery().use { result ->
                    result.singleUserOrNull()
                }
            }
        }

    override fun findUserBySocialProvider(provider: String, subject: String): AppUser? =
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                SELECT u.id, u.email, u.display_name, u.username, u.phone_number,
                       u.map_and_venue_activity_enabled, u.diagnostics_enabled, u.notifications_enabled, u.analytics_enabled,
                       u.password_hash, u.roles, u.disabled, u.last_login_at
                FROM app_users u
                INNER JOIN user_auth_providers p ON p.user_id = u.id
                WHERE p.provider = ? AND p.provider_subject = ?
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, provider.trim().uppercase())
                statement.setString(2, subject)
                statement.executeQuery().use { result ->
                    if (result.next()) result.toAppUser() else null
                }
            }
        }

    override fun createPasswordUser(
        email: String,
        passwordHash: String,
        displayName: String?,
        username: String?,
        phoneNumber: String?,
        roles: Set<AuthRole>,
    ): StoredAuthUser =
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                val user = insertUser(
                    email = email,
                    passwordHash = passwordHash,
                    displayName = displayName,
                    username = username,
                    phoneNumber = phoneNumber,
                    roles = roles,
                    connection = connection,
                )
                linkSocialProviderToUser(
                    userId = user.user.id,
                    provider = AuthProvider.PASSWORD.name,
                    subject = user.user.email,
                    email = user.user.email,
                    emailVerified = true,
                    displayName = user.user.displayName,
                    avatarUrl = null,
                    connection = connection,
                )
                connection.commit()
                user
            } catch (cause: Throwable) {
                connection.rollback()
                throw cause
            }
        }

    override fun setPasswordLogin(userId: String, passwordHash: String): StoredAuthUser? =
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                val user = connection.prepareStatement(
                    """
                    UPDATE app_users
                    SET password_hash = ?, updated_at = now()
                    WHERE id = ?
                    RETURNING id, email, display_name, username, phone_number,
                              map_and_venue_activity_enabled, diagnostics_enabled, notifications_enabled, analytics_enabled,
                              password_hash, roles
                    """.trimIndent(),
                ).use { statement ->
                    statement.setString(1, passwordHash)
                    statement.setString(2, userId)
                    statement.executeQuery().use { result -> result.singleUserOrNull() }
                }
                if (user == null) {
                    connection.rollback()
                    return@use null
                }
                linkSocialProviderToUser(
                    userId = user.user.id,
                    provider = AuthProvider.PASSWORD.name,
                    subject = user.user.email,
                    email = user.user.email,
                    emailVerified = true,
                    displayName = user.user.displayName,
                    avatarUrl = null,
                    connection = connection,
                )
                connection.commit()
                user
            } catch (cause: Throwable) {
                connection.rollback()
                throw cause
            }
        }

    override fun upsertExternalUser(identity: ExternalIdentity): StoredAuthUser =
        findUserBySocialProvider(identity.provider.name, identity.providerSubject)?.toStoredAuthUser()
            ?: dataSource.connection.use { connection ->
                connection.autoCommit = false
                try {
                    val existingUser = findByEmail(identity.email)
                    if (existingUser != null && !identity.emailVerified) {
                        throw AuthProblemException(
                            status = io.ktor.http.HttpStatusCode.Conflict,
                            code = "external_email_not_verified",
                            message = "The provider must verify this email before it can be linked to the existing account.",
                        )
                    }
                    val storedUser = existingUser ?: insertUser(
                        email = identity.email,
                        passwordHash = null,
                        displayName = identity.displayName,
                        username = null,
                        phoneNumber = null,
                        roles = setOf(AuthRole.CUSTOMER),
                        connection = connection,
                    )
                    linkSocialProviderToUser(
                        userId = storedUser.user.id,
                        provider = identity.provider.name,
                        subject = identity.providerSubject,
                        email = identity.email,
                        emailVerified = identity.emailVerified,
                        displayName = identity.displayName,
                        avatarUrl = identity.avatarUrl,
                        connection = connection,
                    )
                    connection.commit()
                    storedUser
                } catch (cause: Throwable) {
                    connection.rollback()
                    throw cause
                }
            }

    private fun insertUser(
        email: String,
        passwordHash: String?,
        displayName: String?,
        username: String?,
        phoneNumber: String?,
        roles: Set<AuthRole>,
        connection: java.sql.Connection,
    ): StoredAuthUser =
        connection.prepareStatement(
            """
            INSERT INTO app_users (
                email, display_name, username, phone_number,
                map_and_venue_activity_enabled, diagnostics_enabled, notifications_enabled, analytics_enabled,
                password_hash, roles
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?::TEXT[])
            RETURNING id, email, display_name, username, phone_number,
                      map_and_venue_activity_enabled, diagnostics_enabled, notifications_enabled, analytics_enabled,
                      password_hash, roles
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, email.trim().lowercase())
            statement.setString(2, displayName?.trim()?.takeIf { it.isNotEmpty() })
            statement.setString(3, username?.trim()?.lowercase()?.takeIf { it.isNotEmpty() })
            statement.setString(4, phoneNumber?.trim()?.takeIf { it.isNotEmpty() })
            statement.setBoolean(5, true)
            statement.setBoolean(6, true)
            statement.setBoolean(7, true)
            statement.setBoolean(8, false)
            statement.setString(9, passwordHash)
            statement.setString(10, roles.joinToString(prefix = "{", postfix = "}") { it.name })
            statement.executeQuery().use { result ->
                check(result.next()) { "User insert did not return a row" }
                result.toStoredAuthUser()
            }
        }

    override fun createOAuthAttempt(attempt: OAuthLoginAttempt, expiresAt: Instant) {
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                INSERT INTO oauth_login_attempts (
                    id, provider, state_hash, code_verifier_hash, code_verifier, nonce_hash, nonce, app_redirect_uri, expires_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, attempt.id)
                statement.setString(2, attempt.provider.name)
                statement.setString(3, secureHash(attempt.state))
                statement.setString(4, secureHash(attempt.codeVerifier))
                statement.setString(5, attempt.codeVerifier)
                statement.setString(6, secureHash(attempt.nonce))
                statement.setString(7, attempt.nonce)
                statement.setString(8, attempt.appRedirectUri)
                statement.setTimestamp(9, Timestamp.from(expiresAt))
                statement.executeUpdate()
            }
        }
    }

    override fun consumeOAuthAttempt(provider: AuthProvider, state: String): OAuthLoginAttempt? =
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                val attempt = connection.prepareStatement(
                    """
                    SELECT id, provider, state_hash, code_verifier, nonce, app_redirect_uri
                    FROM oauth_login_attempts
                    WHERE provider = ?
                      AND state_hash = ?
                      AND consumed_at IS NULL
                      AND expires_at > now()
                    FOR UPDATE
                    """.trimIndent(),
                ).use { statement ->
                    statement.setString(1, provider.name)
                    statement.setString(2, secureHash(state))
                    statement.executeQuery().use { result ->
                        if (result.next()) {
                            OAuthLoginAttempt(
                                id = result.getString("id"),
                                provider = AuthProvider.valueOf(result.getString("provider")),
                                state = state,
                                codeVerifier = result.getString("code_verifier"),
                                nonce = result.getString("nonce"),
                                appRedirectUri = result.getString("app_redirect_uri"),
                            )
                        } else {
                            null
                        }
                    }
                }
                if (attempt != null) {
                    connection.prepareStatement(
                        "UPDATE oauth_login_attempts SET consumed_at = now() WHERE id = ?",
                    ).use { statement ->
                        statement.setString(1, attempt.id)
                        statement.executeUpdate()
                    }
                }
                connection.commit()
                attempt
            } catch (cause: Throwable) {
                connection.rollback()
                throw cause
            }
        }

    override fun createOneTimeLoginToken(userId: String, tokenHash: String, expiresAt: Instant) {
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                INSERT INTO auth_one_time_login_tokens (user_id, token_hash, expires_at)
                VALUES (?, ?, ?)
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, userId)
                statement.setString(2, tokenHash)
                statement.setTimestamp(3, Timestamp.from(expiresAt))
                statement.executeUpdate()
            }
        }
    }

    override fun claimOneTimeLoginToken(tokenHash: String): StoredAuthUser? =
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                val userId = connection.prepareStatement(
                    """
                    SELECT user_id
                    FROM auth_one_time_login_tokens
                    WHERE token_hash = ?
                      AND consumed_at IS NULL
                      AND expires_at > now()
                    FOR UPDATE
                    """.trimIndent(),
                ).use { statement ->
                    statement.setString(1, tokenHash)
                    statement.executeQuery().use { result ->
                        if (result.next()) result.getString("user_id") else null
                    }
                }
                if (userId != null) {
                    connection.prepareStatement(
                        "UPDATE auth_one_time_login_tokens SET consumed_at = now() WHERE token_hash = ?",
                    ).use { statement ->
                        statement.setString(1, tokenHash)
                        statement.executeUpdate()
                    }
                }
                connection.commit()
                userId?.let(::findById)
            } catch (cause: Throwable) {
                connection.rollback()
                throw cause
            }
        }

    override fun createRefreshToken(
        userId: String,
        tokenHash: String,
        familyId: String,
        deviceId: String?,
        deviceLabel: String?,
        expiresAt: Instant,
    ): StoredRefreshToken =
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                INSERT INTO auth_refresh_tokens (user_id, token_hash, family_id, device_id, device_label, expires_at)
                VALUES (?, ?, ?, ?, ?, ?)
                RETURNING id, user_id, token_hash, family_id, expires_at, revoked_at, replaced_by_token_id
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, userId)
                statement.setString(2, tokenHash)
                statement.setString(3, familyId)
                statement.setString(4, deviceId?.trim()?.takeIf { it.isNotEmpty() })
                statement.setString(5, deviceLabel?.trim()?.takeIf { it.isNotEmpty() })
                statement.setTimestamp(6, Timestamp.from(expiresAt))
                statement.executeQuery().use { result ->
                    check(result.next()) { "Refresh token insert did not return a row" }
                    result.toStoredRefreshToken()
                }
            }
        }

    override fun findRefreshToken(tokenHash: String): StoredRefreshToken? =
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                SELECT id, user_id, token_hash, family_id, expires_at, revoked_at, replaced_by_token_id
                FROM auth_refresh_tokens
                WHERE token_hash = ?
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, tokenHash)
                statement.executeQuery().use { result ->
                    if (result.next()) result.toStoredRefreshToken() else null
                }
            }
        }

    override fun findActiveRefreshToken(tokenHash: String): StoredRefreshToken? =
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                SELECT id, user_id, token_hash, family_id, expires_at, revoked_at, replaced_by_token_id
                FROM auth_refresh_tokens
                WHERE token_hash = ?
                  AND revoked_at IS NULL
                  AND expires_at > now()
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, tokenHash)
                statement.executeQuery().use { result ->
                    if (result.next()) result.toStoredRefreshToken() else null
                }
            }
        }

    override fun rotateRefreshToken(
        currentTokenHash: String,
        replacementTokenHash: String,
        deviceId: String?,
        deviceLabel: String?,
        replacementExpiresAt: Instant,
    ): RefreshTokenRotation? =
        dataSource.connection.use { connection ->
            val originalAutoCommit = connection.autoCommit
            connection.autoCommit = false
            try {
                val existing = connection.prepareStatement(
                    """
                    SELECT id, user_id, token_hash, family_id, expires_at, revoked_at, replaced_by_token_id
                    FROM auth_refresh_tokens
                    WHERE token_hash = ?
                    FOR UPDATE
                    """.trimIndent(),
                ).use { statement ->
                    statement.setString(1, currentTokenHash)
                    statement.executeQuery().use { result ->
                        if (result.next()) result.toStoredRefreshToken() else null
                    }
                }

                if (existing == null || existing.revokedAt != null || !existing.expiresAt.isAfter(Instant.now())) {
                    connection.rollback()
                    return@use null
                }

                val replacement = connection.prepareStatement(
                    """
                    INSERT INTO auth_refresh_tokens (user_id, token_hash, family_id, device_id, device_label, expires_at)
                    VALUES (?, ?, ?, ?, ?, ?)
                    RETURNING id, user_id, token_hash, family_id, expires_at, revoked_at, replaced_by_token_id
                    """.trimIndent(),
                ).use { statement ->
                    statement.setString(1, existing.userId)
                    statement.setString(2, replacementTokenHash)
                    statement.setString(3, existing.familyId)
                    statement.setString(4, deviceId?.trim()?.takeIf { it.isNotEmpty() })
                    statement.setString(5, deviceLabel?.trim()?.takeIf { it.isNotEmpty() })
                    statement.setTimestamp(6, Timestamp.from(replacementExpiresAt))
                    statement.executeQuery().use { result ->
                        check(result.next()) { "Refresh token rotation did not return a replacement row" }
                        result.toStoredRefreshToken()
                    }
                }

                // Row-level locking plus the revoked_at predicate prevents two concurrent refreshes
                // from producing two valid replacement tokens for the same stolen refresh token.
                val updatedRows = connection.prepareStatement(
                    """
                    UPDATE auth_refresh_tokens
                    SET revoked_at = now(),
                        replaced_by_token_id = ?
                    WHERE id = ?
                      AND revoked_at IS NULL
                    """.trimIndent(),
                ).use { statement ->
                    statement.setString(1, replacement.id)
                    statement.setString(2, existing.id)
                    statement.executeUpdate()
                }

                if (updatedRows != 1) {
                    connection.rollback()
                    return@use null
                }

                connection.commit()
                RefreshTokenRotation(consumed = existing, replacement = replacement)
            } catch (cause: Throwable) {
                connection.rollback()
                throw cause
            } finally {
                connection.autoCommit = originalAutoCommit
            }
        }

    override fun revokeRefreshToken(tokenId: String, replacedByTokenId: String?) {
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                UPDATE auth_refresh_tokens
                SET revoked_at = COALESCE(revoked_at, now()),
                    replaced_by_token_id = COALESCE(?, replaced_by_token_id)
                WHERE id = ?
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, replacedByTokenId)
                statement.setString(2, tokenId)
                statement.executeUpdate()
            }
        }
    }

    override fun revokeRefreshTokenFamily(familyId: String) {
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                UPDATE auth_refresh_tokens
                SET revoked_at = COALESCE(revoked_at, now())
                WHERE family_id = ?
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, familyId)
                statement.executeUpdate()
            }
        }
    }

    override fun isGuestLinkedToUser(userId: String, hotelId: String, guestId: String): Boolean =
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                SELECT 1
                FROM guests
                WHERE user_id = ?
                  AND hotel_id = ?
                  AND id = ?
                LIMIT 1
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, userId)
                statement.setString(2, hotelId)
                statement.setString(3, guestId)
                statement.executeQuery().use { result -> result.next() }
            }
        }

    override fun findById(userId: String): StoredAuthUser? =
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                SELECT id, email, display_name, username, phone_number,
                       map_and_venue_activity_enabled, diagnostics_enabled, notifications_enabled, analytics_enabled,
                       password_hash, roles
                FROM app_users
                WHERE id = ?
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, userId)
                statement.executeQuery().use { result ->
                    result.singleUserOrNull()
                }
            }
        }

    override fun updateProfile(
        userId: String,
        displayName: String?,
        username: String?,
        phoneNumber: String?,
        privacyConsent: AuthPrivacyConsent,
    ): StoredAuthUser? =
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                UPDATE app_users
                SET display_name = ?,
                    username = ?,
                    phone_number = ?,
                    map_and_venue_activity_enabled = ?,
                    diagnostics_enabled = ?,
                    notifications_enabled = ?,
                    analytics_enabled = ?,
                    updated_at = now()
                WHERE id = ?
                RETURNING id, email, display_name, username, phone_number,
                          map_and_venue_activity_enabled, diagnostics_enabled, notifications_enabled, analytics_enabled,
                          password_hash, roles
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, displayName?.trim()?.takeIf { it.isNotEmpty() })
                statement.setString(2, username?.trim()?.lowercase()?.takeIf { it.isNotEmpty() })
                statement.setString(3, phoneNumber?.trim()?.takeIf { it.isNotEmpty() })
                statement.setBoolean(4, privacyConsent.mapAndVenueActivityEnabled)
                statement.setBoolean(5, privacyConsent.diagnosticsEnabled)
                statement.setBoolean(6, privacyConsent.notificationsEnabled)
                statement.setBoolean(7, privacyConsent.analyticsEnabled)
                statement.setString(8, userId)
                statement.executeQuery().use { result ->
                    result.singleUserOrNull()
                }
            }
        }

    override fun listInvitationsForUser(userId: String): List<AuthInvitationSummaryDto> =
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                SELECT
                    i.id,
                    e.id AS event_id,
                    e.title,
                    COALESCE(sp.name, e.event_type) AS venue_label,
                    i.invite_code,
                    COALESCE(i.invited_phone_number, i.invited_email, '') AS contact_label,
                    i.invitation_status,
                    i.access_tier,
                    e.ends_at,
                    p.pass_status
                FROM event_invitations i
                INNER JOIN events e ON e.id = i.event_id
                LEFT JOIN service_places sp ON sp.id = e.place_id
                LEFT JOIN access_passes p ON p.invitation_id = i.id AND p.user_id = i.invited_user_id
                WHERE i.invited_user_id = ?
                ORDER BY e.starts_at DESC, i.id DESC
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, userId)
                statement.executeQuery().use { result ->
                    buildList {
                        while (result.next()) {
                            add(result.toAuthInvitationSummaryDto())
                        }
                    }
                }
            }
        }

    override fun listEventsForUser(userId: String): List<AuthEventSummaryDto> =
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                SELECT DISTINCT
                    CONCAT(e.id, '@', to_char(occ.starts_at, 'YYYYMMDDHH24MI')) AS id,
                    CONCAT('event-day-', to_char(occ.starts_at, 'YYYY-MM-DD')) AS day_id,
                    trim(to_char(occ.starts_at, 'Dy DD Mon')) AS day_label,
                    to_char(occ.starts_at, 'YYYY-MM-DD') AS date_iso,
                    e.title,
                    e.summary,
                    occ.starts_at,
                    occ.ends_at,
                    COALESCE(sp.name, e.title) AS venue_label,
                    organizer.display_name AS host_label,
                    e.visibility,
                    e.attendance_policy,
                    e.capacity_mode,
                    e.requires_identity,
                    e.join_code,
                    CASE
                        WHEN e.organizer_user_id = ? THEN 'ORGANIZER'
                        WHEN m.id IS NOT NULL THEN 'JOINED'
                        WHEN i.id IS NOT NULL THEN 'INVITED'
                        WHEN f.follow_status = 'SAVED' THEN 'SAVED'
                        ELSE NULL
                    END AS viewer_state,
                    (
                        SELECT COUNT(*)::INT
                        FROM event_change_notices n
                        LEFT JOIN event_notice_receipts r
                            ON r.notice_id = n.id
                            AND r.user_id = ?
                        WHERE n.event_id = e.id
                          AND r.seen_at IS NULL
                          AND r.dismissed_at IS NULL
                    ) AS unread_notice_count,
                    (
                        SELECT n.title
                        FROM event_change_notices n
                        WHERE n.event_id = e.id
                        ORDER BY n.created_at DESC
                        LIMIT 1
                    ) AS latest_notice_label,
                    vr.status AS venue_reservation_status,
                    CASE
                        WHEN vr.status = 'CONFIRMED' THEN 'Confirmed by venue'
                        WHEN vr.status = 'PENDING_CONFIRMATION' THEN 'Reservation requested'
                        WHEN vr.status = 'DECLINED' THEN 'Declined by venue'
                        WHEN vr.status = 'CANCELLED' THEN 'Cancelled by venue'
                        WHEN e.place_id IS NOT NULL THEN 'Venue not involved yet'
                        ELSE NULL
                    END AS venue_reservation_label,
                    vr.selected_room_label AS venue_room_label
                FROM events e
                LEFT JOIN event_invitations i
                    ON i.event_id = e.id
                    AND i.invited_user_id = ?
                LEFT JOIN event_memberships m
                    ON m.event_id = e.id
                    AND m.user_id = ?
                    AND m.membership_status = 'ACTIVE'
                LEFT JOIN event_follows f
                    ON f.event_id = e.id
                    AND f.user_id = ?
                LEFT JOIN service_places sp ON sp.id = e.place_id
                LEFT JOIN app_users organizer ON organizer.id = e.organizer_user_id
                LEFT JOIN LATERAL (
                    SELECT status, selected_room_label
                    FROM venue_reservations
                    WHERE event_id = e.id
                    ORDER BY updated_at DESC, created_at DESC
                    LIMIT 1
                ) vr ON true
                CROSS JOIN LATERAL (
                    SELECT e.starts_at, e.ends_at
                    WHERE e.recurrence_frequency IS NULL OR e.recurrence_frequency <> 'WEEKLY'
                    UNION ALL
                    SELECT occurrence_start AS starts_at, occurrence_start + (e.ends_at - e.starts_at) AS ends_at
                    FROM generate_series(
                        e.starts_at,
                        LEAST(COALESCE(e.recurrence_until, now() + interval '1 year'), now() + interval '1 year'),
                        interval '1 week'
                    ) AS gs(occurrence_start)
                    WHERE e.recurrence_frequency = 'WEEKLY'
                ) occ
                WHERE i.id IS NOT NULL OR m.id IS NOT NULL OR f.follow_status = 'SAVED'
                ORDER BY starts_at ASC, id ASC
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, userId)
                statement.setString(2, userId)
                statement.setString(3, userId)
                statement.setString(4, userId)
                statement.setString(5, userId)
                statement.executeQuery().use { result ->
                    buildList {
                        while (result.next()) {
                            add(result.toAuthEventSummaryDto())
                        }
                    }
                }
            }
        }

    override fun listPublicEvents(): List<AuthEventSummaryDto> =
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                SELECT
                    CONCAT(e.id, '@', to_char(occ.starts_at, 'YYYYMMDDHH24MI')) AS id,
                    CONCAT('event-day-', to_char(occ.starts_at, 'YYYY-MM-DD')) AS day_id,
                    trim(to_char(occ.starts_at, 'Dy DD Mon')) AS day_label,
                    to_char(occ.starts_at, 'YYYY-MM-DD') AS date_iso,
                    e.title,
                    e.summary,
                    occ.starts_at,
                    occ.ends_at,
                    COALESCE(sp.name, e.title) AS venue_label,
                    organizer.display_name AS host_label,
                    e.visibility,
                    e.attendance_policy,
                    e.capacity_mode,
                    e.requires_identity,
                    e.join_code,
                    NULL::TEXT AS viewer_state,
                    0::INT AS unread_notice_count,
                    NULL::TEXT AS latest_notice_label,
                    vr.status AS venue_reservation_status,
                    CASE
                        WHEN vr.status = 'CONFIRMED' THEN 'Confirmed by venue'
                        WHEN vr.status = 'PENDING_CONFIRMATION' THEN 'Reservation requested'
                        WHEN vr.status = 'DECLINED' THEN 'Declined by venue'
                        WHEN vr.status = 'CANCELLED' THEN 'Cancelled by venue'
                        WHEN e.place_id IS NOT NULL THEN 'Venue not involved yet'
                        ELSE NULL
                    END AS venue_reservation_label,
                    vr.selected_room_label AS venue_room_label
                FROM events e
                LEFT JOIN service_places sp ON sp.id = e.place_id
                LEFT JOIN app_users organizer ON organizer.id = e.organizer_user_id
                LEFT JOIN LATERAL (
                    SELECT status, selected_room_label
                    FROM venue_reservations
                    WHERE event_id = e.id
                    ORDER BY updated_at DESC, created_at DESC
                    LIMIT 1
                ) vr ON true
                CROSS JOIN LATERAL (
                    SELECT e.starts_at, e.ends_at
                    WHERE e.recurrence_frequency IS NULL OR e.recurrence_frequency <> 'WEEKLY'
                    UNION ALL
                    SELECT occurrence_start AS starts_at, occurrence_start + (e.ends_at - e.starts_at) AS ends_at
                    FROM generate_series(
                        e.starts_at,
                        LEAST(COALESCE(e.recurrence_until, now() + interval '1 year'), now() + interval '1 year'),
                        interval '1 week'
                    ) AS gs(occurrence_start)
                    WHERE e.recurrence_frequency = 'WEEKLY'
                ) occ
                WHERE e.visibility = 'PUBLIC'
                  AND e.lifecycle_status IN ('SCHEDULED', 'PUBLISHED')
                  AND (occ.ends_at IS NULL OR occ.ends_at >= now())
                ORDER BY occ.starts_at ASC, e.id ASC
                LIMIT 100
                """.trimIndent(),
            ).use { statement ->
                statement.executeQuery().use { result ->
                    buildList {
                        while (result.next()) {
                            add(result.toAuthEventSummaryDto())
                        }
                    }
                }
            }
        }

    override fun listSuggestedEventsForUser(userId: String): List<AuthEventSummaryDto> =
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                SELECT
                    CONCAT(e.id, '@', to_char(occ.starts_at, 'YYYYMMDDHH24MI')) AS id,
                    CONCAT('event-day-', to_char(occ.starts_at, 'YYYY-MM-DD')) AS day_id,
                    trim(to_char(occ.starts_at, 'Dy DD Mon')) AS day_label,
                    to_char(occ.starts_at, 'YYYY-MM-DD') AS date_iso,
                    e.title,
                    e.summary,
                    occ.starts_at,
                    occ.ends_at,
                    COALESCE(sp.name, e.title) AS venue_label,
                    organizer.display_name AS host_label,
                    e.visibility,
                    e.attendance_policy,
                    e.capacity_mode,
                    e.requires_identity,
                    e.join_code,
                    'SUGGESTED'::TEXT AS viewer_state,
                    (
                        SELECT COUNT(*)::INT
                        FROM event_change_notices n
                        LEFT JOIN event_notice_receipts r
                            ON r.notice_id = n.id
                            AND r.user_id = ?
                        WHERE n.event_id = e.id
                          AND r.seen_at IS NULL
                          AND r.dismissed_at IS NULL
                    ) AS unread_notice_count,
                    (
                        SELECT n.title
                        FROM event_change_notices n
                        WHERE n.event_id = e.id
                        ORDER BY n.created_at DESC
                        LIMIT 1
                    ) AS latest_notice_label,
                    vr.status AS venue_reservation_status,
                    CASE
                        WHEN vr.status = 'CONFIRMED' THEN 'Confirmed by venue'
                        WHEN vr.status = 'PENDING_CONFIRMATION' THEN 'Reservation requested'
                        WHEN vr.status = 'DECLINED' THEN 'Declined by venue'
                        WHEN vr.status = 'CANCELLED' THEN 'Cancelled by venue'
                        WHEN e.place_id IS NOT NULL THEN 'Venue not involved yet'
                        ELSE NULL
                    END AS venue_reservation_label,
                    vr.selected_room_label AS venue_room_label
                FROM events e
                LEFT JOIN service_places sp ON sp.id = e.place_id
                LEFT JOIN app_users organizer ON organizer.id = e.organizer_user_id
                LEFT JOIN LATERAL (
                    SELECT status, selected_room_label
                    FROM venue_reservations
                    WHERE event_id = e.id
                    ORDER BY updated_at DESC, created_at DESC
                    LIMIT 1
                ) vr ON true
                LEFT JOIN event_follows f
                    ON f.event_id = e.id
                    AND f.user_id = ?
                LEFT JOIN event_invitations i
                    ON i.event_id = e.id
                    AND i.invited_user_id = ?
                LEFT JOIN event_memberships m
                    ON m.event_id = e.id
                    AND m.user_id = ?
                    AND m.membership_status = 'ACTIVE'
                CROSS JOIN LATERAL (
                    SELECT e.starts_at, e.ends_at
                    WHERE e.recurrence_frequency IS NULL OR e.recurrence_frequency <> 'WEEKLY'
                    UNION ALL
                    SELECT occurrence_start AS starts_at, occurrence_start + (e.ends_at - e.starts_at) AS ends_at
                    FROM generate_series(
                        e.starts_at,
                        LEAST(COALESCE(e.recurrence_until, now() + interval '1 year'), now() + interval '1 year'),
                        interval '1 week'
                    ) AS gs(occurrence_start)
                    WHERE e.recurrence_frequency = 'WEEKLY'
                ) occ
                WHERE e.visibility = 'PUBLIC'
                  AND e.lifecycle_status IN ('SCHEDULED', 'PUBLISHED')
                  AND (occ.ends_at IS NULL OR occ.ends_at >= now())
                  AND COALESCE(f.follow_status, '') <> 'DISMISSED'
                  AND COALESCE(f.follow_status, '') <> 'SAVED'
                  AND i.id IS NULL
                  AND m.id IS NULL
                  AND e.organizer_user_id IS DISTINCT FROM ?
                ORDER BY occ.starts_at ASC, e.id ASC
                LIMIT 100
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, userId)
                statement.setString(2, userId)
                statement.setString(3, userId)
                statement.setString(4, userId)
                statement.setString(5, userId)
                statement.executeQuery().use { result ->
                    buildList {
                        while (result.next()) {
                            add(result.toAuthEventSummaryDto())
                        }
                    }
                }
            }
        }

    override fun updateEventFollow(userId: String, eventId: String, status: String) {
        dataSource.connection.use { connection ->
            val baseEventId = eventId.substringBefore("@")
            val normalizedStatus = status.trim().uppercase()
            require(normalizedStatus in setOf("SAVED", "DISMISSED")) { "Unsupported event follow status." }
            connection.prepareStatement(
                """
                INSERT INTO event_follows (event_id, user_id, follow_status)
                VALUES (?, ?, ?)
                ON CONFLICT (event_id, user_id) DO UPDATE
                SET follow_status = EXCLUDED.follow_status,
                    updated_at = now()
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, baseEventId)
                statement.setString(2, userId)
                statement.setString(3, normalizedStatus)
                statement.executeUpdate()
            }
        }
    }

    override fun createEventNotice(userId: String, eventId: String, request: EventNoticeCreateRequest) {
        dataSource.connection.use { connection ->
            val baseEventId = eventId.substringBefore("@")
            val ownsEvent = connection.prepareStatement(
                """
                SELECT 1
                FROM events
                WHERE id = ?
                  AND organizer_user_id = ?
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, baseEventId)
                statement.setString(2, userId)
                statement.executeQuery().use { it.next() }
            }
            require(ownsEvent) { "Only the organizer can publish event notices." }
            connection.prepareStatement(
                """
                INSERT INTO event_change_notices (event_id, change_type, title, message, created_by_user_id)
                VALUES (?, ?, ?, ?, ?)
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, baseEventId)
                statement.setString(2, request.changeType.toEventNoticeType())
                statement.setString(3, request.title.trim().take(160))
                statement.setString(4, request.message.trim())
                statement.setString(5, userId)
                statement.executeUpdate()
            }
        }
    }

    override fun reviewVenueReservation(
        reviewerUserId: String,
        reservationId: String,
        request: VenueReservationReviewRequest,
    ): VenueReservationReviewDto? =
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                UPDATE venue_reservations
                SET status = ?,
                    selected_room_label = COALESCE(?, selected_room_label),
                    venue_note = COALESCE(?, venue_note),
                    confirmed_by_user_id = CASE WHEN ? = 'CONFIRMED' THEN ? ELSE NULL END,
                    confirmed_at = CASE WHEN ? = 'CONFIRMED' THEN now() ELSE NULL END,
                    declined_at = CASE WHEN ? = 'DECLINED' THEN now() ELSE NULL END,
                    updated_at = now()
                WHERE id = ?
                RETURNING id, event_id, place_id, status, selected_room_label, venue_note, confirmed_at, declined_at
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, request.status)
                statement.setString(2, request.selectedRoomLabel)
                statement.setString(3, request.venueNote)
                statement.setString(4, request.status)
                statement.setString(5, reviewerUserId)
                statement.setString(6, request.status)
                statement.setString(7, request.status)
                statement.setString(8, reservationId)
                statement.executeQuery().use { result ->
                    if (result.next()) result.toVenueReservationReviewDto() else null
                }
            }
        }

    override fun findActiveStayForUser(userId: String): AuthActiveStayDto? =
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                SELECT
                    s.hotel_id,
                    h.display_name AS hotel_display_name,
                    g.id AS guest_id,
                    g.full_name AS guest_name,
                    s.id AS stay_id,
                    s.room_id,
                    s.status AS stay_status,
                    s.start_iso_utc,
                    s.end_iso_utc
                FROM guests g
                INNER JOIN stays s ON s.guest_id = g.id
                INNER JOIN hotels h ON h.id = s.hotel_id
                WHERE g.user_id = ?
                    AND s.status = 'ACTIVE'
                    AND now() BETWEEN s.start_iso_utc AND s.end_iso_utc
                ORDER BY s.start_iso_utc DESC
                LIMIT 1
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, userId)
                statement.executeQuery().use { result ->
                    if (result.next()) result.toAuthActiveStayDto() else null
                }
            }
        }

    override fun respondToInvitation(userId: String, invitationId: String, accepted: Boolean): AuthInvitationSummaryDto? =
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                val updated = connection.prepareStatement(
                    """
                    UPDATE event_invitations
                    SET invitation_status = ?,
                        responded_at = now(),
                        accepted_at = CASE WHEN ? THEN now() ELSE NULL END,
                        declined_at = CASE WHEN ? THEN NULL ELSE now() END,
                        updated_at = now()
                    WHERE id = ?
                      AND invited_user_id = ?
                    RETURNING id
                    """.trimIndent(),
                ).use { statement ->
                    statement.setString(1, if (accepted) "ACCEPTED" else "DECLINED")
                    statement.setBoolean(2, accepted)
                    statement.setBoolean(3, accepted)
                    statement.setString(4, invitationId)
                    statement.setString(5, userId)
                    statement.executeQuery().use { result ->
                        if (result.next()) result.getString("id") else null
                    }
                }
                if (updated != null) {
                    connection.prepareStatement(
                        """
                        UPDATE access_passes
                        SET pass_status = CASE
                            WHEN ? THEN CASE WHEN pass_status = 'USED' THEN 'USED' ELSE 'ACTIVE' END
                            ELSE CASE WHEN pass_status = 'USED' THEN 'USED' ELSE 'VOID' END
                        END,
                            updated_at = now()
                        WHERE invitation_id = ?
                        """.trimIndent(),
                    ).use { statement ->
                        statement.setBoolean(1, accepted)
                        statement.setString(2, updated)
                        statement.executeUpdate()
                    }
                }
                val summary = updated?.let {
                    connection.prepareStatement(
                        """
                        SELECT
                            i.id,
                            e.id AS event_id,
                            e.title,
                            COALESCE(sp.name, e.event_type) AS venue_label,
                            i.invite_code,
                            COALESCE(i.invited_phone_number, i.invited_email, '') AS contact_label,
                            i.invitation_status,
                            i.access_tier,
                            e.ends_at,
                            p.pass_status
                        FROM event_invitations i
                        INNER JOIN events e ON e.id = i.event_id
                        LEFT JOIN service_places sp ON sp.id = e.place_id
                        LEFT JOIN access_passes p ON p.invitation_id = i.id AND p.user_id = i.invited_user_id
                        WHERE i.id = ? AND i.invited_user_id = ?
                        """.trimIndent(),
                    ).use { statement ->
                        statement.setString(1, it)
                        statement.setString(2, userId)
                        statement.executeQuery().use { result ->
                            if (result.next()) result.toAuthInvitationSummaryDto() else null
                        }
                    }
                }
                connection.commit()
                summary
            } catch (cause: Throwable) {
                connection.rollback()
                throw cause
            }
        }

    override fun createEvent(userId: String, request: EventCreateRequest): AuthEventSummaryDto =
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                val eventId = connection.prepareStatement(
                    """
                    INSERT INTO events (
                        organizer_user_id, place_id, slug, title, event_type, lifecycle_status,
                        visibility, attendance_policy, capacity_mode, requires_identity,
                        join_code, summary, starts_at, ends_at, recurrence_frequency, recurrence_until
                    )
                    VALUES (?, ?, ?, ?, ?, 'SCHEDULED', ?, ?, ?, ?, ?, ?, COALESCE(?::timestamptz, now()), COALESCE(?::timestamptz, now() + interval '3 hours'), ?, ?::timestamptz)
                    RETURNING id
                    """.trimIndent(),
                ).use { statement ->
                    val title = request.title.trim()
                    val eventType = request.eventType.toEventType()
                    val visibility = request.visibility.toEventVisibility()
                    val attendancePolicy = request.attendancePolicy.toEventAttendancePolicy()
                    val capacityMode = request.capacityMode.toEventCapacityMode()
                    statement.setString(1, userId)
                    statement.setString(2, request.placeId?.trim()?.takeIf { it.isNotEmpty() })
                    statement.setString(3, "${title.toSlug()}-${randomUrlSafeToken(5).lowercase()}")
                    statement.setString(4, title)
                    statement.setString(5, eventType)
                    statement.setString(6, visibility.name)
                    statement.setString(7, attendancePolicy.name)
                    statement.setString(8, capacityMode.name)
                    statement.setBoolean(9, request.requiresIdentity)
                    statement.setString(10, randomEventJoinCode())
                    statement.setString(11, request.summary?.trim()?.takeIf { it.isNotEmpty() })
                    statement.setString(12, request.startsAtIso?.trim()?.takeIf { it.isNotEmpty() })
                    statement.setString(13, request.endsAtIso?.trim()?.takeIf { it.isNotEmpty() })
                    statement.setString(14, request.recurrenceFrequency?.toEventRecurrenceFrequency())
                    statement.setString(15, request.recurrenceUntilIso?.trim()?.takeIf { it.isNotEmpty() })
                    statement.executeQuery().use { result ->
                        check(result.next()) { "Event insert did not return an id" }
                        result.getString("id")
                    }
                }
                connection.prepareStatement(
                    """
                    INSERT INTO event_memberships (event_id, user_id, membership_role, membership_status)
                    VALUES (?, ?, 'ORGANIZER', 'ACTIVE')
                    ON CONFLICT (event_id, user_id) DO UPDATE
                    SET membership_role = EXCLUDED.membership_role,
                        membership_status = EXCLUDED.membership_status,
                        updated_at = now()
                    """.trimIndent(),
                ).use { statement ->
                    statement.setString(1, eventId)
                    statement.setString(2, userId)
                    statement.executeUpdate()
                }
                val summary = connection.findEventSummaryForUser(userId, eventId)
                    ?: error("Created event could not be loaded")
                connection.commit()
                summary
            } catch (cause: Throwable) {
                connection.rollback()
                throw cause
            }
        }

    override fun linkSocialProviderToUser(
        userId: String,
        provider: String,
        subject: String,
        email: String,
        emailVerified: Boolean,
        displayName: String?,
        avatarUrl: String?,
    ): UserAuthProvider =
        dataSource.connection.use { connection ->
            linkSocialProviderToUser(
                userId = userId,
                provider = provider,
                subject = subject,
                email = email,
                emailVerified = emailVerified,
                displayName = displayName,
                avatarUrl = avatarUrl,
                connection = connection,
            )
        }

    private fun linkSocialProviderToUser(
        userId: String,
        provider: String,
        subject: String,
        email: String,
        emailVerified: Boolean,
        displayName: String?,
        avatarUrl: String?,
        connection: java.sql.Connection,
    ): UserAuthProvider =
        connection.prepareStatement(
            """
            INSERT INTO user_auth_providers (user_id, provider, provider_subject, email, email_verified, display_name, avatar_url)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (provider, provider_subject) DO UPDATE
            SET user_id = EXCLUDED.user_id,
                email = EXCLUDED.email,
                email_verified = EXCLUDED.email_verified,
                display_name = EXCLUDED.display_name,
                avatar_url = EXCLUDED.avatar_url,
                updated_at = now()
            RETURNING id, user_id, provider, provider_subject, email, email_verified
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, userId)
            statement.setString(2, provider.trim().uppercase())
            statement.setString(3, subject)
            statement.setString(4, email.trim().lowercase())
            statement.setBoolean(5, emailVerified)
            statement.setString(6, displayName?.trim()?.takeIf { it.isNotEmpty() })
            statement.setString(7, avatarUrl?.trim()?.takeIf { it.isNotEmpty() })
            statement.executeQuery().use { result ->
                check(result.next()) { "Social provider link insert did not return a row" }
                result.toUserAuthProvider()
            }
        }
}

private fun ResultSet.singleUserOrNull(): StoredAuthUser? =
    if (next()) toStoredAuthUser() else null

private fun ResultSet.toStoredAuthUser(): StoredAuthUser =
    StoredAuthUser(
        user = AuthUser(
            id = getString("id"),
            email = getString("email"),
            displayName = getString("display_name"),
            username = getString("username"),
            phoneNumber = getString("phone_number"),
            privacyConsent = AuthPrivacyConsent(
                mapAndVenueActivityEnabled = getBoolean("map_and_venue_activity_enabled"),
                diagnosticsEnabled = getBoolean("diagnostics_enabled"),
                notificationsEnabled = getBoolean("notifications_enabled"),
                analyticsEnabled = getBoolean("analytics_enabled"),
            ),
            roles = getArray("roles")
                ?.array
                ?.let { it as Array<*> }
                ?.mapNotNull { value -> value?.toString()?.let(AuthRole::valueOf) }
                ?.toSet()
                ?: setOf(AuthRole.CUSTOMER),
        ),
        passwordHash = getString("password_hash"),
    )

private fun ResultSet.toAppUser(): AppUser =
    AppUser(
        id = getString("id"),
        email = getString("email"),
        displayName = getString("display_name"),
        username = getString("username"),
        phoneNumber = getString("phone_number"),
        passwordHash = getString("password_hash"),
        roles = getArray("roles")
            ?.array
            ?.let { it as Array<*> }
            ?.mapNotNull { value -> value?.toString() }
            ?: listOf(AuthRole.CUSTOMER.name),
        disabled = getBoolean("disabled"),
        lastLoginAt = getTimestamp("last_login_at")?.toInstant(),
    )

private fun ResultSet.toUserAuthProvider(): UserAuthProvider =
    UserAuthProvider(
        id = getString("id"),
        userId = getString("user_id"),
        provider = getString("provider"),
        providerSubject = getString("provider_subject"),
        email = getString("email"),
        emailVerified = getBoolean("email_verified"),
    )

private fun ResultSet.toStoredRefreshToken(): StoredRefreshToken =
    StoredRefreshToken(
        id = getString("id"),
        userId = getString("user_id"),
        tokenHash = getString("token_hash"),
        familyId = getString("family_id"),
        expiresAt = getTimestamp("expires_at").toInstant(),
        revokedAt = getTimestamp("revoked_at")?.toInstant(),
        replacedByTokenId = getString("replaced_by_token_id"),
    )

private fun ResultSet.toAuthInvitationSummaryDto(): AuthInvitationSummaryDto {
    val invitationStatus = getString("invitation_status")
    val eventEndedAt = getTimestamp("ends_at")?.toInstant()
    val passStatus = getString("pass_status")
    val state = when {
        invitationStatus in setOf("DECLINED", "EXPIRED", "CANCELLED") -> "ARCHIVED"
        passStatus in setOf("USED", "VOID") -> "PAST"
        eventEndedAt != null && eventEndedAt.isBefore(Instant.now()) -> "PAST"
        else -> "ACTIVE"
    }
    return AuthInvitationSummaryDto(
        id = getString("id"),
        eventId = getString("event_id"),
        title = getString("title"),
        subtitle = getString("venue_label"),
        code = getString("invite_code"),
        phoneLabel = getString("contact_label"),
        statusLabel = "${invitationStatus.replace('_', ' ').lowercase().replaceFirstChar(Char::titlecase)} • ${getString("access_tier").replace('_', ' ')}",
        state = state,
        awaitingResponse = invitationStatus == "PENDING",
    )
}

private fun ResultSet.toAuthEventSummaryDto(): AuthEventSummaryDto =
    AuthEventSummaryDto(
        id = getString("id"),
        dayId = getString("day_id"),
        dayLabel = getString("day_label"),
        dateIso = getString("date_iso"),
        title = getString("title"),
        description = getString("summary").orEmpty(),
        startIso = getTimestamp("starts_at").toInstant().toString(),
        endIso = getTimestamp("ends_at").toInstant().toString(),
        venueLabel = getString("venue_label"),
        hostLabel = getString("host_label"),
        visibility = getString("visibility"),
        attendancePolicy = getString("attendance_policy"),
        capacityMode = getString("capacity_mode"),
        requiresIdentity = getBoolean("requires_identity"),
        joinCode = getString("join_code"),
        viewerState = getString("viewer_state"),
        unreadNoticeCount = getInt("unread_notice_count"),
        latestNoticeLabel = getString("latest_notice_label"),
        venueReservationStatus = getString("venue_reservation_status"),
        venueReservationLabel = getString("venue_reservation_label"),
        venueRoomLabel = getString("venue_room_label"),
    )

private fun ResultSet.toVenueReservationReviewDto(): VenueReservationReviewDto =
    VenueReservationReviewDto(
        id = getString("id"),
        eventId = getString("event_id"),
        placeId = getString("place_id"),
        status = getString("status"),
        selectedRoomLabel = getString("selected_room_label"),
        venueNote = getString("venue_note"),
        confirmedAt = getTimestamp("confirmed_at")?.toInstant()?.toString(),
        declinedAt = getTimestamp("declined_at")?.toInstant()?.toString(),
    )

private fun java.sql.Connection.findEventSummaryForUser(userId: String, eventId: String): AuthEventSummaryDto? =
    prepareStatement(
        """
        SELECT
            e.id,
            CONCAT('event-day-', to_char(e.starts_at, 'YYYY-MM-DD')) AS day_id,
            trim(to_char(e.starts_at, 'Dy DD Mon')) AS day_label,
            to_char(e.starts_at, 'YYYY-MM-DD') AS date_iso,
            e.title,
            e.summary,
            e.starts_at,
            e.ends_at,
            COALESCE(sp.name, e.title) AS venue_label,
            organizer.display_name AS host_label,
            e.visibility,
            e.attendance_policy,
            e.capacity_mode,
            e.requires_identity,
            e.join_code,
            CASE
                WHEN e.organizer_user_id = ? THEN 'ORGANIZER'
                WHEN m.id IS NOT NULL THEN 'JOINED'
                WHEN f.follow_status = 'SAVED' THEN 'SAVED'
                ELSE NULL
            END AS viewer_state,
            (
                SELECT COUNT(*)::INT
                FROM event_change_notices n
                LEFT JOIN event_notice_receipts r
                    ON r.notice_id = n.id
                    AND r.user_id = ?
                WHERE n.event_id = e.id
                  AND r.seen_at IS NULL
                  AND r.dismissed_at IS NULL
            ) AS unread_notice_count,
            (
                SELECT n.title
                FROM event_change_notices n
                WHERE n.event_id = e.id
                ORDER BY n.created_at DESC
                LIMIT 1
            ) AS latest_notice_label,
            vr.status AS venue_reservation_status,
            CASE
                WHEN vr.status = 'CONFIRMED' THEN 'Confirmed by venue'
                WHEN vr.status = 'PENDING_CONFIRMATION' THEN 'Reservation requested'
                WHEN vr.status = 'DECLINED' THEN 'Declined by venue'
                WHEN vr.status = 'CANCELLED' THEN 'Cancelled by venue'
                WHEN e.place_id IS NOT NULL THEN 'Venue not involved yet'
                ELSE NULL
            END AS venue_reservation_label,
            vr.selected_room_label AS venue_room_label
        FROM events e
        LEFT JOIN service_places sp ON sp.id = e.place_id
        LEFT JOIN app_users organizer ON organizer.id = e.organizer_user_id
        LEFT JOIN LATERAL (
            SELECT status, selected_room_label
            FROM venue_reservations
            WHERE event_id = e.id
            ORDER BY updated_at DESC, created_at DESC
            LIMIT 1
        ) vr ON true
        LEFT JOIN event_memberships m
            ON m.event_id = e.id
            AND m.user_id = ?
            AND m.membership_status = 'ACTIVE'
        LEFT JOIN event_follows f
            ON f.event_id = e.id
            AND f.user_id = ?
        WHERE e.id = ?
          AND (e.organizer_user_id = ? OR m.id IS NOT NULL OR f.follow_status = 'SAVED')
        """.trimIndent(),
    ).use { statement ->
        statement.setString(1, userId)
        statement.setString(2, userId)
        statement.setString(3, userId)
        statement.setString(4, userId)
        statement.setString(5, eventId)
        statement.setString(6, userId)
        statement.executeQuery().use { result ->
            if (result.next()) result.toAuthEventSummaryDto() else null
        }
    }

private fun String.toEventVisibility(): EventVisibility =
    runCatching { EventVisibility.valueOf(trim().uppercase()) }.getOrDefault(EventVisibility.UNLISTED)

private fun String.toEventAttendancePolicy(): EventAttendancePolicy =
    runCatching { EventAttendancePolicy.valueOf(trim().uppercase()) }.getOrDefault(EventAttendancePolicy.INVITE_OR_CODE)

private fun String.toEventCapacityMode(): EventCapacityMode =
    runCatching { EventCapacityMode.valueOf(trim().uppercase()) }.getOrDefault(EventCapacityMode.UNLIMITED)

private fun String.toEventRecurrenceFrequency(): String? =
    trim().uppercase().takeIf { it == "WEEKLY" }

private fun String.toEventNoticeType(): String =
    trim()
        .uppercase()
        .replace(Regex("[^A-Z0-9_]+"), "_")
        .trim('_')
        .ifBlank { "ORGANIZER_NOTE" }
        .take(64)

private fun String.toEventType(): String =
    trim()
        .uppercase()
        .replace(Regex("[^A-Z0-9_]+"), "_")
        .trim('_')
        .ifBlank { "OTHER" }
        .take(64)

private fun String.toSlug(): String =
    trim()
        .lowercase()
        .replace(Regex("[^a-z0-9]+"), "-")
        .trim('-')
        .ifBlank { "event" }
        .take(80)

private fun randomEventJoinCode(): String =
    randomUrlSafeToken(6).uppercase().filter(Char::isLetterOrDigit).take(8).padEnd(6, 'X')

private fun ResultSet.toAuthActiveStayDto(): AuthActiveStayDto =
    AuthActiveStayDto(
        hotelId = getString("hotel_id"),
        hotelDisplayName = getString("hotel_display_name"),
        guestId = getString("guest_id"),
        guestName = getString("guest_name"),
        stayId = getString("stay_id"),
        roomId = getString("room_id"),
        stayStatus = getString("stay_status"),
        startsAtIso = getTimestamp("start_iso_utc").toInstant().toString(),
        endsAtIso = getTimestamp("end_iso_utc").toInstant().toString(),
    )

private fun AppUser.toStoredAuthUser(): StoredAuthUser =
    StoredAuthUser(
        user = AuthUser(
            id = id,
            email = email,
            displayName = displayName,
            username = username,
            phoneNumber = phoneNumber,
            roles = roles.mapNotNull { role -> runCatching { AuthRole.valueOf(role) }.getOrNull() }.toSet()
                .ifEmpty { setOf(AuthRole.CUSTOMER) },
        ),
        passwordHash = passwordHash,
    )
