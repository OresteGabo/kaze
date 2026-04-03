package dev.orestegabo.kaze.domain

import dev.orestegabo.kaze.domain.map.importing.MapImportProfile

data class Hotel(
    val id: String,
    val slug: String,
    val name: String,
    val market: HotelMarket,
    val timezoneId: String,
    val config: HotelConfig,
    val campus: HotelCampus,
    val activeExperiences: Set<ExperienceMode> = emptySet(),
)

data class HotelConfig(
    val hotelId: String,
    val displayName: String,
    val branding: HotelBranding,
    val supportedLocales: List<String> = listOf("en"),
    val defaultCurrencyCode: String = "USD",
    val mapImportProfile: MapImportProfile? = null,
)

data class HotelBranding(
    val primaryHex: String,
    val secondaryHex: String,
    val accentHex: String,
    val surfaceHex: String,
    val backgroundHex: String,
    val logoAsset: String,
    val wordmarkAsset: String = logoAsset,
    val typography: TypographySpec = TypographySpec(),
)

data class TypographySpec(
    val headingScale: Float = 1f,
    val bodyScale: Float = 1f,
    val labelScale: Float = 1f,
)

data class HotelCampus(
    val city: String,
    val countryCode: String,
    val buildings: List<HotelBuilding>,
)

data class HotelBuilding(
    val id: String,
    val name: String,
    val floors: List<String>,
)

enum class HotelMarket {
    LUXURY_HOTEL,
    RESORT,
    CONFERENCE_VENUE,
    BOUTIQUE_HOTEL,
}

enum class ExperienceMode {
    STAY,
    EVENT,
    EXPLORE,
    SERVICE_REQUESTS,
}
