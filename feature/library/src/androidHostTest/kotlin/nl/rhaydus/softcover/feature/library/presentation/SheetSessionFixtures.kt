package nl.rhaydus.softcover.feature.library.presentation

import io.kotest.matchers.nulls.shouldNotBeNull
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.lists.ChooseListsUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.feature.library.presentation.collector.ChooseListsSnapshot
import nl.rhaydus.softcover.feature.library.presentation.collector.toUiModel
import nl.rhaydus.softcover.feature.library.presentation.state.CreateList
import nl.rhaydus.softcover.feature.library.presentation.state.ManageShelves
import nl.rhaydus.softcover.feature.library.presentation.state.ShelvesSheetUiModel
import nl.rhaydus.softcover.feature.library.presentation.state.bulkRemoveSheetScaffoldFor
import nl.rhaydus.softcover.feature.library.presentation.state.shelvesSheetUiModel

internal fun shelvesSheetModel(): ShelvesSheetUiModel = shelvesSheetUiModel(rows = persistentListOf())

internal fun openShelvesSession(
    closing: Boolean = false,
    pending: ManageShelves? = null,
): SheetSession<ShelvesSheetUiModel, ManageShelves> = SheetSession.Open(
    model = shelvesSheetModel().closedIf(closing = closing),
    pending = pending,
)

internal fun bulkRemoveModel(
    bookCount: Int = 1,
    inProgress: Boolean = false,
): SheetScaffoldUiModel = bulkRemoveSheetScaffoldFor(
    bookCount = bookCount,
    inProgress = inProgress,
)

internal fun openBulkRemoveSession(
    closing: Boolean = false,
    bookCount: Int = 1,
): SheetSession<SheetScaffoldUiModel, Nothing> = SheetSession.Open(
    model = bulkRemoveModel(bookCount = bookCount).closedIf(closing = closing),
)

internal fun chooseListsModel(selectedBookIds: Set<Int> = setOf(1)): ChooseListsUiModel = ChooseListsSnapshot(
    selectedBookIds = selectedBookIds,
    customLists = emptyList(),
    listsBeingMutated = emptySet(),
    jacketBooks = emptyList(),
).toUiModel(previous = null).shouldNotBeNull()

internal fun openChooseListsSession(
    closing: Boolean = false,
    pending: CreateList? = null,
): SheetSession<ChooseListsUiModel, CreateList> = SheetSession.Open(
    model = chooseListsModel().closedIf(closing = closing),
    pending = pending,
)

private fun <Model : SheetModel<Model>> Model.closedIf(closing: Boolean): Model =
    if (closing) requestingClose() else this
