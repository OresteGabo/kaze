package dev.orestegabo.kaze.ai

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
import kotlinx.coroutines.tasks.await

internal actual fun createImageSegmentationService(): ImageSegmentationService =
    AndroidImageSegmentationService()

internal class AndroidImageSegmentationService : ImageSegmentationService {
    private val personSegmenter by lazy {
        val options = SelfieSegmenterOptions.Builder()
            .setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE)
            .enableRawSizeMask()
            .build()
        Segmentation.getClient(options)
    }

    override val isAvailable: Boolean = true

    suspend fun segmentPerson(bitmap: Bitmap): SegmentedImageResult {
        val input = InputImage.fromBitmap(bitmap, 0)
        val mask = personSegmenter.process(input).await()
        val buffer = mask.buffer
        buffer.rewind()
        val confidence = FloatArray(mask.width * mask.height)
        var index = 0
        while (buffer.hasRemaining() && index < confidence.size) {
            confidence[index] = buffer.float
            index += 1
        }
        return SegmentedImageResult(
            width = mask.width,
            height = mask.height,
            foregroundConfidence = confidence,
        )
    }
}
