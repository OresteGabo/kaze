package dev.orestegabo.kaze.presentation.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dev.orestegabo.kaze.data.repository.MapRepository
import dev.orestegabo.kaze.presentation.navigation.MapNavigationTarget
import dev.orestegabo.kaze.presentation.util.runImmediateSuspend

internal class MapViewModel(
    private val hotelId: String,
    private val mapId: String,
    private val mapRepository: MapRepository,
) : ViewModel() {
    var uiState by mutableStateOf(MapUiState())
        private set

    init {
        uiState = uiState.copy(
            hotelMap = runImmediateSuspend { mapRepository.getHotelMap(hotelId, mapId) },
        )
    }

    fun applyNavigationTarget(target: MapNavigationTarget) {
        uiState = uiState.copy(
            activeRoute = target.route,
            selectedFloorId = target.floorId,
        )
    }

    fun onFloorSelected(floorId: String) {
        uiState = uiState.copy(selectedFloorId = floorId)
    }

    fun onSwitchFloor() {
        val floors = uiState.floors
        if (floors.isEmpty()) return
        val currentIndex = floors.indexOfFirst { it.id == uiState.selectedFloorId }.coerceAtLeast(0)
        val nextFloor = floors[(currentIndex + 1) % floors.size]
        uiState = uiState.copy(selectedFloorId = nextFloor.id)
    }
}
