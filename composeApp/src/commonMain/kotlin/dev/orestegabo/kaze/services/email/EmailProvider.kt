package dev.orestegabo.kaze.services.email

sealed class EmailResult {
    object Success : EmailResult()
    object LimitReached : EmailResult()
    data class Failure(val error: String) : EmailResult()
}

interface EmailProvider {
    val name: String
    suspend fun send(to: String, subject: String, body: String): EmailResult
}