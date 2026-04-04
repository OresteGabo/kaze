package dev.orestegabo.kaze.platform

import java.util.prefs.Preferences

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
            private val preferences = Preferences.userRoot().node("dev.orestegabo.kaze")

            override suspend fun put(key: String, value: String) {
                preferences.put(key, value)
            }

            override suspend fun get(key: String): String? = preferences.get(key, null)

            override suspend fun remove(key: String) {
                preferences.remove(key)
            }
        },
    )
}
