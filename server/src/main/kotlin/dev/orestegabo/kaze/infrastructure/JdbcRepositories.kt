package dev.orestegabo.kaze.infrastructure

import dev.orestegabo.kaze.application.AmenityStatus
import dev.orestegabo.kaze.application.GuestProfile
import dev.orestegabo.kaze.data.repository.ExperienceRepository
import dev.orestegabo.kaze.data.repository.HotelRepository
import dev.orestegabo.kaze.data.repository.MapRepository
import dev.orestegabo.kaze.data.repository.StayRepository
import dev.orestegabo.kaze.domain.ExperienceMode
import dev.orestegabo.kaze.domain.Hotel
import dev.orestegabo.kaze.domain.HotelBranding
import dev.orestegabo.kaze.domain.HotelBuilding
import dev.orestegabo.kaze.domain.HotelCampus
import dev.orestegabo.kaze.domain.HotelConfig
import dev.orestegabo.kaze.domain.HotelMarket
import dev.orestegabo.kaze.domain.Itinerary
import dev.orestegabo.kaze.domain.ItineraryItem
import dev.orestegabo.kaze.domain.ItineraryItemCategory
import dev.orestegabo.kaze.domain.ItineraryMode
import dev.orestegabo.kaze.domain.ItinerarySection
import dev.orestegabo.kaze.domain.ItineraryTab
import dev.orestegabo.kaze.domain.PlaceService
import dev.orestegabo.kaze.domain.PlaceServiceCategory
import dev.orestegabo.kaze.domain.PlaceServicePricing
import dev.orestegabo.kaze.domain.ReservationStatus
import dev.orestegabo.kaze.domain.TimeWindow
import dev.orestegabo.kaze.domain.TypographySpec
import dev.orestegabo.kaze.domain.VenueRef
import dev.orestegabo.kaze.domain.experience.AmenityHighlight
import dev.orestegabo.kaze.domain.experience.EventDay
import dev.orestegabo.kaze.domain.experience.ScheduledExperience
import dev.orestegabo.kaze.domain.guest.FollowUpPreference
import dev.orestegabo.kaze.domain.guest.GuestIdentity
import dev.orestegabo.kaze.domain.guest.LateCheckoutDecision
import dev.orestegabo.kaze.domain.guest.LateCheckoutStatus
import dev.orestegabo.kaze.domain.guest.LateCheckoutSubmission
import dev.orestegabo.kaze.domain.guest.ServiceRequestDraft
import dev.orestegabo.kaze.domain.guest.ServiceRequestReceipt
import dev.orestegabo.kaze.domain.guest.ServiceRequestStatus
import dev.orestegabo.kaze.domain.guest.ServiceRequestType
import dev.orestegabo.kaze.domain.map.FloorLevel
import dev.orestegabo.kaze.domain.map.HotelMap
import dev.orestegabo.kaze.domain.map.MapNode
import dev.orestegabo.kaze.domain.map.MapNodeKind
import dev.orestegabo.kaze.domain.map.MapPoint
import dev.orestegabo.kaze.domain.map.MapSize
import dev.orestegabo.kaze.domain.map.importing.HotelMapSourceManifest
import dev.orestegabo.kaze.domain.map.importing.MapSourceFormat
import dev.orestegabo.kaze.domain.map.importing.TenantScopedImportRequest
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Types
import java.time.Instant
import java.util.UUID
import javax.sql.DataSource

