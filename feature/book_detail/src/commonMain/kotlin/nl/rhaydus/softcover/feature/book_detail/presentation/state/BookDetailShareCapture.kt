package nl.rhaydus.softcover.feature.book_detail.presentation.state

import nl.rhaydus.softcover.core.component.share.ShareCaptureState
import nl.rhaydus.softcover.core.component.share.withShareCapture
import nl.rhaydus.softcover.core.component.sheet.withModel

/** Records [capture] and rebuilds the held share sheet's scaffold to match. */
internal fun BookDetailUiState.withShareCapture(capture: ShareCaptureState): BookDetailUiState =
    copy(
        shareCapture = capture,
        shareSheet = shareSheet.withModel { held ->
            held.copy(
                sheet = held.sheet.copy(
                    scaffold = held.sheet.scaffold.withShareCapture(capture),
                ),
            )
        },
    )
