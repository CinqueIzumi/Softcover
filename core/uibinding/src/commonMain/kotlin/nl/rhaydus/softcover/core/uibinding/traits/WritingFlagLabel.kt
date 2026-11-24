package nl.rhaydus.softcover.core.uibinding.traits

import nl.rhaydus.softcover.core.domain.model.WritingFlag

fun WritingFlag.toLabel(): String = when (this) {
    WritingFlag.VIVID -> "Vivid"
    WritingFlag.POETIC -> "Poetic"
    WritingFlag.UNCONVENTIONAL -> "Unconventional"
}
