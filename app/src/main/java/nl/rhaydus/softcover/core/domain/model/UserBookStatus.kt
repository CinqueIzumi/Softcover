package nl.rhaydus.softcover.core.domain.model

enum class UserBookStatus(val code: Int) {
    WANT_TO_READ(code = 1),
    CURRENTLY_READING(code = 2),
    READ(code = 3),
    PAUSED(code = 4),
    DID_NOT_FINISH(code = 5),
}