package nl.rhaydus.softcover.core.designsystem.presentation.component

import androidx.compose.runtime.Composable

// Touch platforms have no secondary click; selection is reached via long-press, so this is a pure
// pass-through (no wrapping layout node) and leaves the wrapped surface byte-identical.
@Composable
actual fun DesktopContextMenu(
    items: List<DesktopContextMenuItem>,
    content: @Composable () -> Unit,
) {
    content()
}
