package nl.rhaydus.softcover.core.component.gallery

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.designsystem.layout.rememberWindowSizeClass
import nl.rhaydus.softcover.core.component.sheet.SheetActionRow
import nl.rhaydus.softcover.core.component.sheet.SheetActionRowUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetLayout
import nl.rhaydus.softcover.core.component.sheet.SheetRow
import nl.rhaydus.softcover.core.component.sheet.SheetRowGroup
import nl.rhaydus.softcover.core.component.sheet.SheetRowGroupUiModel
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
            GallerySheetFrame(
                scaffold = model,
                modifier = modifier,
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
    galleryEntry(
        name = "SheetActionRow",
        family = GalleryFamily.SHEET,
        blurb = "One plain action row of a sheet menu: primary-tinted icon, sans label and a " +
            "muted description. It runs an action and holds no selection.",
        previews = SheetActionRowUiModel,
        content = { model, modifier ->
            SheetActionRow(
                model = model,
                onEvent = {},
                modifier = modifier,
            )
        },
    ),
    galleryEntry(
        name = "SheetRowGroup",
        family = GalleryFamily.SHEET,
        blurb = "A single-choice list of SheetRows grouped for selection semantics; picking a row " +
            "runs the sheet's animated dismiss unless the host closes it itself.",
        previews = SheetRowGroupUiModel,
        content = { model, modifier ->
            SheetRowGroup(
                model = model,
                onEvent = {},
                modifier = modifier,
            )
        },
    ),
)

/**
 * Hosts a sheet's regions inline for the gallery: a scrolling sheet is capped, while a pinned one is
 * given a fixed height so its body has room to scroll against its pinned header and footer.
 */
@Composable
internal fun GallerySheetFrame(
    scaffold: SheetScaffoldUiModel,
    modifier: Modifier = Modifier,
    lazyBody: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    SheetScaffoldContent(
        model = scaffold,
        onEvent = {},
        modifier = if (scaffold.layout != SheetLayout.Scrolling) {
            modifier.height(420.dp)
        } else {
            modifier.heightIn(max = rememberWindowSizeClass().heightDp)
        },
        lazyBody = lazyBody,
        content = content,
    )
}
