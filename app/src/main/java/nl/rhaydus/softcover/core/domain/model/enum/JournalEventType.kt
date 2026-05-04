package nl.rhaydus.softcover.core.domain.model.enum

enum class JournalEventType(val eventName: String) {
    StatusDidNotFinish(eventName = "status_stopped"),
    StatusFinished(eventName = "user_book_read_finished"),
    ProgressUpdated(eventName = "progress_updated"),
}