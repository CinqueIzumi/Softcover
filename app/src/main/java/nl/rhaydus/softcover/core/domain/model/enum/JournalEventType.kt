package nl.rhaydus.softcover.core.domain.model.enum

enum class JournalEventType(val eventName: String) {
    StatusDidNotFinish(eventName = "status_stopped"),
    StatusWantToRead(eventName = "status_want_to_read"),
    StatusFinished(eventName = "user_book_read_finished"),
}