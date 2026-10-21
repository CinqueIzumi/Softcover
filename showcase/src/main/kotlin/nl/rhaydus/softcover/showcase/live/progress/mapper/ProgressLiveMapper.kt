package nl.rhaydus.softcover.showcase.live.progress.mapper

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.progress.ProgressSheetMedium
import nl.rhaydus.softcover.core.component.progress.ProgressSheetTab
import nl.rhaydus.softcover.core.component.progress.ProgressSheetUiModel
import nl.rhaydus.softcover.core.component.progress.ProgressTimeUnit
import nl.rhaydus.softcover.core.component.progress.progressTimeField
import nl.rhaydus.softcover.core.component.sheet.SheetActionRowUiModel
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.showcase.live.progress.model.ProgressDemo

private const val MAX_MINUTES = 59

internal fun progressLiveTopBar(): TopBarUiModel = TopBarUiModel(title = "Progress")

internal fun progressDemos(): ImmutableList<SheetActionRowUiModel<ProgressDemo>> = persistentListOf(
    SheetActionRowUiModel(
        key = ProgressDemo.PAGES,
        label = "A paperback",
        description = "Pages and percentage tabs over a hero number.",
        icon = SoftcoverIcon.MenuBook,
    ),
    SheetActionRowUiModel(
        key = ProgressDemo.AUDIOBOOK,
        label = "An audiobook",
        description = "Hours, minutes and seconds with a minutes stepper.",
        icon = SoftcoverIcon.Headset,
    ),
    SheetActionRowUiModel(
        key = ProgressDemo.NO_TOTAL,
        label = "A book with no page count",
        description = "The percentage tab and the progress bar drop out.",
        icon = SoftcoverIcon.Info,
    ),
)

internal fun progressDemoModel(demo: ProgressDemo): ProgressSheetUiModel = ProgressSheetUiModel.previews.getValue(
    when (demo) {
        ProgressDemo.PAGES -> "Pages"
        ProgressDemo.AUDIOBOOK -> "Audiobook"
        ProgressDemo.NO_TOTAL -> "Paged, no total"
    },
)

internal fun ProgressSheetUiModel.selectingTab(tab: ProgressSheetTab): ProgressSheetUiModel = copy(
    tabs = ProgressSheetUiModel.tabsFor(
        medium = medium,
        selectedTab = tab,
    ),
)

internal fun ProgressSheetUiModel.withTime(
    unit: ProgressTimeUnit,
    text: String,
): ProgressSheetUiModel {
    val timed = medium as? ProgressSheetMedium.Timed ?: return this

    val field = progressTimeField(
        unit = unit,
        text = text,
    )

    return copy(
        medium = when (unit) {
            ProgressTimeUnit.HOURS -> timed.copy(hours = field)

            ProgressTimeUnit.MINUTES -> timed.copy(
                minutes = field,
                canDecreaseMinutes = (text.toIntOrNull() ?: 0) > 0,
                canIncreaseMinutes = (text.toIntOrNull() ?: 0) < MAX_MINUTES,
            )

            ProgressTimeUnit.SECONDS -> timed.copy(seconds = field)
        },
    )
}

internal fun ProgressSheetUiModel.steppingMinutes(delta: Int): ProgressSheetUiModel {
    val timed = medium as? ProgressSheetMedium.Timed ?: return this

    val stepped = ((timed.minutes.text.toIntOrNull() ?: 0) + delta).coerceIn(
        0,
        MAX_MINUTES,
    )

    return withTime(
        unit = ProgressTimeUnit.MINUTES,
        text = stepped.toString(),
    )
}
