package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.chip.toChipSet
import nl.rhaydus.softcover.core.component.field.TextFieldUiModel
import nl.rhaydus.softcover.core.component.field.TextFieldVariant
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetFooterButton
import nl.rhaydus.softcover.core.component.sheet.SheetFooterEmphasis
import nl.rhaydus.softcover.core.component.sheet.SheetFooterUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetLayout
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.domain.model.DidNotFinishReason
import nl.rhaydus.softcover.core.domain.model.DidNotFinishReasons
import nl.rhaydus.softcover.core.uibinding.didnotfinish.toLabel
import nl.rhaydus.softcover.feature.book_detail.presentation.state.DidNotFinishReasonDraft
import nl.rhaydus.softcover.feature.book_detail.presentation.state.DidNotFinishReasonSheetUiModel

internal const val DID_NOT_FINISH_OWN_WORDS_KEY = "did-not-finish-own-words"

internal fun DidNotFinishReasons.toDidNotFinishReasonDraft() = DidNotFinishReasonDraft(
    reasons = reasons,
    ownWords = ownWords.orEmpty(),
)

internal fun DidNotFinishReasonDraft.toDidNotFinishReasons(bookId: Int) = DidNotFinishReasons(
    bookId = bookId,
    reasons = reasons,
    ownWords = ownWords,
)

internal fun DidNotFinishReasonDraft.withReasonToggled(reason: DidNotFinishReason) = copy(
    reasons = if (reason in reasons) reasons - reason else reasons + reason,
)

internal fun DidNotFinishReasonDraft.withOwnWords(text: String) = copy(ownWords = text)

internal fun DidNotFinishReasonDraft.toDidNotFinishReasonSheet(bookTitle: String) = DidNotFinishReasonSheetUiModel(
    scaffold = SheetScaffoldUiModel(
        header = SheetHeaderUiModel(
            section = SectionHeaderUiModel.Section(
                eyebrow = bookTitle,
                headline = "Why you set it aside",
                description = "Pick any that fit. It stays between you and this phone.",
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
        layout = SheetLayout.Scrolling,
    ),
    reasons = DidNotFinishReason.entries
        .map { reason ->
            ChipUiModel(
                key = reason.name,
                label = reason.toLabel(),
                selected = reason in reasons,
            ) to reason
        }
        .toChipSet(),
    ownWordsHeader = SectionHeaderUiModel.Label(eyebrow = "In your own words"),
    ownWords = TextFieldUiModel(
        key = DID_NOT_FINISH_OWN_WORDS_KEY,
        text = ownWords,
        placeholder = "Anything else about why",
        variant = TextFieldVariant.Multiline,
    ),
)
