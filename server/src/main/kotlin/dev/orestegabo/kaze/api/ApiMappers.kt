package dev.orestegabo.kaze.api

import dev.orestegabo.kaze.application.AmenityStatus
import dev.orestegabo.kaze.application.GuestProfile
import dev.orestegabo.kaze.domain.Hotel
import dev.orestegabo.kaze.domain.Itinerary
import dev.orestegabo.kaze.domain.ItineraryItem
import dev.orestegabo.kaze.domain.ItinerarySection
import dev.orestegabo.kaze.domain.ItineraryTab
import dev.orestegabo.kaze.domain.TimeWindow
import dev.orestegabo.kaze.domain.VenueRef
import dev.orestegabo.kaze.domain.experience.AmenityHighlight
import dev.orestegabo.kaze.domain.experience.EventDay
import dev.orestegabo.kaze.domain.experience.ScheduledExperience
import dev.orestegabo.kaze.domain.guest.LateCheckoutDecision
import dev.orestegabo.kaze.domain.guest.ServiceRequestReceipt
import dev.orestegabo.kaze.domain.map.FloorLevel
import dev.orestegabo.kaze.domain.map.HotelMap
import dev.orestegabo.kaze.domain.map.MapNode

internal fun Hotel.toDto(): HotelDto = HotelDto(
    id = id,
    slug = slug,
    name = name,
    market = market.name,
    timezoneId = timezoneId,
    displayName = config.displayName,
    city = campus.city,
    countryCode = campus.countryCode,
    supportedLocales = config.supportedLocales,
    activeExperiences = activeExperiences.map { it.name }.sorted(),
)

internal fun GuestProfile.toDto(): GuestProfileDto = GuestProfileDto(
    hotelId = hotelId,
    guestId = guestId,
    stayId = stayId,
    roomId = roomId,
    fullName = fullName,
)

internal fun Itinerary.toDto(): ItineraryDto = ItineraryDto(
    id = id,
    hotelId = hotelId,
    guestId = guestId,
    stayWindow = stayWindow.toDto(),
    tabs = tabs.map(ItineraryTab::toDto),
)

private fun ItineraryTab.toDto(): ItineraryTabDto = ItineraryTabDto(
    mode = mode.name,
    title = title,
    sections = sections.map(ItinerarySection::toDto),
)

private fun ItinerarySection.toDto(): ItinerarySectionDto = ItinerarySectionDto(
    id = id,
    title = title,
    items = items.map(ItineraryItem::toDto),
)

private fun ItineraryItem.toDto(): ItineraryItemDto = ItineraryItemDto(
    id = id,
    title = title,
    category = category.name,
    status = status.name,
    startIsoUtc = timeWindow.startIsoUtc,
    endIsoUtc = timeWindow.endIsoUtc,
    venue = venue?.toDto(),
    notes = notes,
)

private fun TimeWindow.toDto(): TimeWindowDto = TimeWindowDto(
    startIsoUtc = startIsoUtc,
    endIsoUtc = endIsoUtc,
)

private fun VenueRef.toDto(): VenueRefDto = VenueRefDto(
    nodeId = nodeId,
    floorId = floorId,
    label = label,
)

internal fun EventDay.toDto(): EventDayDto = EventDayDto(
    id = id,
    label = label,
    dateIso = dateIso,
)

internal fun ScheduledExperience.toDto(): ScheduledExperienceDto = ScheduledExperienceDto(
    id = id,
    dayId = dayId,
    title = title,
    description = description,
    startIso = startIso,
    endIso = endIso,
    venueLabel = venueLabel,
    hostLabel = hostLabel,
)

internal fun AmenityHighlight.toDto(): AmenityHighlightDto = AmenityHighlightDto(
    id = id,
    title = title,
    description = description,
    locationLabel = locationLabel,
    availabilityLabel = availabilityLabel,
    categoryLabel = categoryLabel,
    accessLabel = accessLabel,
    actionLabel = actionLabel,
)

internal fun AmenityStatus.toDto(): AmenityStatusDto = AmenityStatusDto(
    id = id,
    title = title,
    locationLabel = locationLabel,
    statusLabel = statusLabel,
    hoursLabel = hoursLabel,
    openNow = openNow,
)

internal fun HotelMap.toDto(): HotelMapDto = HotelMapDto(
    hotelId = hotelId,
    mapId = mapId,
    name = name,
    floors = floors.map(FloorLevel::toDto),
)

private fun FloorLevel.toDto(): FloorDto = FloorDto(
    id = id,
    label = label,
    levelIndex = levelIndex,
    width = canvasSize.width,
    height = canvasSize.height,
    nodes = nodes.map(MapNode::toDto),
)

private fun MapNode.toDto(): MapNodeDto = MapNodeDto(
    id = id,
    label = label,
    kind = kind.name,
    x = position.x,
    y = position.y,
)

internal fun LateCheckoutDecision.toDto(): LateCheckoutDecisionDto = LateCheckoutDecisionDto(
    requestId = requestId,
    status = status.name,
    approvedCheckoutTimeIso = approvedCheckoutTimeIso,
    feeAmountMinor = feeAmountMinor,
    currencyCode = currencyCode,
    note = note,
)

internal fun ServiceRequestReceipt.toDto(): ServiceRequestReceiptDto = ServiceRequestReceiptDto(
    requestId = requestId,
    type = type.name,
    status = status.name,
    note = note,
)
