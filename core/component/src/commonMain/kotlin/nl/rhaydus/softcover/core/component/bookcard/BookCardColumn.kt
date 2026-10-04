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
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

@Composable
internal fun BookCardCoverOnlyLayout(
    model: BookCardUiModel,
    variant: BookCardVariant.CoverOnly,
    onEvent: (BookCardEvent) -> Unit,
    modifier: Modifier,
    isLoading: Boolean,
    content: (@Composable () -> Unit)?,
) {
    BookCardCoverScaffold(
        model = model,
        cover = variant.cover,
        menuLabel = null,
        text = null,
        onEvent = onEvent,
        modifier = modifier,
        isLoading = isLoading,
        content = content,
    )
}

@Composable
internal fun BookCardColumnLayout(
    model: BookCardUiModel,
    variant: BookCardVariant.Column,
    onEvent: (BookCardEvent) -> Unit,
    modifier: Modifier,
    isLoading: Boolean,
    content: (@Composable () -> Unit)?,
) {
    BookCardCoverScaffold(
        model = model,
        cover = variant.cover,
        menuLabel = variant.menuLabel,
        text = variant.text,
        onEvent = onEvent,
        modifier = modifier,
        isLoading = isLoading,
        content = content,
    )
}

@Composable
private fun BookCardCoverScaffold(
    model: BookCardUiModel,
    cover: BookCardCover?,
    menuLabel: String?,
    text: BookCardContent?,
    onEvent: (BookCardEvent) -> Unit,
    modifier: Modifier,
    isLoading: Boolean,
    content: (@Composable () -> Unit)?,
) {
    val progress = model.decorations.progress

    Column(
        modifier = modifier.bookCardInteraction(
            model = model,
            onEvent = onEvent,
            hoverHighlight = false,
            isLoading = isLoading,
        ),
    ) {
        CoverWithCorner(
            cover = cover,
            selection = model.decorations.selection,
            isLoading = isLoading,
            menu = menuLabel?.let { label ->
                CoverMenu(
                    label = label,
                    onClick = { onEvent(BookCardEvent.MenuClicked(key = model.key)) },
                )
            },
            content = content,
        )

        if (progress != null) {
            BookCardProgressBar(
                progress = progress,
                isLoading = isLoading,
                modifier = Modifier.padding(top = 9.dp),
            )
        }

        if (text != null) {
            BookCardTextBlock(
                content = text,
                titleStyle = MaterialTheme.editorialTypography.titleSmall,
                titleMinLines = 2,
                titleMaxLines = 2,
                isLoading = isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 9.dp),
            )
        }
    }
}

private class CoverMenu(
    val label: String,
    val onClick: () -> Unit,
)

@Composable
private fun CoverWithCorner(
    cover: BookCardCover?,
    selection: BookCardSelection?,
    menu: CoverMenu?,
    isLoading: Boolean,
    content: (@Composable () -> Unit)?,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        BookCardCoverLayout(
            cover = cover,
            selection = selection,
            isLoading = isLoading,
            modifier = Modifier.fillMaxWidth(),
        )

        if (isLoading.not() && (menu != null || content != null)) {
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
