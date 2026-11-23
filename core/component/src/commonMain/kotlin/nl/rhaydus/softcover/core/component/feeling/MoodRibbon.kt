package nl.rhaydus.softcover.core.component.feeling

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.theme.Spacing
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/**
 * A hairline under the progress bar with a dot and a lowercase word at each mood's position. A word near
 * either end stays inside the bar rather than being clipped.
 */
@Composable
fun MoodRibbon(
    model: MoodRibbonUiModel,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(34.dp)
            .clearAndSetSemantics {
                contentDescription = model.contentDescription
                role = Role.Image
            },
    ) {
        Box(
            modifier = Modifier
                .padding(top = Spacing.xxs)
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant),
        )

        Layout(
            content = {
                model.marks.forEach { mark ->
                    key(mark.word, mark.position) {
                        MoodRibbonMark(mark = mark)
                    }
                }
            },
        ) { measurables, constraints ->
            val placeables = measurables.map {
                it.measure(
                    constraints.copy(
                        minWidth = 0,
                        minHeight = 0,
                        maxWidth = Constraints.Infinity,
                    ),
                )
            }

            layout(
                width = constraints.maxWidth,
                height = constraints.maxHeight,
            ) {
                placeables.forEachIndexed { index, placeable ->
                    val centre = (model.marks[index].position.coerceIn(
                        0f,
                        1f,
                    ) * constraints.maxWidth).toInt()

                    placeable.place(
                        x = (centre - placeable.width / 2)
                            .coerceIn(
                                0,
                                (constraints.maxWidth - placeable.width).coerceAtLeast(0),
                            ),
                        y = 0,
                    )
                }
            }
        }
    }
}

@Composable
private fun MoodRibbonMark(mark: MoodRibbonMarkUiModel) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .alpha(
                    mark.strength?.let {
                        (it / 100f).coerceIn(
                            0.4f,
                            1f,
                        )
                    } ?: 0.3f,
                )
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape,
                ),
        )

        SoftcoverText(
            text = mark.word,
            style = MaterialTheme.editorialTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            softWrap = false,
            modifier = Modifier.padding(top = Spacing.xxxs),
        )
    }
}
