package dev.orestegabo.kaze.usecase

import dev.orestegabo.kaze.data.repository.MapRepository
import dev.orestegabo.kaze.domain.map.HotelMap
import dev.orestegabo.kaze.domain.map.importing.HotelMapSourceManifest
import dev.orestegabo.kaze.domain.map.importing.TenantScopedImportRequest

class ImportHotelMapUseCase(
    private val mapRepository: MapRepository,
) {
    suspend operator fun invoke(
        manifest: HotelMapSourceManifest,
        requests: List<TenantScopedImportRequest>,
    ): HotelMap = mapRepository.importHotelMap(manifest, requests)
}
