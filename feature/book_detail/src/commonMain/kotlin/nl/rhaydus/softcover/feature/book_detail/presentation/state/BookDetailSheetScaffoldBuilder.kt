package nl.rhaydus.softcover.feature.book_detail.presentation.state

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.control.SegmentUiModel
import nl.rhaydus.softcover.core.component.control.SegmentedControlUiModel
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.header.HeaderTextSegment
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.share.ShareCaptureProgress
import nl.rhaydus.softcover.core.component.share.ShareCardSheetUiModel
import nl.rhaydus.softcover.core.component.share.ShareCardUiModel
import nl.rhaydus.softcover.core.component.share.withShareCapture
import nl.rhaydus.softcover.core.component.sheet.SheetFooterButton
import nl.rhaydus.softcover.core.component.sheet.SheetFooterUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderTrailing
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetLayout
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.domain.model.BookEdition

internal fun tagEditorSheetScaffoldFor(
    bookTitle: String,
    cover: CoverUiModel?,
): SheetScaffoldUiModel =
    SheetScaffoldUiModel(
        header = SheetHeaderUiModel(
            section = SectionHeaderUiModel.Section(
                eyebrow = "Your tags",
                headline = persistentListOf(HeaderTextSegment.Plain(text = "Add your own tags.")),
                description = persistentListOf(
                    HeaderTextSegment.Plain(text = "Your own genres, moods and notes on "),
                    HeaderTextSegment.Emphasised(text = bookTitle),
                    HeaderTextSegment.Plain(text = ". Mark any as a spoiler to keep it off shared cards."),
                ),
            ),
            trailing = cover?.let { SheetHeaderTrailing.Jacket(cover = it) },
        ),
        dismissLabel = "Done",
        layout = SheetLayout.Pinned,
    )

internal fun editionSheetScaffoldFor(
    bookTitle: String,
    currentEdition: BookEdition,
    selectedEdition: BookEdition,
    cover: CoverUiModel?,
    isLoading: Boolean,
): SheetScaffoldUiModel =
    SheetScaffoldUiModel(
        header = SheetHeaderUiModel(
            section = SectionHeaderUiModel.Section(
                eyebrow = "Editions",
                headline = "Change edition",
                description = currentEdition.authorString.let { author ->
                    if (author.isBlank()) bookTitle else "$bookTitle · $author"
                },
            ),
            trailing = cover?.let { SheetHeaderTrailing.Jacket(cover = it) },
        ),
        footer = SheetFooterUiModel.Stacked(
            primary = SheetFooterButton(
                label = "Track this edition",
                enabled = isLoading.not() && selectedEdition != currentEdition,
                dismissesSheet = true,
            ),
            secondary = null,
        ),
        layout = SheetLayout.Pinned,
    )

internal fun shareSheetFor(
    card: ShareCardUiModel,
    bookTitle: String,
    variant: ShareCardVariant,
    hasUpdateCard: Boolean,
    progress: ShareCaptureProgress = ShareCaptureProgress.Idle,
): ShareSheetUiModel =
    ShareSheetUiModel(
        sheet = ShareCardSheetUiModel(
            card = card,
            scaffold = shareSheetScaffoldFor(
                bookTitle = bookTitle,
                variant = variant,
            ).withShareCapture(progress = progress),
            displayName = bookTitle,
        ),
        variantToggle = if (hasUpdateCard) {
            SegmentedControlUiModel(
                options = ShareCardVariant.entries
                    .map { option ->
                        SegmentUiModel(
                            key = option,
                            label = option.label,
                        )
                    }
                    .toImmutableList(),
                selected = variant,
            )
        } else {
            null
        },
    )

private fun shareSheetScaffoldFor(
    bookTitle: String,
    variant: ShareCardVariant,
): SheetScaffoldUiModel =
    SheetScaffoldUiModel(
        header = SheetHeaderUiModel(
            section = SectionHeaderUiModel.Section(
                eyebrow = "Share",
                headline = "Share this book",
                description = when (variant) {
                    ShareCardVariant.MY_UPDATE -> "Your reading update for $bookTitle"
                    ShareCardVariant.BOOK -> "A share card for $bookTitle"
                },
            ),
        ),
    )
