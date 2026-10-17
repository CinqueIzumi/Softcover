package nl.rhaydus.softcover.feature.settings.presentation.model

import nl.rhaydus.softcover.core.component.control.ToggleUiModel
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.model.LibraryTab

internal sealed class LibraryTabEntry(
    val id: String,
    val label: String,
    val count: Int,
    val isReorderable: Boolean,
    val canHide: Boolean,
    val isList: Boolean,
    val visibilityToggle: ToggleUiModel?,
) {
    data class All(
        val totalCount: Int,
    ) : LibraryTabEntry(
        id = ALL_ID,
        label = "All",
        count = totalCount,
        isReorderable = false,
        canHide = false,
        isList = false,
        visibilityToggle = null,
    )

    data class Status(
        val status: UserBookStatus,
        val statusLabel: String,
        val statusCount: Int,
        val statusVisibilityToggle: ToggleUiModel?,
    ) : LibraryTabEntry(
        id = LibraryTab.Status.idFor(status = status),
        label = statusLabel,
        count = statusCount,
        isReorderable = true,
        canHide = status.isAlwaysVisibleInLibrary.not(),
        isList = false,
        visibilityToggle = statusVisibilityToggle,
    )

    data class CustomList(
        val listId: Int,
        val listName: String,
        val listCount: Int,
        val listVisibilityToggle: ToggleUiModel,
    ) : LibraryTabEntry(
        id = LibraryTab.CustomList.idFor(listId = listId),
        label = listName,
        count = listCount,
        isReorderable = true,
        canHide = true,
        isList = true,
        visibilityToggle = listVisibilityToggle,
    )

    companion object {
        val ALL_ID: String = LibraryTab.All.id
    }
}
