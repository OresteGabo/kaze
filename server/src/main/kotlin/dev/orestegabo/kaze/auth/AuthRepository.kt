package dev.orestegabo.kaze.auth

import java.sql.ResultSet
import javax.sql.DataSource

internal interface AuthRepository {
    fun findByEmail(email: String): StoredAuthUser?
    fun findByProvider(provider: AuthProvider, providerSubject: String): StoredAuthUser?
    fun createPasswordUser(
        email: String,
        passwordHash: String,
        displayName: String?,
        roles: Set<AuthRole> = setOf(AuthRole.CUSTOMER),
    ): StoredAuthUser

    fun upsertExternalUser(identity: ExternalIdentity): StoredAuthUser
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
                SELECT id, email, display_name, password_hash, roles
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

    override fun findByProvider(provider: AuthProvider, providerSubject: String): StoredAuthUser? =
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                SELECT u.id, u.email, u.display_name, u.password_hash, u.roles
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

    override fun createPasswordUser(
        email: String,
        passwordHash: String,
        displayName: String?,
        roles: Set<AuthRole>,
    ): StoredAuthUser =
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                val user = insertUser(
                    email = email,
                    passwordHash = passwordHash,
                    displayName = displayName,
                    roles = roles,
                    connection = connection,
                )
                insertProvider(user.user.id, AuthProvider.PASSWORD, user.user.email, user.user.email, connection)
                connection.commit()
                user
            } catch (cause: Throwable) {
                connection.rollback()
                throw cause
            }
        }

    override fun upsertExternalUser(identity: ExternalIdentity): StoredAuthUser =
        findByProvider(identity.provider, identity.providerSubject)
            ?: dataSource.connection.use { connection ->
                connection.autoCommit = false
                try {
                    val existingUser = findByEmail(identity.email)
                    val storedUser = existingUser ?: insertUser(
                        email = identity.email,
                        passwordHash = null,
                        displayName = identity.displayName,
                        roles = setOf(AuthRole.CUSTOMER),
                        connection = connection,
                    )
                    insertProvider(
                        userId = storedUser.user.id,
                        provider = identity.provider,
                        providerSubject = identity.providerSubject,
                        email = identity.email,
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
        roles: Set<AuthRole>,
        connection: java.sql.Connection,
    ): StoredAuthUser =
        connection.prepareStatement(
            """
            INSERT INTO app_users (email, display_name, password_hash, roles)
            VALUES (?, ?, ?, ?::TEXT[])
            RETURNING id, email, display_name, password_hash, roles
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, email.trim().lowercase())
            statement.setString(2, displayName?.trim()?.takeIf { it.isNotEmpty() })
            statement.setString(3, passwordHash)
            statement.setString(4, roles.joinToString(prefix = "{", postfix = "}") { it.name })
            statement.executeQuery().use { result ->
                check(result.next()) { "User insert did not return a row" }
                result.toStoredAuthUser()
            }
        }

    private fun insertProvider(
        userId: String,
        provider: AuthProvider,
        providerSubject: String,
        email: String,
        connection: java.sql.Connection,
    ) {
        connection.prepareStatement(
            """
            INSERT INTO user_auth_providers (user_id, provider, provider_subject, email)
            VALUES (?, ?, ?, ?)
            ON CONFLICT (provider, provider_subject) DO NOTHING
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, userId)
            statement.setString(2, provider.name)
            statement.setString(3, providerSubject)
            statement.setString(4, email.trim().lowercase())
            statement.executeUpdate()
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
            roles = getArray("roles")
                ?.array
                ?.let { it as Array<*> }
                ?.mapNotNull { value -> value?.toString()?.let(AuthRole::valueOf) }
                ?.toSet()
                ?: setOf(AuthRole.CUSTOMER),
        ),
        passwordHash = getString("password_hash"),
    )
