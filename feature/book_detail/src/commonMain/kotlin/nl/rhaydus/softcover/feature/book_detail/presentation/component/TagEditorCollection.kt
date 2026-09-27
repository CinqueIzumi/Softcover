package nl.rhaydus.softcover.feature.book_detail.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.designsystem.presentation.modifier.quoteGlyphSway
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.UserTag

@Composable
internal fun TagEditorCollection(
    groups: List<TagGroup>,
    newlyAddedKeys: Set<TagKey>,
    onToggleSpoiler: (UserTag) -> Unit,
    onRemove: (UserTag) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        if (groups.isEmpty()) {
            TagEditorEmptyState(modifier = Modifier.fillMaxWidth())
        } else {
            groups.forEachIndexed { index, group ->
                TagGroupSection(
                    group = group,
                    newlyAddedKeys = newlyAddedKeys,
                    onToggleSpoiler = onToggleSpoiler,
                    onRemove = onRemove,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = if (index == 0) 6.dp else 26.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagGroupSection(
    group: TagGroup,
    newlyAddedKeys: Set<TagKey>,
    onToggleSpoiler: (UserTag) -> Unit,
    onRemove: (UserTag) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = group.category.label.uppercase(),
                style = MaterialTheme.editorialTypography.eyebrowSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.width(8.dp))

            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.outlineVariant,
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = group.tags.size.toString(),
                style = MaterialTheme.editorialTypography.eyebrowSmall.copy(fontFeatureSettings = "tnum"),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }

        Spacer(modifier = Modifier.height(11.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            group.tags.forEach { tag ->
                key(tag.category, tag.name) {
                    TagChip(
                        tag = tag,
                        isNewlyAdded = tag.asKey() in newlyAddedKeys,
                        onToggleSpoiler = { onToggleSpoiler(tag) },
                        onRemove = { onRemove(tag) },
                    )
                }
            }
        }
    }
}

@Composable
private fun TagEditorEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(
            top = 40.dp,
            bottom = 30.dp,
            start = 10.dp,
            end = 10.dp,
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "“",
            style = MaterialTheme.editorialTypography.quoteGlyph,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            modifier = Modifier.quoteGlyphSway(),
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Nothing here yet.",
            style = MaterialTheme.editorialTypography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Name the first thing this book is to you.",
            style = MaterialTheme.editorialTypography.body,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
