package dev.orestegabo.kaze.data.repository

import dev.orestegabo.kaze.domain.map.HotelMap
import dev.orestegabo.kaze.domain.map.importing.HotelMapSourceManifest
import dev.orestegabo.kaze.domain.map.importing.TenantScopedImportRequest

interface MapRepository {
    suspend fun getHotelMap(hotelId: String, mapId: String): HotelMap?
    suspend fun saveHotelMap(map: HotelMap)
    suspend fun importHotelMap(
        manifest: HotelMapSourceManifest,
        requests: List<TenantScopedImportRequest>,
    ): HotelMap
}
