package dev.orestegabo.kaze.platform

data class PlatformServices(
    val roomKeyService: RoomKeyService,
    val qrScannerService: QrScannerService,
    val hapticsService: HapticsService,
    val secureStore: SecureStore,
)

expect object PlatformServicesProvider {
    fun create(): PlatformServices
}
