package nl.rhaydus.softcover.core.component.feeling

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.snap
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScaleClickable
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.designsystem.theme.Spacing
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.designsystem.presentation.theme.tabularFigures

/**
 * The read-only "How it felt" block: the whole book's moods, each with a bar filled to its strength, then the
 * moods marked along the way.
 */
@Composable
fun HowItFeltBlock(
    model: HowItFeltBlockUiModel,
    onEvent: (HowItFeltBlockEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val playMotion = playDecorativeMotion()
    val sizeSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntSize>()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = if (playMotion) sizeSpec else snap()),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SoftcoverText(
                text = model.title.uppercase(),
                style = MaterialTheme.editorialTypography.eyebrowSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.semantics(mergeDescendants = true) { heading() },
            )

            Box(
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .pointerHandCursor()
                    .pressScaleClickable(
                        onClick = { onEvent(HowItFeltBlockEvent.EditRequested) },
                        role = Role.Button,
                    )
                    .padding(horizontal = Spacing.xs),
                contentAlignment = Alignment.Center,
            ) {
                SoftcoverText(
                    text = model.editLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        FeelingReveal(visible = model.isEmpty) {
            SoftcoverText(
                text = model.invitation,
                style = MaterialTheme.editorialTypography.body,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = Spacing.sm),
            )
        }

        FeelingReveal(visible = model.moods.isNotEmpty()) {
            Column(
                modifier = Modifier.padding(bottom = Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                model.moods.forEach { mood ->
                    key(mood.key) {
                        HowItFeltMoodRow(mood = mood)
                    }
                }
            }
        }

        FeelingReveal(visible = model.moments.isNotEmpty()) {
            Column(modifier = Modifier.padding(bottom = Spacing.sm)) {
                SoftcoverText(
                    text = model.momentsTitle.uppercase(),
                    style = MaterialTheme.editorialTypography.eyebrowSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(vertical = Spacing.xs)
                        .semantics(mergeDescendants = true) { heading() },
                )

                model.moments.forEach { moment ->
                    key(moment.id) {
                        HowItFeltMomentRow(
                            moment = moment,
                            onClick = { onEvent(HowItFeltBlockEvent.MomentClicked(id = moment.id)) },
                        )
                    }
                }
            }
        }

        SoftcoverText(
            text = model.caption,
            style = MaterialTheme.editorialTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun HowItFeltMoodRow(mood: HowItFeltMoodUiModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
        verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
    ) {
        HowItFeltLabelAndValue(
            label = mood.label,
            strength = mood.strength,
        )

        FeelingTrack(strength = mood.strength)
    }
}

@Composable
private fun HowItFeltMomentRow(
    moment: HowItFeltMomentUiModel,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .pointerHandCursor()
            .pressScaleClickable(
                onClick = onClick,
                role = Role.Button,
            ),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SoftcoverText(
            text = moment.at,
            style = MaterialTheme.typography.labelLarge.tabularFigures(),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(52.dp),
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
        ) {
            HowItFeltLabelAndValue(
                label = moment.label,
                strength = moment.strength,
            )

            FeelingTrack(strength = moment.strength)
        }
    }
}

@Composable
private fun HowItFeltLabelAndValue(
    label: String,
    strength: Int?,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalAlignment = Alignment.Bottom,
    ) {
        SoftcoverText(
            text = label,
            style = MaterialTheme.editorialTypography.body,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )

        SoftcoverText(
            text = strength?.toString().orEmpty(),
            style = MaterialTheme.typography.labelMedium.tabularFigures(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
        )
    }
}
