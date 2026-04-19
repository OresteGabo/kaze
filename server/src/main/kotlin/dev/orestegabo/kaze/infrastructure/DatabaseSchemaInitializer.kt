package dev.orestegabo.kaze.infrastructure

import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped

internal fun Application.initializeDatabaseSchema(config: DatabaseConfig) {
    when (config.schemaMode) {
        DatabaseSchemaMode.NONE -> {
            environment.log.info("Kaze database schema initialization is disabled")
        }
        DatabaseSchemaMode.CREATE -> {
            runSchemaStatements(config, CREATE_SCHEMA_SQL)
            environment.log.info("Kaze database schema created or verified")
        }
        DatabaseSchemaMode.DROP -> {
            runSchemaStatements(config, DROP_SCHEMA_SQL)
            environment.log.warn("Kaze database schema dropped")
        }
        DatabaseSchemaMode.CREATE_DROP -> {
            runSchemaStatements(config, DROP_SCHEMA_SQL + CREATE_SCHEMA_SQL)
            environment.log.warn("Kaze database schema recreated; it will be dropped when the application stops")
            monitor.subscribe(ApplicationStopped) {
                runSchemaStatements(config, DROP_SCHEMA_SQL)
            }
        }
    }
}

private fun Application.runSchemaStatements(
    config: DatabaseConfig,
    statements: List<String>,
) {
    config.createDataSource().use { dataSource ->
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                statements.forEach { sql ->
                    connection.createStatement().use { statement ->
                        statement.execute(sql)
                    }
                }
                connection.commit()
            } catch (cause: Throwable) {
                connection.rollback()
                throw cause
            }
        }
    }
}

private val DROP_SCHEMA_SQL = listOf(
    "DROP TABLE IF EXISTS service_requests CASCADE",
    "DROP TABLE IF EXISTS late_checkout_requests CASCADE",
    "DROP TABLE IF EXISTS map_nodes CASCADE",
    "DROP TABLE IF EXISTS map_floors CASCADE",
    "DROP TABLE IF EXISTS maps CASCADE",
    "DROP TABLE IF EXISTS amenity_statuses CASCADE",
    "DROP TABLE IF EXISTS amenity_highlights CASCADE",
    "DROP TABLE IF EXISTS scheduled_experiences CASCADE",
    "DROP TABLE IF EXISTS event_days CASCADE",
    "DROP TABLE IF EXISTS itinerary_items CASCADE",
    "DROP TABLE IF EXISTS stays CASCADE",
    "DROP TABLE IF EXISTS guests CASCADE",
    "DROP TABLE IF EXISTS place_services CASCADE",
    "DROP TABLE IF EXISTS hotel_buildings CASCADE",
    "DROP TABLE IF EXISTS hotels CASCADE",
    "DROP TABLE IF EXISTS service_places CASCADE",
)

