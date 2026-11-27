package nl.rhaydus.softcover.core.uibinding.session

import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetFooterButton
import nl.rhaydus.softcover.core.component.sheet.SheetFooterUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel

private const val FALLBACK_TITLE = "this book"

fun endSessionSheetScaffold(
    bookTitle: String?,
    targetStatus: SessionEndingStatus,
): SheetScaffoldUiModel = SheetScaffoldUiModel(
    header = SheetHeaderUiModel(
        section = SectionHeaderUiModel.Section(
            eyebrow = "Reading session",
            headline = "End this session?",
            description = "You're mid-session with ${bookTitle ?: FALLBACK_TITLE}. " +
                "${targetStatus.endSessionConsequence()} ends the session and saves your reading time.",
        ),
    ),
    footer = SheetFooterUiModel.Stacked(
        primary = SheetFooterButton(
            label = targetStatus.endSessionPrimaryLabel(),
            dismissesSheet = true,
        ),
        secondary = SheetFooterButton(
            label = "Keep reading",
            dismissesSheet = true,
        ),
    ),
)

private fun SessionEndingStatus.endSessionConsequence(): String = when (this) {
    SessionEndingStatus.Paused -> "Pausing the book"
    SessionEndingStatus.DidNotFinish -> "Marking it Did not finish"
}

private fun SessionEndingStatus.endSessionPrimaryLabel(): String = when (this) {
    SessionEndingStatus.Paused -> "End session and pause"
    SessionEndingStatus.DidNotFinish -> "End session and mark Did not finish"
}
