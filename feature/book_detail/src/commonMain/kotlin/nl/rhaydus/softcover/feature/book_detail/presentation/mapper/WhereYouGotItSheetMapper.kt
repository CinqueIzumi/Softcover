package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.field.TextFieldUiModel
import nl.rhaydus.softcover.core.component.field.TextFieldVariant
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetFooterButton
import nl.rhaydus.softcover.core.component.sheet.SheetFooterEmphasis
import nl.rhaydus.softcover.core.component.sheet.SheetFooterUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetInputRowUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetRowFace
import nl.rhaydus.softcover.core.component.sheet.SheetRowGroupUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetRowUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.domain.model.AcquisitionKind
import nl.rhaydus.softcover.core.uibinding.acquisition.toLabel
import nl.rhaydus.softcover.feature.book_detail.presentation.state.WhereYouGotItDraft
import nl.rhaydus.softcover.feature.book_detail.presentation.state.WhereYouGotItSheetUiModel

internal const val WHERE_YOU_GOT_IT_INPUT_KEY = "where-you-got-it-other"

internal fun WhereYouGotItDraft.toWhereYouGotItSheet(bookTitle: String): WhereYouGotItSheetUiModel {
    val selectedKey = (this as? WhereYouGotItDraft.Preset)?.key
    val customText = (this as? WhereYouGotItDraft.Custom)?.text

    return WhereYouGotItSheetUiModel(
        scaffold = SheetScaffoldUiModel(
            header = SheetHeaderUiModel(
                section = SectionHeaderUiModel.Section(
                    eyebrow = bookTitle,
                    headline = "Where you got it",
                ),
            ),
            footer = SheetFooterUiModel.Inline(
                primary = SheetFooterButton(
                    label = "Save",
                    dismissesSheet = true,
                ),
                secondary = SheetFooterButton(
                    label = "Cancel",
                    emphasis = SheetFooterEmphasis.Outlined,
                    dismissesSheet = true,
                ),
            ),
        ),
        group = SheetRowGroupUiModel(
            rows = AcquisitionKind.entries
                .map { kind ->
                    SheetRowUiModel(
                        key = kind.toKey(),
                        label = kind.toLabel(),
                        selected = kind.toKey() == selectedKey,
                        gloss = kind.toGloss(),
                    )
                }
                .toImmutableList(),
            dismissesSheet = false,
            input = SheetInputRowUiModel(
                field = TextFieldUiModel(
                    key = WHERE_YOU_GOT_IT_INPUT_KEY,
                    text = customText.orEmpty(),
                    placeholder = WHERE_YOU_GOT_IT_PLACEHOLDER,
                    variant = TextFieldVariant.Plain,
                ),
                selected = customText != null,
            ),
            face = SheetRowFace.Body,
        ),
    )
}
