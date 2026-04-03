package dev.orestegabo.kaze.usecase

import dev.orestegabo.kaze.data.repository.HotelRepository
import dev.orestegabo.kaze.domain.Hotel

class ObserveHotelContextUseCase(
    private val hotelRepository: HotelRepository,
) {
    suspend operator fun invoke(hotelId: String): Hotel =
        hotelRepository.requireHotel(hotelId)
}
