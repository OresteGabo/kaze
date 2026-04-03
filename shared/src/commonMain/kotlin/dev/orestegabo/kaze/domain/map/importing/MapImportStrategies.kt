package dev.orestegabo.kaze.domain.map.importing

abstract class XmlFloorPlanImportStrategy(
    override val id: String,
    final override val supportedFormats: Set<MapSourceFormat>,
) : FloorPlanImportStrategy {
    final override fun import(request: TenantScopedImportRequest): ImportedFloorGeometry {
        val document = request.payload.trim()
        require(document.startsWith("<")) {
            "XML import strategy '$id' expected XML payload for '${request.sourceFile.path}'"
        }
        return importXml(request, document)
    }

    protected abstract fun importXml(
        request: TenantScopedImportRequest,
        xml: String,
    ): ImportedFloorGeometry
}

abstract class VectorFloorPlanImportStrategy(
    override val id: String,
    final override val supportedFormats: Set<MapSourceFormat>,
) : FloorPlanImportStrategy {
    final override fun import(request: TenantScopedImportRequest): ImportedFloorGeometry {
        val source = request.payload.trim()
        require(source.isNotEmpty()) {
            "Vector import strategy '$id' received an empty payload for '${request.sourceFile.path}'"
        }
        return importVectorSource(request, source)
    }

    protected abstract fun importVectorSource(
        request: TenantScopedImportRequest,
        source: String,
    ): ImportedFloorGeometry
}

class PassthroughSeedImportStrategy(
    override val id: String = "passthrough-seed",
) : FloorPlanImportStrategy {
    override val supportedFormats: Set<MapSourceFormat> = setOf(MapSourceFormat.JSON)

    override fun import(request: TenantScopedImportRequest): ImportedFloorGeometry {
        error(
            "PassthroughSeedImportStrategy is a placeholder for app-generated seed JSON. " +
                "Hook your JSON deserializer here for '${request.sourceFile.path}'.",
        )
    }
}
