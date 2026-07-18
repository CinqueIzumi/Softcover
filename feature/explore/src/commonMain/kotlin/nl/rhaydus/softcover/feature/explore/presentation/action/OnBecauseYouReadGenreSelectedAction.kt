package nl.rhaydus.softcover.feature.explore.presentation.action

import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope

/**
 * Picks a "because you read" genre from the dropdown (explore-3a feedback item 10). [genre] null
 * clears the override, falling back to the auto-derived most-read genre. On success the persisted
 * choice flows back into state reactively through `BecauseYouReadCollector`, which clears the
 * loading flag this action sets. On failure that collector never re-fires (the preference flow
 * never emits a new value), so this action clears the flag itself - matching
 * `GetBecauseYouReadBooksUseCase`'s "never surfaces as an error" contract for this section rather
 * than leaving the rail spinning forever.
 *
 * The initial `setState` also updates [ExploreScreenUiState.becauseYouReadGenre] and clears
 * [ExploreScreenUiState.becauseYouReadBooks] optimistically, before `setBecauseYouReadGenreUseCase`
 * even runs. Without this the headline/picker and the rail's contents would keep showing the OLD
 * genre and OLD books for the entire network fetch that follows - the section already sets
 * `loadingBecauseYouReadBooks = true`, but the UI only reads that flag to choose skeleton-vs-list,
 * not to hide the stale headline or list, so the switch would look like it did nothing until the
 * fetch resolves. [genre] null (the "Auto" option) has no genre of its own to show immediately, so
 * it resolves the display genre to the current top pick in [ExploreScreenUiState.becauseYouReadGenreOptions]
 * (falling back to whatever genre is already showing if that list is empty) rather than blanking
 * the headline for the duration of the fetch.
 *
 * No-op guard: [GetBecauseYouReadBooksUseCase] derives its genre from `distinctUntilChanged()` on
 * BOTH the persisted-preference flow and its own EFFECTIVE-genre computation, so re-selecting a
 * genre that resolves to the genre already in effect never causes a re-emission - there's nothing
 * downstream to clear the `loadingBecauseYouReadBooks` flag this action would otherwise set. That's
 * reachable two ways: tapping the currently-selected row (raw and effective genre both unchanged),
 * or tapping "Auto" while pinned to [ExploreScreenUiState.becauseYouReadGenreOptions]'s top genre,
 * or vice versa (the raw selection changes - null vs a name - but resolves to the same effective
 * genre, so the use case's `distinctUntilChanged` still swallows it even though the preference flow
 * itself emits). [target] is this selection's resulting effective genre, computed with the same
 * "null falls back to the top option" rule the use case applies; when it matches what's already
 * showing, this returns without setting the loading flag, clearing the books, or persisting -
 * nothing would change and no fetch would follow to ever clear the flag again.
 */
internal data class OnBecauseYouReadGenreSelectedAction(val genre: String?) : ExploreAction {
    override suspend fun execute(
        dependencies: ExploreDependencies,
        scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
    ) {
        val target = genre ?: scope.currentState.becauseYouReadGenreOptions.firstOrNull()
        if (target == scope.currentState.becauseYouReadGenre) return

        scope.setState {
            it.copy(
                becauseYouReadGenre = target ?: it.becauseYouReadGenre,
                becauseYouReadBooks = emptyList(),
                loadingBecauseYouReadBooks = true,
            )
        }

        dependencies.setBecauseYouReadGenreUseCase(genre = genre)
            .onFailure {
                scope.setState { it.copy(loadingBecauseYouReadBooks = false) }
            }
    }
}
