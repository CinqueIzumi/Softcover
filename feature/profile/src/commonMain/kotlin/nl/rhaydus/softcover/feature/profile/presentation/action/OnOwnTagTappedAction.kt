package nl.rhaydus.softcover.feature.profile.presentation.action

import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.StatsAtlasDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasLocalVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState
import nl.rhaydus.toad.ActionScope

/** Nothing happens while the rows are masked stand-ins, or for a tag the ranking does not hold. */
internal class OnOwnTagTappedAction(
    private val tagName: String,
) : StatsAtlasAction {
    override suspend fun execute(
        dependencies: StatsAtlasDependencies,
        scope: ActionScope<StatsAtlasUiState, StatsAtlasEvent, StatsAtlasLocalVariables>,
    ) {
        val inYourWords = scope.currentState.inYourWords

        if (inYourWords.isLoading) return

        val ownTags = inYourWords.ownTags ?: return

        if (ownTags.ranking.rows.none { it.key == tagName }) return

        scope.sendEvent(event = StatsAtlasEvent.OpenLibraryForTag(tagName = tagName))
    }
}
