package dev.orestegabo.kaze.platform

import android.content.Context

actual object PlatformServicesProvider {
    private const val STORE_NAME = "kaze_platform_store"
    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    actual fun create(): PlatformServices = PlatformServices(
        roomKeyService = object : RoomKeyService {
            override suspend fun isAvailable(): Boolean = false

            override suspend fun provisionKey(hotelId: String, guestId: String): RoomKeyProvisionResult =
                RoomKeyProvisionResult(false, "android-placeholder", "Room keys are not provisioned in demo mode.")
        },
        qrScannerService = object : QrScannerService {
            override suspend fun scan(): QrScanResult? = null
        },
        hapticsService = object : HapticsService {
            override fun perform(effect: HapticEffect) = Unit
        },
        secureStore = appContext?.let { context ->
            object : SecureStore {
                private val preferences = context.getSharedPreferences(STORE_NAME, Context.MODE_PRIVATE)

                override suspend fun put(key: String, value: String) {
                    preferences.edit().putString(key, value).apply()
                }

                override suspend fun get(key: String): String? = preferences.getString(key, null)

                override suspend fun remove(key: String) {
                    preferences.edit().remove(key).apply()
                }
            }
        } ?: object : SecureStore {
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
