package nl.rhaydus.softcover.core.component.quote

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.rhaydus.designsystem.haptics.LocalHaptics
import nl.rhaydus.designsystem.modifier.pressScaleClickable
import nl.rhaydus.designsystem.modifier.skeletonMask
import nl.rhaydus.designsystem.theme.Corners
import nl.rhaydus.designsystem.theme.Spacing
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.component.cover.Cover
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.LocalSchemeBrightness
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/**
 * A quote in the Editorial quote pattern: a saved quote with its byline and Delete menu, or a quote over
 * the footer of its book, where the whole card emits [QuoteCardEvent.Clicked].
 */
@Composable
fun QuoteCard(
    model: QuoteCardUiModel,
    onEvent: (QuoteCardEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (model) {
        is QuoteCardUiModel.Saved -> SavedQuoteCard(
            model = model,
            onEvent = onEvent,
            modifier = modifier,
        )

        is QuoteCardUiModel.Book -> BookQuoteCard(
            model = model,
            onEvent = onEvent,
            modifier = modifier,
        )
    }
}

@Composable
private fun BookQuoteCard(
    model: QuoteCardUiModel.Book,
    onEvent: (QuoteCardEvent) -> Unit,
    modifier: Modifier,
) {
    val haptics = LocalHaptics.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .pressScaleClickable(
                onClick = {
                    onEvent(QuoteCardEvent.Clicked)
                    haptics.select()
                },
                hoverShape = Corners.xl,
                role = Role.Button.takeIf { model.isLoading.not() },
                enabled = model.isLoading.not(),
            ),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = Corners.xl,
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            SoftcoverText(
                text = "“",
                style = MaterialTheme.editorialTypography.quoteGlyph,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.18f),
                modifier = Modifier.align(Alignment.TopStart),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = Spacing.lg,
                        top = Spacing.lg,
                        end = Spacing.lg,
                        bottom = Spacing.md,
                    ),
            ) {
                SoftcoverText(
                    text = model.text,
                    style = MaterialTheme.editorialTypography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.skeletonMask(
                        visible = model.isLoading,
                        shape = Corners.xs,
                    ),
                )

                Spacer(modifier = Modifier.height(Spacing.md))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Cover(
                        model = model.cover,
                        modifier = Modifier
                            .width(32.dp)
                            .skeletonMask(
                                visible = model.isLoading,
                                shape = Corners.xs,
                            ),
                    )

                    Column {
                        SoftcoverText(
                            text = model.title,
                            style = MaterialTheme.editorialTypography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        SoftcoverText(
                            text = model.caption,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedQuoteCard(
    model: QuoteCardUiModel.Saved,
    onEvent: (QuoteCardEvent) -> Unit,
    modifier: Modifier,
) {
    var menuOpen by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = Corners.lg,
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            val glyphAlpha = if (LocalSchemeBrightness.current.isDark) 0.22f else 0.32f

            SoftcoverText(
                text = "“",
                style = MaterialTheme.editorialTypography.quoteGlyph.copy(fontSize = 48.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = glyphAlpha),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(
                        start = Spacing.md,
                        top = Spacing.xxs,
                    ),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.md),
            ) {
                Spacer(modifier = Modifier.height(Spacing.xxl))

                SoftcoverText(
                    text = model.text,
                    style = MaterialTheme.editorialTypography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(Spacing.md))

                SoftcoverText(
                    text = model.byline,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Box(modifier = Modifier.align(Alignment.TopEnd)) {
                IconButton(onClick = { menuOpen = true }) {
                    val moreIcon = drawableIconResource(
                        icon = SoftcoverIcon.MoreVert,
                        contentDescription = "Quote actions",
                    )

                    Icon(
                        painter = moreIcon.getIconPainter(),
                        contentDescription = moreIcon.contentDescription,
                    )
                }

                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false },
                ) {
                    DropdownMenuItem(
                        text = { SoftcoverText(text = "Delete") },
                        leadingIcon = {
                            val deleteIcon = drawableIconResource(
                                icon = SoftcoverIcon.Delete,
                                contentDescription = "",
                            )

                            Icon(
                                painter = deleteIcon.getIconPainter(),
                                contentDescription = deleteIcon.contentDescription,
                            )
                        },
                        onClick = {
                            menuOpen = false
                            onEvent(QuoteCardEvent.DeleteRequested(key = model.key))
                        },
                    )
                }
            }
        }
    }
}

@StandardPreview
@Composable
private fun QuoteCardShortPreview() {
    SoftcoverTheme {
        QuoteCard(
            model = QuoteCardUiModel.previews.getValue("Short quote"),
            onEvent = {},
        )
    }
}

@StandardPreview
@Composable
private fun QuoteCardBookPreview() {
    SoftcoverTheme {
        QuoteCard(
            model = QuoteCardUiModel.previews.getValue("Book quote"),
            onEvent = {},
        )
    }
}

@StandardPreview
@Composable
private fun QuoteCardBookLoadingPreview() {
    SoftcoverTheme {
        QuoteCard(
            model = QuoteCardUiModel.previews.getValue("Book quote, loading"),
            onEvent = {},
        )
    }
}

@StandardPreview
@Composable
private fun QuoteCardLongPreview() {
    SoftcoverTheme {
        QuoteCard(
            model = QuoteCardUiModel.previews.getValue("Long quote"),
            onEvent = {},
        )
    }
}
