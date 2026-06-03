package nl.rhaydus.softcover.core.domain.model

import nl.rhaydus.softcover.core.domain.logging.AppLog

enum class BookStatus(
    val code: Int,
    val label: String,
) {
    WantToRead(code = 1, label = "Want to Read"),
    Reading(code = 2, label = "Currently Reading"),
    Read(code = 3, label = "Read"),
    DidNotFinish(code = 5, label = "Did not finish"),
    None(code = 6, label = "None");

    companion object {
        fun getFromCode(code: Int): BookStatus {
            entries.firstOrNull { it.code == code }?.let { return it }

            // Code 4 is Hardcover's legacy "Paused" status, which Softcover no
            // longer supports. Books stuck on that status stay hidden but are
            // worth logging so we notice if any user still has them.
            if (code == 4) AppLog.w("Encountered unsupported Paused status (code 4)")

            return None
        }
    }
}
