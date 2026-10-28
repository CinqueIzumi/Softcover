package nl.rhaydus.softcover.feature.profile.presentation.state

import nl.rhaydus.softcover.core.profile.domain.model.ReadingActivity
import nl.rhaydus.softcover.core.profile.domain.model.ReadingStreak

/** The streak facts the day sheet attributes a day to, written once by `YearGridCollector`. */
internal data class AtlasStreaks(
    val activity: ReadingActivity = ReadingActivity(
        pagesByDay = emptyMap(),
        streaks = emptyList(),
    ),
    val longestInAtlasYear: ReadingStreak? = null,
    val current: ReadingStreak? = null,
)
