package nl.rhaydus.softcover.core.component.lists

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.triStateToggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.modifier.conditional
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScale
import nl.rhaydus.designsystem.modifier.pressScaleClickable
import nl.rhaydus.softcover.core.component.chip.Chip
import nl.rhaydus.softcover.core.component.cover.Cover
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.divider.Divider
import nl.rhaydus.softcover.core.component.sheet.SheetScaffold
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldEvent
import nl.rhaydus.softcover.core.component.state.EmptyState
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/**
 * The editorial "index of shelves" (redesign brief): a shared surface for adding/removing one or more
 * books to the user's custom lists, consumed by both `book_detail` and the library **bulk-select**
 * pattern. The header names the act ("Shelve {book}" / "Shelve {n} books") with the book's jacket (or a
 * stack of the selection's) as the header's trailing slot; the list rows read like a ruled index —
 * italic chapter-style names, a leading bookmark that fills when the list already holds the selection,
 * and a reversible trailing control (a removable `primaryContainer` "On the list ×" chip, a quiet
 * outline "+ Add", or — in bulk, for a part-filled list — a filled "+ Add the other N"). Every toggle
 * is instant/optimistic; there is no save button.
 *
 * The sheet is a dumb renderer: the caller owns the `AddBookToListUseCase` / `RemoveBookFromListUseCase`
 * writes behind [ChooseListsEvent.MembershipToggled] and decides whether to stay open for consecutive
 * adds. Reach for it for any add-to-list affordance; do not build a per-screen list picker.
 */
@Composable
fun ChooseListsBottomSheet(
    model: ChooseListsUiModel,
    onEvent: (ChooseListsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    SheetScaffold(
        model = model.scaffold,
        onEvent = { event ->
            when (event) {
                SheetScaffoldEvent.Dismissed -> onEvent(ChooseListsEvent.Dismissed)

                SheetScaffoldEvent.PrimaryClicked,
                SheetScaffoldEvent.SecondaryClicked,
                SheetScaffoldEvent.HeaderActionClicked -> Unit
            }
        },
        modifier = modifier,
    ) {
        if (model.rows.isEmpty()) {
            EmptyState(
                model = model.emptyState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(
                        weight = 1f,
                        fill = false,
                    ),
            ) {
                items(items = model.rows, key = { it.listId }) { row ->
                    ChooseListsRow(
                        row = row,
                        onClick = {
                            onEvent(
                                ChooseListsEvent.MembershipToggled(
                                    listId = row.listId,
                                    membership = row.membership,
                                ),
                            )
                        },
                    )
                }
            }
        }

        NewListRow(
            onClick = { onEvent(ChooseListsEvent.NewListRequested) },
            modifier = Modifier.padding(top = 20.dp),
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
    val interactionSource = remember { MutableInteractionSource() }

    Column {
        Divider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .conditional(
                    condition = row.isPending.not(),
                    ifTrue = {
                        Modifier
                            .pointerHandCursor()
                            .pressScale(interactionSource)
                            .triStateToggleable(
                                state = row.membership.toToggleableState(),
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = onClick,
                            )
                    },
                )
                .padding(vertical = 17.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BookmarkGlyph(membership = row.membership)

            Column(modifier = Modifier.weight(1f)) {
                SoftcoverText(
                    text = row.name,
                    style = MaterialTheme.editorialTypography.titleLarge.copy(fontStyle = FontStyle.Italic),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(3.dp))

                SoftcoverText(
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
                Chip(model = row.membershipChip)
            }
        }
    }
}

private fun ListMembership.toToggleableState(): ToggleableState = when (this) {
    ListMembership.ALL -> ToggleableState.On
    ListMembership.PARTIAL -> ToggleableState.Indeterminate
    ListMembership.NONE -> ToggleableState.Off
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
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            val icon = drawableIconResource(
                icon = SoftcoverIcon.Add,
                contentDescription = "",
            )

            Icon(
                painter = icon.getIconPainter(),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
        }

        SoftcoverText(
            text = "Create a new list",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
