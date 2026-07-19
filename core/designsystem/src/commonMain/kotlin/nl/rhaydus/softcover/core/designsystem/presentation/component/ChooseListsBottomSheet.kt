package nl.rhaydus.softcover.core.designsystem.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.AdaptiveModalSheet
import nl.rhaydus.designsystem.modifier.conditional
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScaleClickable
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookList

/**
 * The editorial "index of shelves" (redesign brief): a shared surface for adding/removing one or more
 * books to the user's custom lists, consumed by both `book_detail` (single book, [bookTitle] set) and
 * the library **bulk-select** pattern ([bookTitle] left `null`, a `Set<bookId>`). The header names the
 * act ("Shelve {book}" / "Shelve {n} books") with the book's own jacket at top; the list rows read like
 * a ruled index — italic chapter-style names, a leading bookmark that fills when the list already holds
 * the selection, and a reversible trailing control (a removable `primaryContainer` "On the list ×" chip,
 * a quiet outline "+ Add", or — in bulk, for a part-filled list — a filled "+ Add the other N"). Every
 * toggle is instant/optimistic; there is no save button.
 *
 * [bookCovers] supplies the header jacket(s): a single edition for the single-book case, up to three for
 * the bulk rotated stack. Callers pass whichever edition already resolves the cover they want shown
 * (typically `Book.currentEdition`) — the same [BookEdition] type `EditionImage` already renders
 * elsewhere, so no new cover-carrier type is needed.
 */
@Composable
fun ChooseListsBottomSheet(
    bookIds: Set<Int>,
    customLists: List<BookList>,
    listsBeingMutated: Set<Int>,
    onDismissRequest: () -> Unit,
    onToggleMembership: (listId: Int, membership: ListMembership) -> Unit,
    onCreateNewListClick: () -> Unit,
    bookTitle: String? = null,
    bookCovers: List<BookEdition> = emptyList(),
) {
    val isBulk = bookTitle == null

    AdaptiveModalSheet(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
        ) {
            ChooseListsHeader(
                bookTitle = bookTitle,
                bookIdsCount = bookIds.size,
                bookCovers = bookCovers,
                isBulk = isBulk,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(18.dp))

            if (customLists.isEmpty()) {
                ChooseListsEmptyState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 4.dp),
                ) {
                    items(items = customLists, key = { it.id }) { list ->
                        val info = list.membershipFor(bookIds = bookIds)
                        val isPending = list.id in listsBeingMutated

                        ChooseListsRow(
                            list = list,
                            membership = info.membership,
                            matchingCount = info.matchingCount,
                            totalCount = bookIds.size,
                            isBulk = isBulk,
                            isPending = isPending,
                            onClick = {
                                if (isPending.not()) {
                                    onToggleMembership(
                                        list.id,
                                        info.membership,
                                    )
                                }
                            },
                        )
                    }
                }
            }

            NewListRow(
                onClick = onCreateNewListClick,
                modifier = Modifier.padding(top = 20.dp),
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

/** Membership plus how many of [ChooseListsBottomSheet]'s selected books are already in the list. */
private data class ListMembershipInfo(
    val membership: ListMembership,
    val matchingCount: Int,
)

private fun BookList.membershipFor(bookIds: Set<Int>): ListMembershipInfo {
    if (bookIds.isEmpty()) {
        return ListMembershipInfo(
            membership = ListMembership.NONE,
            matchingCount = 0,
        )
    }

    val listBookIds = books.mapTo(mutableSetOf()) { it.bookId }
    val matching = bookIds.count { it in listBookIds }

    val membership = when (matching) {
        0 -> ListMembership.NONE
        bookIds.size -> ListMembership.ALL
        else -> ListMembership.PARTIAL
    }

    return ListMembershipInfo(
        membership = membership,
        matchingCount = matching,
    )
}

/**
 * The canonical opening — accent bar, eyebrow, italic headline, italic description — composed locally
 * rather than through `EditorialSectionHeader` (which has no trailing slot and can't tint a substring of
 * its headline), following the Tag-editor-sheet precedent. The headline names the act: the book's own
 * title in a `primary` span for the single case, or "{n} books" for bulk.
 */
@Composable
private fun ChooseListsHeader(
    bookTitle: String?,
    bookIdsCount: Int,
    bookCovers: List<BookEdition>,
    isBulk: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(width = 32.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.primary),
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "LISTS",
                    style = MaterialTheme.editorialTypography.eyebrow,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = chooseListsHeadline(
                    bookTitle = bookTitle,
                    bookIdsCount = bookIdsCount,
                ),
                style = MaterialTheme.editorialTypography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isBulk) {
                    "A part-filled list adds the rest first — then a second tap takes all three off."
                } else {
                    "Tap a list to file it here — tap a filled one to take it off. Saved as you go."
                },
                style = MaterialTheme.editorialTypography.body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (isBulk) {
            StackedJackets(
                covers = bookCovers,
                fallbackTitle = "$bookIdsCount books",
            )
        } else {
            EditionImage(
                edition = bookCovers.firstOrNull(),
                defaultEdition = bookCovers.firstOrNull(),
                isLoading = false,
                coverlessTitle = bookTitle,
                cornerRadius = 4.dp,
                elevation = 4.dp,
                shadowColor = Color.Black.copy(alpha = 0.5f),
                modifier = Modifier.width(56.dp),
            )
        }
    }
}

