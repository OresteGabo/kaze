package dev.orestegabo.kaze.auth

import java.time.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

@Serializable
internal data class AppUser(
    val id: String,
    val email: String,
    val displayName: String? = null,
    val username: String? = null,
    val phoneNumber: String? = null,
    val passwordHash: String? = null,
    val roles: List<String> = emptyList(),
    val disabled: Boolean = false,
    val lastLoginAt: @Serializable(with = InstantIso8601Serializer::class) Instant? = null,
)

@Serializable
internal data class UserAuthProvider(
    val id: String,
    val userId: String,
    val provider: String,
    val providerSubject: String,
    val email: String,
    val emailVerified: Boolean = false,
)

@Serializable
internal data class AuthRefreshToken(
    val id: String,
    val userId: String,
    val tokenHash: String,
    val familyId: String,
    val expiresAt: @Serializable(with = InstantIso8601Serializer::class) Instant,
    val revokedAt: @Serializable(with = InstantIso8601Serializer::class) Instant? = null,
)

@Serializable
internal data class AppUserResponse(
    val id: String,
    val email: String,
    val displayName: String? = null,
    val roles: List<String> = emptyList(),
    val disabled: Boolean = false,
    val lastLoginAt: @Serializable(with = InstantIso8601Serializer::class) Instant? = null,
)

@Serializable
internal data class UserAuthProviderResponse(
    val id: String,
    val userId: String,
    val provider: String,
    val providerSubject: String,
    val email: String,
    val emailVerified: Boolean = false,
)

internal object AppUsersTable : IdTable<String>("app_users") {
    override val id = varchar("id", 120).entityId()
    val email = varchar("email", 320)
    val displayName = varchar("display_name", 240).nullable()
    val username = varchar("username", 80).nullable()
    val phoneNumber = varchar("phone_number", 32).nullable()
    val mapAndVenueActivityEnabled = bool("map_and_venue_activity_enabled").default(true)
    val diagnosticsEnabled = bool("diagnostics_enabled").default(true)
    val notificationsEnabled = bool("notifications_enabled").default(true)
    val analyticsEnabled = bool("analytics_enabled").default(false)
    val passwordHash = text("password_hash").nullable()
    val roles = array<String>("roles")
    val disabled = bool("disabled").default(false)
    val lastLoginAt = timestampWithTimeZone("last_login_at").nullable()
    val createdAt = timestampWithTimeZone("created_at")
    val updatedAt = timestampWithTimeZone("updated_at")

    override val primaryKey = PrimaryKey(id)
}

internal object UserAuthProvidersTable : IdTable<String>("user_auth_providers") {
    override val id = varchar("id", 120).entityId()
    val userId = varchar("user_id", 120).references(AppUsersTable.id)
    val provider = varchar("provider", 40)
    val providerSubject = varchar("provider_subject", 320)
    val email = varchar("email", 320)
    val emailVerified = bool("email_verified").default(false)
    val displayName = varchar("display_name", 240).nullable()
    val avatarUrl = text("avatar_url").nullable()
    val createdAt = timestampWithTimeZone("created_at")
    val updatedAt = timestampWithTimeZone("updated_at")

    override val primaryKey = PrimaryKey(id)
}

internal object AuthRefreshTokensTable : IdTable<String>("auth_refresh_tokens") {
    override val id = varchar("id", 120).entityId()
    val userId = varchar("user_id", 120).references(AppUsersTable.id)
    val tokenHash = text("token_hash")
    val familyId = varchar("family_id", 120)
    val deviceId = varchar("device_id", 240).nullable()
    val deviceLabel = varchar("device_label", 240).nullable()
    val expiresAt = timestampWithTimeZone("expires_at")
    val revokedAt = timestampWithTimeZone("revoked_at").nullable()
    val replacedByTokenId = varchar("replaced_by_token_id", 120).nullable()
    val createdAt = timestampWithTimeZone("created_at")
    val lastUsedAt = timestampWithTimeZone("last_used_at").nullable()

    override val primaryKey = PrimaryKey(id)
}

internal object InstantIso8601Serializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("InstantIso8601", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Instant =
        Instant.parse(decoder.decodeString())
}

internal fun AppUser.toResponse(): AppUserResponse =
    AppUserResponse(
        id = id,
        email = email,
        displayName = displayName,
        roles = roles,
        disabled = disabled,
        lastLoginAt = lastLoginAt,
    )

internal fun UserAuthProvider.toResponse(): UserAuthProviderResponse =
    UserAuthProviderResponse(
        id = id,
        userId = userId,
        provider = provider,
        providerSubject = providerSubject,
        email = email,
        emailVerified = emailVerified,
    )
