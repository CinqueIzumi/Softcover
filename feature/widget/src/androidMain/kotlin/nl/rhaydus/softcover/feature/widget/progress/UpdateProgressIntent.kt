package nl.rhaydus.softcover.feature.widget.progress

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

internal const val EXTRA_BOOK_ID = "nl.rhaydus.softcover.feature.widget.BOOK_ID"

internal fun updateProgressIntent(
    context: Context,
    bookId: Int,
): Intent = Intent(
    context,
    UpdateProgressActivity::class.java,
).apply {
    data = "softcover://widget/update-progress/$bookId".toUri()
    flags = Intent.FLAG_ACTIVITY_NEW_TASK
    putExtra(
        EXTRA_BOOK_ID,
        bookId,
    )
}
