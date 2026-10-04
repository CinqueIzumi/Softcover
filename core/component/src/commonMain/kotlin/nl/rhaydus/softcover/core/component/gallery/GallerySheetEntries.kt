package nl.rhaydus.softcover.core.component.gallery

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.sheet.SheetLayout
import nl.rhaydus.softcover.core.component.sheet.SheetRow
import nl.rhaydus.softcover.core.component.sheet.SheetRowUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldContent
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.component.text.SoftcoverText

internal val sheetEntries: ImmutableList<GalleryEntry> = persistentListOf(
    galleryEntry(
        name = "SheetScaffold",
        family = GalleryFamily.SHEET,
        blurb = "The chrome every modal sheet shares — dismiss label, editorial header with an optional " +
            "trailing jacket, jacket stack or action, and a footer. Scrolling lets the whole sheet " +
            "scroll; Pinned fixes the header and footer around a body that scrolls itself. Shown " +
            "inline, without the modal container.",
        previews = SheetScaffoldUiModel,
        content = { model, modifier ->
            SheetScaffoldContent(
                model = model,
                onEvent = {},
                modifier = if (model.layout != SheetLayout.Scrolling) {
                    modifier.height(420.dp)
                } else {
                    modifier.heightIn(max = 420.dp)
                },
            ) {
                SoftcoverText(
                    text = "The sheet's own body goes here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    ),
    galleryEntry(
        name = "SheetRow",
        family = GalleryFamily.SHEET,
        blurb = "One ruled row of a single-choice sheet list: italic label, optional leading icon " +
            "and count, and a check that marks the selection.",
        previews = SheetRowUiModel,
        content = { model, modifier ->
            SheetRow(
                model = model,
                onEvent = {},
                modifier = modifier,
            )
        },
    ),
)
