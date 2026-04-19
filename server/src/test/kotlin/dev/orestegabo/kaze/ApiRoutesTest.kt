package dev.orestegabo.kaze

import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
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
}
