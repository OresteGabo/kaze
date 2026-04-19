package dev.orestegabo.kaze.api

import dev.orestegabo.kaze.auth.AuthResponseDto
import dev.orestegabo.kaze.auth.AuthLogoutResponseDto
import dev.orestegabo.kaze.auth.AuthService
import dev.orestegabo.kaze.auth.AuthSigninRequest
import dev.orestegabo.kaze.auth.AuthSignupRequest
import dev.orestegabo.kaze.auth.AuthUserDto
import dev.orestegabo.kaze.auth.SocialSigninRequest
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
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
                call.respond(AuthLogoutResponseDto())
            }
        }
    }
}
