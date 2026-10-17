package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.tagNamingFieldFor
import nl.rhaydus.toad.ActionScope

internal class TagNamingFieldCollector : BookDetailCollector {
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        scope.state
            .map { state ->
                tagNamingFieldFor(
                    input = state.tagEditorInput,
                    category = state.tagEditorCategory,
                    addPill = state.tagAddPill,
                )
            }
            .distinctUntilChanged()
            .collectLatest { field ->
                scope.setState {
                    it.copy(
                        tagNamingField = it.tagNamingField.copy(
                            placeholder = field.placeholder,
                            variant = field.variant,
                        ),
                    )
                }
            }
    }
}
