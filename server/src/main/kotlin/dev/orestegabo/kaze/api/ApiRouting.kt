package dev.orestegabo.kaze.api

import dev.orestegabo.kaze.auth.AuthService
import dev.orestegabo.kaze.application.ServerDependencies
import io.ktor.server.application.Application
import io.ktor.server.auth.AuthenticationStrategy
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.request.receive
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

        swaggerUI(path = "swagger", swaggerFile = "openapi/kaze-api.yaml")

        rateLimit(ApiRateLimit) {
            route("/api/v1") {
                registerAuthRoutes(authService)
                registerPublicApiV1Routes(dependencies)

                if (isApiAuthenticationEnabled() || isJwtAuthenticationRequired()) {
                    authenticate(ApiJwtAuth, ApiAuth, strategy = AuthenticationStrategy.FirstSuccessful) {
                        registerPrivateApiV1Routes(dependencies)
                    }
                } else {
                    registerPrivateApiV1Routes(dependencies)
                }
            }
        }
    }
}

private fun Route.registerPublicApiV1Routes(
    dependencies: ServerDependencies,
) {
    get {
        call.respond(ApiInfoDto(name = "Kaze API", status = "ready", version = "v1"))
    }

    get("/hotels") {
        call.respond(dependencies.hotelService.listHotels().map { it.toDto() })
    }

    route("/hotels/{hotelId}") {
        get {
            val hotelId = call.requiredParam("hotelId")
            call.respond(dependencies.hotelService.getHotel(hotelId).toDto())
        }

        get("/events/days") {
            val hotelId = call.requiredParam("hotelId")
            call.respond(dependencies.experienceService.getEventDays(hotelId).map { it.toDto() })
        }

        get("/events/schedule") {
            val hotelId = call.requiredParam("hotelId")
            val dayId = call.requiredQuery("dayId")
            call.respond(dependencies.experienceService.getSchedule(hotelId, dayId).map { it.toDto() })
        }

        get("/explore/highlights") {
            val hotelId = call.requiredParam("hotelId")
            call.respond(dependencies.experienceService.getHighlights(hotelId).map { it.toDto() })
        }

        get("/amenities/status") {
            val hotelId = call.requiredParam("hotelId")
            call.respond(dependencies.assistantService.listAmenityStatuses(hotelId).map { it.toDto() })
        }
    }
}

private fun Route.registerPrivateApiV1Routes(
    dependencies: ServerDependencies,
) {
    route("/hotels/{hotelId}") {
        get("/map") {
            val hotelId = call.requiredParam("hotelId")
            val mapId = call.queryOrDefault("mapId", "temporary-svg-venue")
            call.respond(dependencies.mapService.getHotelMap(hotelId, mapId).toDto())
        }

        route("/guests/{guestId}") {
            get {
                val hotelId = call.requiredParam("hotelId")
                val guestId = call.requiredParam("guestId")
                call.respond(dependencies.guestStayService.getGuest(hotelId, guestId).toDto())
            }

            get("/itinerary") {
                val hotelId = call.requiredParam("hotelId")
                val guestId = call.requiredParam("guestId")
                call.respond(dependencies.guestStayService.getItinerary(hotelId, guestId).toDto())
            }

            get("/late-checkout") {
                val hotelId = call.requiredParam("hotelId")
                val guestId = call.requiredParam("guestId")
                call.respond(dependencies.guestStayService.getLateCheckoutHistory(hotelId, guestId).map { it.toDto() })
            }

            post("/late-checkout") {
                val hotelId = call.requiredParam("hotelId")
                val guestId = call.requiredParam("guestId")
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
                val hotelId = call.requiredParam("hotelId")
                val guestId = call.requiredParam("guestId")
                call.respond(dependencies.guestStayService.getServiceRequestHistory(hotelId, guestId).map { it.toDto() })
            }

            post("/service-requests") {
                val hotelId = call.requiredParam("hotelId")
                val guestId = call.requiredParam("guestId")
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
