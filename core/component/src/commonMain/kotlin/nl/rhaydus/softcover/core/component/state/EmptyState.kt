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
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import kotlin.math.roundToInt
import nl.rhaydus.softcover.core.component.button.PillButton
import nl.rhaydus.softcover.core.component.button.PillButtonUiModel
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.modifier.quoteGlyphSway
import nl.rhaydus.softcover.core.designsystem.presentation.theme.LocalSchemeBrightness
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
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
    button: PillButtonUiModel?,
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
                alpha = if (LocalSchemeBrightness.current.isDark) 0.15f else 0.3f,
            ),
            overflow = TextOverflow.Visible,
            modifier = Modifier
                .cropToQuoteMark()
                .quoteGlyphSway()
                .clearAndSetSemantics {},
        )

        Spacer(modifier = Modifier.height(Spacing.sm))

        SoftcoverText(
            text = model.title,
            style = MaterialTheme.editorialTypography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() },
        )

        model.body?.let { body ->
            Spacer(modifier = Modifier.height(Spacing.sm))

            SoftcoverText(
                text = body,
                style = MaterialTheme.editorialTypography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        button?.let {
            Spacer(modifier = Modifier.height(Spacing.xl))

            PillButton(
                model = it,
                onEvent = { onEvent(EmptyStateEvent.ButtonClicked) },
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
            Spacer(modifier = Modifier.height(Spacing.xxs))

            SoftcoverText(
                text = body,
                style = MaterialTheme.editorialTypography.body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Start,
            )
        }
    }
}

/**
 * Sizes the quote glyph to its ink rather than its line box: in the display font the mark covers
 * only the band between [QUOTE_MARK_TOP_FRACTION] and the sum of both fractions of the measured
 * height, measured on device.
 */
private fun Modifier.cropToQuoteMark(): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints.copy(minHeight = 0))
    val markTop = (placeable.height * QUOTE_MARK_TOP_FRACTION).roundToInt()
    val markHeight = (placeable.height * QUOTE_MARK_HEIGHT_FRACTION).roundToInt()

    layout(
        width = placeable.width,
        height = markHeight,
    ) {
        placeable.place(
            x = 0,
            y = -markTop,
        )
    }
}

private const val QUOTE_MARK_TOP_FRACTION = 0.185f
private const val QUOTE_MARK_HEIGHT_FRACTION = 0.215f
