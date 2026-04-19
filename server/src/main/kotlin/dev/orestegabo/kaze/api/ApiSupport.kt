package dev.orestegabo.kaze.api

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.compression.Compression
import io.ktor.server.plugins.compression.deflate
import io.ktor.server.plugins.compression.gzip
import io.ktor.server.plugins.compression.minimumSize
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.ratelimit.RateLimit
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.minutes

internal val ApiRateLimit = RateLimitName("api")

internal fun Application.configureHttp() {
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
    install(RateLimit) {
        register(ApiRateLimit) {
            rateLimiter(limit = API_RATE_LIMIT_REQUESTS, refillPeriod = API_RATE_LIMIT_WINDOW)
            requestKey { call ->
                call.request.headers["X-Forwarded-For"]
                    ?.substringBefore(",")
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() }
                    ?: call.request.headers["X-Real-IP"]?.trim()?.takeIf { it.isNotEmpty() }
                    ?: "direct-client"
            }
        }
    }
    install(StatusPages) {
        exception<ApiNotFoundException> { call, cause ->
            call.respond(HttpStatusCode.NotFound, ApiProblem("not_found", cause.message ?: "Resource not found"))
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

private const val API_RATE_LIMIT_REQUESTS = 120
private const val API_COMPRESSION_MIN_BYTES = 256L
private val API_RATE_LIMIT_WINDOW = 1.minutes

@Serializable
internal data class ApiProblem(
    val code: String,
    val message: String,
)
