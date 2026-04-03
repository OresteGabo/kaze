package dev.orestegabo.kaze.presentation.explore

import dev.orestegabo.kaze.domain.experience.AmenityHighlight

internal data class ExploreUiState(
    val highlights: List<AmenityHighlight> = emptyList(),
)

internal sealed interface ExploreActionResult {
    data class NavigateToMap(
        val route: String,
        val floorId: String,
        val floorLabel: String,
    ) : ExploreActionResult

    data class Feedback(val message: String) : ExploreActionResult
}
