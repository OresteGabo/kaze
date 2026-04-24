package dev.orestegabo.kaze.api

import dev.orestegabo.kaze.auth.AuthProblemException
import dev.orestegabo.kaze.auth.AuthRefreshRequest
import dev.orestegabo.kaze.auth.AuthSessionClaimRequest
import dev.orestegabo.kaze.auth.AuthService
import dev.orestegabo.kaze.auth.AuthSigninRequest
import dev.orestegabo.kaze.auth.AuthSignupRequest
import dev.orestegabo.kaze.auth.SocialSigninRequest
import dev.orestegabo.kaze.auth.loadJwtConfig
import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.CachingOptions
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.bearer
import io.ktor.server.plugins.autohead.AutoHeadResponse
import io.ktor.server.plugins.cachingheaders.CachingHeaders
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.conditionalheaders.ConditionalHeaders
import io.ktor.server.plugins.compression.Compression
import io.ktor.server.plugins.compression.deflate
import io.ktor.server.plugins.compression.gzip
import io.ktor.server.plugins.compression.minimumSize
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.plugins.forwardedheaders.ForwardedHeaders
import io.ktor.server.plugins.forwardedheaders.XForwardedHeaders
import io.ktor.server.plugins.ratelimit.RateLimit
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.origin
import io.ktor.server.plugins.requestvalidation.RequestValidation
import io.ktor.server.plugins.requestvalidation.RequestValidationException
import io.ktor.server.plugins.requestvalidation.ValidationResult
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.path
import io.ktor.server.response.respond
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.event.Level
import kotlin.time.Duration.Companion.minutes

internal val ApiRateLimit = RateLimitName("api")
internal const val ApiAuth = "api-bearer"
internal const val ApiJwtAuth = "api-jwt"

internal fun Application.configureHttp(authService: AuthService) {
    val securityConfig = loadApiSecurityConfig()
    val jwtConfig = loadJwtConfig()

    install(ForwardedHeaders)
    install(XForwardedHeaders)
    install(DefaultHeaders) {
        header(HttpHeaders.Server, "Kaze")
        header("X-Content-Type-Options", "nosniff")
        header("X-Frame-Options", "DENY")
        header("Referrer-Policy", "no-referrer")
    }
    install(CORS) {
        securityConfig.corsAllowedHosts.forEach { host ->
            allowHost(host, schemes = listOf("http", "https"))
        }
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Head)
        allowMethod(HttpMethod.Options)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Accept)
        allowHeader(HttpHeaders.AcceptLanguage)
        maxAgeInSeconds = CORS_PREFLIGHT_CACHE_SECONDS
    }
    install(AutoHeadResponse)
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path() != "/health" }
    }
    install(CachingHeaders) {
        options { _, content ->
            when (content.contentType?.withoutParameters()) {
                ContentType.Text.Html -> CachingOptions(CacheControl.MaxAge(maxAgeSeconds = DOCS_CACHE_SECONDS))
                ContentType.Application.Json -> CachingOptions(CacheControl.NoStore(CacheControl.Visibility.Private))
                else -> null
            }
        }
    }
    install(ConditionalHeaders)
    install(Compression) {
        gzip {
            priority = 1.0
            minimumSize(API_COMPRESSION_MIN_BYTES)
        }
        deflate {
            priority = 0.8
            minimumSize(API_COMPRESSION_MIN_BYTES)
        }
    }
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                ignoreUnknownKeys = true
                encodeDefaults = true
            },
        )
    }
    install(Authentication) {
        bearer(ApiAuth) {
            realm = "Kaze API"
            authenticate { credentials ->
                securityConfig.apiToken
                    ?.takeIf { it == credentials.token }
                    ?.let { UserIdPrincipal("api-client") }
            }
        }
        jwt(ApiJwtAuth) {
            realm = jwtConfig.realm
            verifier(authService.verifier())
            validate { credential ->
                credential.payload.subject
                    ?.takeIf { it.isNotBlank() }
                    ?.let { JWTPrincipal(credential.payload) }
            }
        }
    }
    install(RequestValidation) {
        validate<AuthSignupRequest> { request ->
            when {
                request.email.isBlank() -> ValidationResult.Invalid("email is required")
                request.password.length < AUTH_PASSWORD_MIN_LENGTH ->
                    ValidationResult.Invalid("password must be at least $AUTH_PASSWORD_MIN_LENGTH characters")
                request.username != null && request.username.isNotBlank() &&
                    !AUTH_USERNAME_REGEX.matches(request.username.trim().lowercase()) ->
                    ValidationResult.Invalid("username must be 3 to 32 characters and use only letters, numbers, dots, and underscores")
                request.phoneNumber != null && request.phoneNumber.isNotBlank() &&
                    !AUTH_PHONE_NUMBER_REGEX.matches(normalizeAuthPhoneNumber(request.phoneNumber)) ->
                    ValidationResult.Invalid("phoneNumber must be a valid international phone number")
                else -> ValidationResult.Valid
            }
        }
        validate<AuthSigninRequest> { request ->
            when {
                request.identifier.isNullOrBlank() && request.email.isNullOrBlank() ->
                    ValidationResult.Invalid("identifier is required")
                request.password.isBlank() -> ValidationResult.Invalid("password is required")
                else -> ValidationResult.Valid
            }
        }
        validate<SocialSigninRequest> { request ->
            when {
                request.idToken.isBlank() -> ValidationResult.Invalid("idToken is required")
                else -> ValidationResult.Valid
            }
        }
        validate<AuthSessionClaimRequest> { request ->
            when {
                request.loginToken.isBlank() -> ValidationResult.Invalid("loginToken is required")
                else -> ValidationResult.Valid
            }
        }
        validate<AuthRefreshRequest> { request ->
            when {
                request.refreshToken.isBlank() -> ValidationResult.Invalid("refreshToken is required")
                else -> ValidationResult.Valid
            }
        }
        validate<LateCheckoutSubmissionRequest> { request ->
            when {
                request.checkoutTimeIso.isBlank() -> ValidationResult.Invalid("checkoutTimeIso is required")
                request.feeAmountMinor < 0 -> ValidationResult.Invalid("feeAmountMinor must be zero or greater")
                request.currencyCode.isBlank() -> ValidationResult.Invalid("currencyCode is required")
                request.paymentPreference.isBlank() -> ValidationResult.Invalid("paymentPreference is required")
                request.followUpPreference.isBlank() -> ValidationResult.Invalid("followUpPreference is required")
                else -> ValidationResult.Valid
            }
        }
        validate<ServiceRequestSubmissionRequest> { request ->
            when {
                request.type.isBlank() -> ValidationResult.Invalid("type is required")
                request.note != null && request.note.length > SERVICE_REQUEST_NOTE_MAX_LENGTH ->
                    ValidationResult.Invalid("note must be $SERVICE_REQUEST_NOTE_MAX_LENGTH characters or fewer")
                else -> ValidationResult.Valid
            }
        }
        validate<AssistantQueryRequest> { request ->
            when {
                request.question.isBlank() -> ValidationResult.Invalid("question is required")
                request.question.length > ASSISTANT_QUESTION_MAX_LENGTH ->
                    ValidationResult.Invalid("question must be $ASSISTANT_QUESTION_MAX_LENGTH characters or fewer")
                else -> ValidationResult.Valid
            }
        }
    }
    install(RateLimit) {
        register(ApiRateLimit) {
            rateLimiter(limit = API_RATE_LIMIT_REQUESTS, refillPeriod = API_RATE_LIMIT_WINDOW)
            requestKey { call ->
                call.request.origin.remoteHost.takeIf { it.isNotBlank() }
                    ?: call.request.headers["X-Real-IP"]?.trim()?.takeIf { it.isNotEmpty() }
                    ?: "direct-client"
            }
        }
    }
    install(StatusPages) {
        exception<ApiNotFoundException> { call, cause ->
            call.respond(HttpStatusCode.NotFound, ApiProblem("not_found", cause.message ?: "Resource not found"))
        }
        exception<AuthProblemException> { call, cause ->
            call.respond(cause.status, ApiProblem(cause.code, cause.message))
        }
        exception<RequestValidationException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, ApiProblem("validation_error", cause.reasons.joinToString("; ")))
        }
        exception<IllegalArgumentException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, ApiProblem("bad_request", cause.message ?: "Invalid request"))
        }
        exception<Throwable> { call, cause ->
            call.respond(HttpStatusCode.InternalServerError, ApiProblem("internal_error", cause.message ?: "Unexpected server error"))
        }
    }
}

