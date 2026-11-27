package nl.rhaydus.softcover.core.component.yourcopy

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

@Immutable
data class YourCopyRowUiModel<K>(
    val key: K,
    val icon: SoftcoverIcon,
    val label: String,
    val value: YourCopyValue,
)
