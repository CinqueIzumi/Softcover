package nl.rhaydus.softcover.feature.library.presentation.state

import nl.rhaydus.softcover.core.component.button.PillButtonContent
import nl.rhaydus.softcover.core.component.button.PillButtonTone
import nl.rhaydus.softcover.core.component.button.PillButtonUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

internal fun filterPillButton(active: Boolean): PillButtonUiModel = PillButtonUiModel(
    key = "filter",
    content = PillButtonContent.Label(
        text = "Filter",
        leadingIcon = SoftcoverIcon.FilterList,
        indicator = active,
    ),
    tone = PillButtonTone.Quiet,
)

internal fun selectPillButton(): PillButtonUiModel = PillButtonUiModel(
    key = "select",
    content = PillButtonContent.IconOnly(
        icon = SoftcoverIcon.CheckCircle,
        description = "Select books",
    ),
    tone = PillButtonTone.Quiet,
)
