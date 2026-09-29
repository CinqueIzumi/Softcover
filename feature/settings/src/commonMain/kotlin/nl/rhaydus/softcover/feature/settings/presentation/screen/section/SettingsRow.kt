package nl.rhaydus.softcover.feature.settings.presentation.screen.section

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScale
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/**
 * The one flat row anatomy behind [SettingsMenuRow], [SettingsToggleRow] and [SettingsSelectableRow]:
 * an optional [leading] slot, [title] over an optional [secondary] line, and an optional [trailing]
 * slot, at 16dp vertical / 4dp horizontal padding with a 4dp title→secondary gap and a 16dp gap before
 * [trailing]. [interactionSource] doubles as the row's selectability switch: `null` renders no press
 * treatment (the Copyable menu row); non-null drives `pointerHandCursor()` + `pressScale` + an animated
 * `surfaceContainer` wash gated on `playDecorativeMotion()`, wrapping the caller's [semantics] modifier
 * (`toggleable` / `selectable` / `clickable`, built against the same [interactionSource]).
 * [selectionContainer] wraps [title] for the row whose only affordance is copying its text.
 */
@Composable
internal fun SettingsRow(
    title: String,
    secondary: String?,
    modifier: Modifier = Modifier,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    secondaryStyle: TextStyle = MaterialTheme.editorialTypography.bodySmall,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    selectionContainer: Boolean = false,
    interactionSource: MutableInteractionSource? = null,
    semantics: Modifier = Modifier,
) {
    val interactionModifier = if (interactionSource != null) {
        val isPressed by interactionSource.collectIsPressedAsState()
        val playMotion = playDecorativeMotion()

        val rowBackground by animateColorAsState(
            targetValue = if (isPressed && playMotion) {
                MaterialTheme.colorScheme.surfaceContainer
            } else {
                Color.Transparent
            },
            label = "settingsRowPress",
        )

        Modifier
            .pointerHandCursor()
            .pressScale(interactionSource)
            .then(semantics)
            .background(rowBackground)
    } else {
        Modifier
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(interactionModifier)
            .padding(
                horizontal = 4.dp,
                vertical = 16.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            leading()
            Spacer(modifier = Modifier.width(16.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            val titleText: @Composable () -> Unit = {
                Text(
                    text = title,
                    style = MaterialTheme.editorialTypography.titleMedium,
                    color = titleColor,
                )
            }

            if (selectionContainer) {
                SelectionContainer {
                    titleText()
                }
            } else {
                titleText()
            }

            if (secondary != null) {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = secondary,
                    style = secondaryStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (trailing != null) {
            Spacer(modifier = Modifier.width(16.dp))
            trailing()
        }
    }
}
