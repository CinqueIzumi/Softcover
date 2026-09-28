package nl.rhaydus.softcover.feature.lists.presentation.state

import nl.rhaydus.softcover.core.component.divider.DividerUiModel
import nl.rhaydus.softcover.core.component.divider.DividerVariant
import nl.rhaydus.softcover.core.domain.model.PrivacySetting
import nl.rhaydus.toad.UiState

internal data class CreateListUiState(
    val name: String = "",
    val privacy: PrivacySetting = PrivacySetting.PUBLIC,
    val isSubmitting: Boolean = false,
    val quoteDivider: DividerUiModel = DividerUiModel(DividerVariant.CreateListQuote),
) : UiState {
    val canSubmit: Boolean
        get() = name.trim().isNotEmpty() && isSubmitting.not()
}
