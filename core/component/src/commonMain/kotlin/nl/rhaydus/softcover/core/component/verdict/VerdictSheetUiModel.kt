package nl.rhaydus.softcover.core.component.verdict

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.control.ToggleRowUiModel
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.richtext.RichTextUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetFooterButton
import nl.rhaydus.softcover.core.component.sheet.SheetFooterUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderTrailing
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetLayout
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel

/**
 * Everything [VerdictSheet] renders: the [scaffold] chrome carrying the copy (header, dismiss and save
 * labels, and a secondary delete action only when there is a review to delete), and the verdict the draft
 * starts from.
 *
 * [initialRating], [initialReview] and [spoilers] seed the sheet's local draft once; later changes to the
 * model do not reset what the reader has typed.
 */
@Immutable
data class VerdictSheetUiModel(
    val scaffold: SheetScaffoldUiModel,
    val initialRating: Double?,
    val initialReview: RichTextUiModel,
    val spoilers: ToggleRowUiModel,
    val reviewLabel: SectionHeaderUiModel.Label = SectionHeaderUiModel.Label(eyebrow = "In your words"),
) {
    companion object : UiModelPreviews<VerdictSheetUiModel> {
        private fun fixtureScaffold(
            header: SectionHeaderUiModel.Section,
            dismissLabel: String,
            saveLabel: String,
            canDelete: Boolean,
        ): SheetScaffoldUiModel = SheetScaffoldUiModel(
            header = SheetHeaderUiModel(
                section = header,
                trailing = SheetHeaderTrailing.Jacket(cover = fixtureCover),
            ),
            dismissLabel = dismissLabel,
            footer = SheetFooterUiModel.Stacked(
                primary = SheetFooterButton(label = saveLabel),
                secondary = if (canDelete) SheetFooterButton(label = "Delete review") else null,
            ),
            layout = SheetLayout.Pinned,
        )

        private fun fixtureSpoilers(checked: Boolean): ToggleRowUiModel = ToggleRowUiModel(
            key = "spoilers",
            title = "Contains spoilers",
            gloss = "Readers will need to tap to reveal the whole review.",
            checked = checked,
        )

        private val fixtureCover = CoverUiModel(
            source = null,
            coverlessTitle = "Piranesi",
            variant = CoverVariant.Jacket,
        )

        /** One fixture per anatomy: the empty finished prompt, and an edit with a verdict and a Delete action. */
        override val previews: ImmutableList<VerdictSheetUiModel> = persistentListOf(
            VerdictSheetUiModel(
                scaffold = fixtureScaffold(
                    header = SectionHeaderUiModel.Section(
                        eyebrow = "The last page",
                        headline = "You finished Piranesi.",
                        description = "A rating and a few words, if you like — both are optional.",
                    ),
                    dismissLabel = "Not now",
                    saveLabel = "Save",
                    canDelete = false,
                ),
                initialRating = null,
                initialReview = RichTextUiModel.EMPTY,
                spoilers = fixtureSpoilers(checked = false),
            ),
            VerdictSheetUiModel(
                scaffold = fixtureScaffold(
                    header = SectionHeaderUiModel.Section(
                        eyebrow = "Your verdict",
                        headline = "Your take on Piranesi.",
                    ),
                    dismissLabel = "Cancel",
                    saveLabel = "Save changes",
                    canDelete = true,
                ),
                initialRating = 4.5,
                initialReview = RichTextUiModel.previews.first(),
                spoilers = fixtureSpoilers(checked = true),
            ),
        )
    }
}
