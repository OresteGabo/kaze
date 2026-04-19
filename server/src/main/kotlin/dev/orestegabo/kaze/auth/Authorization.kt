package dev.orestegabo.kaze.auth

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal

internal fun ApplicationCall.requireAnyRole(
    vararg allowedRoles: AuthRole,
): JWTPrincipal {
    val principal = principal<JWTPrincipal>()
        ?: throw AuthProblemException(HttpStatusCode.Unauthorized, "unauthorized", "Authentication is required.")
    val currentRoles = principal.payload.getClaim("roles").asList(String::class.java)
        ?.mapNotNull { role -> runCatching { AuthRole.valueOf(role) }.getOrNull() }
        ?.toSet()
        ?: emptySet()

    if (currentRoles.none { role -> role in allowedRoles }) {
        throw AuthProblemException(HttpStatusCode.Forbidden, "forbidden", "This account does not have access to this action.")
    }

    return principal
}
