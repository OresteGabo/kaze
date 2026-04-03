package dev.orestegabo.kaze.presentation.map

import dev.orestegabo.kaze.domain.map.FloorLevel
import dev.orestegabo.kaze.domain.map.HotelMap

internal data class MapUiState(
    val hotelMap: HotelMap? = null,
    val activeRoute: String = "Guest arrival to Great Rift Ballroom",
    val selectedFloorId: String = "l1",
    val guestAccess: GuestAccessContext = sampleGuestAccess,
) {
    val floors: List<FloorLevel>
        get() = hotelMap?.floors.orEmpty()
}
