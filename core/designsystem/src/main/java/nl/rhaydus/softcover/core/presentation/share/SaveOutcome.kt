package nl.rhaydus.softcover.core.presentation.share

import android.net.Uri

sealed interface SaveOutcome {

    data class Cached(
        val uri: Uri,
    ) : SaveOutcome

    data class Saved(
        val uri: Uri,
        val displayPath: String,
    ) : SaveOutcome
}
