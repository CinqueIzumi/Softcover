package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.LocalModalSheetDismiss
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScaleClickable
import nl.rhaydus.softcover.core.component.sheet.SheetRowEvent
import nl.rhaydus.softcover.core.component.sheet.SheetRowGroup
import nl.rhaydus.softcover.core.component.sheet.SheetRowGroupUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffold
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldEvent
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
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
    sheet: SheetScaffoldUiModel,
    genreGroup: SheetRowGroupUiModel<String?>,
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
            sheet = sheet,
            genreGroup = genreGroup,
            runAction = runAction,
            onDismissRequest = { sheetVisible = false },
        )
    }
}

@Composable
private fun BecauseYouReadGenreSheet(
    sheet: SheetScaffoldUiModel,
    genreGroup: SheetRowGroupUiModel<String?>,
    runAction: (ExploreAction) -> Unit,
    onDismissRequest: () -> Unit,
) {
    SheetScaffold(
        model = sheet,
        onEvent = { event ->
            when (event) {
                SheetScaffoldEvent.Dismissed -> onDismissRequest()

                SheetScaffoldEvent.TopActionClicked,
                SheetScaffoldEvent.PrimaryClicked,
                SheetScaffoldEvent.SecondaryClicked,
                SheetScaffoldEvent.HeaderActionClicked,
                -> Unit
            }
        },
    ) {
        val dismiss = LocalModalSheetDismiss.current

        SheetRowGroup(
            model = genreGroup,
            onEvent = { event ->
                when (event) {
                    is SheetRowEvent.Selected -> {
                        runAction(OnBecauseYouReadGenreSelectedAction(genre = event.key))

                        dismiss()
                    }
                }
            },
        )
    }
}
