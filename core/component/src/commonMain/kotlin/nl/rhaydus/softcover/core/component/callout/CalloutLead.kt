package nl.rhaydus.softcover.core.component.callout

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

@Immutable
sealed interface CalloutLead {
    data class Icon(val icon: SoftcoverIcon) : CalloutLead

    data object AccentBar : CalloutLead
}
