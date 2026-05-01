package dev.orestegabo.kaze.models

import kotlinx.serialization.Serializable

@Serializable
data class EmailRequest(
    val to: String,
    val subject: String,
    val htmlBody: String
)