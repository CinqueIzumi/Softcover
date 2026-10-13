package nl.rhaydus.softcover.feature.explore.presentation.collector

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.sheet.SheetRowGroupUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetRowUiModel
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.model.becauseYouReadGenreLabelFor
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope

internal class BecauseYouReadGenreSheetCollector : ExploreCollector {
    override suspend fun onLaunch(
        scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
        dependencies: ExploreDependencies,
    ) {
        scope.state
            .map {
                GenreSheetSnapshot(
                    genre = it.becauseYouReadGenre,
                    options = it.becauseYouReadGenreOptions,
                )
            }
            .distinctUntilChanged()
            .collectLatest { (genre, options) ->
                val rows = becauseYouReadGenreRowsFor(
                    genre = genre,
                    options = options,
                )

                scope.setState {
                    it.copy(
                        becauseYouReadGenreControl = it.becauseYouReadGenreControl.copy(
                            label = becauseYouReadGenreLabelFor(genre),
                            hasOptions = options.isNotEmpty(),
                            genreGroup = SheetRowGroupUiModel(
                                rows = rows,
                            ),
                        ),
                    )
                }
            }
    }
}

internal fun becauseYouReadGenreRowsFor(
    genre: String?,
    options: List<String>,
): ImmutableList<SheetRowUiModel<String?>> =
    buildList<SheetRowUiModel<String?>> {
        add(
            SheetRowUiModel(
                key = null,
                label = "Auto (most-read)",
                selected = options.none { it == genre },
            ),
        )

        options.forEach { option ->
            add(
                SheetRowUiModel(
                    key = option,
                    label = option,
                    selected = option == genre,
                ),
            )
        }
    }.toImmutableList()
