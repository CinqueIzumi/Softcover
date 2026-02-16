package nl.rhaydus.softcover.core.domain.model

enum class PrivacySetting(val code: Int) {
    PUBLIC(code = 1),
    FOLLOWERS(code = 2),
    PRIVATE(code = 3),
}