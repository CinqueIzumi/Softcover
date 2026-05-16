package nl.rhaydus.softcover.feature.library.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import nl.rhaydus.softcover.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeRowActions(
    onMarkAsRead: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    allowMarkAsRead: Boolean = true,
    backgroundShape: Shape = RoundedCornerShape(size = 0.dp),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable () -> Unit,
) {
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    if (allowMarkAsRead) {
                        onMarkAsRead()
                        true
                    } else {
                        false
                    }
                }

                SwipeToDismissBoxValue.EndToStart -> {
                    onRemove()
                    true
                }

                SwipeToDismissBoxValue.Settled -> true
            }
        },
        positionalThreshold = { distance -> distance * 0.4f },
    )

    LaunchedEffect(state.currentValue) {
        if (state.currentValue != SwipeToDismissBoxValue.Settled) {
            state.reset()
        }
    }

    SwipeToDismissBox(
        state = state,
        modifier = modifier,
        enableDismissFromStartToEnd = allowMarkAsRead,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            SwipeBackground(
                progress = abs(state.progress).coerceIn(0f, 1f),
                direction = state.dismissDirection,
                shape = backgroundShape,
                padding = contentPadding,
            )
        },
        content = { content() },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeBackground(
    progress: Float,
    direction: SwipeToDismissBoxValue,
    shape: Shape,
    padding: PaddingValues,
) {
    val isRead = direction == SwipeToDismissBoxValue.StartToEnd
    val color = if (isRead) {
        MaterialTheme.colorScheme.tertiaryContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }

    val contentColor = if (isRead) {
        MaterialTheme.colorScheme.onTertiaryContainer
    } else {
        MaterialTheme.colorScheme.onErrorContainer
    }

    val iconRes = if (isRead) R.drawable.ic_bookmark_check else R.drawable.ic_delete
    val label = if (isRead) "Mark as read" else "Remove"
    val alignment = if (isRead) Alignment.CenterStart else Alignment.CenterEnd
    val arrangement = if (isRead) Arrangement.Start else Arrangement.End

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .graphicsLayer { alpha = (progress * 1.2f).coerceAtMost(1f) }
            .background(
                color = color,
                shape = shape,
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(alignment)
                .padding(horizontal = 24.dp),
            horizontalArrangement = arrangement,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = contentColor,
            )

            Spacer(modifier = Modifier.padding(start = 8.dp))

            Text(
                text = label,
                color = contentColor,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            )
        }
    }
}
