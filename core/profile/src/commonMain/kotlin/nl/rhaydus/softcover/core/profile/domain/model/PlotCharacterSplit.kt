package nl.rhaydus.softcover.core.profile.domain.model

data class PlotCharacterSplit(
    val plot: Int,
    val mix: Int,
    val character: Int,
) {
    val total: Int
        get() = plot + mix + character
}