internal class ApiNotFoundException(message: String) : RuntimeException(message)

internal fun Application.isApiAuthenticationEnabled(): Boolean = loadApiSecurityConfig().apiToken != null

internal fun Application.isJwtAuthenticationRequired(): Boolean = loadJwtConfig().requireJwtForApi

private fun Application.loadApiSecurityConfig(): ApiSecurityConfig =
    ApiSecurityConfig(
        apiToken = environment.config.propertyOrNull("kaze.security.apiToken")
            ?.getString()
            ?.trim()
            ?.takeIf { it.isNotEmpty() },
        corsAllowedHosts = environment.config.propertyOrNull("kaze.security.cors.allowedHosts")
            ?.getString()
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?.ifEmpty { DEFAULT_CORS_ALLOWED_HOSTS }
            ?: DEFAULT_CORS_ALLOWED_HOSTS,
    )

private data class ApiSecurityConfig(
    val apiToken: String?,
    val corsAllowedHosts: List<String>,
)

private fun normalizeAuthPhoneNumber(phoneNumber: String): String =
    buildString(phoneNumber.length) {
        phoneNumber.trim().forEachIndexed { index, char ->
            when {
                char.isDigit() -> append(char)
                char == '+' && index == 0 -> append(char)
            }
        }
    }

private const val API_RATE_LIMIT_REQUESTS = 120
private const val API_COMPRESSION_MIN_BYTES = 256L
private const val AUTH_PASSWORD_MIN_LENGTH = 8
private const val ASSISTANT_QUESTION_MAX_LENGTH = 1_000
private const val SERVICE_REQUEST_NOTE_MAX_LENGTH = 500
private const val CORS_PREFLIGHT_CACHE_SECONDS = 3_600L
private const val DOCS_CACHE_SECONDS = 300
private val AUTH_USERNAME_REGEX = Regex("^[a-z0-9._]{3,32}$")
private val AUTH_PHONE_NUMBER_REGEX = Regex("^\\+?[1-9][0-9]{7,14}$")
private val API_RATE_LIMIT_WINDOW = 1.minutes
private val DEFAULT_CORS_ALLOWED_HOSTS = listOf(
    "localhost:3000",
    "localhost:5173",
    "127.0.0.1:3000",
    "127.0.0.1:5173",
)

@Serializable
internal data class ApiProblem(
    val code: String,
    val message: String,
)
