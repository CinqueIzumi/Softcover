package nl.rhaydus.softcover.showcase.live.verdict.mapper

import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import nl.rhaydus.softcover.core.component.chip.ChipInteraction
import nl.rhaydus.softcover.core.component.richtext.RichTextMark
import nl.rhaydus.softcover.core.component.richtext.RichTextMarkType
import nl.rhaydus.softcover.core.component.sheet.SheetActionRowUiModel
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.core.component.verdict.VerdictSheetUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.showcase.live.verdict.model.VerdictDemo
import nl.rhaydus.softcover.showcase.live.verdict.model.VerdictDemoUiModel

internal fun verdictLiveTopBar(): TopBarUiModel = TopBarUiModel(title = "Verdict")

internal fun verdictDemos(): ImmutableList<SheetActionRowUiModel<VerdictDemo>> = persistentListOf(
    SheetActionRowUiModel(
        key = VerdictDemo.FINISHED,
        label = "Just finished",
        description = "Stars, a review and a spoiler switch, all still empty.",
        icon = SoftcoverIcon.StarFilled,
    ),
    SheetActionRowUiModel(
        key = VerdictDemo.EDIT,
        label = "Editing a verdict",
        description = "A rated review with marks, ready to change or delete.",
        icon = SoftcoverIcon.Edit,
    ),
)

internal fun verdictDemoModel(demo: VerdictDemo): VerdictDemoUiModel = VerdictDemoUiModel(
    sheet = VerdictSheetUiModel.previews
        .getValue(
            when (demo) {
                VerdictDemo.FINISHED -> "Finished, unrated"
                VerdictDemo.EDIT -> "Edit, rated with a review"
            },
        )
        .withToolbarState(),
)

internal fun VerdictDemoUiModel.withRating(rating: Double?): VerdictDemoUiModel = copy(sheet = sheet.copy(rating = rating))

internal fun VerdictDemoUiModel.withSpoilers(checked: Boolean): VerdictDemoUiModel = copy(
    sheet = sheet.copy(spoilers = sheet.spoilers.copy(checked = checked)),
)

internal fun VerdictDemoUiModel.withReview(value: TextFieldValue): VerdictDemoUiModel {
    val length = value.text.length

    val marks = sheet.marks
        .map { it.copy(end = it.end.coerceAtMost(length)) }
        .filter { it.start < it.end }
        .toImmutableList()

    return copy(
        sheet = sheet
            .copy(
                review = value,
                marks = marks,
            )
            .withToolbarState(),
    )
}

internal fun VerdictDemoUiModel.togglingMark(mark: RichTextMarkType): VerdictDemoUiModel {
    val selection = sheet.review.selection

    if (selection.collapsed) return this

    val existing = sheet.marks.firstOrNull { it.type == mark && it.start == selection.min && it.end == selection.max }

    val marks = if (existing != null) {
        sheet.marks - existing
    } else {
        sheet.marks + RichTextMark(
            start = selection.min,
            end = selection.max,
            type = mark,
        )
    }

    return copy(sheet = sheet.copy(marks = marks.toImmutableList()).withToolbarState())
}

private fun VerdictSheetUiModel.withToolbarState(): VerdictSheetUiModel {
    val selection = review.selection

    return copy(
        markChips = markChips
            .mapValues { (type, chip) ->
                chip.copy(
                    selected = selection.collapsed.not() && marks.any {
                        it.type == type && it.start <= selection.min && it.end >= selection.max
                    },
                    interaction = if (selection.collapsed) ChipInteraction.Disabled else ChipInteraction.Clickable,
                )
            }
            .toImmutableMap(),
    )
}
