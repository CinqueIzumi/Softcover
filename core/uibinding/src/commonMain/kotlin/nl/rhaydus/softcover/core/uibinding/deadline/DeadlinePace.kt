package nl.rhaydus.softcover.core.uibinding.deadline

import nl.rhaydus.common.secondsToHm
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit

/** [amount] in [unit]'s words: "1 page", "35 pages", or the audiobook "1h 5m" time. */
internal fun formatUnitAmount(
    amount: Int,
    unit: DeadlineUnit,
): String = when (unit) {
    DeadlineUnit.PAGES -> "$amount ${if (amount == 1) "page" else "pages"}"
    DeadlineUnit.SECONDS -> secondsToHm(amount)
}

/** The one pace wording every screen shows: "35 pages/day" or "1h 5m/day". */
fun formatPacePerDay(
    amount: Int,
    unit: DeadlineUnit,
): String {
    val formatted = formatUnitAmount(
        amount = amount,
        unit = unit,
    )

    return "$formatted/day"
}
