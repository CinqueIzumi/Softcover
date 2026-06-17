package nl.rhaydus.softcover.core.designsystem.presentation.modifier

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Desktop modifier-click selection, inert on touch.
 *
 * On desktop ([jvmMain]) a primary click with **Ctrl/Cmd** held fires [onCtrlClick] (toggle this
 * item's selection) and with **Shift** held fires [onShiftClick] (range-select to this item) — the
 * standard pointer multi-select gestures. The interceptor runs in the pointer **Initial** pass and
 * consumes the press only when a modifier is held, so the surface's ordinary click (a later, inner
 * `combinedClickable`) still fires for a plain click and is preempted only for a modifier-click — no
 * double-fire. On touch ([mobileMain]) it is a pure pass-through (returns the receiver unchanged), so
 * a shared cell that wires it is byte-identical on Android and iOS, where selection is reached via
 * long-press instead.
 */
@Composable
expect fun Modifier.platformModifierClick(
    onCtrlClick: () -> Unit,
    onShiftClick: () -> Unit,
): Modifier
