package nl.rhaydus.softcover.feature.widget.domain.model

internal data class RandomPickSettings(
    val source: RandomPickSource = RandomPickSource.WantToRead,
    val cadence: RandomPickCadence = RandomPickCadence.Daily,
)
