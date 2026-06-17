package nl.rhaydus.softcover.core.designsystem.presentation.component

import androidx.compose.runtime.Composable

/**
 * Wraps a surface in a desktop right-click context menu, inert on touch.
 *
 * The desktop-native way to act on an item (a book cover/row) is a secondary-click menu — the
 * counterpart to the touch long-press. On desktop ([jvmMain]) this renders a `ContextMenuArea`; on
 * touch ([mobileMain]) it is a pure pass-through (no extra layout node), so a shared composable
 * wrapped in it is byte-identical on Android and iOS. An empty [items] list is also a pass-through, so
 * a call site can wrap unconditionally and supply items only where a menu makes sense.
 */
@Composable
expect fun DesktopContextMenu(
    items: List<DesktopContextMenuItem>,
    content: @Composable () -> Unit,
)
