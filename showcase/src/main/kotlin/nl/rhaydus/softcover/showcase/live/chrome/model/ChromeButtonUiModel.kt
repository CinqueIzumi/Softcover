package nl.rhaydus.softcover.showcase.live.chrome.model

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

@Immutable
internal data class ChromeButtonUiModel(
    val key: ChromeButtonKey,
    val icon: SoftcoverIcon,
    val label: String,
)
