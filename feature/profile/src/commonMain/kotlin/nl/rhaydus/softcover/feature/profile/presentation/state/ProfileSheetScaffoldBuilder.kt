package nl.rhaydus.softcover.feature.profile.presentation.state

import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetFooterButton
import nl.rhaydus.softcover.core.component.sheet.SheetFooterLayout
import nl.rhaydus.softcover.core.component.sheet.SheetFooterUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetLayout
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel

internal fun profileShareSheetScaffold(): SheetScaffoldUiModel =
    SheetScaffoldUiModel(
        header = SheetHeaderUiModel(
            section = SectionHeaderUiModel.Section(
                eyebrow = "Share",
                headline = "Share your profile",
                description = "A card built from everything you've read this year.",
            ),
        ),
        topAction = null,
        footer = null,
        layout = SheetLayout.Scrolling,
    )

internal fun logOutSheetScaffold(): SheetScaffoldUiModel =
    SheetScaffoldUiModel(
        header = SheetHeaderUiModel(
            section = SectionHeaderUiModel.Section(
                eyebrow = "Account",
                headline = "Log out?",
                description = "You'll need to sign back in to pick up where you left off.",
            ),
        ),
        topAction = null,
        footer = SheetFooterUiModel(
            primary = SheetFooterButton(
                label = "Log out",
                dismissesSheet = true,
            ),
            secondary = SheetFooterButton(
                label = "Stay",
                dismissesSheet = true,
            ),
            layout = SheetFooterLayout.Stacked,
        ),
        layout = SheetLayout.Scrolling,
    )