@Composable
private fun chooseListsHeadline(
    bookTitle: String?,
    bookIdsCount: Int,
): AnnotatedString {
    val primary = MaterialTheme.colorScheme.primary
    val namedSpan = bookTitle ?: "$bookIdsCount books"

    return remember(namedSpan, primary) {
        buildAnnotatedString {
            append("Shelve ")
            withStyle(SpanStyle(color = primary)) {
                append(namedSpan)
            }
        }
    }
}

/** The bulk header's rotated 3-jacket stack (behind → front: −8°, +4°, 0°), each a 52×78dp cover. */
@Composable
private fun StackedJackets(
    covers: List<BookEdition>,
    fallbackTitle: String,
    modifier: Modifier = Modifier,
) {
    val rotations = listOf(-8f, 4f, 0f)

    Box(
        modifier = modifier.size(
            width = 72.dp,
            height = 78.dp,
        ),
        contentAlignment = Alignment.Center,
    ) {
        if (covers.isEmpty()) {
            EditionImage(
                edition = null,
                defaultEdition = null,
                isLoading = false,
                coverlessTitle = fallbackTitle,
                cornerRadius = 4.dp,
                elevation = 3.dp,
                shadowColor = Color.Black.copy(alpha = 0.35f),
                modifier = Modifier.width(52.dp),
            )
        } else {
            covers.take(3).forEachIndexed { index, edition ->
                EditionImage(
                    edition = edition,
                    defaultEdition = edition,
                    isLoading = false,
                    coverlessTitle = edition.title ?: fallbackTitle,
                    cornerRadius = 4.dp,
                    elevation = 3.dp,
                    shadowColor = Color.Black.copy(alpha = 0.35f),
                    modifier = Modifier
                        .width(52.dp)
                        .rotate(rotations.getOrElse(index) { 0f }),
                )
            }
        }
    }
}

@Composable
private fun ChooseListsEmptyState(modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(vertical = 20.dp)) {
        Text(
            text = "No custom lists yet",
            style = MaterialTheme.editorialTypography.titleLarge.copy(fontStyle = FontStyle.Italic),
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Create your first list below.",
            style = MaterialTheme.editorialTypography.body,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** One ruled-index row: top hairline, leading bookmark, italic name + caption, reversible trailing control. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ChooseListsRow(
    list: BookList,
    membership: ListMembership,
    matchingCount: Int,
    totalCount: Int,
    isBulk: Boolean,
    isPending: Boolean,
    onClick: () -> Unit,
) {
    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .conditional(
                    condition = isPending.not(),
                    ifTrue = { Modifier.pointerHandCursor().pressScaleClickable(onClick = onClick) },
                )
                .padding(vertical = 17.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BookmarkGlyph(membership = membership)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = list.name,
                    style = MaterialTheme.editorialTypography.titleLarge.copy(fontStyle = FontStyle.Italic),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = captionFor(
                        list = list,
                        isBulk = isBulk,
                        membership = membership,
                        matchingCount = matchingCount,
                        totalCount = totalCount,
                    ),
                    style = MaterialTheme.editorialTypography.eyebrowSmall,
                    color = if (membership == ListMembership.PARTIAL) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }

            if (isPending) {
                CircularWavyProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                MembershipPill(
                    membership = membership,
                    isBulk = isBulk,
                    matchingCount = matchingCount,
                    totalCount = totalCount,
                )
            }
        }
    }
}

