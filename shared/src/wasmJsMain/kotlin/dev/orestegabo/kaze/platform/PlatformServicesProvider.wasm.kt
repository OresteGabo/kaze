package dev.orestegabo.kaze.platform

import kotlinx.browser.localStorage

actual object PlatformServicesProvider {
    actual fun create(): PlatformServices = PlatformServices(
        roomKeyService = object : RoomKeyService {
            override suspend fun isAvailable(): Boolean = false

            override suspend fun provisionKey(hotelId: String, guestId: String): RoomKeyProvisionResult =
                RoomKeyProvisionResult(false, "wasm-placeholder")
        },
        qrScannerService = object : QrScannerService {
            override suspend fun scan(): QrScanResult? = null
        },
        hapticsService = object : HapticsService {
            override fun perform(effect: HapticEffect) = Unit
        },
        secureStore = object : SecureStore {
            override suspend fun put(key: String, value: String) {
                localStorage.setItem(key, value)
            }

            override suspend fun get(key: String): String? = localStorage.getItem(key)

            override suspend fun remove(key: String) {
                localStorage.removeItem(key)
            }
        },
    )
}
