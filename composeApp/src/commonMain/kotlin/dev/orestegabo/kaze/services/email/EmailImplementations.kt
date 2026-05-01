package dev.orestegabo.kaze.services.email

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*

class BrevoProvider(private val client: HttpClient, private val apiKey: String) : EmailProvider {
    override val name = "Brevo"

    override suspend fun send(to: String, subject: String, body: String): EmailResult {
        return try {
            val response = client.post("https://api.brevo.com/v3/smtp/email") {
                header("api-key", apiKey)
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "sender" to mapOf("name" to "Kaze App", "email" to "noreply@kaze.dev"),
                    "to" to listOf(mapOf("email" to to)),
                    "subject" to subject,
                    "htmlContent" to body
                ))
            }
            if (response.status == HttpStatusCode.TooManyRequests) EmailResult.LimitReached
            else if (response.status.isSuccess()) EmailResult.Success
            else EmailResult.Failure("Status: ${response.status}")
        } catch (e: Exception) {
            EmailResult.Failure(e.message ?: "Unknown Error")
        }
    }
}

class ResendProvider(private val client: HttpClient, private val apiKey: String) : EmailProvider {
    override val name = "Resend"

    override suspend fun send(to: String, subject: String, body: String): EmailResult {
        return try {
            val response = client.post("https://api.resend.com/emails") {
                header("Authorization", "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "from" to "Kaze App <onboarding@resend.dev>",
                    "to" to listOf(to),
                    "subject" to subject,
                    "html" to body
                ))
            }
            if (response.status.isSuccess()) EmailResult.Success else EmailResult.Failure("Error")
        } catch (e: Exception) {
            EmailResult.Failure(e.message ?: "Unknown Error")
        }
    }
}