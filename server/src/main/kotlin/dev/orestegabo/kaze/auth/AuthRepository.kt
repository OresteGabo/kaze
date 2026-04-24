package dev.orestegabo.kaze.auth

import java.sql.ResultSet
import java.sql.Timestamp
import java.time.Instant
import javax.sql.DataSource

internal interface AuthRepository {
    fun findByEmail(email: String): StoredAuthUser?
    fun findByUsername(username: String): StoredAuthUser?
    fun findByPhoneNumber(phoneNumber: String): StoredAuthUser?
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

    fun findActiveRefreshToken(tokenHash: String): StoredRefreshToken?
    fun revokeRefreshToken(tokenId: String, replacedByTokenId: String? = null)
    fun revokeRefreshTokenFamily(familyId: String)
    fun findById(userId: String): StoredAuthUser?
}

internal data class StoredAuthUser(
    val user: AuthUser,
    val passwordHash: String?,
)

internal class JdbcAuthRepository(
    private val dataSource: DataSource,
) : AuthRepository {

    override fun findByEmail(email: String): StoredAuthUser? =
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                SELECT id, email, display_name, username, phone_number, password_hash, roles
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
                SELECT id, email, display_name, username, phone_number, password_hash, roles
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
                SELECT id, email, display_name, username, phone_number, password_hash, roles
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

    override fun findByProvider(provider: AuthProvider, providerSubject: String): StoredAuthUser? =
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                SELECT u.id, u.email, u.display_name, u.username, u.phone_number, u.password_hash, u.roles
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
                SELECT u.id, u.email, u.display_name, u.username, u.phone_number, u.password_hash, u.roles, u.disabled, u.last_login_at
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

    override fun upsertExternalUser(identity: ExternalIdentity): StoredAuthUser =
        findUserBySocialProvider(identity.provider.name, identity.providerSubject)?.toStoredAuthUser()
            ?: dataSource.connection.use { connection ->
                connection.autoCommit = false
                try {
                    val existingUser = findByEmail(identity.email)
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
            INSERT INTO app_users (email, display_name, username, phone_number, password_hash, roles)
            VALUES (?, ?, ?, ?, ?, ?::TEXT[])
            RETURNING id, email, display_name, username, phone_number, password_hash, roles
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, email.trim().lowercase())
            statement.setString(2, displayName?.trim()?.takeIf { it.isNotEmpty() })
            statement.setString(3, username?.trim()?.lowercase()?.takeIf { it.isNotEmpty() })
            statement.setString(4, phoneNumber?.trim()?.takeIf { it.isNotEmpty() })
            statement.setString(5, passwordHash)
            statement.setString(6, roles.joinToString(prefix = "{", postfix = "}") { it.name })
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
                RETURNING id, user_id, token_hash, family_id
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

    override fun findActiveRefreshToken(tokenHash: String): StoredRefreshToken? =
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                SELECT id, user_id, token_hash, family_id
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

    override fun findById(userId: String): StoredAuthUser? =
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                SELECT id, email, display_name, username, phone_number, password_hash, roles
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
