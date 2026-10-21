package nl.rhaydus.softcover.showcase.live.share.mapper

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.share.ShareCaptureProgress
import nl.rhaydus.softcover.core.component.share.ShareCardSheetEvent
import nl.rhaydus.softcover.core.component.share.ShareCardSheetUiModel
import nl.rhaydus.softcover.core.component.share.ShareCardUiModel
import nl.rhaydus.softcover.core.component.share.withShareCapture
import nl.rhaydus.softcover.core.component.sheet.SheetActionRowUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.showcase.live.share.model.ShareDemo

internal fun shareLiveTopBar(): TopBarUiModel = TopBarUiModel(title = "Share")

internal fun shareDemos(): ImmutableList<SheetActionRowUiModel<ShareDemo>> = persistentListOf(
    SheetActionRowUiModel(
        key = ShareDemo.BOOK,
        label = "A book",
        description = "Cover, stats and a short description on one card.",
        icon = SoftcoverIcon.MenuBook,
    ),
    SheetActionRowUiModel(
        key = ShareDemo.READING_UPDATE,
        label = "A finished book",
        description = "Rating, tags and a review with its spoiler concealed.",
        icon = SoftcoverIcon.Check,
    ),
    SheetActionRowUiModel(
        key = ShareDemo.QUOTE,
        label = "A quote",
        description = "A passage set on its own card.",
        icon = SoftcoverIcon.Share,
    ),
)

internal fun shareDemoModel(demo: ShareDemo): ShareCardSheetUiModel = ShareCardSheetUiModel(
    card = ShareCardUiModel.previews.getValue(
        when (demo) {
            ShareDemo.BOOK -> "Book"
            ShareDemo.READING_UPDATE -> "Reading update (finished)"
            ShareDemo.QUOTE -> "Quote"
        },
    ),
    scaffold = SheetScaffoldUiModel(
        header = SheetHeaderUiModel(
            section = SectionHeaderUiModel.Section(
                eyebrow = "Share",
                headline = "A card to keep",
            ),
        ),
        dismissLabel = "Done",
    ).withShareCapture(progress = ShareCaptureProgress.Idle),
    displayName = "softcover-card",
)

internal fun ShareCardSheetUiModel.reducing(event: ShareCardSheetEvent): ShareCardSheetUiModel {
    val progress = when (event) {
        ShareCardSheetEvent.ShareStarted -> ShareCaptureProgress.Sharing
        ShareCardSheetEvent.SaveStarted -> ShareCaptureProgress.Saving
        ShareCardSheetEvent.CaptureFinished,
        ShareCardSheetEvent.Shared,
        ShareCardSheetEvent.Dismissed -> ShareCaptureProgress.Idle
    }

    return copy(scaffold = scaffold.withShareCapture(progress = progress))
}
