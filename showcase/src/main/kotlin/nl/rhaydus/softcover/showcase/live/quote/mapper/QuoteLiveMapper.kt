package nl.rhaydus.softcover.showcase.live.quote.mapper

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.progress.ProgressTimeUnit
import nl.rhaydus.softcover.core.component.progress.progressTimeField
import nl.rhaydus.softcover.core.component.quote.QuotePositionInputUiModel
import nl.rhaydus.softcover.core.component.quote.QuotePositionTab
import nl.rhaydus.softcover.core.component.quote.QuotePrivacyOption
import nl.rhaydus.softcover.core.component.quote.QuoteSheetUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetActionRowUiModel
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.showcase.live.quote.model.QuoteDemo

private const val BOOK_TITLE = "Piranesi"

internal fun quoteLiveTopBar(): TopBarUiModel = TopBarUiModel(title = "Quote")

internal fun quoteDemos(): ImmutableList<SheetActionRowUiModel<QuoteDemo>> = persistentListOf(
    SheetActionRowUiModel(
        key = QuoteDemo.EMPTY,
        label = "A blank page",
        description = "Save stays off until the passage has some words.",
        icon = SoftcoverIcon.Edit,
    ),
    SheetActionRowUiModel(
        key = QuoteDemo.PAGE,
        label = "A quote from page 214",
        description = "Where it sits, who may read it, and whether it moves your progress.",
        icon = SoftcoverIcon.MenuBook,
    ),
    SheetActionRowUiModel(
        key = QuoteDemo.TIME,
        label = "A quote from an audiobook",
        description = "A position in hours, minutes and seconds.",
        icon = SoftcoverIcon.Headset,
    ),
)

internal fun quoteDemoModel(demo: QuoteDemo): QuoteSheetUiModel = QuoteSheetUiModel.previews.getValue(
    when (demo) {
        QuoteDemo.EMPTY -> "Empty draft"
        QuoteDemo.PAGE -> "Page, advancing progress"
        QuoteDemo.TIME -> "Time"
    },
)

internal fun QuoteSheetUiModel.withText(text: String): QuoteSheetUiModel = copy(
    text = this.text.copy(text = text),
    scaffold = scaffold.copy(
        footer = QuoteSheetUiModel.scaffoldFor(
            bookTitle = BOOK_TITLE,
            canSave = text.isNotBlank(),
        ).footer,
    ),
)

internal fun QuoteSheetUiModel.selectingTab(tab: QuotePositionTab): QuoteSheetUiModel = copy(
    positionTabs = positionTabs.copy(selected = tab),
    position = when (tab) {
        QuotePositionTab.PAGE -> QuotePositionInputUiModel.Page(
            field = QuoteSheetUiModel.positionField(
                tab = tab,
                text = "",
            ),
            suffix = "of 437 pages",
        )

        QuotePositionTab.PERCENTAGE -> QuotePositionInputUiModel.Percentage(
            field = QuoteSheetUiModel.positionField(
                tab = tab,
                text = "",
            ),
        )

        QuotePositionTab.TIME -> QuotePositionInputUiModel.Time(
            hours = progressTimeField(
                unit = ProgressTimeUnit.HOURS,
                text = "",
            ),
            minutes = progressTimeField(
                unit = ProgressTimeUnit.MINUTES,
                text = "",
            ),
            seconds = progressTimeField(
                unit = ProgressTimeUnit.SECONDS,
                text = "",
            ),
            suffix = "of 27:52:00",
        )
    },
)

internal fun QuoteSheetUiModel.withPosition(text: String): QuoteSheetUiModel = copy(
    position = when (val current = position) {
        is QuotePositionInputUiModel.Page -> current.copy(field = current.field.copy(text = text))

        is QuotePositionInputUiModel.Percentage -> current.copy(field = current.field.copy(text = text))

        is QuotePositionInputUiModel.Time -> current
    },
)

internal fun QuoteSheetUiModel.withTime(
    unit: ProgressTimeUnit,
    text: String,
): QuoteSheetUiModel {
    val time = position as? QuotePositionInputUiModel.Time ?: return this

    val field = progressTimeField(
        unit = unit,
        text = text,
    )

    return copy(
        position = when (unit) {
            ProgressTimeUnit.HOURS -> time.copy(hours = field)
            ProgressTimeUnit.MINUTES -> time.copy(minutes = field)
            ProgressTimeUnit.SECONDS -> time.copy(seconds = field)
        },
    )
}

internal fun QuoteSheetUiModel.withProgressAdvance(enabled: Boolean): QuoteSheetUiModel = copy(
    progressAdvance = progressAdvance?.copy(checked = enabled),
)

internal fun QuoteSheetUiModel.withPrivacy(option: QuotePrivacyOption): QuoteSheetUiModel = copy(
    privacy = QuoteSheetUiModel.privacyFor(selected = option),
)
