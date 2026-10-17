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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/**
 * The one flat row anatomy behind [SettingsMenuRow] and [SettingsSelectableRow]:
 * an optional [leading] slot, [title] over an optional [secondary] line, and an optional [trailing]
 * slot, at 16dp vertical / 4dp horizontal padding with a 4dp title→secondary gap and a 16dp gap before
 * [trailing]. [interactionSource] doubles as the row's selectability switch: `null` renders no press
 * treatment (the Copyable menu row); non-null drives an animated `surfaceContainer` wash and wraps the
 * caller's [semantics] modifier (`tactileSelectable` / `tactileClickable`, built against the same
 * [interactionSource]).
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

        val rowBackground by animateColorAsState(
            targetValue = if (isPressed) {
                MaterialTheme.colorScheme.surfaceContainer
            } else {
                Color.Transparent
            },
            animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
            label = "settingsRowPress",
        )

        Modifier
            .then(semantics)
            .background(rowBackground)
    } else {
        Modifier
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(interactionModifier)
            .padding(vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            leading()
            Spacer(modifier = Modifier.width(Spacing.md))
        }

        Column(modifier = Modifier.weight(1f)) {
            val titleText: @Composable () -> Unit = {
                SoftcoverText(
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
                Spacer(modifier = Modifier.height(Spacing.xxs))

                SoftcoverText(
                    text = secondary,
                    style = secondaryStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (trailing != null) {
            Spacer(modifier = Modifier.width(Spacing.md))
            trailing()
        }
    }
}