internal class JdbcHotelRepository(
    private val dataSource: DataSource,
) : HotelRepository {

    override suspend fun listHotels(): List<Hotel> =
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                SELECT h.id, h.slug, h.market, h.timezone_id, h.display_name,
                       h.primary_hex, h.secondary_hex, h.accent_hex, h.surface_hex, h.background_hex,
                       h.logo_asset, h.wordmark_asset, h.heading_scale, h.body_scale, h.label_scale,
                       h.supported_locales, h.default_currency_code, h.active_experiences,
                       sp.name, sp.city, sp.country_code
                FROM hotels h
                INNER JOIN service_places sp ON sp.id = h.id
                ORDER BY sp.city, h.display_name
                """.trimIndent(),
            ).use { statement ->
                statement.executeQuery().use { result ->
                    buildList {
                        while (result.next()) {
                            add(result.toHotel(connection))
                        }
                    }
                }
            }
        }

    override suspend fun getHotel(hotelId: String): Hotel? =
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                SELECT h.id, h.slug, h.market, h.timezone_id, h.display_name,
                       h.primary_hex, h.secondary_hex, h.accent_hex, h.surface_hex, h.background_hex,
                       h.logo_asset, h.wordmark_asset, h.heading_scale, h.body_scale, h.label_scale,
                       h.supported_locales, h.default_currency_code, h.active_experiences,
                       sp.name, sp.city, sp.country_code
                FROM hotels h
                INNER JOIN service_places sp ON sp.id = h.id
                WHERE h.id = ?
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, hotelId)
                statement.executeQuery().use { result ->
                    if (result.next()) result.toHotel(connection) else null
                }
            }
        }

    override suspend fun requireHotel(hotelId: String): Hotel =
        getHotel(hotelId) ?: error("Unknown hotel id: $hotelId")

    private fun ResultSet.toHotel(connection: Connection): Hotel {
        val hotelId = getString("id")
        return Hotel(
            id = hotelId,
            slug = getString("slug"),
            name = getString("name"),
            market = getString("market").toHotelMarket(),
            timezoneId = getString("timezone_id"),
            config = HotelConfig(
                hotelId = hotelId,
                displayName = getString("display_name"),
                branding = HotelBranding(
                    primaryHex = getString("primary_hex"),
                    secondaryHex = getString("secondary_hex"),
                    accentHex = getString("accent_hex"),
                    surfaceHex = getString("surface_hex"),
                    backgroundHex = getString("background_hex"),
                    logoAsset = getString("logo_asset"),
                    wordmarkAsset = getString("wordmark_asset"),
                    typography = TypographySpec(
                        headingScale = getFloat("heading_scale"),
                        bodyScale = getFloat("body_scale"),
                        labelScale = getFloat("label_scale"),
                    ),
                ),
                supportedLocales = getTextArray("supported_locales"),
                defaultCurrencyCode = getString("default_currency_code"),
                serviceCatalog = loadServiceCatalog(connection, hotelId),
            ),
            campus = HotelCampus(
                city = getString("city"),
                countryCode = getString("country_code"),
                buildings = loadBuildings(connection, hotelId),
            ),
            activeExperiences = getTextArray("active_experiences")
                .mapNotNull { runCatching { ExperienceMode.valueOf(it) }.getOrNull() }
                .toSet(),
        )
    }

    private fun loadBuildings(connection: Connection, hotelId: String): List<HotelBuilding> =
        connection.prepareStatement(
            """
            SELECT id, name, floors
            FROM hotel_buildings
            WHERE hotel_id = ?
            ORDER BY name
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, hotelId)
            statement.executeQuery().use { result ->
                buildList {
                    while (result.next()) {
                        add(
                            HotelBuilding(
                                id = result.getString("id"),
                                name = result.getString("name"),
                                floors = result.getTextArray("floors"),
                            ),
                        )
                    }
                }
            }
        }

    private fun loadServiceCatalog(connection: Connection, placeId: String): List<PlaceService> =
        connection.prepareStatement(
            """
            SELECT id, title, description, category, pricing_kind, amount_label, requestable
            FROM place_services
            WHERE place_id = ?
            ORDER BY title
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, placeId)
            statement.executeQuery().use { result ->
                buildList {
                    while (result.next()) {
                        add(
                            PlaceService(
                                id = result.getString("id"),
                                title = result.getString("title"),
                                description = result.getString("description"),
                                category = result.getString("category").toPlaceServiceCategory(),
                                pricing = result.toPlaceServicePricing(),
                                requestable = result.getBoolean("requestable"),
                            ),
                        )
                    }
                }
            }
        }
}

internal class JdbcExperienceRepository(
    private val dataSource: DataSource,
) : ExperienceRepository {

    override suspend fun getEventDays(hotelId: String): List<EventDay> =
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                SELECT id, label, date_iso
                FROM event_days
                WHERE hotel_id = ?
                ORDER BY date_iso
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, hotelId)
                statement.executeQuery().use { result ->
                    buildList {
                        while (result.next()) {
                            add(
                                EventDay(
                                    id = result.getString("id"),
                                    label = result.getString("label"),
                                    dateIso = result.getString("date_iso"),
                                ),
                            )
                        }
                    }
                }
            }
        }

    override suspend fun getEventSchedule(hotelId: String, dayId: String): List<ScheduledExperience> =
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                SELECT id, day_id, title, description, start_iso, end_iso, venue_label, host_label
                FROM scheduled_experiences
                WHERE hotel_id = ? AND day_id = ?
                ORDER BY start_iso
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, hotelId)
                statement.setString(2, dayId)
                statement.executeQuery().use { result ->
                    buildList {
                        while (result.next()) {
                            add(
                                ScheduledExperience(
                                    id = result.getString("id"),
                                    dayId = result.getString("day_id"),
                                    title = result.getString("title"),
                                    description = result.getString("description"),
                                    startIso = result.getTimestamp("start_iso").toInstant().toString(),
                                    endIso = result.getTimestamp("end_iso").toInstant().toString(),
                                    venueLabel = result.getString("venue_label"),
                                    hostLabel = result.getString("host_label"),
                                ),
                            )
                        }
                    }
                }
            }
        }

    override suspend fun getAmenityHighlights(hotelId: String): List<AmenityHighlight> =
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                SELECT id, title, description, location_label, availability_label, category_label, access_label, action_label
                FROM amenity_highlights
                WHERE hotel_id = ?
                ORDER BY title
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, hotelId)
                statement.executeQuery().use { result ->
                    buildList {
                        while (result.next()) {
                            add(
                                AmenityHighlight(
                                    id = result.getString("id"),
                                    title = result.getString("title"),
                                    description = result.getString("description"),
                                    locationLabel = result.getString("location_label"),
                                    availabilityLabel = result.getString("availability_label"),
                                    categoryLabel = result.getString("category_label"),
                                    accessLabel = result.getString("access_label"),
                                    actionLabel = result.getString("action_label"),
                                ),
                            )
                        }
                    }
                }
            }
        }
}

internal class JdbcMapRepository(
    private val dataSource: DataSource,
) : MapRepository {

    override suspend fun getHotelMap(hotelId: String, mapId: String?): HotelMap? =
        dataSource.connection.use { connection ->
            val mapRow = connection.prepareStatement(
                """
                SELECT id, name
                FROM maps
                WHERE hotel_id = ?
                ${if (mapId != null) "AND id = ?" else ""}
                ORDER BY id
                LIMIT 1
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, hotelId)
                if (mapId != null) {
                    statement.setString(2, mapId)
                }
                statement.executeQuery().use { result ->
                    if (result.next()) result.getString("id") to result.getString("name") else null
                }
            } ?: return null

            val floors = loadFloors(connection, mapRow.first)
            HotelMap(
                hotelId = hotelId,
                mapId = mapRow.first,
                name = mapRow.second,
                floors = floors,
            )
        }

    override suspend fun saveHotelMap(map: HotelMap) {
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                connection.prepareStatement(
                    """
                    INSERT INTO maps (id, hotel_id, name, source_format)
                    VALUES (?, ?, ?, ?)
                    ON CONFLICT (id) DO UPDATE SET
                        hotel_id = EXCLUDED.hotel_id,
                        name = EXCLUDED.name,
                        source_format = EXCLUDED.source_format
                    """.trimIndent(),
                ).use { statement ->
                    statement.setString(1, map.mapId)
                    statement.setString(2, map.hotelId)
                    statement.setString(3, map.name)
                    statement.setString(4, map.sourceManifest?.sourceFiles?.firstOrNull()?.format?.name)
                    statement.executeUpdate()
                }

                connection.prepareStatement(
                    """
                    DELETE FROM map_nodes
                    WHERE floor_id IN (SELECT id FROM map_floors WHERE map_id = ?)
                    """.trimIndent(),
                ).use { statement ->
                    statement.setString(1, map.mapId)
                    statement.executeUpdate()
                }
                connection.prepareStatement("DELETE FROM map_floors WHERE map_id = ?").use { statement ->
                    statement.setString(1, map.mapId)
                    statement.executeUpdate()
                }

                map.floors.forEach { floor ->
                    connection.prepareStatement(
                        """
                        INSERT INTO map_floors (id, map_id, building_id, label, level_index, width, height)
                        VALUES (?, ?, ?, ?, ?, ?, ?)
                        """.trimIndent(),
                    ).use { statement ->
                        statement.setString(1, floor.id)
                        statement.setString(2, map.mapId)
                        statement.setString(3, floor.buildingId)
                        statement.setString(4, floor.label)
                        statement.setInt(5, floor.levelIndex)
                        statement.setFloat(6, floor.canvasSize.width)
                        statement.setFloat(7, floor.canvasSize.height)
                        statement.executeUpdate()
                    }
                    floor.nodes.forEach { node ->
                        connection.prepareStatement(
                            """
                            INSERT INTO map_nodes (id, floor_id, label, kind, x, y)
                            VALUES (?, ?, ?, ?, ?, ?)
                            """.trimIndent(),
                        ).use { statement ->
                            statement.setString(1, node.id)
                            statement.setString(2, floor.id)
                            statement.setString(3, node.label)
                            statement.setString(4, node.kind.name)
                            statement.setFloat(5, node.position.x)
                            statement.setFloat(6, node.position.y)
                            statement.executeUpdate()
                        }
                    }
                }
                connection.commit()
            } catch (cause: Throwable) {
                connection.rollback()
                throw cause
            }
        }
    }

    override suspend fun importHotelMap(
        manifest: HotelMapSourceManifest,
        requests: List<TenantScopedImportRequest>,
    ): HotelMap {
        val map = HotelMap(
            hotelId = manifest.hotelId,
            mapId = manifest.mapId,
            name = "Imported ${manifest.mapId}",
            sourceManifest = manifest,
            floors = manifest.sourceFiles.sortedBy { it.levelIndex }.map { sourceFile ->
                FloorLevel(
                    id = sourceFile.floorId,
                    buildingId = sourceFile.buildingId,
                    label = sourceFile.label,
                    levelIndex = sourceFile.levelIndex,
                    canvasSize = MapSize(width = 1200f, height = 900f),
                    nodes = emptyList(),
                    edges = emptyList(),
                )
            },
        )
        saveHotelMap(map)
        return map
    }

    private fun loadFloors(connection: Connection, mapId: String): List<FloorLevel> {
        val nodeMap = loadNodesByFloor(connection, mapId)
        return connection.prepareStatement(
            """
            SELECT id, building_id, label, level_index, width, height
            FROM map_floors
            WHERE map_id = ?
            ORDER BY level_index
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, mapId)
            statement.executeQuery().use { result ->
                buildList {
                    while (result.next()) {
                        val floorId = result.getString("id")
                        add(
                            FloorLevel(
                                id = floorId,
                                buildingId = result.getString("building_id"),
                                label = result.getString("label"),
                                levelIndex = result.getInt("level_index"),
                                canvasSize = MapSize(
                                    width = result.getFloat("width"),
                                    height = result.getFloat("height"),
                                ),
                                nodes = nodeMap[floorId].orEmpty(),
                                edges = emptyList(),
                            ),
                        )
                    }
                }
            }
        }
    }

    private fun loadNodesByFloor(connection: Connection, mapId: String): Map<String, List<MapNode>> =
        connection.prepareStatement(
            """
            SELECT n.id, n.floor_id, n.label, n.kind, n.x, n.y
            FROM map_nodes n
            INNER JOIN map_floors f ON f.id = n.floor_id
            WHERE f.map_id = ?
            ORDER BY n.label
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, mapId)
            statement.executeQuery().use { result ->
                buildMap<String, MutableList<MapNode>> {
                    while (result.next()) {
                        val floorId = result.getString("floor_id")
                        val nodes = getOrPut(floorId) { mutableListOf() }
                        nodes += MapNode(
                            id = result.getString("id"),
                            label = result.getString("label"),
                            kind = result.getString("kind").toMapNodeKind(),
                            floorId = floorId,
                            position = MapPoint(
                                x = result.getFloat("x"),
                                y = result.getFloat("y"),
                            ),
                        )
                    }
                }
            }
        }
}

