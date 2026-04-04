package dev.orestegabo.kaze.platform

import platform.Foundation.NSUserDefaults

actual object PlatformServicesProvider {
    actual fun create(): PlatformServices = PlatformServices(
        roomKeyService = object : RoomKeyService {
            override suspend fun isAvailable(): Boolean = false

            override suspend fun provisionKey(hotelId: String, guestId: String): RoomKeyProvisionResult =
                RoomKeyProvisionResult(false, "ios-placeholder", "Wallet and room key integrations are not wired yet.")
        },
        qrScannerService = object : QrScannerService {
            override suspend fun scan(): QrScanResult? = null
        },
        hapticsService = object : HapticsService {
            override fun perform(effect: HapticEffect) = Unit
        },
        secureStore = object : SecureStore {
            private val defaults = NSUserDefaults.standardUserDefaults

            override suspend fun put(key: String, value: String) {
                defaults.setObject(value, forKey = key)
            }

            override suspend fun get(key: String): String? = defaults.stringForKey(key)

            override suspend fun remove(key: String) {
                defaults.removeObjectForKey(key)
            }
        },
    )
}
