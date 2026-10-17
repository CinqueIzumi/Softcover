package nl.rhaydus.softcover.feature.reading.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.uibinding.richtext.toRichTextUiModel
import nl.rhaydus.softcover.core.uibinding.verdict.VerdictSheetContext
import nl.rhaydus.softcover.core.uibinding.verdict.carryingDraftOf
import nl.rhaydus.softcover.core.uibinding.verdict.toVerdictSheetUiModel
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope

/**
 * Maps the book the verdict prompt is open for, with its review, and its verdict cover to the sheet's UI
 * model off the composition (R9), where the two converge.
 *
 * A rebuild keeps the draft the reader has already edited.
 *
 * Deriving it here means `verdictSheet` is null exactly when `verdictPromptBook` is, without the
 * actions that clear the prompt having to remember the model too. Reading only ever raises the prompt
 * after a finish, so the context is always `FINISHED`.
 */
internal class VerdictSheetCollector : ReadingCollector {
    override suspend fun onLaunch(
        scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>,
        dependencies: ReadingScreenDependencies,
    ) {
        scope.state
            .map {
                VerdictSheetSnapshot(
                    book = it.verdictPromptBook,
                    cover = it.verdictCover,
                )
            }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                val book = snapshot.book

                scope.setState {
                    it.copy(
                        verdictSheet = book
                            ?.toVerdictSheetUiModel(
                                context = VerdictSheetContext.FINISHED,
                                review = book.userBook?.reviewDocument?.toRichTextUiModel(),
                                cover = snapshot.cover,
                            )
                            ?.carryingDraftOf(previous = it.verdictSheet),
                    )
                }
            }
    }
}
