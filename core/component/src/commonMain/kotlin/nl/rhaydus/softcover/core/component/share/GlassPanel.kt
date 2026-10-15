package nl.rhaydus.softcover.core.component.share

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Corners
import nl.rhaydus.softcover.core.designsystem.presentation.theme.OverMediaScrim
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing

@Composable
internal fun GlassPanel(
    modifier: Modifier = Modifier,
    shape: Shape = Corners.md,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .background(
                color = OverMediaScrim.copy(alpha = 0.38f),
                shape = shape,
            )
            .padding(Spacing.sm),
    ) {
        content()
    }
}
