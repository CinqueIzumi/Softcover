package nl.rhaydus.softcover.feature.library.presentation.mapper

import nl.rhaydus.softcover.core.component.state.EmptyStateUiModel
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.model.LibraryTab

internal fun LibraryTab.toEmptyStateUiModel(): EmptyStateUiModel {
    val isDnf = this is LibraryTab.Status && status == UserBookStatus.DID_NOT_FINISH

    return if (isDnf) {
        EmptyStateUiModel(
            title = "Nothing abandoned",
            body = "Books you stop reading end up here. So far, none.",
        )
    } else {
        EmptyStateUiModel(
            title = "An empty shelf",
            body = "Nothing on your $label list yet. " +
                "Books you add will show up here.",
        )
    }
}
