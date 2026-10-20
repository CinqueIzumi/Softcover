package nl.rhaydus.softcover.core.designsystem.preset

import java.math.BigDecimal

/** The shortest plain decimal for a float: `16`, `0.25`, `2.5`, never `16.0` or `1.0E-4`. */
internal fun Float.toPlain(): String = BigDecimal(toString()).stripTrailingZeros().toPlainString()

/** `16px`, `0.25px`, `2.5px`. */
internal fun Float.toPx(): String = toPlain() + "px"

internal fun BigDecimal.toPlain(): String = stripTrailingZeros().toPlainString()
