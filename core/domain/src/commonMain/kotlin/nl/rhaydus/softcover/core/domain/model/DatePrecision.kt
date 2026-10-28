package nl.rhaydus.softcover.core.domain.model

/** How exactly Hardcover knows a date; a month or year date is stored as its first day. */
enum class DatePrecision(val code: Int) {
    DAY(code = 1),
    MONTH(code = 2),
    YEAR(code = 3),
    ;

    companion object {
        fun fromCode(code: Int?): DatePrecision? = entries.firstOrNull { it.code == code }
    }
}
