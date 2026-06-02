package nl.rhaydus.softcover.feature.reading.presentation.action

import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import timber.log.Timber

data class OnUpdatePercentageProgressClickAction(
    val newPercentage: String,
) : ReadingAction {
    override suspend fun execute(
        dependencies: ReadingScreenDependencies,
        scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>,
    ) {
        val bookToUpdate: Book = scope.currentState.bookToUpdate ?: return

        val newPercentageValue: Double = newPercentage.toDoubleOrNull() ?: 0.0
        val fraction = (newPercentageValue / 100.0).coerceIn(0.0, 1.0)

        val edition = bookToUpdate.currentEdition ?: run {
            scope.setState {
                it.copy(
                    showProgressSheet = false,
                    bookToUpdate = null,
                )
            }
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

        scope.currentLocalVariables.bookMutationJobs[bookToUpdate.id]?.cancel()

        val job = dependencies.launch {
            dependencies.recordBookProgressUseCase(
                book = bookToUpdate,
                newPage = newPage,
                newSeconds = newSeconds,
            ).onFailure { error ->
                Timber.e("$error")

                scope.setState {
                    it.copy(failedMutationBookIds = it.failedMutationBookIds + bookToUpdate.id)
                }
            }
        }

        scope.setLocalVariables {
            it.copy(bookMutationJobs = it.bookMutationJobs + (bookToUpdate.id to job))
        }

        scope.setState {
            it.copy(
                showProgressSheet = false,
                bookToUpdate = null,
            )
        }
    }
}
