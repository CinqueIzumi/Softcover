package nl.rhaydus.softcover.core.component.callout

import androidx.compose.runtime.Immutable

@Immutable
sealed interface CalloutActionUiModel {
    data class Button(val label: String) : CalloutActionUiModel

    data class Busy(val label: String) : CalloutActionUiModel

    data object Progress : CalloutActionUiModel
}
