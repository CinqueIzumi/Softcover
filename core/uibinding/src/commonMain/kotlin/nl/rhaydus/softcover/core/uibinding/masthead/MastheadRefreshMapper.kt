package nl.rhaydus.softcover.core.uibinding.masthead

import nl.rhaydus.softcover.core.component.header.MastheadRefreshUiModel

/** The masthead refresh action: "Refreshing…" and disabled while [isLoading], otherwise "Refresh" and enabled. */
fun mastheadRefreshFor(isLoading: Boolean): MastheadRefreshUiModel = MastheadRefreshUiModel(
    label = if (isLoading) "Refreshing…" else "Refresh",
    enabled = isLoading.not(),
)
