package dev.orestegabo.kaze.api

import dev.orestegabo.kaze.auth.AuthResponseDto
import dev.orestegabo.kaze.auth.AuthActiveStayResponseDto
import dev.orestegabo.kaze.auth.AuthProvider
import dev.orestegabo.kaze.auth.AuthRefreshRequest
import dev.orestegabo.kaze.auth.AuthLogoutResponseDto
import dev.orestegabo.kaze.auth.AuthInvitationResponseRequest
import dev.orestegabo.kaze.auth.AuthProfileUpdateRequest
import dev.orestegabo.kaze.auth.AuthSessionClaimRequest
import dev.orestegabo.kaze.auth.AuthSessionBootstrapDto
import dev.orestegabo.kaze.auth.AuthSetPasswordRequest
import dev.orestegabo.kaze.auth.AuthService
import dev.orestegabo.kaze.auth.AuthSigninRequest
import dev.orestegabo.kaze.auth.AuthStartResponseDto
import dev.orestegabo.kaze.auth.AuthSignupRequest
import dev.orestegabo.kaze.auth.AuthUserDto
import dev.orestegabo.kaze.auth.AuthRole
import dev.orestegabo.kaze.auth.EventCreateRequest
import dev.orestegabo.kaze.auth.EventFollowRequest
import dev.orestegabo.kaze.auth.EventNoticeCreateRequest
import dev.orestegabo.kaze.auth.SocialSigninRequest
import dev.orestegabo.kaze.auth.VenueReservationReviewRequest
import dev.orestegabo.kaze.auth.requireAnyRole
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receiveNullable
import io.ktor.server.request.receive
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.header
import io.ktor.server.routing.put
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route

