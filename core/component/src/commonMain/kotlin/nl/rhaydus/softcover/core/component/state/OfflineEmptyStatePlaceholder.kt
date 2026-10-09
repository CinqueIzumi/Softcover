package nl.rhaydus.softcover.core.component.state

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing

@Composable
fun OfflineEmptyStatePlaceholder(
    model: EmptyStateUiModel,
    modifier: Modifier = Modifier,
) {
    EmptyState(
        model = model,
        modifier = modifier
            .fillMaxSize()
            .padding(
                horizontal = Spacing.gutter,
                vertical = Spacing.xl,
            ),
    )
}
