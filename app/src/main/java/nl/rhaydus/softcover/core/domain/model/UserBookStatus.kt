package nl.rhaydus.softcover.core.domain.model

enum class UserBookStatus(val code: Int) {
    WANT_TO_READ(code = 1),
    CURRENTLY_READING(code = 2),
    READ(code = 3),
    DID_NOT_FINISH(code = 5);

    val isAlwaysVisibleInLibrary: Boolean
        get() = this == CURRENTLY_READING

    companion object {
        val togglableInLibrary: List<UserBookStatus>
            get() = entries.filter { it.isAlwaysVisibleInLibrary.not() }

        fun activeLibraryCodes(enabledCodes: Set<Int>): Set<Int> {
            val alwaysOn = entries.filter { it.isAlwaysVisibleInLibrary }.map { it.code }
            return enabledCodes + alwaysOn
        }

        val alwaysCachedCodes: Set<Int>
            get() = setOf(
                WANT_TO_READ.code,
                CURRENTLY_READING.code,
                READ.code,
                DID_NOT_FINISH.code,
            )
    }
}
