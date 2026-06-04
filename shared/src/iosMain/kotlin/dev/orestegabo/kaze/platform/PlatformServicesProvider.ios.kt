package dev.orestegabo.kaze.platform

import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.KeychainSettings
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
        hapticsService = HapticServiceNoop,
        secureStore = IosKeychainSecureStore(),
    )
}

@OptIn(ExperimentalSettingsImplementation::class)
private class IosKeychainSecureStore : SecureStore {
    private val keychainSettings = KeychainSettings(service = KEYCHAIN_SERVICE)
    private val appPreferences = NSUserDefaults.standardUserDefaults

    override suspend fun put(key: String, value: String) {
        if (key.isAuthKey()) {
            keychainSettings.putString(key, value)
        } else {
            appPreferences.setObject(value, key)
        }
    }

    override suspend fun get(key: String): String? =
        if (key.isAuthKey()) {
            keychainSettings.getStringOrNull(key)
        } else {
            appPreferences.stringForKey(key)
        }

    override suspend fun remove(key: String) {
        if (key.isAuthKey()) {
            keychainSettings.remove(key)
        } else {
            appPreferences.removeObjectForKey(key)
        }
    }

    private fun String.isAuthKey(): Boolean = startsWith("auth.")
}

private object HapticServiceNoop : HapticsService {
    override fun perform(effect: HapticEffect) = Unit
}

private const val KEYCHAIN_SERVICE = "dev.orestegabo.kaze.secure-store"
