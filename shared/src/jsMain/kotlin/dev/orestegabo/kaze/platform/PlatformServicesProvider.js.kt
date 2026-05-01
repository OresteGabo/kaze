package dev.orestegabo.kaze.platform

import kotlinx.browser.localStorage
import kotlinx.browser.sessionStorage

actual object PlatformServicesProvider {
    actual fun create(): PlatformServices = PlatformServices(
        roomKeyService = object : RoomKeyService {
            override suspend fun isAvailable(): Boolean = false

            override suspend fun provisionKey(hotelId: String, guestId: String): RoomKeyProvisionResult =
                RoomKeyProvisionResult(false, "js-placeholder")
        },
        qrScannerService = object : QrScannerService {
            override suspend fun scan(): QrScanResult? = null
        },
        hapticsService = object : HapticsService {
            override fun perform(effect: HapticEffect) = Unit
        },
        secureStore = object : SecureStore {
            override suspend fun put(key: String, value: String) {
                storageFor(key).setItem(key, value)
            }

            override suspend fun get(key: String): String? = storageFor(key).getItem(key)

            override suspend fun remove(key: String) {
                storageFor(key).removeItem(key)
            }
        },
    )
}

private fun storageFor(key: String) =
    if (key.startsWith("auth.")) sessionStorage else localStorage
