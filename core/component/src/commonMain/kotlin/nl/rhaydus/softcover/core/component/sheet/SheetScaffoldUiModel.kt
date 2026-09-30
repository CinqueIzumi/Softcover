package nl.rhaydus.softcover.core.component.sheet

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel

@Immutable
data class SheetScaffoldUiModel(
    val header: SheetHeaderUiModel,
    val topAction: String?,
    val footer: SheetFooterUiModel?,
    val layout: SheetLayout,
    val dismissible: Boolean = true,
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
                topAction = null,
                footer = SheetFooterUiModel.previews.first(),
                layout = SheetLayout.Scrolling,
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
                            variant = CoverVariant.ChooseListsSingleJacket,
                        ),
                    ),
                ),
                topAction = "Done",
                footer = null,
                layout = SheetLayout.Scrolling,
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
                                variant = CoverVariant.ChooseListsStackJacket,
                            )
                        }.toImmutableList(),
                    ),
                ),
                topAction = null,
                footer = SheetFooterUiModel.previews.first { it.layout == SheetFooterLayout.Inline },
                layout = SheetLayout.Pinned,
            ),
            SheetScaffoldUiModel(
                header = SheetHeaderUiModel(
                    section = SectionHeaderUiModel.Section(
                        eyebrow = "Genres",
                        headline = "Pick a genre",
                    ),
                    trailing = SheetHeaderTrailing.Action(label = "Clear"),
                ),
                topAction = null,
                footer = null,
                layout = SheetLayout.Pinned,
            ),
            SheetScaffoldUiModel(
                header = SheetHeaderUiModel(
                    section = SectionHeaderUiModel.Section(
                        eyebrow = "Setting up",
                        headline = "Pulling your library together.",
                    ),
                ),
                topAction = null,
                footer = null,
                layout = SheetLayout.Scrolling,
                dismissible = false,
            ),
        )
    }
}
