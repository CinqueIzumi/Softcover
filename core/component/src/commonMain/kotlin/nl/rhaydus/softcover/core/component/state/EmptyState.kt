package nl.rhaydus.softcover.core.component.state

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.modifier.quoteGlyphSway
import nl.rhaydus.softcover.core.designsystem.presentation.theme.LocalDarkTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/**
 * The message a surface shows in place of content it has nothing to draw.
 *
 * It takes only the size its [modifier] gives it: a [EmptyStateVariant.Centered] state centres
 * vertically only when the caller hands it a height, as a full pane does with `fillMaxSize()`.
 */
@Composable
fun EmptyState(
    model: EmptyStateUiModel,
    onEvent: (EmptyStateEvent) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val style = emptyStateStyle(
        variant = model.variant,
        editorialTypography = MaterialTheme.editorialTypography,
    )

    Column(
        modifier = modifier,
        verticalArrangement = style.verticalArrangement,
        horizontalAlignment = style.horizontalAlignment,
    ) {
        if (style.showsGlyph) {
            SoftcoverText(
                text = "“",
                style = MaterialTheme.editorialTypography.quoteGlyph,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                    alpha = if (LocalDarkTheme.current) 0.15f else 0.3f,
                ),
                modifier = Modifier
                    .quoteGlyphSway()
                    .clearAndSetSemantics {},
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        SoftcoverText(
            text = model.title,
            style = style.titleStyle,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = style.textAlign,
            modifier = Modifier.semantics { heading() },
        )

        model.body?.let { body ->
            Spacer(modifier = Modifier.height(style.bodyGap))

            SoftcoverText(
                text = body,
                style = style.bodyStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = style.textAlign,
            )
        }

        when (val variant = model.variant) {
            is EmptyStateVariant.Centered -> variant.button
            EmptyStateVariant.Inline -> null
        }?.let { button ->
            Spacer(modifier = Modifier.height(24.dp))

            RhaydusButton(
                label = button.label,
                style = ButtonStyle.FILLED,
                icon = button.icon?.let { icon ->
                    drawableIconResource(
                        icon = icon,
                        contentDescription = "",
                    )
                },
                onClick = { onEvent(EmptyStateEvent.ButtonClicked) },
            )
        }
    }
}
