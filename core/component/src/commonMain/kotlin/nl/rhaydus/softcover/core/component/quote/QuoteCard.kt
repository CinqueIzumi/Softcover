package nl.rhaydus.softcover.core.component.quote

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.sp
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Corners
import nl.rhaydus.softcover.core.designsystem.presentation.theme.LocalSchemeBrightness
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/**
 * One saved quote in the Editorial quote pattern: an oversized low-alpha opening mark behind the italic
 * passage, the byline beneath it, and a menu holding Delete. Only whether the menu is open is local.
 */
@Composable
fun QuoteCard(
    model: QuoteCardUiModel,
    onEvent: (QuoteCardEvent) -> Unit,
    modifier: Modifier = Modifier,
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
                style = MaterialTheme.editorialTypography.quoteGlyph.copy(fontSize = 92.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = glyphAlpha),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = Spacing.md, top = Spacing.xxs),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.md),
            ) {
                Spacer(modifier = Modifier.height(Spacing.xxl))

                SoftcoverText(
                    text = model.text,
                    style = MaterialTheme.editorialTypography.headlineMedium.copy(fontStyle = FontStyle.Italic),
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
private fun QuoteCardLongPreview() {
    SoftcoverTheme {
        QuoteCard(
            model = QuoteCardUiModel.previews.getValue("Long quote"),
            onEvent = {},
        )
    }
}
