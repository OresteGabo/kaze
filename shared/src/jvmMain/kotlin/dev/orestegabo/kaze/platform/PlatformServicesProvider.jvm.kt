package dev.orestegabo.kaze.platform

actual object PlatformServicesProvider {
    actual fun create(): PlatformServices = PlatformServices(
        roomKeyService = object : RoomKeyService {
            override suspend fun isAvailable(): Boolean = false

            override suspend fun provisionKey(hotelId: String, guestId: String): RoomKeyProvisionResult =
                RoomKeyProvisionResult(false, "jvm-placeholder")
        },
        qrScannerService = object : QrScannerService {
            override suspend fun scan(): QrScanResult? = null
        },
        hapticsService = object : HapticsService {
            override fun perform(effect: HapticEffect) = Unit
        },
        secureStore = object : SecureStore {
            private val values = mutableMapOf<String, String>()

            override suspend fun put(key: String, value: String) {
                values[key] = value
            }

            override suspend fun get(key: String): String? = values[key]

            override suspend fun remove(key: String) {
                values.remove(key)
            }
        },
    )
}
