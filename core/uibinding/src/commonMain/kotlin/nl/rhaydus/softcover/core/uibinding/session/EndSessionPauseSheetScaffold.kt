package nl.rhaydus.softcover.core.uibinding.session

import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetFooterButton
import nl.rhaydus.softcover.core.component.sheet.SheetFooterUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel

private const val FALLBACK_TITLE = "this book"

fun endSessionPauseSheetScaffold(bookTitle: String?): SheetScaffoldUiModel =
    SheetScaffoldUiModel(
        header = SheetHeaderUiModel(
            section = SectionHeaderUiModel.Section(
                eyebrow = "Reading session",
                headline = "End this session?",
                description = "You're mid-session with ${bookTitle ?: FALLBACK_TITLE}. " +
                    "Pausing the book ends the session and saves your reading time.",
            ),
        ),
        footer = SheetFooterUiModel.Stacked(
            primary = SheetFooterButton(
                label = "End session and pause",
                dismissesSheet = true,
            ),
            secondary = SheetFooterButton(
                label = "Keep reading",
                dismissesSheet = true,
            ),
        ),
    )
