package nl.rhaydus.softcover.core.component.callout

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

/**
 * How a [Callout] looks. [Neutral] is a quiet surface card whose eyebrow may carry an [icon];
 * [Highlight] is a primary-container card with an accent-bar eyebrow and a headline [title].
 */
@Immutable
sealed interface CalloutVariant {
    data class Neutral(val icon: SoftcoverIcon? = null) : CalloutVariant

    data class Highlight(val title: String) : CalloutVariant
}