internal class GuestRepository(
    private val dataSource: DataSource,
) {
    fun findGuest(hotelId: String, guestId: String): GuestProfile? =
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                SELECT g.id, g.full_name, s.id AS stay_id, s.room_id
                FROM guests g
                LEFT JOIN stays s ON s.guest_id = g.id AND s.hotel_id = g.hotel_id
                WHERE g.hotel_id = ? AND g.id = ?
                ORDER BY CASE WHEN s.status = 'ACTIVE' THEN 0 ELSE 1 END,
                         s.start_iso_utc DESC NULLS LAST
                LIMIT 1
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, hotelId)
                statement.setString(2, guestId)
                statement.executeQuery().use { result ->
                    if (result.next()) {
                        GuestProfile(
                            hotelId = hotelId,
                            guestId = result.getString("id"),
                            fullName = result.getString("full_name"),
                            stayId = result.getString("stay_id"),
                            roomId = result.getString("room_id"),
                        )
                    } else {
                        null
                    }
                }
            }
        }
}

internal class JdbcStayRepository(
    private val dataSource: DataSource,
) : StayRepository {

    override suspend fun getStayItinerary(guest: GuestIdentity): Itinerary? =
        dataSource.connection.use { connection ->
            val stay = resolveStay(connection, guest) ?: return null
            val items = connection.prepareStatement(
                """
                SELECT id, title, category, status, start_iso_utc, end_iso_utc, venue_node_id, venue_floor_id, venue_label, notes
                FROM itinerary_items
                WHERE stay_id = ?
                ORDER BY start_iso_utc
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, stay.id)
                statement.executeQuery().use { result ->
                    buildList {
                        while (result.next()) {
                            add(
                                ItineraryItem(
                                    id = result.getString("id"),
                                    title = result.getString("title"),
                                    category = result.getString("category").toItineraryCategory(),
                                    timeWindow = TimeWindow(
                                        startIsoUtc = result.getTimestamp("start_iso_utc").toInstant().toString(),
                                        endIsoUtc = result.getTimestamp("end_iso_utc").toInstant().toString(),
                                    ),
                                    venue = result.getString("venue_label")?.let { venueLabel ->
                                        val nodeId = result.getString("venue_node_id")
                                        val floorId = result.getString("venue_floor_id")
                                        if (nodeId != null && floorId != null) {
                                            VenueRef(nodeId = nodeId, floorId = floorId, label = venueLabel)
                                        } else {
                                            null
                                        }
                                    },
                                    status = result.getString("status").toReservationStatus(),
                                    notes = result.getString("notes"),
                                ),
                            )
                        }
                    }
                }
            }

            Itinerary(
                id = "itinerary_${guest.guestId}",
                hotelId = guest.hotelId,
                guestId = guest.guestId,
                stayWindow = TimeWindow(
                    startIsoUtc = stay.start.toString(),
                    endIsoUtc = stay.end.toString(),
                ),
                tabs = listOf(
                    ItineraryTab(
                        mode = ItineraryMode.MY_STAY,
                        title = "My Stay",
                        sections = listOf(
                            ItinerarySection(
                                id = "core",
                                title = "Confirmed moments",
                                items = items,
                            ),
                        ),
                    ),
                ),
            )
        }

    override suspend fun submitLateCheckout(submission: LateCheckoutSubmission): LateCheckoutDecision {
        val requestId = "late_${UUID.randomUUID().toString().replace("-", "")}"
        dataSource.connection.use { connection ->
            val guest = submission.guest
            val stayId = guest.stayId ?: resolveStay(connection, guest)?.id
            val roomId = guest.roomId ?: resolveStay(connection, guest)?.roomId
            connection.prepareStatement(
                """
                INSERT INTO late_checkout_requests (
                    id, hotel_id, guest_id, stay_id, room_id, checkout_time_iso, fee_amount_minor,
                    currency_code, payment_preference, follow_up_preference, status, note
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, requestId)
                statement.setString(2, guest.hotelId)
                statement.setString(3, guest.guestId)
                statement.setNullableString(4, stayId)
                statement.setNullableString(5, roomId)
                statement.setTimestamp(6, java.sql.Timestamp.from(Instant.parse(submission.selection.checkoutTimeIso)))
                statement.setLong(7, submission.selection.feeAmountMinor)
                statement.setString(8, submission.selection.currencyCode)
                statement.setString(9, submission.paymentPreference.name)
                statement.setString(10, submission.followUpPreference.name)
                statement.setString(11, LateCheckoutStatus.PENDING.name)
                statement.setString(12, submission.notes)
                statement.executeUpdate()
            }
        }
        return LateCheckoutDecision(
            requestId = requestId,
            status = LateCheckoutStatus.PENDING,
            approvedCheckoutTimeIso = submission.selection.checkoutTimeIso,
            feeAmountMinor = submission.selection.feeAmountMinor,
            currencyCode = submission.selection.currencyCode,
            note = submission.notes ?: submission.followUpPreference.defaultLateCheckoutNote(),
        )
    }

    override suspend fun submitServiceRequest(request: ServiceRequestDraft): ServiceRequestReceipt {
        val requestId = "service_${UUID.randomUUID().toString().replace("-", "")}"
        dataSource.connection.use { connection ->
            val guest = request.guest
            val stayId = guest.stayId ?: resolveStay(connection, guest)?.id
            val roomId = guest.roomId ?: resolveStay(connection, guest)?.roomId
            connection.prepareStatement(
                """
                INSERT INTO service_requests (id, hotel_id, guest_id, stay_id, room_id, type, status, note)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, requestId)
                statement.setString(2, guest.hotelId)
                statement.setString(3, guest.guestId)
                statement.setNullableString(4, stayId)
                statement.setNullableString(5, roomId)
                statement.setString(6, request.type.name)
                statement.setString(7, ServiceRequestStatus.PENDING.name)
                statement.setString(8, request.note)
                statement.executeUpdate()
            }
        }
        return ServiceRequestReceipt(
            requestId = requestId,
            type = request.type,
            status = ServiceRequestStatus.PENDING,
            note = request.note ?: "The hotel team has received the request.",
        )
    }

    override suspend fun getLateCheckoutHistory(guest: GuestIdentity): List<LateCheckoutDecision> =
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                SELECT id, status, checkout_time_iso, fee_amount_minor, currency_code, note
                FROM late_checkout_requests
                WHERE hotel_id = ? AND guest_id = ?
                ORDER BY created_at DESC
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, guest.hotelId)
                statement.setString(2, guest.guestId)
                statement.executeQuery().use { result ->
                    buildList {
                        while (result.next()) {
                            add(
                                LateCheckoutDecision(
                                    requestId = result.getString("id"),
                                    status = result.getString("status").toLateCheckoutStatus(),
                                    approvedCheckoutTimeIso = result.getTimestamp("checkout_time_iso")?.toInstant()?.toString(),
                                    feeAmountMinor = result.getLong("fee_amount_minor").takeIf { !result.wasNull() },
                                    currencyCode = result.getString("currency_code"),
                                    note = result.getString("note"),
                                ),
                            )
                        }
                    }
                }
            }
        }

    override suspend fun getServiceRequestHistory(guest: GuestIdentity): List<ServiceRequestReceipt> =
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                SELECT id, type, status, note
                FROM service_requests
                WHERE hotel_id = ? AND guest_id = ?
                ORDER BY created_at DESC
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, guest.hotelId)
                statement.setString(2, guest.guestId)
                statement.executeQuery().use { result ->
                    buildList {
                        while (result.next()) {
                            add(
                                ServiceRequestReceipt(
                                    requestId = result.getString("id"),
                                    type = result.getString("type").toServiceRequestType(),
                                    status = result.getString("status").toServiceRequestStatus(),
                                    note = result.getString("note"),
                                ),
                            )
                        }
                    }
                }
            }
        }

    private fun resolveStay(connection: Connection, guest: GuestIdentity): StayRecord? {
        val sql = buildString {
            append(
                """
                SELECT id, room_id, start_iso_utc, end_iso_utc
                FROM stays
                WHERE hotel_id = ? AND guest_id = ?
                """.trimIndent(),
            )
            if (guest.stayId != null) {
                append(" AND id = ?")
            }
            append(" ORDER BY CASE WHEN status = 'ACTIVE' THEN 0 ELSE 1 END, start_iso_utc DESC LIMIT 1")
        }
        return connection.prepareStatement(sql).use { statement ->
            statement.setString(1, guest.hotelId)
            statement.setString(2, guest.guestId)
            if (guest.stayId != null) {
                statement.setString(3, guest.stayId)
            }
            statement.executeQuery().use { result ->
                if (result.next()) {
                    StayRecord(
                        id = result.getString("id"),
                        roomId = result.getString("room_id"),
                        start = result.getTimestamp("start_iso_utc").toInstant(),
                        end = result.getTimestamp("end_iso_utc").toInstant(),
                    )
                } else {
                    null
                }
            }
        }
    }
}

internal class AmenityKnowledgeRepository(
    private val dataSource: DataSource,
) {
    fun listAmenities(hotelId: String): List<AmenityStatus> =
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                SELECT id, title, location_label, status_label, hours_label, open_now
                FROM amenity_statuses
                WHERE hotel_id = ?
                ORDER BY title
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, hotelId)
                statement.executeQuery().use { result ->
                    buildList {
                        while (result.next()) {
                            add(
                                AmenityStatus(
                                    id = result.getString("id"),
                                    title = result.getString("title"),
                                    locationLabel = result.getString("location_label"),
                                    statusLabel = result.getString("status_label"),
                                    hoursLabel = result.getString("hours_label"),
                                    openNow = result.getBoolean("open_now"),
                                ),
                            )
                        }
                    }
                }
            }
        }

    fun findAmenity(hotelId: String, key: String): AmenityStatus? {
        val normalizedKey = key.trim().lowercase()
        return listAmenities(hotelId).firstOrNull { amenity ->
            amenity.id.lowercase().contains(normalizedKey) ||
                amenity.title.lowercase().contains(normalizedKey) ||
                amenity.locationLabel.lowercase().contains(normalizedKey)
        }
    }
}

private data class StayRecord(
    val id: String,
    val roomId: String?,
    val start: Instant,
    val end: Instant,
)

private fun ResultSet.getTextArray(column: String): List<String> =
    (getArray(column)?.array as? Array<*>)?.mapNotNull { it?.toString() } ?: emptyList()

private fun ResultSet.toPlaceServicePricing(): PlaceServicePricing {
    val kind = getString("pricing_kind")?.uppercase()
    val amountLabel = getString("amount_label")
    return when (kind) {
        "INCLUDED" -> PlaceServicePricing.Included
        "FREE" -> PlaceServicePricing.Free
        "FROM" -> amountLabel?.let(PlaceServicePricing::Paid) ?: PlaceServicePricing.RequiresConfirmation
        "QUOTE" -> PlaceServicePricing.RequiresConfirmation
        else -> amountLabel?.let(PlaceServicePricing::Paid) ?: PlaceServicePricing.RequiresConfirmation
    }
}

private fun String.toHotelMarket(): HotelMarket =
    runCatching { HotelMarket.valueOf(trim().uppercase()) }.getOrDefault(HotelMarket.BOUTIQUE_HOTEL)

private fun String.toPlaceServiceCategory(): PlaceServiceCategory =
    runCatching { PlaceServiceCategory.valueOf(trim().uppercase()) }.getOrDefault(PlaceServiceCategory.ADD_ON)

private fun String.toItineraryCategory(): ItineraryItemCategory =
    when (trim().uppercase()) {
        "CHECK_IN" -> ItineraryItemCategory.CHECK_IN
        "CHECK_OUT" -> ItineraryItemCategory.CHECK_OUT
        "DINING" -> ItineraryItemCategory.DINING
        "SPA" -> ItineraryItemCategory.SPA
        "TRANSPORT" -> ItineraryItemCategory.TRANSPORT
        "AMENITY" -> ItineraryItemCategory.AMENITY
        "SERVICE_REQUEST" -> ItineraryItemCategory.SERVICE_REQUEST
        "EVENT", "MEETING", "EVENT_SESSION" -> ItineraryItemCategory.EVENT_SESSION
        else -> ItineraryItemCategory.AMENITY
    }

private fun String.toReservationStatus(): ReservationStatus =
    runCatching { ReservationStatus.valueOf(trim().uppercase()) }.getOrDefault(ReservationStatus.CONFIRMED)

private fun String.toLateCheckoutStatus(): LateCheckoutStatus =
    runCatching { LateCheckoutStatus.valueOf(trim().uppercase()) }.getOrDefault(LateCheckoutStatus.PENDING)

private fun String.toServiceRequestType(): ServiceRequestType =
    runCatching { ServiceRequestType.valueOf(trim().uppercase()) }.getOrDefault(ServiceRequestType.CONCIERGE)

private fun String.toServiceRequestStatus(): ServiceRequestStatus =
    runCatching { ServiceRequestStatus.valueOf(trim().uppercase()) }.getOrDefault(ServiceRequestStatus.PENDING)

private fun String.toMapNodeKind(): MapNodeKind =
    when (trim().uppercase()) {
        "ROOM", "SUITE", "ELEVATOR", "STAIRS", "LOBBY", "RESTAURANT", "BALLROOM",
        "SPA", "GYM", "POOL", "BAR", "CONCIERGE", "RESTROOM", "EXIT", "LANDMARK" ->
            MapNodeKind.valueOf(trim().uppercase())
        "REGISTRATION" -> MapNodeKind.CONCIERGE
        "MEETING_ROOM", "BREAKOUT_ROOM", "AUDITORIUM" -> MapNodeKind.BALLROOM
        "GARDEN" -> MapNodeKind.LANDMARK
        else -> MapNodeKind.LANDMARK
    }

private fun FollowUpPreference.defaultLateCheckoutNote(): String =
    when (this) {
        FollowUpPreference.VISIT_ROOM -> "Reception will coordinate an in-room follow-up if policy allows."
        FollowUpPreference.CALL_ROOM -> "Reception will call the room once availability is confirmed."
        FollowUpPreference.CONFIRM_IN_APP -> "Approval will appear quietly in the app."
    }

private fun java.sql.PreparedStatement.setNullableString(index: Int, value: String?) {
    if (value == null) {
        setNull(index, Types.VARCHAR)
    } else {
        setString(index, value)
    }
}
