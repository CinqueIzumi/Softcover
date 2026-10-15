package nl.rhaydus.softcover.feature.library.presentation.mapper

import nl.rhaydus.softcover.core.component.state.EmptyStateUiModel
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.model.LibraryTab

internal fun LibraryTab.toEmptyStateUiModel(): EmptyStateUiModel {
    val shelfStatus = (this as? LibraryTab.Status)?.status

    return when (shelfStatus) {
        UserBookStatus.DID_NOT_FINISH -> EmptyStateUiModel(
            title = "Nothing abandoned",
            body = "Books you stop reading end up here. So far, none.",
        )
        UserBookStatus.PAUSED -> EmptyStateUiModel(
            title = "Nothing on hold",
            body = "Books you set aside wait here until you pick them back up.",
        )
        else -> EmptyStateUiModel(
            title = "An empty shelf",
            body = "Nothing on your $label list yet. " +
                "Books you add will show up here.",
        )
    }
}
