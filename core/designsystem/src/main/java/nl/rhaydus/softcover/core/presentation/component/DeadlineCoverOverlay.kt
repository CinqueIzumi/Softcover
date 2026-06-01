package nl.rhaydus.softcover.core.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.domain.model.DeadlineProgress
import nl.rhaydus.softcover.core.domain.model.DeadlineStatus
import nl.rhaydus.softcover.core.presentation.modifier.conditional
import nl.rhaydus.softcover.core.presentation.modifier.grayscale

@Composable
fun DeadlineCoverOverlay(
    progress: DeadlineProgress?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val status = progress?.status

    Box(
        modifier = modifier
            .conditional(
                condition = status == DeadlineStatus.Expired,
                ifTrue = { Modifier.grayscale() },
            ),
        contentAlignment = Alignment.TopEnd,
    ) {
        content()

        if (status != null) {
            DeadlineBadge(
                status = status,
                modifier = Modifier.padding(all = 6.dp),
            )
        }
    }
}
