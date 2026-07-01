package dev.orestegabo.kaze.ai

internal actual fun createImageSegmentationService(): ImageSegmentationService =
    NoopImageSegmentationService()
