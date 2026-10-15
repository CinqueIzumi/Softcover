package nl.rhaydus.softcover.feature.settings.presentation.source

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.feature.settings.presentation.model.DocumentPageSnapshot

/**
 * What differs between document pages: where the content comes from and how it maps to sections. Each
 * page's chrome and copy live in its initial state instead, so a source is only touched off composition.
 */
internal interface DocumentPageSource {
    fun observe(): Flow<DocumentPageSnapshot>

    suspend fun refresh(force: Boolean): Result<Unit>
}
