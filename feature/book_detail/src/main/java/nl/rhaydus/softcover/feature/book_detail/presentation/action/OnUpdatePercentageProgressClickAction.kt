package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState

internal data class OnUpdatePercentageProgressClickAction(
    val newPercentage: String,
) : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        val bookToUpdate: Book = scope.currentState.book ?: return

        val newPercentageValue: Double = newPercentage.toDoubleOrNull() ?: 0.0
        val fraction = (newPercentageValue / 100.0).coerceIn(
            0.0,
            1.0,
        )

        val edition = bookToUpdate.currentEdition ?: run {
            scope.setState { it.copy(showUpdateProgressSheet = false) }
            return
        }
        val isAudiobook = edition.isAudiobook

        val newPage: Int? = if (isAudiobook) {
            null
        } else {
            val total = edition.pages ?: bookToUpdate.defaultEdition?.pages ?: 0
            (fraction * total).toInt()
        }

        val newSeconds: Int? = if (isAudiobook) {
            val total = edition.audioSeconds ?: 0
            (fraction * total).toInt()
        } else {
            null
        }

        dependencies.launch {
            dependencies.recordBookProgressUseCase(
                book = bookToUpdate,
                newPage = newPage,
                newSeconds = newSeconds,
            )
        }

        scope.setState {
            it.copy(showUpdateProgressSheet = false)
        }
    }
}
