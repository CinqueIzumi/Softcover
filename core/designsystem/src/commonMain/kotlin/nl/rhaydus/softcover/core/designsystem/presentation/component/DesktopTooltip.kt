package nl.rhaydus.softcover.core.designsystem.presentation.component

import androidx.compose.runtime.Composable

/**
 * Wraps a pointer-only control in a hover tooltip on desktop, inert on touch.
 *
 * The desktop sibling of `Modifier.pointerHandCursor()` / `Modifier.hoverHighlight()` (DESIGN_SYSTEM
 * §2.5): an icon-only control that only carries a `contentDescription` is a dead end under a pointer —
 * the hover state promises information no label delivers. On desktop ([jvmMain]) this renders a small
 * editorial tooltip surface after a short hover delay; on touch ([mobileMain]) it is a pure
 * pass-through (no extra layout node), so wrapping a shared composable is byte-identical on Android and
 * iOS. Reach for it on every icon-only desktop control; pass the human-readable label as [text] (the
 * icon's `contentDescription` stays as the accessibility label).
 */
@Composable
expect fun DesktopTooltip(
    text: String,
    content: @Composable () -> Unit,
)
