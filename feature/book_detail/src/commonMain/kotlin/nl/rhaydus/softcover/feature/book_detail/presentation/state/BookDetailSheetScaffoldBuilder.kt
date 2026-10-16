package nl.rhaydus.softcover.feature.book_detail.presentation.state

import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.chip.ChipTone
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.chip.toChipSet
import nl.rhaydus.softcover.core.component.control.SegmentUiModel
import nl.rhaydus.softcover.core.component.control.SegmentedControlUiModel
import nl.rhaydus.softcover.core.component.control.SegmentedControlVariant
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
    size: ShareCardSize,
    hasExpandableText: Boolean,
    tagPicker: ShareTagPickerUiModel?,
    progress: ShareCaptureProgress = ShareCaptureProgress.Idle,
): ShareSheetUiModel =
    ShareSheetUiModel(
        sheet = ShareCardSheetUiModel(
            card = card,
            scaffold = shareSheetScaffoldFor(bookTitle = bookTitle).withShareCapture(progress = progress),
            displayName = bookTitle,
            previewMaxHeight = if (tagPicker?.expanded == true) 280.dp else 440.dp,
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
                variant = SegmentedControlVariant.Prose(
                    prefix = "A card for ",
                    suffix = ".",
                ),
            )
        } else {
            null
        },
        options = buildList {
            if (hasExpandableText) {
                add(
                    ChipUiModel(
                        key = SHARE_FULL_TEXT_KEY,
                        label = ShareCardSize.FULL_TEXT.label,
                        tone = ChipTone.Choice,
                        selected = size == ShareCardSize.FULL_TEXT,
                    ) to ShareSheetOption.FULL_TEXT,
                )
            }

            if (tagPicker != null) add(tagPicker.header to ShareSheetOption.TAGS)
        }.toChipSet(),
        tagPicker = tagPicker,
    )

private const val SHARE_FULL_TEXT_KEY = "share-full-text"

private fun shareSheetScaffoldFor(bookTitle: String): SheetScaffoldUiModel =
    SheetScaffoldUiModel(
        header = SheetHeaderUiModel(
            section = SectionHeaderUiModel.Section(
                eyebrow = "Share",
                headline = bookTitle,
            ),
        ),
        layout = SheetLayout.Pinned,
    )

internal fun deleteQuoteSheetScaffold(): SheetScaffoldUiModel =
    SheetScaffoldUiModel(
        header = SheetHeaderUiModel(
            section = SectionHeaderUiModel.Section(
                eyebrow = "Delete quote",
                headline = "Delete this quote?",
                description = "It comes off your Hardcover account too, and you can't undo this.",
            ),
        ),
        footer = SheetFooterUiModel.Stacked(
            primary = SheetFooterButton(
                label = "Delete",
                dismissesSheet = true,
            ),
            secondary = SheetFooterButton(
                label = "Cancel",
                dismissesSheet = true,
            ),
        ),
    )
