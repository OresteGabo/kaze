package dev.orestegabo.kaze.platform

interface RoomKeyService {
    suspend fun isAvailable(): Boolean
    suspend fun provisionKey(hotelId: String, guestId: String): RoomKeyProvisionResult
}

data class RoomKeyProvisionResult(
    val success: Boolean,
    val provider: String,
    val message: String? = null,
)
