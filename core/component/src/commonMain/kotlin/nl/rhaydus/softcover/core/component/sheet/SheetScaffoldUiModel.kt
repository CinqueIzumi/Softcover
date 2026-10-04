package nl.rhaydus.softcover.core.component.sheet

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel

/**
 * The chrome around a sheet's feature body. [closeRequested] hides the sheet even when [dismissible] is
 * false; while [busy] the secondary button is disabled and tapping outside or pressing back does not
 * dismiss.
 */
@Immutable
data class SheetScaffoldUiModel(
    val header: SheetHeaderUiModel,
    val dismissLabel: String? = null,
    val footer: SheetFooterUiModel? = null,
    val layout: SheetLayout = SheetLayout.Scrolling,
    val dismissible: Boolean = true,
    val busy: Boolean = false,
    val busyDescription: String = "Loading",
    val closeRequested: Boolean = false,
) {
    companion object : UiModelPreviews<SheetScaffoldUiModel> {
        override val previews: ImmutableList<SheetScaffoldUiModel> = persistentListOf(
            SheetScaffoldUiModel(
                header = SheetHeaderUiModel(
                    section = SectionHeaderUiModel.Section(
                        eyebrow = "Reading progress",
                        headline = "Piranesi",
                    ),
                ),
                footer = SheetFooterUiModel.previews.first(),
            ),
            SheetScaffoldUiModel(
                header = SheetHeaderUiModel(
                    section = SectionHeaderUiModel.Section(
                        eyebrow = "Tags",
                        headline = "Piranesi",
                        description = "Tag it however you like.",
                    ),
                    trailing = SheetHeaderTrailing.Jacket(
                        cover = CoverUiModel(
                            source = null,
                            coverlessTitle = "Piranesi",
                            variant = CoverVariant.Jacket,
                        ),
                    ),
                ),
                dismissLabel = "Done",
            ),
            SheetScaffoldUiModel(
                header = SheetHeaderUiModel(
                    section = SectionHeaderUiModel.Section(
                        eyebrow = "Lists",
                        headline = "Shelve 3 books",
                    ),
                    trailing = SheetHeaderTrailing.JacketStack(
                        covers = listOf("Piranesi", "It", "The Name of the Wind").map { title ->
                            CoverUiModel(
                                source = null,
                                coverlessTitle = title,
                                variant = CoverVariant.StackedJacket,
                            )
                        }.toImmutableList(),
                    ),
                ),
                footer = SheetFooterUiModel.previews.filterIsInstance<SheetFooterUiModel.Inline>().first(),
                layout = SheetLayout.PinnedLazy,
            ),
            SheetScaffoldUiModel(
                header = SheetHeaderUiModel(
                    section = SectionHeaderUiModel.Section(
                        eyebrow = "Genres",
                        headline = "Pick a genre",
                    ),
                    trailing = SheetHeaderTrailing.Action(label = "Clear"),
                ),
                layout = SheetLayout.Pinned,
            ),
            SheetScaffoldUiModel(
                header = SheetHeaderUiModel(
                    section = SectionHeaderUiModel.Section(
                        eyebrow = "Setting up",
                        headline = "Pulling your library together.",
                    ),
                ),
                dismissible = false,
            ),
            SheetScaffoldUiModel(
                header = SheetHeaderUiModel(
                    section = SectionHeaderUiModel.Section(
                        eyebrow = "Signing in",
                        headline = "Welcome back",
                    ),
                ),
                footer = SheetFooterUiModel.Stacked(
                    primary = SheetFooterButton(label = "Sign in"),
                    secondary = SheetFooterButton(label = "Not now"),
                ),
                busy = true,
            ),
        )
    }
}
