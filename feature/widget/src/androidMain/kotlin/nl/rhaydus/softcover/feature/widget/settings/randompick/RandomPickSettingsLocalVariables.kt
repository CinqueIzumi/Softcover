package nl.rhaydus.softcover.feature.widget.settings.randompick

import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.feature.widget.randompick.RandomPickCadence
import nl.rhaydus.softcover.feature.widget.randompick.RandomPickSource
import nl.rhaydus.toad.LocalVariables

internal data class RandomPickSettingsLocalVariables(
    val appWidgetId: Int? = null,
    val source: RandomPickSource = RandomPickSource.WantToRead,
    val cadence: RandomPickCadence = RandomPickCadence.Daily,
    val lists: List<BookList> = emptyList(),
) : LocalVariables
