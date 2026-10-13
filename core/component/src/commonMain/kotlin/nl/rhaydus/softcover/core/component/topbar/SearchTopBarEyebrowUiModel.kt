package nl.rhaydus.softcover.core.component.topbar

import androidx.compose.runtime.Immutable

/**
 * The pull-to-refresh label [SearchTopBar] draws above its row: [text] at rest, [refreshingText]
 * while [isRefreshing].
 */
@Immutable
data class SearchTopBarEyebrowUiModel(
    val text: String,
    val refreshingText: String,
    val isRefreshing: Boolean,
)
