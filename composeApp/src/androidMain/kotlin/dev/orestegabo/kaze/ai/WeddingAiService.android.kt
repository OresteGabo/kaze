package dev.orestegabo.kaze.ai

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import dev.orestegabo.kaze.platform.AndroidApplicationContext

private const val GEMMA_MODEL_ASSET_NAME = "gemma.task"

internal actual fun createWeddingAiService(enabled: Boolean): WeddingAiService =
    AndroidWeddingAiService(
        enabled = enabled,
        context = AndroidApplicationContext.require(),
    )

internal class AndroidWeddingAiService(
    enabled: Boolean,
    private val context: Context,
) : WeddingAiService {
    private var inference: LlmInference? = null

    override val status: EdgeAiStatus = EdgeAiStatus(
        enabled = enabled,
        availableCapabilities = if (enabled) {
            setOf(
                EdgeAiCapability.KOOG_ORCHESTRATION,
                EdgeAiCapability.PERSON_SEGMENTATION,
                EdgeAiCapability.SUBJECT_SEGMENTATION,
            ) + if (hasBundledGemmaModel()) setOf(EdgeAiCapability.GEMMA_TEXT) else emptySet()
        } else {
            emptySet()
        },
        modelMessage = if (hasBundledGemmaModel()) {
            "Gemma model asset found. Local invitation copy can run on-device."
        } else {
            "Add a Gemma LiteRT/MediaPipe model at assets/$GEMMA_MODEL_ASSET_NAME to enable local generation."
        },
    )

    override suspend fun generateInvitationCopy(prompt: WeddingInvitationPrompt): WeddingInvitationCopy {
        if (!status.enabled || !status.isGemmaReady) {
            return LocalWeddingCopyFallback.generate(prompt)
        }
        return withContext(Dispatchers.Default) {
            runCatching {
                val model = inference ?: createInference().also { inference = it }
                val response = model.generateResponse(prompt.toGemmaInstruction())
                response.toWeddingCopyFallback(prompt)
            }.getOrElse {
                LocalWeddingCopyFallback.generate(prompt)
            }
        }
    }

    private fun createInference(): LlmInference {
        val options = LlmInference.LlmInferenceOptions.builder()
            .setModelPath(GEMMA_MODEL_ASSET_NAME)
            .setMaxTokens(512)
            .build()
        return LlmInference.createFromOptions(context, options)
    }

    private fun hasBundledGemmaModel(): Boolean =
        runCatching { context.assets.open(GEMMA_MODEL_ASSET_NAME).close() }.isSuccess
}

private fun WeddingInvitationPrompt.toGemmaInstruction(): String = buildString {
    appendLine("Generate wedding invitation copy for a Rwanda-first mobile app.")
    appendLine("Return four short sections separated by new lines:")
    appendLine("Headline:")
    appendLine("Body:")
    appendLine("WhatsApp:")
    appendLine("Reminder:")
    appendLine("Language: ${language.label}")
    appendLine("Tone: ${tone.label}")
    appendLine("Couple: $coupleNames")
    appendLine("Date: $weddingDate")
    appendLine("Venue: $venueName")
    if (extraNotes.isNotBlank()) {
        appendLine("Notes: $extraNotes")
    }
}

private fun String.toWeddingCopyFallback(prompt: WeddingInvitationPrompt): WeddingInvitationCopy {
    val fallback = LocalWeddingCopyFallback.generate(prompt)
    fun section(name: String): String =
        lineSequence()
            .firstOrNull { it.trim().startsWith("$name:", ignoreCase = true) }
            ?.substringAfter(":")
            ?.trim()
            .orEmpty()

    return WeddingInvitationCopy(
        headline = section("Headline").ifBlank { fallback.headline },
        body = section("Body").ifBlank { fallback.body },
        whatsappMessage = section("WhatsApp").ifBlank { fallback.whatsappMessage },
        rsvpReminder = section("Reminder").ifBlank { fallback.rsvpReminder },
    )
}
