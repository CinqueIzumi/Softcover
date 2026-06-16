package nl.rhaydus.softcover.core.designsystem.presentation.component

import androidx.compose.runtime.Composable

// Touch platforms have no pointer, so there is nothing to hover — a pure pass-through (no wrapping
// layout node) keeps the wrapped control byte-identical to an un-tooltipped one on Android and iOS.
@Composable
actual fun DesktopTooltip(
    text: String,
    content: @Composable () -> Unit,
) {
    content()
}
