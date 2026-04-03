package dev.orestegabo.kaze.presentation.explore

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dev.orestegabo.kaze.data.repository.ExperienceRepository
import dev.orestegabo.kaze.domain.experience.AmenityHighlight
import dev.orestegabo.kaze.presentation.util.runImmediateSuspend

internal class ExploreViewModel(
    private val hotelId: String,
    private val experienceRepository: ExperienceRepository,
) : ViewModel() {
    var uiState by mutableStateOf(ExploreUiState())
        private set

    init {
        uiState = uiState.copy(
            highlights = runImmediateSuspend {
                experienceRepository.getAmenityHighlights(hotelId)
            },
        )
    }

    fun onHighlightAction(highlight: AmenityHighlight): ExploreActionResult =
        when (highlight.actionLabel) {
            "Open amenity map", "Open amenity", "Start route" -> ExploreActionResult.NavigateToMap(
                route = "Arrival route to ${highlight.locationLabel}",
                floorId = "l1",
                floorLabel = highlight.locationLabel,
            )
            else -> ExploreActionResult.Feedback("${highlight.title} reserved.")
        }

    fun reserveExperience(): ExploreActionResult.Feedback =
        ExploreActionResult.Feedback("Experience reserved.")

    fun openPoolDeckRoute(): ExploreActionResult.NavigateToMap =
        ExploreActionResult.NavigateToMap(
            route = "Arrival route to Pool Deck",
            floorId = "l1",
            floorLabel = "Pool Deck",
        )
}
