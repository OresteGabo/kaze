package dev.orestegabo.kaze.data.repository

import dev.orestegabo.kaze.domain.Hotel

interface HotelRepository {
    suspend fun getHotel(hotelId: String): Hotel?
    suspend fun requireHotel(hotelId: String): Hotel
}
