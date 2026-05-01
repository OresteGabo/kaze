package dev.orestegabo.kaze

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.request.header
import io.ktor.client.request.options
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import java.time.Instant
import java.util.Date

class ApiRoutesTest {

    @Test
    fun health_endpoint_returns_healthy_status() = kazeTestApplication {

        val response = client.get("/health")

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("\"status\": \"healthy\""))
    }

    @Test
    fun swagger_docs_are_available_from_backend() = kazeTestApplication {

        val response = client.get("/swagger")

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("Swagger UI"))
    }

    @Test
    fun get_routes_support_head_requests() = kazeTestApplication {

        val response = client.head("/health")

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun cors_preflight_allows_configured_web_hosts() = kazeTestApplication {

        val response = client.options("/api/v1") {
            header(HttpHeaders.Origin, "http://localhost:5173")
            header(HttpHeaders.AccessControlRequestMethod, HttpMethod.Get.value)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("http://localhost:5173", response.headers[HttpHeaders.AccessControlAllowOrigin])
    }

    @Test
    fun hotel_endpoint_returns_seeded_hotel() = kazeTestApplication {

        val response = client.get("/api/v1/hotels/rw-kgl-marriott")

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("\"displayName\": \"Kigali Marriott\""))
    }

    @Test
    fun itinerary_endpoint_returns_guest_schedule() = kazeTestApplication {

        val response = client.get("/api/v1/hotels/rw-kgl-marriott/guests/guest_jean_paul/itinerary")

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("\"title\": \"Registration and badge pickup\""))
    }

    @Test
    fun late_checkout_endpoint_accepts_submission() = kazeTestApplication {

        val response = client.post("/api/v1/hotels/rw-kgl-marriott/guests/guest_ange/late-checkout") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                  "checkoutTimeIso": "2026-04-06T14:00:00Z",
                  "feeAmountMinor": 55000,
                  "currencyCode": "RWF",
                  "paymentPreference": "CHARGE_TO_ROOM",
                  "followUpPreference": "CONFIRM_IN_APP",
                  "notes": "Flight departs late"
                }
                """.trimIndent(),
            )
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("\"status\": \"PENDING\""))
    }

    @Test
    fun late_checkout_validation_rejects_invalid_submission() = kazeTestApplication {

        val response = client.post("/api/v1/hotels/rw-kgl-marriott/guests/guest_ange/late-checkout") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                  "checkoutTimeIso": "",
                  "feeAmountMinor": -1,
                  "currencyCode": "RWF",
                  "paymentPreference": "CHARGE_TO_ROOM",
                  "followUpPreference": "CONFIRM_IN_APP"
                }
                """.trimIndent(),
            )
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("validation_error"))
    }

    @Test
    fun api_routes_require_bearer_token_when_configured() = kazeTestApplication(
        "kaze.security.apiToken" to "test-token",
    ) {

        val unauthorizedResponse = client.get("/api/v1/hotels/rw-kgl-marriott/guests/guest_jean_paul/itinerary")
        val authorizedResponse = client.get("/api/v1/hotels/rw-kgl-marriott/guests/guest_jean_paul/itinerary") {
            header(HttpHeaders.Authorization, "Bearer test-token")
        }

        assertEquals(HttpStatusCode.Unauthorized, unauthorizedResponse.status)
        assertEquals(HttpStatusCode.OK, authorizedResponse.status)
    }

    @Test
    fun api_routes_accept_jwt_when_required() = kazeTestApplication(
        "kaze.security.jwt.requireForApi" to "true",
        "kaze.security.jwt.secret" to TEST_JWT_SECRET,
    ) {

        val unauthorizedResponse = client.get("/api/v1/hotels/rw-kgl-marriott/guests/guest_jean_paul/itinerary")
        val authorizedResponse = client.get("/api/v1/hotels/rw-kgl-marriott/guests/guest_jean_paul/itinerary") {
            header(HttpHeaders.Authorization, "Bearer ${testJwt()}")
        }

        assertEquals(HttpStatusCode.Unauthorized, unauthorizedResponse.status)
        assertEquals(HttpStatusCode.OK, authorizedResponse.status)
    }

    @Test
    fun guest_mode_can_see_basic_public_info_when_jwt_is_required() = kazeTestApplication(
        "kaze.security.jwt.requireForApi" to "true",
        "kaze.security.jwt.secret" to TEST_JWT_SECRET,
    ) {

        val apiResponse = client.get("/api/v1")
        val hotelsResponse = client.get("/api/v1/hotels")
        val hotelResponse = client.get("/api/v1/hotels/rw-kgl-marriott")
        val eventDaysResponse = client.get("/api/v1/hotels/rw-kgl-marriott/events/days")
        val itineraryResponse = client.get("/api/v1/hotels/rw-kgl-marriott/guests/guest_jean_paul/itinerary")

        assertEquals(HttpStatusCode.OK, apiResponse.status)
        assertEquals(HttpStatusCode.OK, hotelsResponse.status)
        assertEquals(HttpStatusCode.OK, hotelResponse.status)
        assertEquals(HttpStatusCode.OK, eventDaysResponse.status)
        assertEquals(HttpStatusCode.Unauthorized, itineraryResponse.status)
    }

    @Test
    fun auth_me_returns_jwt_claims() = kazeTestApplication(
        "kaze.security.jwt.secret" to TEST_JWT_SECRET,
    ) {

        val response = client.get("/api/v1/auth/me") {
            header(HttpHeaders.Authorization, "Bearer ${testJwt()}")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("\"email\": \"ange.uwase@kaze.dev\""))
        assertTrue(response.bodyAsText().contains("CUSTOMER"))
        assertTrue(response.bodyAsText().contains("\"privacyConsent\""))
    }

    @Test
    fun auth_profile_update_persists_privacy_consent() = kazeTestApplication(
        "kaze.security.jwt.secret" to TEST_JWT_SECRET,
    ) {

        val response = client.put("/api/v1/auth/me") {
            header(HttpHeaders.Authorization, "Bearer ${testJwt()}")
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                  "displayName": "Ange Uwase",
                  "username": "ange.uwase",
                  "phoneNumber": "+250788123456",
                  "privacyConsent": {
                    "mapAndVenueActivityEnabled": false,
                    "diagnosticsEnabled": true,
                    "notificationsEnabled": false,
                    "analyticsEnabled": true
                  }
                }
                """.trimIndent(),
            )
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("\"mapAndVenueActivityEnabled\": false"))
        assertTrue(response.bodyAsText().contains("\"notificationsEnabled\": false"))
        assertTrue(response.bodyAsText().contains("\"analyticsEnabled\": true"))
    }

    @Test
    fun auth_logout_accepts_jwt_and_returns_logout_instruction() = kazeTestApplication(
        "kaze.security.jwt.secret" to TEST_JWT_SECRET,
    ) {

        val response = client.post("/api/v1/auth/logout") {
            header(HttpHeaders.Authorization, "Bearer ${testJwt()}")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("signed_out"))
    }

    @Test
    fun google_signin_requires_client_id_configuration() = kazeTestApplication {

        val response = client.post("/api/v1/auth/google") {
            contentType(ContentType.Application.Json)
            setBody("""{"idToken":"not-a-real-token"}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("google_auth_not_configured"))
    }

    @Test
    fun facebook_signin_requires_configuration() = kazeTestApplication {

        val response = client.post("/api/v1/auth/facebook") {
            contentType(ContentType.Application.Json)
            setBody("""{"accessToken":"not-a-real-token"}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("facebook_auth_not_configured"))
    }

    @Test
    fun social_oauth_start_rejects_unconfigured_provider() = kazeTestApplication {

        val response = client.get("/api/v1/auth/google/start")

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("google_auth_not_configured"))
    }

    @Test
    fun social_oauth_start_rejects_unknown_provider() = kazeTestApplication {

        val response = client.get("/api/v1/auth/github/start")

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("unsupported_auth_provider"))
    }

    @Test
    fun refresh_requires_refresh_token() = kazeTestApplication {

        val response = client.post("/api/v1/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody("""{"refreshToken":""}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("validation_error"))
    }

    @Test
    fun assistant_query_returns_structured_kitchen_answer() = kazeTestApplication {

        val response = client.post("/api/v1/hotels/rw-kgl-marriott/assistant/query") {
            contentType(ContentType.Application.Json)
            setBody("""{"question":"Is the kitchen open?"}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("Hotel kitchen is open right now"))
    }

    @Test
    fun api_routes_are_rate_limited_per_client() = kazeTestApplication {

        val responses = (1..121).map {
            client.get("/api/v1") {
                header("X-Forwarded-For", "203.0.113.77")
            }
        }

        assertEquals(HttpStatusCode.OK, responses.first().status)
        assertEquals(HttpStatusCode.TooManyRequests, responses.last().status)
    }

    @Test
    fun api_responses_are_gzip_compressed_when_supported() = kazeTestApplication {

        val response = client.get("/api/v1/hotels/rw-kgl-marriott") {
            header(HttpHeaders.AcceptEncoding, "gzip")
            header("X-Forwarded-For", "203.0.113.88")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("gzip", response.headers[HttpHeaders.ContentEncoding])
    }

    private fun testJwt(): String {
        val now = Instant.now()
        return JWT.create()
            .withIssuer("kaze-api")
            .withAudience("kaze-mobile")
            .withSubject("user_ange_uwase")
            .withClaim("email", "ange.uwase@kaze.dev")
            .withClaim("roles", listOf("CUSTOMER"))
            .withIssuedAt(Date.from(now))
            .withExpiresAt(Date.from(now.plusSeconds(3600)))
            .sign(Algorithm.HMAC256(TEST_JWT_SECRET))
    }

    private companion object {
        const val TEST_JWT_SECRET = "test-jwt-secret-that-is-long-enough-for-hmac"
    }
}
