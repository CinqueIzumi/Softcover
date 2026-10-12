package nl.rhaydus.softcover.core.component.gallery

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.dialog.DatePickerUiModel
import nl.rhaydus.softcover.core.component.dialog.toPickerMillis
import nl.rhaydus.softcover.core.component.lists.ChooseListsBody
import nl.rhaydus.softcover.core.component.lists.ChooseListsUiModel
import nl.rhaydus.softcover.core.component.progress.ProgressBottomSheetContent
import nl.rhaydus.softcover.core.component.progress.ProgressSheetUiModel
import nl.rhaydus.softcover.core.component.share.ScaledShareCardPreview
import nl.rhaydus.softcover.core.component.share.ShareCard
import nl.rhaydus.softcover.core.component.share.ShareCardSheetBody
import nl.rhaydus.softcover.core.component.share.ShareCardSheetUiModel
import nl.rhaydus.softcover.core.component.share.ShareCardUiModel
import nl.rhaydus.softcover.core.component.share.rememberShareCardCapture
import nl.rhaydus.softcover.core.component.share.softcoverShareCardCaptureConfig
import nl.rhaydus.softcover.core.component.sheet.LoadingSheetBody
import nl.rhaydus.softcover.core.component.sheet.LoadingSheetUiModel
import nl.rhaydus.softcover.core.component.verdict.VerdictSheetBody
import nl.rhaydus.softcover.core.component.verdict.VerdictSheetUiModel

internal val modalEntries: ImmutableList<GalleryEntry> = persistentListOf(
    galleryEntry(
        name = "ChooseListsBottomSheet",
        family = GalleryFamily.LISTS,
        blurb = "The ruled index of custom lists a book or a selection is shelved on: a bookmark that " +
            "fills with membership, a reversible chip, and an empty state when there are none. " +
            "Shown inline, without the modal container.",
        previews = ChooseListsUiModel,
        content = { model, modifier ->
            GallerySheetFrame(
                scaffold = model.scaffold,
                modifier = modifier,
            ) {
                ChooseListsBody(
                    model = model,
                    onEvent = {},
                )
            }
        },
    ),
    galleryEntry(
        name = "UpdateProgressBottomSheet",
        family = GalleryFamily.PROGRESS,
        blurb = "The reading-progress sheet: a unit switcher over a hero numeric input, the " +
            "backdating row and a footer. With no known total it drops the percentage tab and " +
            "the progress bar. Shown inline, without the modal container.",
        previews = ProgressSheetUiModel,
        content = { model, modifier ->
            GallerySheetFrame(
                scaffold = model.scaffold,
                modifier = modifier,
            ) {
                ProgressBottomSheetContent(
                    model = model,
                    onEvent = {},
                    onNumberChange = {},
                )
            }
        },
    ),
    galleryEntry(
        name = "LoadingSheet",
        family = GalleryFamily.SHEET,
        blurb = "The blocking sheet that reports a long setup step, indeterminate or with a " +
            "fraction. Shown inline, without the modal container.",
        previews = LoadingSheetUiModel,
        content = { model, modifier ->
            GallerySheetFrame(
                scaffold = model.scaffold,
                modifier = modifier,
            ) {
                LoadingSheetBody(
                    model = model,
                    onEvent = {},
                )
            }
        },
    ),
    galleryEntry(
        name = "VerdictSheet",
        family = GalleryFamily.VERDICT,
        blurb = "The sheet that fuses a half-star rating with an optional written review, with " +
            "bold, italic and spoiler marks and a spoilers switch. Shown inline, without the " +
            "modal container.",
        previews = VerdictSheetUiModel,
        content = { model, modifier ->
            GallerySheetFrame(
                scaffold = model.scaffold,
                modifier = modifier,
            ) {
                VerdictSheetBody(
                    model = model,
                    onEvent = {},
                )
            }
        },
    ),
    galleryEntry(
        name = "ShareCardSheetContent",
        family = GalleryFamily.SHARE,
        blurb = "The share sheet every card goes through: a scaled preview of the card with save " +
            "and share as its inline footer. Shown inline, without the modal container or the " +
            "capture effects.",
        previews = ShareCardSheetUiModel,
        content = { model, modifier ->
            GallerySheetFrame(
                scaffold = model.scaffold,
                modifier = modifier,
            ) {
                ShareCardSheetBody(
                    model = model,
                    capture = rememberShareCardCapture(config = softcoverShareCardCaptureConfig),
                )
            }
        },
    ),
    galleryEntry(
        name = "ScaledShareCardPreview",
        family = GalleryFamily.SHARE,
        blurb = "Shrinks a share card to fit a box, centred and never enlarged, while the capture " +
            "still records it at full size.",
        previews = ShareCardUiModel,
        content = { model, modifier ->
            ScaledShareCardPreview(
                capture = rememberShareCardCapture(config = softcoverShareCardCaptureConfig),
                modifier = modifier,
            ) {
                ShareCard(model = model)
            }
        },
    ),
    galleryEntry(
        name = "SoftcoverDatePickerDialog",
        family = GalleryFamily.DIALOG,
        blurb = "The shared calendar dialog for picking one day, which owns the UTC-millis contract " +
            "so a caller never converts a timestamp. The dialog window cannot be hosted inline, so " +
            "this draws its calendar.",
        previews = DatePickerUiModel,
        content = { model, modifier ->
            GalleryDatePicker(
                model = model,
                modifier = modifier,
            )
        },
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GalleryDatePicker(
    model: DatePickerUiModel,
    modifier: Modifier,
) {
    val state = rememberDatePickerState(initialSelectedDateMillis = model.initialDate.toPickerMillis())

    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter,
    ) {
        DatePicker(state = state)
    }
}
