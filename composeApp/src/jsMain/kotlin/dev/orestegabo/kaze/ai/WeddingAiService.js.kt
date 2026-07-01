package dev.orestegabo.kaze.ai

internal actual fun createWeddingAiService(enabled: Boolean): WeddingAiService =
    NoopWeddingAiService(enabled)
