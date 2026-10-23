package nl.rhaydus.softcover.showcase.live.sheet.mapper

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.LoadingSheetUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetActionRowUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetFooterButton
import nl.rhaydus.softcover.core.component.sheet.SheetFooterEmphasis
import nl.rhaydus.softcover.core.component.sheet.SheetFooterUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetLayout
import nl.rhaydus.softcover.core.component.sheet.SheetRowGroupUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetRowUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.showcase.live.sheet.model.SheetDemo
import nl.rhaydus.softcover.showcase.live.sheet.model.SheetDemoUiModel

private val genres = listOf(
    "Fantasy",
    "Science fiction",
    "Mystery",
    "Gothic",
    "Historical fiction",
    "Literary fiction",
    "Horror",
    "Romance",
    "Thriller",
    "Poetry",
    "Memoir",
    "Biography",
    "Essays",
    "Travel",
    "Nature writing",
    "Philosophy",
    "Classics",
    "Short stories",
    "Graphic novels",
    "Young adult",
)

internal fun sheetLiveTopBar(): TopBarUiModel = TopBarUiModel(title = "Sheet")

internal fun sheetDemos(): ImmutableList<SheetActionRowUiModel<SheetDemo>> = persistentListOf(
    SheetActionRowUiModel(
        key = SheetDemo.SHORT,
        label = "Short sheet",
        description = "A header and a footer around a few lines.",
        icon = SoftcoverIcon.Bookmark,
    ),
    SheetActionRowUiModel(
        key = SheetDemo.LONG,
        label = "Long sheet",
        description = "A pinned header and footer over a list that scrolls.",
        icon = SoftcoverIcon.LibraryBooks,
    ),
    SheetActionRowUiModel(
        key = SheetDemo.LOADING,
        label = "Loading sheet",
        description = "A blocking sheet that closes itself once the wait is over.",
        icon = SoftcoverIcon.History,
    ),
    SheetActionRowUiModel(
        key = SheetDemo.ERROR,
        label = "Error sheet",
        description = "A sheet that says what went wrong and offers a way out.",
        icon = SoftcoverIcon.Info,
    ),
)

internal fun sheetDemoModel(demo: SheetDemo): SheetDemoUiModel = when (demo) {
    SheetDemo.SHORT -> SheetDemoUiModel.Scrolling(
        scaffold = SheetScaffoldUiModel(
            header = header(
                eyebrow = "Reading progress",
                headline = "Piranesi",
            ),
            footer = SheetFooterUiModel.Stacked(
                primary = SheetFooterButton(
                    label = "Save",
                    dismissesSheet = true,
                ),
                secondary = SheetFooterButton(
                    label = "Not now",
                    dismissesSheet = true,
                ),
            ),
        ),
        body = "The House has many halls, and the tides come and go. Swipe down, tap outside or press either button " +
            "to close this sheet.",
    )

    SheetDemo.LONG -> SheetDemoUiModel.Pinned(
        scaffold = SheetScaffoldUiModel(
            header = header(
                eyebrow = "Genres",
                headline = "Pick a genre",
            ),
            footer = SheetFooterUiModel.Inline(
                primary = SheetFooterButton(
                    label = "Apply",
                    dismissesSheet = true,
                ),
                secondary = SheetFooterButton(
                    label = "Clear",
                    emphasis = SheetFooterEmphasis.Outlined,
                ),
            ),
            layout = SheetLayout.Pinned,
        ),
        rows = SheetRowGroupUiModel(
            rows = genres
                .map { genre ->
                    SheetRowUiModel(
                        key = genre,
                        label = genre,
                        selected = genre == genres.first(),
                    )
                }
                .toImmutableList(),
            dismissesSheet = false,
        ),
    )

    SheetDemo.LOADING -> SheetDemoUiModel.Loading(
        sheet = LoadingSheetUiModel(
            scaffold = SheetScaffoldUiModel(
                header = SheetHeaderUiModel(
                    section = SectionHeaderUiModel.Section(
                        eyebrow = "Setting up",
                        headline = "Getting your shelves ready",
                        description = "Pulling your library from Hardcover.",
                    ),
                ),
                dismissible = false,
            ),
            progress = 0f,
        ),
    )

    SheetDemo.ERROR -> SheetDemoUiModel.Scrolling(
        scaffold = SheetScaffoldUiModel(
            header = header(
                eyebrow = "Something went wrong",
                headline = "The shelf would not load",
            ),
            footer = SheetFooterUiModel.Stacked(
                primary = SheetFooterButton(
                    label = "Try again",
                    dismissesSheet = true,
                ),
                secondary = SheetFooterButton(
                    label = "Cancel",
                    dismissesSheet = true,
                    emphasis = SheetFooterEmphasis.Outlined,
                ),
            ),
        ),
        body = "Hardcover did not answer. Check your connection and try again.",
    )
}

internal fun SheetDemoUiModel.selecting(key: String): SheetDemoUiModel = when (this) {
    is SheetDemoUiModel.Pinned -> copy(
        rows = rows.copy(
            rows = rows.rows
                .map { it.copy(selected = it.key == key) }
                .toImmutableList(),
        ),
    )

    is SheetDemoUiModel.Scrolling,
    is SheetDemoUiModel.Loading -> this
}

internal fun SheetDemoUiModel.cleared(): SheetDemoUiModel = when (this) {
    is SheetDemoUiModel.Pinned -> copy(
        rows = rows.copy(
            rows = rows.rows
                .map { it.copy(selected = false) }
                .toImmutableList(),
        ),
    )

    is SheetDemoUiModel.Scrolling,
    is SheetDemoUiModel.Loading -> this
}

internal fun SheetDemoUiModel.withProgress(progress: Float): SheetDemoUiModel = when (this) {
    is SheetDemoUiModel.Loading -> copy(sheet = sheet.copy(progress = progress))

    is SheetDemoUiModel.Scrolling,
    is SheetDemoUiModel.Pinned -> this
}

private fun header(
    eyebrow: String,
    headline: String,
): SheetHeaderUiModel = SheetHeaderUiModel(
    section = SectionHeaderUiModel.Section(
        eyebrow = eyebrow,
        headline = headline,
    ),
)
