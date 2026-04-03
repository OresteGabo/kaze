package dev.orestegabo.kaze.platform

interface QrScannerService {
    suspend fun scan(): QrScanResult?
}

data class QrScanResult(
    val rawValue: String,
    val format: String,
)
