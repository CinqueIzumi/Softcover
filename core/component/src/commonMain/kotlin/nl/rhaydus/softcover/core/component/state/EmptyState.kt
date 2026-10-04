package nl.rhaydus.softcover.core.component.state

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
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
    when (val variant = model.variant) {
        is EmptyStateVariant.Centered -> CenteredEmptyState(
            model = model,
            button = variant.button,
            onEvent = onEvent,
            modifier = modifier,
        )

        EmptyStateVariant.Inline -> InlineEmptyState(
            model = model,
            modifier = modifier,
        )
    }
}

@Composable
private fun CenteredEmptyState(
    model: EmptyStateUiModel,
    button: EmptyStateButtonUiModel?,
    onEvent: (EmptyStateEvent) -> Unit,
    modifier: Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
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

        SoftcoverText(
            text = model.title,
            style = MaterialTheme.editorialTypography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() },
        )

        model.body?.let { body ->
            Spacer(modifier = Modifier.height(12.dp))

            SoftcoverText(
                text = body,
                style = MaterialTheme.editorialTypography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        button?.let {
            Spacer(modifier = Modifier.height(24.dp))

            RhaydusButton(
                label = it.label,
                style = ButtonStyle.FILLED,
                icon = it.icon?.let { icon ->
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

@Composable
private fun InlineEmptyState(
    model: EmptyStateUiModel,
    modifier: Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
    ) {
        SoftcoverText(
            text = model.title,
            style = MaterialTheme.editorialTypography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Start,
            modifier = Modifier.semantics { heading() },
        )

        model.body?.let { body ->
            Spacer(modifier = Modifier.height(4.dp))

            SoftcoverText(
                text = body,
                style = MaterialTheme.editorialTypography.body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Start,
            )
        }
    }
}
