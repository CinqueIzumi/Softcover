package nl.rhaydus.softcover.orchestration.presentation.state

import nl.rhaydus.softcover.core.component.callout.BannerUiModel
import nl.rhaydus.toad.UiState

internal data class RootUiState(
    /** `null` until the library's offline copy has resolved. */
    val offlineBanner: BannerUiModel? = null,
) : UiState
