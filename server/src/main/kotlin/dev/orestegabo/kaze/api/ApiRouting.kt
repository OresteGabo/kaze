package dev.orestegabo.kaze.api

import dev.orestegabo.kaze.auth.AuthService
import dev.orestegabo.kaze.application.ReservationDraftSubmission
import dev.orestegabo.kaze.application.ServerDependencies
import dev.orestegabo.kaze.application.isProductionEnvironment
import io.ktor.http.HttpHeaders
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.AuthenticationStrategy
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.request.receive
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

internal fun Application.registerApiRoutes(
    dependencies: ServerDependencies,
    authService: AuthService,
) {
    routing {
        get("/") {
            call.respond(ApiInfoDto(name = "Kaze API", status = "running", version = "1.0.0"))
        }

        get("/health") {
            call.respond(ApiInfoDto(name = "Kaze API", status = "healthy", version = "1.0.0"))
        }

        if (!isProductionEnvironment()) {
            swaggerUI(path = "swagger", swaggerFile = "openapi/kaze-api.yaml")
        }

        rateLimit(ApiRateLimit) {
            route("/api/v1") {
                rateLimit(AuthRateLimit) {
                    registerAuthRoutes(authService)
                }
                registerPublicApiV1Routes(dependencies, authService)

                if (isApiAuthenticationEnabled() || isJwtAuthenticationRequired()) {
                    authenticate(ApiJwtAuth, ApiAuth, strategy = AuthenticationStrategy.FirstSuccessful) {
                        registerPrivateApiV1Routes(dependencies, authService)
                    }
                } else {
                    registerPrivateApiV1Routes(dependencies, authService)
                }
            }
        }
    }
}

private fun Route.registerPublicApiV1Routes(
    dependencies: ServerDependencies,
    authService: AuthService,
) {
    get {
        call.cachePublicJson()
        call.respond(ApiInfoDto(name = "Kaze API", status = "ready", version = "v1"))
    }

    get("/hotels") {
        call.cachePublicJson()
        call.respond(dependencies.hotelService.listHotels().map { it.toDto() })
    }

    get("/places") {
        call.cachePublicJson()
        call.respond(dependencies.placeService.listPlaces().map { it.toDto() })
    }

    get("/events") {
        call.cachePublicJson()
        call.respond(authService.publicEvents())
    }

    route("/hotels/{hotelId}") {
        get {
            call.cachePublicJson()
            val hotelId = call.requiredParam("hotelId")
            call.respond(dependencies.hotelService.getHotel(hotelId).toDto())
        }

        get("/events/days") {
            call.cachePublicJson()
            val hotelId = call.requiredParam("hotelId")
            call.respond(dependencies.experienceService.getEventDays(hotelId).map { it.toDto() })
        }

        get("/events/schedule") {
            call.cachePublicJson()
            val hotelId = call.requiredParam("hotelId")
            val dayId = call.requiredQuery("dayId")
            call.respond(dependencies.experienceService.getSchedule(hotelId, dayId).map { it.toDto() })
        }

        get("/explore/highlights") {
            call.cachePublicJson()
            val hotelId = call.requiredParam("hotelId")
            call.respond(dependencies.experienceService.getHighlights(hotelId).map { it.toDto() })
        }

        get("/amenities/status") {
            call.cachePublicJson(maxAgeSeconds = AMENITY_STATUS_CACHE_SECONDS)
            val hotelId = call.requiredParam("hotelId")
            call.respond(dependencies.assistantService.listAmenityStatusesCached(hotelId).map { it.toDto() })
        }
    }
}

