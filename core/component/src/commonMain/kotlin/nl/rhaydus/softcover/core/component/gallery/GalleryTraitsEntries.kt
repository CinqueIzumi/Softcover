package nl.rhaydus.softcover.core.component.gallery

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.traits.HowItReadBlock
import nl.rhaydus.softcover.core.component.traits.HowItReadBlockUiModel
import nl.rhaydus.softcover.core.component.traits.HowItReadSheetBody
import nl.rhaydus.softcover.core.component.traits.HowItReadSheetUiModel

internal val traitsEntries: ImmutableList<GalleryEntry> = persistentListOf(
    galleryEntry(
        name = "HowItReadBlock",
        family = GalleryFamily.TRAITS,
        blurb = "The read-only \"How it read\" panel: one invitation when nothing is answered, otherwise a " +
            "summary line that opens the answers, with a slider answer drawn as a dot on a track.",
        previews = HowItReadBlockUiModel,
        content = { model, modifier ->
            HowItReadBlock(
                model = model,
                onEvent = {},
                modifier = modifier,
            )
        },
    ),
    galleryEntry(
        name = "HowItReadSheet",
        family = GalleryFamily.TRAITS,
        blurb = "The \"How it read\" survey sheet: pace, a plot-to-character slider that starts as a bare " +
            "track, character chips that cycle through yes and complicated, and the writing style. Shown " +
            "inline, without the modal container.",
        previews = HowItReadSheetUiModel,
        content = { model, modifier ->
            GallerySheetFrame(
                scaffold = model.scaffold,
                modifier = modifier,
            ) {
                HowItReadSheetBody(
                    model = model,
                    onEvent = {},
                )
            }
        },
    ),
)
