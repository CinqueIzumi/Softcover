package nl.rhaydus.softcover.core.uibinding.traits

import nl.rhaydus.softcover.core.domain.model.WritingStyle

fun WritingStyle.toLabel(): String = when (this) {
    WritingStyle.SIMPLE -> "Simple"
    WritingStyle.MODERATE -> "Moderate"
    WritingStyle.DEMANDING -> "Demanding"
}
