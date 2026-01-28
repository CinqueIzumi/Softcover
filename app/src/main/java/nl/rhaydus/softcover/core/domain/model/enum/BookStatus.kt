package nl.rhaydus.softcover.core.domain.model.enum

enum class BookStatus(val code: Int, val label: String) {
    WantToRead(code = 1, label = "Want to Read"),
    Reading(code = 2, label = "Currently Reading"),
    Read(code = 3, label = "Read"),
    Paused(code = 4, label = "Paused"),
    DidNotFinish(code = 5, label = "Did not finish"),
    None(code = 6, label = "None");

    companion object {
        fun getFromCode(code: Int): BookStatus = entries.firstOrNull { it.code == code } ?: None
    }
}