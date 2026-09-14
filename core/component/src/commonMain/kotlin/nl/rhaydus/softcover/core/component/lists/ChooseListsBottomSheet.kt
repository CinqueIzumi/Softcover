package nl.rhaydus.softcover.core.component.lists

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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.AdaptiveModalSheet
import nl.rhaydus.designsystem.modifier.conditional
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScaleClickable
import nl.rhaydus.softcover.core.component.cover.Cover
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

// The jacket treatment now rides on the cover's own variant — `CoverVariant.ChooseListsSingleJacket`
// and `ChooseListsStackJacket` in `CoverDimensions`. The stack sits a touch flatter than the lone
// upright jacket because three overlapping shadows at the single jacket's depth read as mud. The
// sheet still owns the widths and the rotations below: those are layout, not cover treatment.

/**
 * The editorial "index of shelves" (redesign brief): a shared surface for adding/removing one or more
 * books to the user's custom lists, consumed by both `book_detail` and the library **bulk-select**
 * pattern — which of the two is [ChooseListsUiModel.variant], a sealed `SingleBook | ManyBooks`. The
 * header names the act ("Shelve {book}" / "Shelve {n} books") with the book's own jacket at top; the
 * list rows read like a ruled index — italic chapter-style names, a leading bookmark that fills when
 * the list already holds the selection, and a reversible trailing control (a removable
 * `primaryContainer` "On the list ×" chip, a quiet outline "+ Add", or — in bulk, for a part-filled
 * list — a filled "+ Add the other N"). Every toggle is instant/optimistic; there is no save button.
 *
 * The header jacket(s) come from the variant's own cover models — one upright cover for the
 * single-book case, up to three for the bulk rotated stack. They arrive already resolved on the
 * variant: resolving a cover needs the reader's chosen edition, the book's default, a fallback URL
 * and a locally persisted file, none of which a component may know (R4). The sheet still owns the
 * widths and the rotations.
 */
@Composable
fun ChooseListsBottomSheet(
    model: ChooseListsUiModel,
    onEvent: (ChooseListsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    AdaptiveModalSheet(
        onDismissRequest = { onEvent(ChooseListsEvent.Dismissed) },
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
        ) {
            ChooseListsHeader(
                variant = model.variant,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(18.dp))

            if (model.rows.isEmpty()) {
                ChooseListsEmptyState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 4.dp),
                ) {
                    items(items = model.rows, key = { it.listId }) { row ->
                        ChooseListsRow(
                            row = row,
                            onClick = {
                                if (row.isPending.not()) {
                                    onEvent(
                                        ChooseListsEvent.MembershipToggled(
                                            listId = row.listId,
                                            membership = row.membership,
                                        ),
                                    )
                                }
                            },
                        )
                    }
                }
            }

            NewListRow(
                onClick = { onEvent(ChooseListsEvent.NewListRequested) },
                modifier = Modifier.padding(top = 20.dp),
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

/**
 * The canonical opening — accent bar, eyebrow, italic headline, italic description — composed locally
 * rather than through `EditorialSectionHeader` (which has no trailing slot and can't tint a substring of
 * its headline), following the Tag-editor-sheet precedent. The headline names the act: the book's own
 * title in a `primary` span for the single case, or "{n} books" for bulk.
 */
@Composable
private fun ChooseListsHeader(
    variant: ChooseListsVariant,
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
                text = chooseListsHeadline(name = variant.name),
                style = MaterialTheme.editorialTypography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = when (variant) {
                    is ChooseListsVariant.ManyBooks ->
                        "A part-filled list adds the rest first — then a second tap takes all three off."

                    is ChooseListsVariant.SingleBook ->
                        "Tap a list to file it here — tap a filled one to take it off. Saved as you go."
                },
                style = MaterialTheme.editorialTypography.body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        when (variant) {
            is ChooseListsVariant.ManyBooks -> StackedJackets(
                variant = variant,
            )

            is ChooseListsVariant.SingleBook -> Cover(
                model = variant.cover,
                modifier = Modifier.width(56.dp),
            )
        }
    }
}

@Composable
private fun chooseListsHeadline(name: String): AnnotatedString {
    val primary = MaterialTheme.colorScheme.primary

    return remember(name, primary) {
        buildAnnotatedString {
            append("Shelve ")
            withStyle(SpanStyle(color = primary)) {
                append(name)
            }
        }
    }
}

/**
 * The bulk header's rotated jacket stack (behind → front: −8°, +4°, 0°), each a 52×78dp cover.
 *
 * The sheet owns the width and the rotation — they are handed to the slot in its `Modifier` — so a
 * caller that resolves the wrong cover can still not draw the stack wrong.
 */
@Composable
private fun StackedJackets(
    variant: ChooseListsVariant.ManyBooks,
    modifier: Modifier = Modifier,
) {
    val rotations = listOf(-8f, 4f, 0f)

    // Built here rather than mapped in, so `covers` stays the single source of truth for the stack.
    // Constructing a library model out of library data is not a domain mapping, so R9 is untouched.
    val emptyStackCover = remember(variant.name) {
        CoverUiModel(
            source = null,
            coverlessTitle = variant.name,
            variant = CoverVariant.ChooseListsStackJacket,
        )
    }

    Box(
        modifier = modifier.size(
            width = 72.dp,
            height = 78.dp,
        ),
        contentAlignment = Alignment.Center,
    ) {
        if (variant.covers.isEmpty()) {
            // No cover resolved for any selected book: one jacket, and deliberately **not** rotated.
            // A single tilted placeholder reads as a rendering mistake rather than as a stack, so the
            // rotation only applies once there is more than a placeholder to stack.
            Cover(
                model = emptyStackCover,
                modifier = Modifier.width(52.dp),
            )
        } else {
            variant.covers.take(rotations.size).forEachIndexed { index, cover ->
                Cover(
                    model = cover,
                    modifier = Modifier
                        .width(52.dp)
                        .rotate(rotations[index]),
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
    row: ChooseListsRowUiModel,
    onClick: () -> Unit,
) {
    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .conditional(
                    condition = row.isPending.not(),
                    ifTrue = { Modifier.pointerHandCursor().pressScaleClickable(onClick = onClick) },
                )
                .padding(vertical = 17.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BookmarkGlyph(membership = row.membership)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = row.name,
                    style = MaterialTheme.editorialTypography.titleLarge.copy(fontStyle = FontStyle.Italic),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = row.caption,
                    style = MaterialTheme.editorialTypography.eyebrowSmall,
                    color = if (row.membership == ListMembership.PARTIAL) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }

            if (row.isPending) {
                CircularWavyProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                MembershipPill(
                    membership = row.membership,
                    label = row.actionLabel,
                )
            }
        }
    }
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
    label: String,
) {
    when (membership) {
        ListMembership.ALL -> OnListChip(label = label)
        ListMembership.PARTIAL -> AddFilledPill(label = label)
        ListMembership.NONE -> AddOutlinePill(label = label)
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
