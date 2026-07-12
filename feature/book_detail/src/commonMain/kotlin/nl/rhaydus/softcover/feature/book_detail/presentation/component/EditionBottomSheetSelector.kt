package nl.rhaydus.softcover.feature.book_detail.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.AdaptiveModalSheet
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.editorial.component.EditorialSectionHeader
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.designsystem.modifier.conditional
import nl.rhaydus.designsystem.modifier.noRippleClickable
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.designsystem.presentation.component.EditionImage
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.preview.PreviewData
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.ReadingFormat

@Composable
internal fun EditionBottomSheetSelector(
    bookTitle: String,
    currentEdition: BookEdition,
    defaultEdition: BookEdition?,
    editions: List<BookEdition>,
    isLoading: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onDismissRequest: () -> Unit,
    onConfirmClick: (BookEdition) -> Unit,
) {
    AdaptiveModalSheet(onDismissRequest = onDismissRequest) {
        EditionBottomSheetContent(
            bookTitle = bookTitle,
            currentEdition = currentEdition,
            defaultEdition = defaultEdition,
            editions = editions,
            isLoading = isLoading,
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            onConfirmClick = onConfirmClick,
        )
    }
}

@Composable
private fun EditionBottomSheetContent(
    bookTitle: String,
    currentEdition: BookEdition,
    defaultEdition: BookEdition?,
    editions: List<BookEdition>,
    isLoading: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onConfirmClick: (BookEdition) -> Unit,
) {
    var selectedEdition by remember {
        mutableStateOf(currentEdition)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .imePadding()
            .padding(horizontal = 24.dp)
            .padding(bottom = 16.dp),
    ) {
        EditorialSectionHeader(
            eyebrow = "Editions",
            headline = "Change edition",
            description = bookTitle,
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text(text = "Search by ISBN or publisher") },
            leadingIcon = {
                val searchIcon = drawableIconResource(
                    icon = SoftcoverIcon.Search,
                    contentDescription = "Search",
                )

                Icon(
                    painter = searchIcon.getIconPainter(),
                    contentDescription = searchIcon.contentDescription,
                )
            },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        val clearIcon = drawableIconResource(
                            icon = SoftcoverIcon.Close,
                            contentDescription = "Clear search",
                        )

                        Icon(
                            painter = clearIcon.getIconPainter(),
                            contentDescription = clearIcon.contentDescription,
                        )
                    }
                }
            } else {
                null
            },
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading && editions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else if (editions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No editions match your search",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.editorialTypography.body,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(editions, key = { it.id }) { edition ->
                    EditionItem(
                        edition = edition,
                        selected = edition.id == selectedEdition.id,
                        onEditionClick = { selectedEdition = edition },
                        defaultEdition = defaultEdition ?: edition,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        RhaydusButton(
            label = "Confirm edition",
            style = ButtonStyle.FILLED,
            size = ButtonSize.M,
            enabled = isLoading.not() && selectedEdition != currentEdition,
            onClick = { onConfirmClick(selectedEdition) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun EditionItem(
    edition: BookEdition,
    defaultEdition: BookEdition,
    selected: Boolean,
    onEditionClick: () -> Unit,
) {
    val cardShape = RoundedCornerShape(8.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = cardShape)
            .conditional(
                condition = selected,
                ifTrue = {
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = cardShape,
                    )
                },
            )
            .background(color = MaterialTheme.colorScheme.surfaceContainer)
            .noRippleClickable(onEditionClick)
            .padding(all = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            EditionImage(
                edition = edition,
                modifier = Modifier.width(width = 60.dp),
                isLoading = false,
                defaultEdition = defaultEdition,
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                edition.title?.let { title ->
                    Text(
                        text = title,
                        style = MaterialTheme.editorialTypography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                edition.publisher?.let { publisher ->
                    Text(
                        text = publisher,
                        style = MaterialTheme.editorialTypography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                edition.pages?.let {
                    Text(
                        text = "${edition.pages} pages",
                        style = MaterialTheme.editorialTypography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                edition.isbn13?.takeIf { it.isNotBlank() }?.let { isbn ->
                    Text(
                        text = "ISBN-13: $isbn",
                        style = MaterialTheme.editorialTypography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                edition.isbn10?.takeIf { it.isNotBlank() }?.let { isbn ->
                    Text(
                        text = "ISBN-10: $isbn",
                        style = MaterialTheme.editorialTypography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                edition.format.takeIf { it.isNotEmpty() }?.let { format ->
                    Text(
                        text = format,
                        style = MaterialTheme.editorialTypography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                edition.readingFormat?.let { readingFormat ->
                    Text(
                        text = readingFormat.label,
                        style = MaterialTheme.editorialTypography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (edition.owned) {
                    Text(
                        text = "You own this edition!",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.editorialTypography.eyebrowSmall,
                    )
                }
            }
        }
    }
}

@StandardPreview
@Composable
private fun EditionBottomSheetContentPreview() {
    val baseEdition = PreviewData.baseEdition.copy(title = "Snake-Eater")

    val editions = listOf(
        baseEdition.copy(
            pages = 271,
            isbn10 = "0451524934",
            isbn13 = "9780451524935",
            publisher = "47 north",
            id = 40,
        ),
        baseEdition.copy(
            pages = 352,
            isbn10 = "1789091349",
            isbn13 = null,
            publisher = "Titan Books",
            id = 20,
            format = "",
            readingFormat = ReadingFormat.Audio,
            owned = true,
        ),
        baseEdition.copy(
            pages = 267,
            isbn10 = null,
            isbn13 = "9780062060624",
            publisher = "47 north",
            id = 80,
            readingFormat = ReadingFormat.Ebook,
        ),
    )

    SoftcoverTheme {
        EditionBottomSheetContent(
            bookTitle = PreviewData.baseBook.title,
            currentEdition = editions.first(),
            defaultEdition = editions.first(),
            editions = editions,
            isLoading = false,
            searchQuery = "",
            onSearchQueryChange = {},
            onConfirmClick = {},
        )
    }
}