private val CREATE_SCHEMA_SQL = listOf(
    """
    CREATE TABLE IF NOT EXISTS service_places (
        id VARCHAR(120) PRIMARY KEY,
        name VARCHAR(240) NOT NULL,
        kind VARCHAR(64) NOT NULL,
        city VARCHAR(120) NOT NULL,
        country_code VARCHAR(8) NOT NULL,
        address_label VARCHAR(240),
        map_id VARCHAR(120),
        created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
    )
    """.trimIndent(),
    """
    CREATE TABLE IF NOT EXISTS hotels (
        id VARCHAR(120) PRIMARY KEY REFERENCES service_places(id) ON DELETE CASCADE,
        slug VARCHAR(160) NOT NULL UNIQUE,
        market VARCHAR(64) NOT NULL,
        timezone_id VARCHAR(80) NOT NULL,
        display_name VARCHAR(240) NOT NULL,
        primary_hex VARCHAR(16) NOT NULL,
        secondary_hex VARCHAR(16) NOT NULL,
        accent_hex VARCHAR(16) NOT NULL,
        surface_hex VARCHAR(16) NOT NULL,
        background_hex VARCHAR(16) NOT NULL,
        logo_asset VARCHAR(240) NOT NULL,
        wordmark_asset VARCHAR(240) NOT NULL,
        heading_scale REAL NOT NULL DEFAULT 1,
        body_scale REAL NOT NULL DEFAULT 1,
        label_scale REAL NOT NULL DEFAULT 1,
        supported_locales TEXT[] NOT NULL DEFAULT ARRAY['en'],
        default_currency_code VARCHAR(8) NOT NULL DEFAULT 'USD',
        active_experiences TEXT[] NOT NULL DEFAULT ARRAY[]::TEXT[]
    )
    """.trimIndent(),
    """
    CREATE TABLE IF NOT EXISTS hotel_buildings (
        id VARCHAR(120) PRIMARY KEY,
        hotel_id VARCHAR(120) NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
        name VARCHAR(240) NOT NULL,
        floors TEXT[] NOT NULL DEFAULT ARRAY[]::TEXT[]
    )
    """.trimIndent(),
    """
    CREATE TABLE IF NOT EXISTS place_services (
        id VARCHAR(120) PRIMARY KEY,
        place_id VARCHAR(120) NOT NULL REFERENCES service_places(id) ON DELETE CASCADE,
        title VARCHAR(240) NOT NULL,
        description TEXT NOT NULL,
        category VARCHAR(64) NOT NULL,
        pricing_kind VARCHAR(64) NOT NULL,
        amount_label VARCHAR(80),
        requestable BOOLEAN NOT NULL DEFAULT true
    )
    """.trimIndent(),
    """
    CREATE TABLE IF NOT EXISTS guests (
        id VARCHAR(120) PRIMARY KEY,
        hotel_id VARCHAR(120) NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
        full_name VARCHAR(240) NOT NULL,
        created_at TIMESTAMPTZ NOT NULL DEFAULT now()
    )
    """.trimIndent(),
    """
    CREATE TABLE IF NOT EXISTS stays (
        id VARCHAR(120) PRIMARY KEY,
        hotel_id VARCHAR(120) NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
        guest_id VARCHAR(120) NOT NULL REFERENCES guests(id) ON DELETE CASCADE,
        room_id VARCHAR(120),
        start_iso_utc TIMESTAMPTZ NOT NULL,
        end_iso_utc TIMESTAMPTZ NOT NULL,
        status VARCHAR(64) NOT NULL DEFAULT 'ACTIVE'
    )
    """.trimIndent(),
    """
    CREATE TABLE IF NOT EXISTS itinerary_items (
        id VARCHAR(120) PRIMARY KEY,
        stay_id VARCHAR(120) NOT NULL REFERENCES stays(id) ON DELETE CASCADE,
        title VARCHAR(240) NOT NULL,
        category VARCHAR(80) NOT NULL,
        status VARCHAR(80) NOT NULL,
        start_iso_utc TIMESTAMPTZ NOT NULL,
        end_iso_utc TIMESTAMPTZ NOT NULL,
        venue_node_id VARCHAR(120),
        venue_floor_id VARCHAR(120),
        venue_label VARCHAR(240),
        notes TEXT
    )
    """.trimIndent(),
    """
    CREATE TABLE IF NOT EXISTS event_days (
        id VARCHAR(120) PRIMARY KEY,
        hotel_id VARCHAR(120) NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
        label VARCHAR(120) NOT NULL,
        date_iso DATE NOT NULL
    )
    """.trimIndent(),
    """
    CREATE TABLE IF NOT EXISTS scheduled_experiences (
        id VARCHAR(120) PRIMARY KEY,
        hotel_id VARCHAR(120) NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
        day_id VARCHAR(120) NOT NULL REFERENCES event_days(id) ON DELETE CASCADE,
        title VARCHAR(240) NOT NULL,
        description TEXT NOT NULL,
        start_iso TIMESTAMPTZ NOT NULL,
        end_iso TIMESTAMPTZ NOT NULL,
        venue_label VARCHAR(240) NOT NULL,
        host_label VARCHAR(240)
    )
    """.trimIndent(),
    """
    CREATE TABLE IF NOT EXISTS amenity_highlights (
        id VARCHAR(120) PRIMARY KEY,
        hotel_id VARCHAR(120) NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
        title VARCHAR(240) NOT NULL,
        description TEXT NOT NULL,
        location_label VARCHAR(240) NOT NULL,
        availability_label VARCHAR(160) NOT NULL,
        category_label VARCHAR(160) NOT NULL,
        access_label VARCHAR(160) NOT NULL,
        action_label VARCHAR(160) NOT NULL
    )
    """.trimIndent(),
    """
    CREATE TABLE IF NOT EXISTS amenity_statuses (
        id VARCHAR(120) PRIMARY KEY,
        hotel_id VARCHAR(120) NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
        title VARCHAR(240) NOT NULL,
        location_label VARCHAR(240) NOT NULL,
        status_label VARCHAR(160) NOT NULL,
        hours_label VARCHAR(240) NOT NULL,
        open_now BOOLEAN NOT NULL DEFAULT false
    )
    """.trimIndent(),
    """
    CREATE TABLE IF NOT EXISTS maps (
        id VARCHAR(120) PRIMARY KEY,
        hotel_id VARCHAR(120) NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
        name VARCHAR(240) NOT NULL,
        source_format VARCHAR(80)
    )
    """.trimIndent(),
    """
    CREATE TABLE IF NOT EXISTS map_floors (
        id VARCHAR(120) PRIMARY KEY,
        map_id VARCHAR(120) NOT NULL REFERENCES maps(id) ON DELETE CASCADE,
        label VARCHAR(160) NOT NULL,
        level_index INTEGER NOT NULL,
        width REAL NOT NULL,
        height REAL NOT NULL
    )
    """.trimIndent(),
    """
    CREATE TABLE IF NOT EXISTS map_nodes (
        id VARCHAR(120) PRIMARY KEY,
        floor_id VARCHAR(120) NOT NULL REFERENCES map_floors(id) ON DELETE CASCADE,
        label VARCHAR(240) NOT NULL,
        kind VARCHAR(80) NOT NULL,
        x REAL NOT NULL,
        y REAL NOT NULL
    )
    """.trimIndent(),
    """
    CREATE TABLE IF NOT EXISTS late_checkout_requests (
        id VARCHAR(120) PRIMARY KEY,
        hotel_id VARCHAR(120) NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
        guest_id VARCHAR(120) NOT NULL REFERENCES guests(id) ON DELETE CASCADE,
        stay_id VARCHAR(120) REFERENCES stays(id) ON DELETE SET NULL,
        room_id VARCHAR(120),
        checkout_time_iso TIMESTAMPTZ NOT NULL,
        fee_amount_minor BIGINT NOT NULL,
        currency_code VARCHAR(8) NOT NULL,
        payment_preference VARCHAR(80) NOT NULL,
        follow_up_preference VARCHAR(80) NOT NULL,
        status VARCHAR(80) NOT NULL,
        note TEXT,
        created_at TIMESTAMPTZ NOT NULL DEFAULT now()
    )
    """.trimIndent(),
    """
    CREATE TABLE IF NOT EXISTS service_requests (
        id VARCHAR(120) PRIMARY KEY,
        hotel_id VARCHAR(120) NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
        guest_id VARCHAR(120) NOT NULL REFERENCES guests(id) ON DELETE CASCADE,
        stay_id VARCHAR(120) REFERENCES stays(id) ON DELETE SET NULL,
        room_id VARCHAR(120),
        type VARCHAR(80) NOT NULL,
        status VARCHAR(80) NOT NULL,
        note TEXT,
        created_at TIMESTAMPTZ NOT NULL DEFAULT now()
    )
    """.trimIndent(),
    "CREATE INDEX IF NOT EXISTS guests_hotel_id_idx ON guests(hotel_id)",
    "CREATE INDEX IF NOT EXISTS stays_guest_id_idx ON stays(guest_id)",
    "CREATE INDEX IF NOT EXISTS event_days_hotel_id_idx ON event_days(hotel_id)",
    "CREATE INDEX IF NOT EXISTS scheduled_experiences_day_id_idx ON scheduled_experiences(day_id)",
    "CREATE INDEX IF NOT EXISTS late_checkout_requests_guest_id_idx ON late_checkout_requests(guest_id)",
    "CREATE INDEX IF NOT EXISTS service_requests_guest_id_idx ON service_requests(guest_id)",
)
