package dev.orestegabo.kaze.api

import dev.orestegabo.kaze.auth.AuthResponseDto
import dev.orestegabo.kaze.auth.AuthProvider
import dev.orestegabo.kaze.auth.AuthRefreshRequest
import dev.orestegabo.kaze.auth.AuthLogoutResponseDto
import dev.orestegabo.kaze.auth.AuthSessionClaimRequest
import dev.orestegabo.kaze.auth.AuthService
import dev.orestegabo.kaze.auth.AuthSigninRequest
import dev.orestegabo.kaze.auth.AuthStartResponseDto
import dev.orestegabo.kaze.auth.AuthSignupRequest
import dev.orestegabo.kaze.auth.AuthUserDto
import dev.orestegabo.kaze.auth.SocialSigninRequest
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receiveNullable
import io.ktor.server.request.receive
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

internal fun Route.registerAuthRoutes(
    authService: AuthService,
) {
    route("/auth") {
        post("/signup") {
            call.respond<AuthResponseDto>(authService.signup(call.receive<AuthSignupRequest>()))
        }

        post("/signin") {
            call.respond<AuthResponseDto>(authService.signin(call.receive<AuthSigninRequest>()))
        }

        post("/google") {
            call.respond<AuthResponseDto>(authService.signinWithGoogle(call.receive<SocialSigninRequest>()))
        }

        post("/apple") {
            call.respond<AuthResponseDto>(authService.signinWithApple(call.receive<SocialSigninRequest>()))
        }

        get("/{provider}/start") {
            call.respond<AuthStartResponseDto>(
                authService.createAuthorizationRequest(
                    providerName = call.parameters["provider"].orEmpty(),
                    appRedirectUri = call.request.queryParameters["appRedirectUri"],
                ),
            )
        }

        get("/google/callback") {
            call.respondRedirect(
                authService.completeOAuthCallback(
                    provider = AuthProvider.GOOGLE,
                    code = call.request.queryParameters["code"],
                    state = call.request.queryParameters["state"],
                ),
            )
        }

        post("/apple/callback") {
            val form = call.receiveParameters()
            call.respondRedirect(
                authService.completeOAuthCallback(
                    provider = AuthProvider.APPLE,
                    code = form["code"],
                    state = form["state"],
                ),
            )
        }

        get("/facebook/callback") {
            call.respondRedirect(
                authService.completeOAuthCallback(
                    provider = AuthProvider.FACEBOOK,
                    code = call.request.queryParameters["code"],
                    state = call.request.queryParameters["state"],
                ),
            )
        }

        post("/session/claim") {
            call.respond<AuthResponseDto>(authService.claimOneTimeLoginToken(call.receive<AuthSessionClaimRequest>()))
        }

        post("/refresh") {
            call.respond<AuthResponseDto>(authService.refresh(call.receive<AuthRefreshRequest>()))
        }

        authenticate(ApiJwtAuth) {
            get("/me") {
                val principal = call.principal<JWTPrincipal>()
                    ?: throw IllegalArgumentException("Missing JWT principal")
                call.respond(
                    AuthUserDto(
                        id = principal.payload.subject,
                        email = principal.payload.getClaim("email").asString().orEmpty(),
                        roles = principal.payload.getClaim("roles").asList(String::class.java) ?: emptyList(),
                    ),
                )
            }

            post("/logout") {
                val request = runCatching { call.receiveNullable<AuthRefreshRequest>() }.getOrNull()
                authService.logout(request?.refreshToken)
                call.respond(AuthLogoutResponseDto())
            }
        }
    }
}