internal fun Route.registerAuthRoutes(
    authService: AuthService,
) {
    route("/auth") {
        post("/signup") {
            call.noStoreAuthResponse()
            call.respond<AuthResponseDto>(authService.signup(call.receive<AuthSignupRequest>()))
        }

        post("/signin") {
            call.noStoreAuthResponse()
            call.respond<AuthResponseDto>(authService.signin(call.receive<AuthSigninRequest>()))
        }

        post("/google") {
            call.noStoreAuthResponse()
            call.respond<AuthResponseDto>(authService.signinWithGoogle(call.receive<SocialSigninRequest>()))
        }

        post("/apple") {
            call.noStoreAuthResponse()
            call.respond<AuthResponseDto>(authService.signinWithApple(call.receive<SocialSigninRequest>()))
        }

        post("/facebook") {
            call.noStoreAuthResponse()
            call.respond<AuthResponseDto>(authService.signinWithFacebook(call.receive<SocialSigninRequest>()))
        }

        get("/{provider}/start") {
            call.noStoreAuthResponse()
            call.respond<AuthStartResponseDto>(
                authService.createAuthorizationRequest(
                    providerName = call.parameters["provider"].orEmpty(),
                    appRedirectUri = call.request.queryParameters["appRedirectUri"],
                ),
            )
        }

        get("/google/callback") {
            call.noStoreAuthResponse()
            call.respondRedirect(
                authService.completeOAuthCallback(
                    provider = AuthProvider.GOOGLE,
                    code = call.request.queryParameters["code"],
                    state = call.request.queryParameters["state"],
                ),
            )
        }

        post("/apple/callback") {
            call.noStoreAuthResponse()
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
            call.noStoreAuthResponse()
            call.respondRedirect(
                authService.completeOAuthCallback(
                    provider = AuthProvider.FACEBOOK,
                    code = call.request.queryParameters["code"],
                    state = call.request.queryParameters["state"],
                ),
            )
        }

        post("/session/claim") {
            call.noStoreAuthResponse()
            call.respond<AuthResponseDto>(authService.claimOneTimeLoginToken(call.receive<AuthSessionClaimRequest>()))
        }

        post("/refresh") {
            call.noStoreAuthResponse()
            call.respond<AuthResponseDto>(authService.refresh(call.receive<AuthRefreshRequest>()))
        }

        authenticate(ApiJwtAuth) {
            get("/session") {
                call.noStoreAuthResponse()
                val principal = call.authenticatedJwtPrincipal()
                call.respond<AuthSessionBootstrapDto>(authService.currentUserSession(principal.payload.subject))
            }

            get("/me") {
                call.noStoreAuthResponse()
                val principal = call.authenticatedJwtPrincipal()
                call.respond(authService.currentUser(principal.payload.subject))
            }

            get("/me/invitations") {
                call.noStoreAuthResponse()
                val principal = call.authenticatedJwtPrincipal()
                call.respond(authService.currentUserInvitations(principal.payload.subject))
            }

            get("/me/events") {
                call.noStoreAuthResponse()
                val principal = call.authenticatedJwtPrincipal()
                call.respond(authService.currentUserEvents(principal.payload.subject))
            }

            get("/me/event-suggestions") {
                call.noStoreAuthResponse()
                val principal = call.authenticatedJwtPrincipal()
                call.respond(authService.suggestedEvents(principal.payload.subject))
            }

            post("/me/events") {
                call.noStoreAuthResponse()
                val principal = call.authenticatedJwtPrincipal()
                call.respond(authService.createEvent(principal.payload.subject, call.receive<EventCreateRequest>()))
            }

            put("/me/events/{eventId}/follow") {
                call.noStoreAuthResponse()
                val principal = call.authenticatedJwtPrincipal()
                val eventId = call.parameters["eventId"]
                    ?: throw IllegalArgumentException("Missing event id")
                authService.updateEventFollow(principal.payload.subject, eventId, call.receive<EventFollowRequest>())
                call.respond(HttpStatusCode.OK, mapOf("status" to "updated"))
            }

            post("/me/events/{eventId}/notices") {
                call.noStoreAuthResponse()
                val principal = call.authenticatedJwtPrincipal()
                val eventId = call.parameters["eventId"]
                    ?: throw IllegalArgumentException("Missing event id")
                authService.createEventNotice(principal.payload.subject, eventId, call.receive<EventNoticeCreateRequest>())
                call.respond(HttpStatusCode.Created, mapOf("status" to "created"))
            }

            patch("/me/venue-reservations/{reservationId}") {
                call.noStoreAuthResponse()
                val principal = call.requireAnyRole(AuthRole.STAFF, AuthRole.ADMIN)
                val reservationId = call.parameters["reservationId"]
                    ?: throw IllegalArgumentException("Missing reservation id")
                call.respond(
                    authService.reviewVenueReservation(
                        reviewerUserId = principal.payload.subject,
                        reservationId = reservationId,
                        request = call.receive<VenueReservationReviewRequest>(),
                    ),
                )
            }

            get("/me/active-stay") {
                call.noStoreAuthResponse()
                val principal = call.authenticatedJwtPrincipal()
                call.respond(AuthActiveStayResponseDto(authService.currentUserActiveStay(principal.payload.subject)))
            }

            patch("/me/invitations/{invitationId}") {
                call.noStoreAuthResponse()
                val principal = call.authenticatedJwtPrincipal()
                val invitationId = call.parameters["invitationId"]
                    ?: throw IllegalArgumentException("Missing invitation id")
                call.respond(authService.respondToInvitation(principal.payload.subject, invitationId, call.receive<AuthInvitationResponseRequest>()))
            }

            put("/me") {
                call.noStoreAuthResponse()
                val principal = call.authenticatedJwtPrincipal()
                call.respond(authService.updateProfile(principal.payload.subject, call.receive<AuthProfileUpdateRequest>()))
            }

            put("/me/password") {
                call.noStoreAuthResponse()
                val principal = call.authenticatedJwtPrincipal()
                call.respond(authService.setPassword(principal.payload.subject, call.receive<AuthSetPasswordRequest>()))
            }

            post("/logout") {
                call.noStoreAuthResponse()
                val principal = call.authenticatedJwtPrincipal()
                val request = runCatching { call.receiveNullable<AuthRefreshRequest>() }.getOrNull()
                authService.logout(request?.refreshToken, principal.payload)
                call.respond(AuthLogoutResponseDto())
            }
        }
    }
}

private fun ApplicationCall.authenticatedJwtPrincipal(): JWTPrincipal =
    principal<JWTPrincipal>()
        ?: throw IllegalArgumentException("Missing JWT principal")

private fun ApplicationCall.noStoreAuthResponse() {
    // Auth responses contain bearer/refresh tokens or private profile data. no-store prevents
    // browser, proxy, and CDN caches from persisting sensitive authentication material.
    if (response.headers[HttpHeaders.CacheControl] != null) return
    response.header(HttpHeaders.CacheControl, "no-store, no-cache, must-revalidate, max-age=0")
    response.header(HttpHeaders.Pragma, "no-cache")
    response.header(HttpHeaders.Expires, "0")
    response.header("Surrogate-Control", "no-store")
}
