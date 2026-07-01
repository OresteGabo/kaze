package dev.orestegabo.kaze.ai

internal enum class ImageSegmentationMode {
    PERSON,
    SUBJECT,
}

internal data class SegmentedImageResult(
    val width: Int,
    val height: Int,
    val foregroundConfidence: FloatArray,
) {
    init {
        require(foregroundConfidence.size == width * height) {
            "Foreground confidence mask must contain width * height values."
        }
    }

    override fun equals(other: Any?): Boolean =
        other is SegmentedImageResult &&
            width == other.width &&
            height == other.height &&
            foregroundConfidence.contentEquals(other.foregroundConfidence)

    override fun hashCode(): Int {
        var result = width
        result = 31 * result + height
        result = 31 * result + foregroundConfidence.contentHashCode()
        return result
    }
}

internal interface ImageSegmentationService {
    val isAvailable: Boolean
}

internal class NoopImageSegmentationService : ImageSegmentationService {
    override val isAvailable: Boolean = false
}

internal expect fun createImageSegmentationService(): ImageSegmentationService
