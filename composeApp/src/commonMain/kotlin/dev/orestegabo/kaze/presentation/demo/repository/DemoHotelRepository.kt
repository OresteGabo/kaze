package dev.orestegabo.kaze.presentation.demo.repository

import dev.orestegabo.kaze.data.repository.HotelRepository
import dev.orestegabo.kaze.domain.Hotel
import dev.orestegabo.kaze.presentation.demo.sampleHotel

internal class DemoHotelRepository : HotelRepository {
    override suspend fun getHotel(hotelId: String): Hotel? =
        if (hotelId == sampleHotel.id) sampleHotel else null

    override suspend fun requireHotel(hotelId: String): Hotel =
        getHotel(hotelId) ?: error("Unknown hotel id: $hotelId")
}