private fun Route.registerPrivateApiV1Routes(
    dependencies: ServerDependencies,
    authService: AuthService,
) {
    post("/reservations") {
        call.noStore()
        val userId = call.authenticatedUserId()
        val request = call.receive<ReservationDraftSubmissionRequest>()
        call.respond(
            dependencies.reservationService.submitReservation(
                ReservationDraftSubmission(
                    organizerUserId = userId,
                    placeId = request.placeId,
                    serviceId = request.serviceId?.takeIf { it.isNotBlank() },
                    eventName = request.eventName.trim(),
                    preferredDateLabel = request.preferredDateLabel.trim(),
                    selectedRoom = request.selectedRoom?.trim()?.takeIf { it.isNotBlank() },
                    guestCount = request.guestCount,
                    packageLabel = request.packageLabel.trim(),
                    addOns = request.addOns.map { it.trim() }.filter { it.isNotBlank() },
                    paymentMethod = request.paymentMethod.trim(),
                    note = request.note?.trim()?.takeIf { it.isNotBlank() },
                ),
            ).toDto(),
        )
    }

    route("/hotels/{hotelId}") {
        get("/map") {
            call.noStore()
            val hotelId = call.requiredParam("hotelId")
            val mapId = call.request.queryParameters["mapId"]?.takeIf { it.isNotBlank() }
            call.respond(dependencies.mapService.getHotelMap(hotelId, mapId).toDto())
        }

        route("/guests/{guestId}") {
            get {
                call.noStore()
                val hotelId = call.requiredParam("hotelId")
                val guestId = call.requiredParam("guestId")
                call.requireGuestAccess(authService, hotelId, guestId)
                call.respond(dependencies.guestStayService.getGuest(hotelId, guestId).toDto())
            }

            get("/itinerary") {
                call.noStore()
                val hotelId = call.requiredParam("hotelId")
                val guestId = call.requiredParam("guestId")
                call.requireGuestAccess(authService, hotelId, guestId)
                call.respond(dependencies.guestStayService.getItinerary(hotelId, guestId).toDto())
            }

            get("/late-checkout") {
                call.noStore()
                val hotelId = call.requiredParam("hotelId")
                val guestId = call.requiredParam("guestId")
                call.requireGuestAccess(authService, hotelId, guestId)
                call.respond(dependencies.guestStayService.getLateCheckoutHistory(hotelId, guestId).map { it.toDto() })
            }

            post("/late-checkout") {
                call.noStore()
                val hotelId = call.requiredParam("hotelId")
                val guestId = call.requiredParam("guestId")
                call.requireGuestAccess(authService, hotelId, guestId)
                val request = call.receive<LateCheckoutSubmissionRequest>()
                call.respond(
                    dependencies.guestStayService.submitLateCheckout(
                        hotelId = hotelId,
                        guestId = guestId,
                        checkoutTimeIso = request.checkoutTimeIso,
                        feeAmountMinor = request.feeAmountMinor,
                        currencyCode = request.currencyCode,
                        paymentPreference = request.paymentPreference,
                        followUpPreference = request.followUpPreference,
                        notes = request.notes,
                        stayId = request.stayId,
                        roomId = request.roomId,
                    ).toDto(),
                )
            }

            get("/service-requests") {
                call.noStore()
                val hotelId = call.requiredParam("hotelId")
                val guestId = call.requiredParam("guestId")
                call.requireGuestAccess(authService, hotelId, guestId)
                call.respond(dependencies.guestStayService.getServiceRequestHistory(hotelId, guestId).map { it.toDto() })
            }

            post("/service-requests") {
                call.noStore()
                val hotelId = call.requiredParam("hotelId")
                val guestId = call.requiredParam("guestId")
                call.requireGuestAccess(authService, hotelId, guestId)
                val request = call.receive<ServiceRequestSubmissionRequest>()
                call.respond(
                    dependencies.guestStayService.submitServiceRequest(
                        hotelId = hotelId,
                        guestId = guestId,
                        type = request.type,
                        note = request.note,
                        stayId = request.stayId,
                        roomId = request.roomId,
                    ).toDto(),
                )
            }
        }

        post("/assistant/query") {
            call.noStore()
            val hotelId = call.requiredParam("hotelId")
            val request = call.receive<AssistantQueryRequest>()
            val answer = dependencies.assistantService.answer(hotelId, request.question)
            call.respond(
                AssistantAnswerDto(
                    answer = answer.answer,
                    source = answer.source,
                    confidence = answer.confidence,
                ),
            )
        }
    }
}

private fun io.ktor.server.application.ApplicationCall.requiredParam(name: String): String =
    parameters[name] ?: throw IllegalArgumentException("Missing path parameter: $name")

private fun io.ktor.server.application.ApplicationCall.requiredQuery(name: String): String =
    request.queryParameters[name] ?: throw IllegalArgumentException("Missing query parameter: $name")

private fun io.ktor.server.application.ApplicationCall.queryOrDefault(name: String, default: String): String =
    request.queryParameters[name] ?: default

private fun ApplicationCall.authenticatedUserId(): String =
    principal<JWTPrincipal>()?.payload?.subject?.takeIf { it.isNotBlank() }
        ?: principal<UserIdPrincipal>()?.name?.takeIf { it.isNotBlank() && it != "api-client" }
        ?: throw IllegalArgumentException("A signed-in user is required.")

private fun ApplicationCall.requireGuestAccess(authService: AuthService, hotelId: String, guestId: String) {
    val jwtUserId = principal<JWTPrincipal>()?.payload?.subject?.takeIf { it.isNotBlank() }
    if (jwtUserId != null) {
        // IDOR guard: a signed-in user may only access guest/stay records linked to their sub.
        // Staff/server API bearer access remains server-to-server and has no end-user sub claim.
        authService.requireGuestAccess(userId = jwtUserId, hotelId = hotelId, guestId = guestId)
        return
    }
    if (principal<UserIdPrincipal>()?.name == "api-client") return
    if (!application.isApiAuthenticationEnabled() && !application.isJwtAuthenticationRequired()) return
    throw IllegalArgumentException("A signed-in user is required.")
}

private fun ApplicationCall.cachePublicJson(maxAgeSeconds: Int = PUBLIC_JSON_CACHE_SECONDS) {
    response.header(
        HttpHeaders.CacheControl,
        "public, max-age=$maxAgeSeconds, stale-while-revalidate=$PUBLIC_JSON_STALE_SECONDS",
    )
}

private fun ApplicationCall.noStore() {
    response.header(HttpHeaders.CacheControl, "no-store, no-cache, must-revalidate, max-age=0")
    response.header(HttpHeaders.Pragma, "no-cache")
    response.header(HttpHeaders.Expires, "0")
    response.header("Surrogate-Control", "no-store")
}

private const val PUBLIC_JSON_CACHE_SECONDS = 120
private const val PUBLIC_JSON_STALE_SECONDS = 300
private const val AMENITY_STATUS_CACHE_SECONDS = 30
