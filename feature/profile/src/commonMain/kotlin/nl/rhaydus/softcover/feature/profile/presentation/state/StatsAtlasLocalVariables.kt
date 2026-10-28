package nl.rhaydus.softcover.feature.profile.presentation.state

import nl.rhaydus.toad.LocalVariables

internal data class StatsAtlasLocalVariables(
    val name: String = "",

    /** Set once the reader taps a scope chip, so the history's default scope never overrides their choice. */
    val hasUserPickedScope: Boolean = false,
) : LocalVariables
