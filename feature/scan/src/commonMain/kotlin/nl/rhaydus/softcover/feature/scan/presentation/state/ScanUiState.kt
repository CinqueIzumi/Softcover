package nl.rhaydus.softcover.feature.scan.presentation.state

import nl.rhaydus.softcover.core.component.field.TextFieldUiModel
import nl.rhaydus.softcover.core.component.field.TextFieldVariant
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.toad.UiState

private const val SCAN_TITLE = "Scan a barcode"

internal data class ScanUiState(
    val isResolving: Boolean = false,
    val unknownIsbn: String? = null,
    val isAddingBook: Boolean = false,

    /**
     * The manual ISBN entry. Its text is the stored draft; `IsbnFieldCollector` keeps `enabled` in step
     * with the in-flight flags.
     */
    val isbnField: TextFieldUiModel = TextFieldUiModel(
        key = "isbn",
        text = "",
        placeholder = "e.g. 9780374710781",
        variant = TextFieldVariant.Outlined(numeric = true),
        autoFocus = manualEntryAutoFocuses,
        submittable = true,
    ),

    /** Asks the shown unknown-ISBN sheet to hide; cleared by its `Dismissed` event. */
    val isUnknownIsbnCloseRequested: Boolean = false,
    val unknownIsbnSheet: SheetScaffoldUiModel = unknownIsbnSheetScaffoldFor(isAdding = false),
    val manualEntryHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "By ISBN",
        headline = "Enter the ISBN.",
        description = "Type the ISBN-10 or ISBN-13 printed near the barcode.",
    ),
    val topBar: TopBarUiModel = TopBarUiModel(
        title = SCAN_TITLE,
    ),
) : UiState
