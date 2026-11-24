package nl.rhaydus.softcover.core.uibinding.traits

import nl.rhaydus.softcover.core.domain.model.Pace

fun Pace.toLabel(): String = when (this) {
    Pace.SLOW -> "Slow"
    Pace.MODERATE -> "Moderate"
    Pace.FAST -> "Fast"
    Pace.VARIABLE -> "Variable"
}
