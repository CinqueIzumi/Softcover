package nl.rhaydus.softcover.feature.book_detail.presentation.component

import nl.rhaydus.designsystem.component.AdaptiveModalSheet
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.LocalModalSheetDismiss
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.designsystem.editorial.component.EditorialSectionHeader
import nl.rhaydus.softcover.core.designsystem.presentation.component.PillChip
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserTag

/**
 * The canonical surface for managing the user's own tags on a book (§3.5 modal sheet). The user picks
 * a category and types a tag name — there is no search step, as the upsert reuses an existing tag of
 * that name or mints a new one server-side. Tags already on the book are listed at the top, each
 * removable and with a per-tag spoiler toggle. Every change is committed immediately by the caller —
 * the sheet is a dumb renderer that reports intents through its callbacks.
 */
private val EDITABLE_CATEGORIES: List<TagCategory> = listOf(
    TagCategory.GENRE,
    TagCategory.MOOD,
    TagCategory.TAG,
    TagCategory.CONTENT_WARNING,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun TagEditorBottomSheet(
    userTags: List<UserTag>,
    selectedCategory: TagCategory,
    draft: String,
    onCategorySelected: (TagCategory) -> Unit,
    onDraftChange: (String) -> Unit,
    onAddTag: (String, TagCategory) -> Unit,
    onRemoveTag: (UserTag) -> Unit,
    onToggleSpoiler: (UserTag) -> Unit,
    onDismissRequest: () -> Unit,
) {
    AdaptiveModalSheet(onDismissRequest = onDismissRequest) {
        val dismiss = LocalModalSheetDismiss.current

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(state = rememberScrollState())
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 8.dp),
        ) {
            EditorialSectionHeader(
                eyebrow = "Your tags",
                headline = "Tag it your way.",
                description = "Add your own genres, moods and tags to this book — mark any as a spoiler to hide it from other readers.",
            )

            if (userTags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))

                userTags.forEach { tag ->
                    key(tag.category, tag.name) {
                        CurrentTagRow(
                            tag = tag,
                            onToggleSpoiler = { onToggleSpoiler(tag) },
                            onRemove = { onRemoveTag(tag) },
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Add a tag",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                EDITABLE_CATEGORIES.forEach { category ->
                    PillChip(
                        label = category.label,
                        selected = category == selectedCategory,
                        onClick = { onCategorySelected(category) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = draft,
                onValueChange = onDraftChange,
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(text = "Name a ${selectedCategory.label} tag") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        onAddTag(
                            draft,
                            selectedCategory,
                        )
                    },
                ),
                trailingIcon = {
                    if (draft.isNotEmpty()) {
                        IconButton(onClick = { onDraftChange("") }) {
                            val clearIcon = drawableIconResource(
                                icon = SoftcoverIcon.Close,
                                contentDescription = "Clear",
                            )

                            Icon(
                                painter = clearIcon.getIconPainter(),
                                contentDescription = clearIcon.contentDescription,
                            )
                        }
                    }
                },
            )

            Spacer(modifier = Modifier.height(12.dp))

            RhaydusButton(
                label = "Add tag",
                onClick = {
                    onAddTag(
                        draft,
                        selectedCategory,
                    )
                },
                style = ButtonStyle.TONAL,
                size = ButtonSize.M,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(24.dp))

            RhaydusButton(
                label = "Done",
                onClick = dismiss,
                style = ButtonStyle.FILLED,
                size = ButtonSize.M,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun CurrentTagRow(
    tag: UserTag,
    onToggleSpoiler: () -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PillChip(label = tag.name)

        Spacer(modifier = Modifier.weight(1f))

        PillChip(
            label = "Spoiler",
            selected = tag.spoiler,
            onClick = onToggleSpoiler,
        )

        IconButton(onClick = onRemove) {
            val removeIcon = drawableIconResource(
                icon = SoftcoverIcon.Close,
                contentDescription = "Remove ${tag.name}",
            )

            Icon(
                painter = removeIcon.getIconPainter(),
                contentDescription = removeIcon.contentDescription,
            )
        }
    }
}
