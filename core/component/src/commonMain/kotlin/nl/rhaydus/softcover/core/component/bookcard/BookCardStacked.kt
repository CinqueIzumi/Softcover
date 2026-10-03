package nl.rhaydus.softcover.core.component.bookcard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

@Composable
internal fun BookCardCoverOnlyLayout(
    model: BookCardUiModel,
    variant: BookCardVariant.CoverOnly,
    onEvent: (BookCardEvent) -> Unit,
    modifier: Modifier,
    content: (@Composable () -> Unit)?,
) {
    val progress = model.decorations.progress

    Column(
        modifier = modifier.bookCardInteraction(
            model = model,
            onEvent = onEvent,
            hoverHighlight = false,
        ),
    ) {
        CoverWithCorner(
            cover = variant.cover,
            decorations = model.decorations,
            menu = null,
            content = content,
        )

        if (progress != null) {
            BookCardProgressBar(
                progress = progress,
                modifier = Modifier.padding(top = 9.dp),
                barHeight = 8.dp,
            )
        }
    }
}

@Composable
internal fun BookCardStackedLayout(
    model: BookCardUiModel,
    variant: BookCardVariant.Stacked,
    onEvent: (BookCardEvent) -> Unit,
    modifier: Modifier,
    content: (@Composable () -> Unit)?,
) {
    Column(
        modifier = modifier.bookCardInteraction(
            model = model,
            onEvent = onEvent,
            hoverHighlight = false,
        ),
    ) {
        CoverWithCorner(
            cover = variant.cover,
            decorations = model.decorations,
            menu = variant.menuLabel?.let { label ->
                CoverMenu(
                    label = label,
                    onClick = { onEvent(BookCardEvent.MenuClicked(key = model.key)) },
                )
            },
            content = content,
        )

        val progress = model.decorations.progress

        if (progress != null) {
            BookCardProgressBar(
                progress = progress,
                modifier = Modifier.padding(top = 9.dp),
                barHeight = 8.dp,
            )
        }

        BookCardTextBlock(
            content = variant.text,
            titleStyle = MaterialTheme.editorialTypography.titleSmall,
            titleMinLines = 1,
            titleMaxLines = 2,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 9.dp),
        )
    }
}

private class CoverMenu(
    val label: String,
    val onClick: () -> Unit,
)

@Composable
private fun CoverWithCorner(
    cover: CoverUiModel?,
    decorations: BookCardDecorations,
    menu: CoverMenu?,
    content: (@Composable () -> Unit)?,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        BookCardCover(
            cover = cover,
            decorations = decorations,
            modifier = Modifier.fillMaxWidth(),
        )

        if (menu != null || content != null) {
            Row(modifier = Modifier.align(Alignment.TopEnd)) {
                if (menu != null) {
                    BookCardMenuButton(
                        contentDescription = menu.label,
                        onClick = menu.onClick,
                    )
                }

                if (content != null) {
                    Box(modifier = Modifier.padding(4.dp)) {
                        content()
                    }
                }
            }
        }
    }
}
