package dev.orestegabo.kaze.api

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

internal fun Application.configureHttp() {
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                ignoreUnknownKeys = true
                encodeDefaults = true
            },
        )
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

@Serializable
internal data class ApiProblem(
    val code: String,
    val message: String,
)
