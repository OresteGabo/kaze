package dev.orestegabo.kaze.platform

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

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
                private val crypto = AndroidSecureStoreCrypto()

                override suspend fun put(key: String, value: String) {
                    preferences.edit().putString(key, crypto.encrypt(value)).apply()
                }

                override suspend fun get(key: String): String? =
                    preferences.getString(key, null)?.let(crypto::decrypt)

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

private class AndroidSecureStoreCrypto {
    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    fun encrypt(value: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())
        return listOf(
            ENCRYPTED_VALUE_PREFIX,
            cipher.iv.base64(),
            cipher.doFinal(value.toByteArray(Charsets.UTF_8)).base64(),
        ).joinToString(":")
    }

    fun decrypt(storedValue: String): String {
        if (!storedValue.startsWith("$ENCRYPTED_VALUE_PREFIX:")) return storedValue
        val parts = storedValue.split(":")
        if (parts.size != 3) return ""
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            secretKey(),
            GCMParameterSpec(GCM_TAG_LENGTH_BITS, parts[1].fromBase64()),
        )
        return cipher.doFinal(parts[2].fromBase64()).toString(Charsets.UTF_8)
    }

    private fun secretKey(): SecretKey {
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build(),
        )
        return generator.generateKey()
    }

    private fun ByteArray.base64(): String =
        Base64.encodeToString(this, Base64.NO_WRAP)

    private fun String.fromBase64(): ByteArray =
        Base64.decode(this, Base64.NO_WRAP)

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "kaze_platform_store_key"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_TAG_LENGTH_BITS = 128
        const val ENCRYPTED_VALUE_PREFIX = "v1"
    }
}
