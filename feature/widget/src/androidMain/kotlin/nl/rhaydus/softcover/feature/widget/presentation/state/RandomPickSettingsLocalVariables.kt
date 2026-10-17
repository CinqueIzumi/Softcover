package nl.rhaydus.softcover.feature.widget.presentation.state

import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.feature.widget.domain.model.RandomPickCadence
import nl.rhaydus.softcover.feature.widget.domain.model.RandomPickSource
import nl.rhaydus.toad.LocalVariables

internal data class RandomPickSettingsLocalVariables(
    val appWidgetId: Int? = null,
    val source: RandomPickSource = RandomPickSource.WantToRead,
    val cadence: RandomPickCadence = RandomPickCadence.Daily,
    val lists: List<BookList> = emptyList(),
) : LocalVariables
