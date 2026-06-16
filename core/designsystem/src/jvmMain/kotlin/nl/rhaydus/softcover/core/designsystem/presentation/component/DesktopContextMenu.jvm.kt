package nl.rhaydus.softcover.core.designsystem.presentation.component

import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.runtime.Composable

// Compose Desktop's right-click menu. An empty list stays a pass-through so call sites can wrap
// unconditionally and only populate items where a menu applies.
@Composable
actual fun DesktopContextMenu(
    items: List<DesktopContextMenuItem>,
    content: @Composable () -> Unit,
) {
    if (items.isEmpty()) {
        content()

        return
    }

    ContextMenuArea(
        items = { items.map { item -> ContextMenuItem(
            item.label,
            item.onClick,
        ) } },
        content = content,
    )
}
