package nl.rhaydus.softcover.core.component.state

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OfflineEmptyStatePlaceholder(
    model: EmptyStateUiModel,
    modifier: Modifier = Modifier,
) {
    EmptyState(
        model = model,
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
    )
}
