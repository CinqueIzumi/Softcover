package nl.rhaydus.softcover.core.presentation.component

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.designsystem.R
import nl.rhaydus.softcover.core.domain.model.BookList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseListsBottomSheet(
    bookIds: Set<Int>,
    customLists: List<BookList>,
    listsBeingMutated: Set<Int>,
    onDismissRequest: () -> Unit,
    onToggleMembership: (listId: Int, membership: ListMembership) -> Unit,
    onCreateNewListClick: () -> Unit,
    headline: String = if (bookIds.size > 1) "Choose lists for ${bookIds.size} books" else "Choose lists",
    description: String = if (bookIds.size > 1) {
        "Pick the lists these books belong in."
    } else {
        "Pick the lists this book belongs in."
    },
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
        ) {
            EditorialSectionHeader(
                eyebrow = "Lists",
                headline = headline,
                description = description,
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (customLists.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(items = customLists, key = { it.id }) { list ->
                        val membership = list.membershipFor(bookIds = bookIds)
                        val isPending = list.id in listsBeingMutated

                        ListRow(
                            name = list.name,
                            membership = membership,
                            isPending = isPending,
                            onClick = {
                                if (isPending.not()) {
                                    onToggleMembership(list.id, membership)
                                }
                            },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            NewListRow(onClick = onCreateNewListClick)

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

private fun BookList.membershipFor(bookIds: Set<Int>): ListMembership {
    if (bookIds.isEmpty()) return ListMembership.NONE

    val listBookIds = books.mapTo(mutableSetOf()) { it.bookId }
    val matching = bookIds.count { it in listBookIds }

    return when (matching) {
        0 -> ListMembership.NONE
        bookIds.size -> ListMembership.ALL
        else -> ListMembership.PARTIAL
    }
}

@Composable
private fun EmptyState() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
        ) {
            Text(
                text = "No custom lists yet",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Create your first list below.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ListRow(
    name: String,
    membership: ListMembership,
    isPending: Boolean,
    onClick: () -> Unit,
) {
    val container = when (membership) {
        ListMembership.ALL,
        ListMembership.PARTIAL,
            -> MaterialTheme.colorScheme.secondaryContainer
        ListMembership.NONE -> MaterialTheme.colorScheme.surfaceContainerHigh
    }

    val content = when (membership) {
        ListMembership.ALL,
        ListMembership.PARTIAL,
            -> MaterialTheme.colorScheme.onSecondaryContainer
        ListMembership.NONE -> MaterialTheme.colorScheme.onSurface
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = container,
        contentColor = content,
        onClick = onClick,
        enabled = isPending.not(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f),
            )

            MembershipIndicator(
                membership = membership,
                isPending = isPending,
            )
        }
    }
}

@Composable
private fun MembershipIndicator(
    membership: ListMembership,
    isPending: Boolean,
) {
    Box(
        modifier = Modifier.size(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        when {
            isPending -> CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary,
            )

            membership == ListMembership.ALL -> Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = "Every selected book is on this list",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )

            // Partial → show a "minus" via a thin horizontal bar inside the empty circle to signal
            // "some but not all"; tapping commits toward ALL (per tristate-checkbox convention).
            membership == ListMembership.PARTIAL -> Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 10.dp, height = 2.dp)
                        .background(MaterialTheme.colorScheme.primary),
                )
            }

            else -> Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            )
        }
    }
}

@Composable
private fun NewListRow(onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "+",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 8.dp),
            )

            Text(
                text = "Create a new list",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
