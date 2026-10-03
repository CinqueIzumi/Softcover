package nl.rhaydus.softcover.feature.library.presentation.mapper

import nl.rhaydus.softcover.core.component.state.EmptyStateUiModel
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.model.LibraryTab

internal fun LibraryTab.toEmptyStateUiModel(): EmptyStateUiModel {
    val isDnf = this is LibraryTab.Status && status == UserBookStatus.DID_NOT_FINISH

    return if (isDnf) {
        EmptyStateUiModel(
            title = "Nothing set aside",
            body = "No books abandoned here — long may it stay that way.",
        )
    } else {
        EmptyStateUiModel(
            title = "An empty shelf",
            body = "Nothing rests on your $label list yet. " +
                "Find a title worth keeping and it will live here.",
        )
    }
}
