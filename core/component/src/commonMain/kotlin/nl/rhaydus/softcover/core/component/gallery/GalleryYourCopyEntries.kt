package nl.rhaydus.softcover.core.component.gallery

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.yourcopy.YourCopyPanel
import nl.rhaydus.softcover.core.component.yourcopy.YourCopyPanelUiModel

internal val yourCopyEntries: ImmutableList<GalleryEntry> = persistentListOf(
    galleryEntry(
        name = "YourCopyPanel",
        family = GalleryFamily.YOURCOPY,
        blurb = "The book page's \"Your copy\" panel: one tappable row per private detail of the reader's " +
            "own copy, showing the value or an invitation to add it.",
        previews = YourCopyPanelUiModel,
        content = { model, modifier ->
            YourCopyPanel(
                model = model,
                onEvent = {},
                modifier = modifier,
            )
        },
    ),
)
