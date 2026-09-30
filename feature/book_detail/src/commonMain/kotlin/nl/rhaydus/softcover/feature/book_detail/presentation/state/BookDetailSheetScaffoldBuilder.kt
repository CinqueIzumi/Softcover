package nl.rhaydus.softcover.feature.book_detail.presentation.state

import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.header.EmphasisTreatment
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.header.TextEmphasis
import nl.rhaydus.softcover.core.component.sheet.SheetFooterButton
import nl.rhaydus.softcover.core.component.sheet.SheetFooterLayout
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
                headline = "Tag it your way.",
                description = "Your own genres, moods and notes on $bookTitle. " +
                    "Mark any as a spoiler to keep it off shared cards.",
                descriptionEmphasis = TextEmphasis(
                    substring = bookTitle,
                    treatment = EmphasisTreatment.Accent,
                ),
            ),
            trailing = cover?.let { SheetHeaderTrailing.Jacket(cover = it) },
        ),
        topAction = "Done",
        footer = null,
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
        topAction = null,
        footer = SheetFooterUiModel(
            primary = SheetFooterButton(
                label = "Track this edition",
                enabled = isLoading.not() && selectedEdition != currentEdition,
            ),
            secondary = null,
            layout = SheetFooterLayout.Stacked,
        ),
        layout = SheetLayout.Pinned,
    )

internal fun shareSheetBodyFor(
    variant: ShareCardVariant,
    hasUpdateCard: Boolean,
    isSharing: Boolean,
    isSavingToGallery: Boolean,
): ShareSheetBodyUiModel =
    ShareSheetBodyUiModel(
        variant = variant,
        shareLabel = if (isSharing) "Sharing…" else "Share",
        saveToGalleryLabel = if (isSavingToGallery) "Saving to gallery…" else "Save to gallery",
        isBusy = isSharing || isSavingToGallery,
        hasVariantToggle = hasUpdateCard,
    )

internal fun shareSheetScaffoldFor(
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
                    ShareCardVariant.BOOK -> "An editorial card of $bookTitle"
                },
            ),
        ),
        topAction = null,
        footer = null,
        layout = SheetLayout.Scrolling,
    )
