package nl.rhaydus.softcover.feature.library.presentation.screen.section

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScaleCombinedClickable
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/**
 * Wraps a grid cell in a Box so a [dragHandle] can be overlaid on the top-right corner of the
 * cover. Used by `LayoutBookEntry` / `LayoutEditionEntry` for the GRID_* layouts where there is
 * no inline trailing slot; the handle sits visually on the cover. When [dragHandle] is null the
 * cell renders without an extra wrapping Box.
 */
@Composable
internal fun CoverGridOverlay(
    dragHandle: (@Composable () -> Unit)?,
    cell: @Composable () -> Unit,
) {
    if (dragHandle == null) {
        cell()
    } else {
        Box {
            cell()

            Box(modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)) {
                dragHandle()
            }
        }
    }
}

/**
 * The wavy shelf-progress signature (redesign brief / design-system.md "Progress — wavy, not a
 * ring"): a thin sine wave under a grid cover or beside a list row. Reserves its height even when
 * [progressFraction] is null so a shelf mixing in-progress and untouched books never jumps a row.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LibraryWaveProgressRow(
    progressFraction: Float?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp),
    ) {
        if (progressFraction != null) {
            LinearWavyProgressIndicator(
                progress = { progressFraction.coerceIn(
                    0f,
                    1f,
                ) },
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
internal fun CoverOnlyCell(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    progressFraction: Float? = null,
    cover: @Composable (Modifier) -> Unit,
) {
    Column(
        modifier = modifier
            .pointerHandCursor()
            .pressScaleCombinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
    ) {
        cover(
            Modifier
                .fillMaxWidth()
                .aspectRatio(ratio = 2f / 3f),
        )

        LibraryWaveProgressRow(
            progressFraction = progressFraction,
            modifier = Modifier.padding(top = 9.dp),
        )
    }
}

@Composable
internal fun GridBookCell(
    title: String,
    authorName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    progressFraction: Float? = null,
    cover: @Composable (Modifier) -> Unit,
) {
    Column(
        modifier = modifier
            .pointerHandCursor()
            .pressScaleCombinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
    ) {
        cover(
            Modifier
                .fillMaxWidth()
                .aspectRatio(ratio = 2f / 3f),
        )

        LibraryWaveProgressRow(
            progressFraction = progressFraction,
            modifier = Modifier.padding(top = 9.dp),
        )

        Spacer(modifier = Modifier.height(9.dp))

        Text(
            text = title,
            style = MaterialTheme.editorialTypography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )

        if (authorName.isNotBlank()) {
            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = authorName,
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
