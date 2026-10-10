package nl.rhaydus.softcover.core.component.sheet

import androidx.compose.runtime.Composable

/**
 * A full-window dialog that paints no dim of its own and plays no platform enter or exit
 * transition, so [AdaptiveModalSheet] owns both. Outside taps are not reported: the caller's
 * content fills the window and handles them itself.
 */
@Composable
internal expect fun ScrimlessDialog(
    onDismissRequest: () -> Unit,
    dismissOnBackPress: Boolean,
    content: @Composable () -> Unit,
)
