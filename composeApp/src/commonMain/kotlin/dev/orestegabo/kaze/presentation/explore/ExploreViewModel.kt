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
            highlights = runCatching {
                runImmediateSuspend {
                    experienceRepository.getAmenityHighlights(hotelId)
                }
            }.getOrDefault(emptyList()),
        )
    }

    fun onHighlightAction(highlight: AmenityHighlight): ExploreActionResult =
        when (highlight.actionLabel) {
            "Open amenity map", "Open venue map", "Start route" ->
                ExploreActionResult.Feedback("Indoor maps are not ready for this place yet. Use the venue details for now.")
            "Open amenity" ->
                ExploreActionResult.Feedback("${highlight.title} details are available from the venue.")
            else -> ExploreActionResult.Feedback("${highlight.title} saved for this event journey.")
        }

    fun reserveExperience(): ExploreActionResult.Feedback =
        ExploreActionResult.Feedback("Choose a venue from Home to start a reservation request.")

    fun openPoolDeckRoute(): ExploreActionResult.Feedback =
        ExploreActionResult.Feedback("Detailed indoor routes are not ready for this place yet.")
}
