package dev.orestegabo.kaze.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwk.JwkProviderBuilder
import io.ktor.http.HttpStatusCode
import org.mindrot.jbcrypt.BCrypt
import java.net.URI
import java.security.interfaces.RSAPublicKey
import java.time.Instant
import java.util.Date
import java.util.UUID
import java.util.concurrent.TimeUnit

internal class AuthService(
    private val repositoryProvider: () -> AuthRepository,
    private val jwtConfig: JwtConfig,
) {
    private val signingAlgorithm = Algorithm.HMAC256(jwtConfig.secret)
    private val repository: AuthRepository
        get() = repositoryProvider()

    fun signup(request: AuthSignupRequest): AuthResponseDto {
        val email = normalizeEmail(request.email)
        validatePassword(request.password)
        if (repository.findByEmail(email) != null) {
            throw AuthProblemException(HttpStatusCode.Conflict, "email_already_registered", "This email is already registered.")
        }

        val user = repository.createPasswordUser(
            email = email,
            passwordHash = BCrypt.hashpw(request.password, BCrypt.gensalt(BCRYPT_COST)),
            displayName = request.displayName,
        ).user
        return user.toAuthResponse()
    }

    fun signin(request: AuthSigninRequest): AuthResponseDto {
        val storedUser = repository.findByEmail(normalizeEmail(request.email))
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

        val token = verifyExternalToken(
            idToken = request.idToken,
            issuer = GOOGLE_ISSUER,
            alternateIssuer = GOOGLE_ALT_ISSUER,
            audiences = jwtConfig.googleClientIds,
            jwksUrl = GOOGLE_JWKS_URL,
        )
        val identity = token.toExternalIdentity(AuthProvider.GOOGLE, request.displayName)
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

        val token = verifyExternalToken(
            idToken = request.idToken,
            issuer = APPLE_ISSUER,
            alternateIssuer = null,
            audiences = jwtConfig.appleClientIds,
            jwksUrl = APPLE_JWKS_URL,
        )
        val identity = token.toExternalIdentity(AuthProvider.APPLE, request.displayName)
        return repository.upsertExternalUser(identity).user.toAuthResponse()
    }

    private fun verifyExternalToken(
        idToken: String,
        issuer: String,
        alternateIssuer: String?,
        audiences: Set<String>,
        jwksUrl: String,
    ): DecodedJWT {
        val decoded = JWT.decode(idToken)
        val provider = JwkProviderBuilder(URI(jwksUrl).toURL())
            .cached(10, 24, TimeUnit.HOURS)
            .rateLimited(10, 1, TimeUnit.MINUTES)
            .build()
        val jwk = provider.get(decoded.keyId)
        val verifier = JWT.require(Algorithm.RSA256(jwk.publicKey as RSAPublicKey, null))
            .withIssuer(*listOfNotNull(issuer, alternateIssuer).toTypedArray())
            .withAudience(*audiences.toTypedArray())
            .build()

        return try {
            verifier.verify(idToken)
        } catch (cause: Throwable) {
            throw AuthProblemException(HttpStatusCode.Unauthorized, "invalid_identity_token", "The identity token is invalid.")
        }
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

    private fun AuthUser.toAuthResponse(): AuthResponseDto =
        AuthResponseDto(
            accessToken = issueAccessToken(this),
            expiresInSeconds = jwtConfig.accessTokenTtlSeconds,
            user = toDto(),
        )

    private fun DecodedJWT.toExternalIdentity(provider: AuthProvider, fallbackDisplayName: String?): ExternalIdentity {
        val email = getClaim("email").asString()?.let(::normalizeEmail)
            ?: throw AuthProblemException(HttpStatusCode.Unauthorized, "identity_email_missing", "The identity token does not include an email.")
        return ExternalIdentity(
            provider = provider,
            providerSubject = subject,
            email = email,
            emailVerified = getClaim("email_verified").asBoolean()
                ?: getClaim("email_verified").asString()?.toBooleanStrictOrNull()
                ?: false,
            displayName = fallbackDisplayName?.trim()?.takeIf { it.isNotEmpty() }
                ?: getClaim("name").asString()?.trim()?.takeIf { it.isNotEmpty() },
        )
    }

    private fun invalidCredentials(): AuthProblemException =
        AuthProblemException(HttpStatusCode.Unauthorized, "invalid_credentials", "Email or password is incorrect.")

    private fun normalizeEmail(email: String): String {
        val normalized = email.trim().lowercase()
        require(EMAIL_REGEX.matches(normalized)) { "A valid email address is required." }
        return normalized
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

private const val MIN_PASSWORD_LENGTH = 8
private const val BCRYPT_COST = 12
private const val GOOGLE_ISSUER = "https://accounts.google.com"
private const val GOOGLE_ALT_ISSUER = "accounts.google.com"
private const val GOOGLE_JWKS_URL = "https://www.googleapis.com/oauth2/v3/certs"
private const val APPLE_ISSUER = "https://appleid.apple.com"
private const val APPLE_JWKS_URL = "https://appleid.apple.com/auth/keys"
private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