private fun captionFor(
    list: BookList,
    isBulk: Boolean,
    membership: ListMembership,
    matchingCount: Int,
    totalCount: Int,
): String {
    val booksCount = "${list.books.size} BOOKS"

    if (isBulk.not()) return booksCount

    val phrase = when (membership) {
        ListMembership.ALL -> "ALL $totalCount HERE"
        ListMembership.PARTIAL -> "$matchingCount OF $totalCount HERE"
        ListMembership.NONE -> "NONE YET"
    }

    return "$booksCount · $phrase"
}

@Composable
private fun BookmarkGlyph(membership: ListMembership) {
    when (membership) {
        ListMembership.ALL -> {
            val icon = drawableIconResource(
                icon = SoftcoverIcon.BookmarkCheck,
                contentDescription = "On this list",
            )

            Icon(
                painter = icon.getIconPainter(),
                contentDescription = icon.contentDescription,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(23.dp),
            )
        }

        ListMembership.PARTIAL -> PartialBookmarkMark()

        ListMembership.NONE -> {
            val icon = drawableIconResource(
                icon = SoftcoverIcon.Bookmark,
                contentDescription = "Not on this list",
            )

            Icon(
                painter = icon.getIconPainter(),
                contentDescription = icon.contentDescription,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(23.dp),
            )
        }
    }
}

/** The hand-drawn tri-state mark: a primary-bordered 19×23dp box with a short horizontal primary bar. */
@Composable
private fun PartialBookmarkMark() {
    val primary = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .size(width = 19.dp, height = 23.dp)
            .border(
                width = 2.4.dp,
                color = primary,
                shape = RoundedCornerShape(
                    topStart = 3.dp,
                    topEnd = 3.dp,
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .width(10.dp)
                .height(2.4.dp)
                .background(primary),
        )
    }
}

@Composable
private fun MembershipPill(
    membership: ListMembership,
    isBulk: Boolean,
    matchingCount: Int,
    totalCount: Int,
) {
    when (membership) {
        ListMembership.ALL -> OnListChip(
            label = if (isBulk) "On all $totalCount" else "On the list",
        )

        ListMembership.PARTIAL -> AddFilledPill(
            label = "Add the other ${totalCount - matchingCount}",
        )

        ListMembership.NONE -> AddOutlinePill(
            label = if (isBulk) "Add all $totalCount" else "Add",
        )
    }
}

@Composable
private fun OnListChip(
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(
                start = 13.dp,
                top = 7.dp,
                end = 10.dp,
                bottom = 7.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )

        val closeIcon = drawableIconResource(
            icon = SoftcoverIcon.Close,
            contentDescription = "Remove from list",
        )

        Icon(
            painter = closeIcon.getIconPainter(),
            contentDescription = closeIcon.contentDescription,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(12.dp),
        )
    }
}

@Composable
private fun AddFilledPill(
    label: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = "+ $label",
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onPrimary,
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(MaterialTheme.colorScheme.primary)
            .padding(
                horizontal = 13.dp,
                vertical = 7.dp,
            ),
    )
}

@Composable
private fun AddOutlinePill(
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(percent = 50),
            )
            .padding(
                horizontal = 14.dp,
                vertical = 7.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "+",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun NewListRow(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .pointerHandCursor()
            .pressScaleClickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(RoundedCornerShape(percent = 50))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "+",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Text(
            text = "Create a new list",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
