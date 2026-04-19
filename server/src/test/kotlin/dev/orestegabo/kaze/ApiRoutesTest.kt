package dev.orestegabo.kaze

import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.request.header
import io.ktor.client.request.options
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApiRoutesTest {

    @Test
    fun health_endpoint_returns_healthy_status() = testApplication {
        application { module() }

        val response = client.get("/health")

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("\"status\": \"healthy\""))
    }

    @Test
    fun swagger_docs_are_available_from_backend() = testApplication {
        application { module() }

        val response = client.get("/swagger")

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("Swagger UI"))
    }

    @Test
    fun get_routes_support_head_requests() = testApplication {
        application { module() }

        val response = client.head("/health")

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun cors_preflight_allows_configured_web_hosts() = testApplication {
        application { module() }

        val response = client.options("/api/v1") {
            header(HttpHeaders.Origin, "http://localhost:5173")
            header(HttpHeaders.AccessControlRequestMethod, HttpMethod.Get.value)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("http://localhost:5173", response.headers[HttpHeaders.AccessControlAllowOrigin])
    }

    @Test
    fun hotel_endpoint_returns_seeded_hotel() = testApplication {
        application { module() }

        val response = client.get("/api/v1/hotels/rw-kgl-marriott")

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("\"displayName\": \"Kigali Marriott\""))
    }

    @Test
    fun itinerary_endpoint_returns_guest_schedule() = testApplication {
        application { module() }

        val response = client.get("/api/v1/hotels/rw-kgl-marriott/guests/guest_aline/itinerary")

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("\"title\": \"Opening keynote\""))
    }

    @Test
    fun late_checkout_endpoint_accepts_submission() = testApplication {
        application { module() }

        val response = client.post("/api/v1/hotels/rw-kgl-marriott/guests/guest_aline/late-checkout") {
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
    fun late_checkout_validation_rejects_invalid_submission() = testApplication {
        application { module() }

        val response = client.post("/api/v1/hotels/rw-kgl-marriott/guests/guest_aline/late-checkout") {
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
    fun api_routes_require_bearer_token_when_configured() = testApplication {
        environment {
            config = MapApplicationConfig("kaze.security.apiToken" to "test-token")
        }
        application { module() }

        val unauthorizedResponse = client.get("/api/v1")
        val authorizedResponse = client.get("/api/v1") {
            header(HttpHeaders.Authorization, "Bearer test-token")
        }

        assertEquals(HttpStatusCode.Unauthorized, unauthorizedResponse.status)
        assertEquals(HttpStatusCode.OK, authorizedResponse.status)
    }

    @Test
    fun assistant_query_returns_structured_kitchen_answer() = testApplication {
        application { module() }

        val response = client.post("/api/v1/hotels/rw-kgl-marriott/assistant/query") {
            contentType(ContentType.Application.Json)
            setBody("""{"question":"Is the kitchen open?"}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("Hotel kitchen is open right now"))
    }

    @Test
    fun api_routes_are_rate_limited_per_client() = testApplication {
        application { module() }

        val responses = (1..121).map {
            client.get("/api/v1") {
                header("X-Forwarded-For", "203.0.113.77")
            }
        }

        assertEquals(HttpStatusCode.OK, responses.first().status)
        assertEquals(HttpStatusCode.TooManyRequests, responses.last().status)
    }

    @Test
    fun api_responses_are_gzip_compressed_when_supported() = testApplication {
        application { module() }

        val response = client.get("/api/v1/hotels/rw-kgl-marriott") {
            header(HttpHeaders.AcceptEncoding, "gzip")
            header("X-Forwarded-For", "203.0.113.88")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("gzip", response.headers[HttpHeaders.ContentEncoding])
    }
}
