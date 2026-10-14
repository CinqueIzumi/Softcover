package nl.rhaydus.softcover.feature.settings.domain.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.feature.settings.domain.model.Changelog

interface ChangelogRepository {
    fun observeChangelog(): Flow<Changelog>

    suspend fun refreshChangelog(force: Boolean)
}
