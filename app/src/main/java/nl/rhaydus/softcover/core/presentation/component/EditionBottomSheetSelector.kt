package nl.rhaydus.softcover.core.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.PreviewData
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.presentation.model.ButtonStyle
import nl.rhaydus.softcover.core.presentation.modifier.conditional
import nl.rhaydus.softcover.core.presentation.modifier.noRippleClickable
import nl.rhaydus.softcover.core.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.presentation.theme.StandardPreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditionBottomSheetSelector(
    book: Book,
    onDismissRequest: () -> Unit,
    onCancelClick: () -> Unit,
    onConfirmClick: (BookEdition) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState()
    ) {
        EditionBottomSheetContent(
            book = book,
            onCancelClick = onCancelClick,
            onConfirmClick = onConfirmClick
        )
    }
}

@Composable
private fun EditionBottomSheetContent(
    book: Book,
    onCancelClick: () -> Unit,
    onConfirmClick: (BookEdition) -> Unit,
) {
    var selectedEdition by remember {
        mutableStateOf(book.currentEdition)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(horizontal = 8.dp)
            .padding(bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SoftcoverButton(
                label = "Cancel",
                onClick = onCancelClick,
                style = ButtonStyle.TEXT
            )

            Text(
                text = "Change edition",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge
            )

            SoftcoverButton(
                label = "Confirm",
                enabled = selectedEdition != book.currentEdition,
                onClick = { onConfirmClick(selectedEdition) },
                style = ButtonStyle.TEXT
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = book.title,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(book.editions) { edition ->
                EditionItem(
                    edition = edition,
                    selected = edition == selectedEdition,
                    onEditionClick = { selectedEdition = edition }
                )
            }
        }
    }
}

@Composable
fun EditionItem(
    edition: BookEdition,
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
                        shape = cardShape
                    )
                }
            )
            .background(color = MaterialTheme.colorScheme.surface)
            .noRippleClickable(onEditionClick)
            .padding(all = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            EditionImage(
                edition = edition,
                modifier = Modifier.width(width = 60.dp),
                isLoading = false,
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                edition.title?.let {
                    Text(
                        text = edition.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                edition.publisher?.let {
                    Text(
                        text = edition.publisher,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                edition.pages?.let {
                    Text(
                        text = "${edition.pages} pages",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                edition.isbn10?.let { isbn ->
                    Text(
                        text = "ISBN: $isbn",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@StandardPreview
@Composable
private fun EditionBottomSheetContentPreview() {
    val baseEdition = PreviewData.baseEdition.copy(
        title = "Snake-Eater",
    )

    val baseBook = PreviewData.baseBook.copy(
        editions = listOf(
            baseEdition.copy(
                pages = 271,
                isbn10 = "123",
                publisher = "47 north",
                id = 40
            ),
            baseEdition.copy(
                pages = 352,
                isbn10 = "234",
                publisher = "Titan Books",
                id = 20
            ),
            baseEdition.copy(
                pages = 267,
                isbn10 = null,
                publisher = "47 north",
                id = 80
            ),
        )
    )

    SoftcoverTheme {
        EditionBottomSheetContent(
            book = baseBook,
            onConfirmClick = {},
            onCancelClick = {},
        )
    }
}