package nl.rhaydus.softcover.core.designsystem.presentation.modifier

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// Touch platforms have no keyboard modifiers or secondary pointer; selection is reached via
// long-press, so this is a pure pass-through and leaves the wrapped cell byte-identical.
@Composable
actual fun Modifier.platformModifierClick(
    onCtrlClick: () -> Unit,
    onShiftClick: () -> Unit,
): Modifier = this
