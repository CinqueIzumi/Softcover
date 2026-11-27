package nl.rhaydus.softcover.core.component.gallery

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.feeling.HowItFeltBlock
import nl.rhaydus.softcover.core.component.feeling.HowItFeltBlockUiModel
import nl.rhaydus.softcover.core.component.feeling.HowItFeltSheetBody
import nl.rhaydus.softcover.core.component.feeling.HowItFeltSheetUiModel
import nl.rhaydus.softcover.core.component.feeling.MoodMomentSheetBody
import nl.rhaydus.softcover.core.component.feeling.MoodMomentSheetUiModel
import nl.rhaydus.softcover.core.component.feeling.MoodRibbon
import nl.rhaydus.softcover.core.component.feeling.MoodRibbonUiModel

internal val feelingEntries: ImmutableList<GalleryEntry> = persistentListOf(
    galleryEntry(
        name = "HowItFeltBlock",
        family = GalleryFamily.FEELING,
        blurb = "The read-only \"How it felt\" block: the moods for the whole book as rows with a hairline " +
            "bar, then the moods marked along the way. It keeps an invitation line when nothing is recorded.",
        previews = HowItFeltBlockUiModel,
        content = { model, modifier ->
            HowItFeltBlock(
                model = model,
                onEvent = {},
                modifier = modifier,
            )
        },
    ),
    galleryEntry(
        name = "HowItFeltSheet",
        family = GalleryFamily.FEELING,
        blurb = "The \"How it felt\" sheet: mood chips with a dashed own-word chip, then one slider per picked " +
            "mood that starts bare until slid. Shown inline, without the modal container.",
        previews = HowItFeltSheetUiModel,
        content = { model, modifier ->
            GallerySheetFrame(
                scaffold = model.scaffold,
                modifier = modifier,
            ) {
                HowItFeltSheetBody(
                    model = model,
                    onEvent = {},
                )
            }
        },
    ),
    galleryEntry(
        name = "MoodMomentSheet",
        family = GalleryFamily.FEELING,
        blurb = "The sheet for one mood at one point along the progress bar: single-choice chips and one " +
            "\"How strongly\" slider. Skip and Add mood when adding, Delete and Save when editing.",
        previews = MoodMomentSheetUiModel,
        content = { model, modifier ->
            GallerySheetFrame(
                scaffold = model.scaffold,
                modifier = modifier,
            ) {
                MoodMomentSheetBody(
                    model = model,
                    onEvent = {},
                )
            }
        },
    ),
    galleryEntry(
        name = "MoodRibbon",
        family = GalleryFamily.FEELING,
        blurb = "A hairline under the progress bar with a dot and a lowercase word at each mood's position; " +
            "a dot with no strength is lighter.",
        previews = MoodRibbonUiModel,
        content = { model, modifier ->
            MoodRibbon(
                model = model,
                modifier = modifier,
            )
        },
    ),
)
