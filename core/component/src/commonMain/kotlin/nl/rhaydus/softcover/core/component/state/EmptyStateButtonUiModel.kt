package nl.rhaydus.softcover.core.component.state

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

@Immutable
data class EmptyStateButtonUiModel(
    val label: String,
    val icon: SoftcoverIcon? = null,
)
