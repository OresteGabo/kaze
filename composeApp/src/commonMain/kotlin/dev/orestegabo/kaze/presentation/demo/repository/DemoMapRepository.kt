package dev.orestegabo.kaze.presentation.demo.repository

import dev.orestegabo.kaze.data.repository.MapRepository
import dev.orestegabo.kaze.domain.map.HotelMap
import dev.orestegabo.kaze.domain.map.sampleMarriottConventionMap
import dev.orestegabo.kaze.domain.map.importing.HotelMapSourceManifest
import dev.orestegabo.kaze.domain.map.importing.TenantScopedImportRequest

internal class DemoMapRepository : MapRepository {
    private var currentMap: HotelMap = sampleMarriottConventionMap

    override suspend fun getHotelMap(hotelId: String, mapId: String): HotelMap? =
        currentMap.takeIf { it.hotelId == hotelId && it.mapId == mapId }

    override suspend fun saveHotelMap(map: HotelMap) {
        currentMap = map
    }

    override suspend fun importHotelMap(
        manifest: HotelMapSourceManifest,
        requests: List<TenantScopedImportRequest>,
    ): HotelMap = currentMap.copy(sourceManifest = manifest)
}
