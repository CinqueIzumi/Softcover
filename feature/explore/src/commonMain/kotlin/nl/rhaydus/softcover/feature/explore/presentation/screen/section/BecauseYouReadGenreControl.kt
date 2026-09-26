package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.AdaptiveModalSheet
import nl.rhaydus.designsystem.component.LocalModalSheetDismiss
import nl.rhaydus.designsystem.editorial.component.EditorialSectionHeader
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScaleClickable
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnBecauseYouReadGenreSelectedAction

/**
 * The "because you read {genre}" picker (explore-3a feedback item 10, rebuilt a second time per
 * follow-up feedback): a **bottom sheet** mirroring Library's own
 * [nl.rhaydus.softcover.feature.library.presentation.component.LibraryShelvesSheet] — not the
 * anchored `DropdownMenu` this control used before. The anchor stays the same compact
 * `eyebrowSmall` label naming the resolved [genre] with a trailing `primary` chevron
 * (`"Genre · {genre}"`, uppercase, the same sheet-opening-label register Library's own sort
 * control uses); tapping it opens [BecauseYouReadGenreSheet]. Hidden when the reader has no genre
 * options to switch between (nothing to pick from).
 */
@Composable
internal fun BecauseYouReadGenreControl(
    genre: String,
    options: List<String>,
    runAction: (ExploreAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (options.isEmpty()) return

    var sheetVisible by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .pointerHandCursor()
            .pressScaleClickable(onClick = { sheetVisible = true }),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Genre · $genre".uppercase(),
            style = MaterialTheme.editorialTypography.eyebrowSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        // Decorative — the adjacent genre label already carries the control's meaning, the
        // same convention MoodTile's search glyph and the rating star use elsewhere in this
        // file; a real description here would double-announce the one tap target.
        val chevronIcon = drawableIconResource(
            icon = SoftcoverIcon.ArrowDropDown,
            contentDescription = "",
        )

        Icon(
            painter = chevronIcon.getIconPainter(),
            contentDescription = chevronIcon.contentDescription,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp),
        )
    }

    if (sheetVisible) {
        BecauseYouReadGenreSheet(
            genre = genre,
            options = options,
            runAction = runAction,
            onDismissRequest = { sheetVisible = false },
        )
    }
}

/**
 * The genre-picker sheet body: [EditorialSectionHeader] (accent bar + "GENRE" eyebrow + "Pick a
 * genre" headline — a short neutral category word rather than repeating the "Because you read"
 * rail name, which read as redundant and option-like once opened inside the sheet) over a vertical
 * list of hairline-divided rows, one for "Auto (most-read)"
 * (the null-genre default, checked whenever [genre] doesn't match any switchable [options] entry)
 * and one per [options] entry — laid out exactly like `LibraryShelvesSheet`'s shelf rows
 * ([BecauseYouReadGenreSheetRow] mirrors `ShelvesSheetRow`'s italic label + active check, minus the
 * trailing item count a genre has none of). Selecting a row dispatches
 * `OnBecauseYouReadGenreSelectedAction` and dismisses.
 */
@Composable
private fun BecauseYouReadGenreSheet(
    genre: String,
    options: List<String>,
    runAction: (ExploreAction) -> Unit,
    onDismissRequest: () -> Unit,
) {
    AdaptiveModalSheet(onDismissRequest = onDismissRequest) {
        val dismiss = LocalModalSheetDismiss.current

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(state = rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
        ) {
            EditorialSectionHeader(
                eyebrow = "Genre",
                headline = "Pick a genre",
            )

            Spacer(modifier = Modifier.height(6.dp))

            BecauseYouReadGenreSheetRow(
                label = "Auto (most-read)",
                active = options.none { it == genre },
                onClick = {
                    runAction(OnBecauseYouReadGenreSelectedAction(genre = null))

                    dismiss()
                },
            )

            options.forEach { option ->
                BecauseYouReadGenreSheetRow(
                    label = option,
                    active = option == genre,
                    onClick = {
                        runAction(OnBecauseYouReadGenreSelectedAction(genre = option))

                        dismiss()
                    },
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

/**
 * One genre-sheet row: hairline divider, `titleLarge` italic label (promoted to `primary` when
 * [active]), and a trailing `primary` check glyph when active — `ShelvesSheetRow`'s row anatomy,
 * minus the leading shelf-type glyph and trailing item count neither applies to a genre.
 */
@Composable
private fun BecauseYouReadGenreSheetRow(
    label: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .pointerHandCursor()
                .pressScaleClickable(onClick = onClick)
                .padding(vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.editorialTypography.titleLarge.copy(fontStyle = FontStyle.Italic),
                color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )

            if (active) {
                val checkIcon = drawableIconResource(
                    icon = SoftcoverIcon.Check,
                    contentDescription = "Current genre",
                )

                Icon(
                    painter = checkIcon.getIconPainter(),
                    contentDescription = checkIcon.contentDescription,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}
