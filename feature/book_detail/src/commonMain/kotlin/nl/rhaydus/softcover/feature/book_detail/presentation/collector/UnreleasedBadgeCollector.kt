package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import nl.rhaydus.common.currentLocalDate
import nl.rhaydus.softcover.core.uibinding.date.formatLongDate
import nl.rhaydus.softcover.core.uibinding.release.toEditionUnreleasedBadgeUiModel
import nl.rhaydus.softcover.core.uibinding.release.toProminentUnreleasedBadgeUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

internal class UnreleasedBadgeCollector : BookDetailCollector {
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        scope.state
            .map { it.book?.releaseDate to it.displayedEdition?.releaseDate }
            .distinctUntilChanged()
            .collectLatest { (bookDate, editionDate) ->
                val today = currentLocalDate()

                val (badge, caption) = when {
                    bookDate != null && editionDate != null && editionDate > today && editionDate > bookDate ->
                        editionDate.toEditionUnreleasedBadgeUiModel() to bookDate.toReleaseCaption(today = today)

                    bookDate != null && bookDate > today -> bookDate.toProminentUnreleasedBadgeUiModel() to null

                    else -> null to null
                }

                scope.setState { state ->
                    state.copy(
                        unreleasedBadge = badge,
                        releaseCaption = caption,
                    )
                }
            }
    }
}

private fun LocalDate.toReleaseCaption(today: LocalDate): String {
    val lead = if (this > today) "First edition out" else "First published"

    return "$lead ${formatLongDate()}."
}
