package dev.orestegabo.kaze.platform

import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.KeychainSettings

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
        hapticsService = HapticServiceNoop,
        secureStore = IosKeychainSecureStore(),
    )
}

@OptIn(ExperimentalSettingsImplementation::class)
private class IosKeychainSecureStore : SecureStore {
    private val settings = KeychainSettings(service = KEYCHAIN_SERVICE)

    override suspend fun put(key: String, value: String) {
        settings.putString(key, value)
    }

    override suspend fun get(key: String): String? =
        settings.getStringOrNull(key)

    override suspend fun remove(key: String) {
        settings.remove(key)
    }
}

private object HapticServiceNoop : HapticsService {
    override fun perform(effect: HapticEffect) = Unit
}

private const val KEYCHAIN_SERVICE = "dev.orestegabo.kaze.secure-store"
