package nl.rhaydus.softcover.core.component.quote

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.chip.ChipLeading
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.control.SegmentUiModel
import nl.rhaydus.softcover.core.component.control.SegmentedControlUiModel
import nl.rhaydus.softcover.core.component.control.ToggleRowUiModel
import nl.rhaydus.softcover.core.component.field.TextFieldUiModel
import nl.rhaydus.softcover.core.component.field.TextFieldVariant
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.progress.ProgressTimeUnit
import nl.rhaydus.softcover.core.component.progress.WhenReadUiModel
import nl.rhaydus.softcover.core.component.progress.progressTimeField
import nl.rhaydus.softcover.core.component.sheet.SheetFooterButton
import nl.rhaydus.softcover.core.component.sheet.SheetFooterUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

/**
 * Everything [QuoteSheet] renders: the draft of a quote being added. Every draft value is host-owned.
 *
 * [positionTabs] carries the position units the host offers and the one showing, and [position] is the
 * matching input. [progressAdvance] is the optional "move my progress to this quote" row; null hides it.
 * [whenRead] is the shared backdating row.
 */
@Immutable
data class QuoteSheetUiModel(
    val text: TextFieldUiModel,
    val positionTabs: SegmentedControlUiModel<QuotePositionTab>,
    val position: QuotePositionInputUiModel,
    val progressAdvance: ToggleRowUiModel<String>?,
    val privacy: SegmentedControlUiModel<QuotePrivacyOption>,
    val whenRead: WhenReadUiModel,
    val scaffold: SheetScaffoldUiModel,
) : SheetModel<QuoteSheetUiModel> {
    override val closeRequested: Boolean get() = scaffold.closeRequested

    override fun requestingClose(): QuoteSheetUiModel = copy(scaffold = scaffold.requestingClose())

    companion object : UiModelPreviews<QuoteSheetUiModel> {
        fun textField(text: String): TextFieldUiModel = TextFieldUiModel(
            key = "quote-text",
            text = text,
            placeholder = "The passage you want to keep",
            variant = TextFieldVariant.Multiline,
        )

        fun positionField(
            tab: QuotePositionTab,
            text: String,
        ): TextFieldUiModel = TextFieldUiModel(
            key = tab.name,
            text = text,
            placeholder = "0",
            variant = TextFieldVariant.Numeral(charCount = if (tab == QuotePositionTab.PAGE) 4 else 3),
        )

        fun positionTabsFor(
            offered: List<QuotePositionTab>,
            selected: QuotePositionTab,
        ): SegmentedControlUiModel<QuotePositionTab> = SegmentedControlUiModel(
            options = offered
                .map { tab ->
                    SegmentUiModel(
                        key = tab,
                        label = tab.tabName,
                    )
                }
                .toImmutableList(),
            selected = selected,
        )

        fun privacyFor(selected: QuotePrivacyOption): SegmentedControlUiModel<QuotePrivacyOption> =
            SegmentedControlUiModel(
                options = QuotePrivacyOption.entries
                    .map { option ->
                        SegmentUiModel(
                            key = option,
                            label = option.label,
                        )
                    }
                    .toImmutableList(),
                selected = selected,
            )

        /** Save is offered only once there is a passage to keep. */
        fun scaffoldFor(
            bookTitle: String,
            canSave: Boolean,
        ): SheetScaffoldUiModel = SheetScaffoldUiModel(
            header = SheetHeaderUiModel(
                section = SectionHeaderUiModel.Section(
                    eyebrow = "New quote",
                    headline = bookTitle,
                ),
            ),
            footer = SheetFooterUiModel.Stacked(
                primary = SheetFooterButton(
                    label = "Save quote",
                    enabled = canSave,
                    dismissesSheet = true,
                ),
                secondary = null,
            ),
        )

        private fun fixture(
            text: String,
            tab: QuotePositionTab,
            offered: List<QuotePositionTab>,
            position: QuotePositionInputUiModel,
            progressAdvance: ToggleRowUiModel<String>?,
        ): QuoteSheetUiModel = QuoteSheetUiModel(
            text = textField(text = text),
            positionTabs = positionTabsFor(
                offered = offered,
                selected = tab,
            ),
            position = position,
            progressAdvance = progressAdvance,
            privacy = privacyFor(selected = QuotePrivacyOption.PRIVATE),
            whenRead = WhenReadUiModel(
                chip = ChipUiModel(
                    key = "when-read",
                    label = "Just now",
                    leading = ChipLeading.Icon(icon = SoftcoverIcon.DateRange),
                ),
                dayLabel = "Today",
                timeLabel = "09:41",
                canShiftLater = false,
            ),
            scaffold = scaffoldFor(
                bookTitle = "Piranesi",
                canSave = text.isNotBlank(),
            ),
        )

        private val allTabs = QuotePositionTab.entries

        override val previews: ImmutableMap<String, QuoteSheetUiModel> = persistentMapOf(
            "Empty draft" to fixture(
                text = "",
                tab = QuotePositionTab.PAGE,
                offered = allTabs,
                position = QuotePositionInputUiModel.Page(
                    field = positionField(
                        tab = QuotePositionTab.PAGE,
                        text = "",
                    ),
                    suffix = "of 437 pages",
                ),
                progressAdvance = null,
            ),
            "Page, advancing progress" to fixture(
                text = "The House is valuable because it is the abode of the Statues.",
                tab = QuotePositionTab.PAGE,
                offered = allTabs,
                position = QuotePositionInputUiModel.Page(
                    field = positionField(
                        tab = QuotePositionTab.PAGE,
                        text = "214",
                    ),
                    suffix = "of 437 pages",
                ),
                progressAdvance = ToggleRowUiModel(
                    key = "progress-advance",
                    title = "Update my progress to page 214",
                    gloss = "You're on page 180.",
                    checked = true,
                ),
            ),
            "Page, last page" to fixture(
                text = "The beauty of the House is immeasurable; its Kindness infinite.",
                tab = QuotePositionTab.PAGE,
                offered = allTabs,
                position = QuotePositionInputUiModel.Page(
                    field = positionField(
                        tab = QuotePositionTab.PAGE,
                        text = "437",
                    ),
                    suffix = "of 437 pages",
                ),
                progressAdvance = ToggleRowUiModel(
                    key = "progress-advance",
                    title = "Mark as read",
                    gloss = "Page 437 is the last page.",
                    checked = false,
                ),
            ),
            "Percentage" to fixture(
                text = "My name is Piranesi.",
                tab = QuotePositionTab.PERCENTAGE,
                offered = allTabs,
                position = QuotePositionInputUiModel.Percentage(
                    field = positionField(
                        tab = QuotePositionTab.PERCENTAGE,
                        text = "41",
                    ),
                ),
                progressAdvance = null,
            ),
            "Time" to fixture(
                text = "A name is a small thing to carry.",
                tab = QuotePositionTab.TIME,
                offered = listOf(QuotePositionTab.TIME, QuotePositionTab.PERCENTAGE),
                position = QuotePositionInputUiModel.Time(
                    hours = progressTimeField(
                        unit = ProgressTimeUnit.HOURS,
                        text = "3",
                    ),
                    minutes = progressTimeField(
                        unit = ProgressTimeUnit.MINUTES,
                        text = "20",
                    ),
                    seconds = progressTimeField(
                        unit = ProgressTimeUnit.SECONDS,
                        text = "45",
                    ),
                    suffix = "of 27:52:00",
                ),
                progressAdvance = null,
            ),
        )
    }
}
