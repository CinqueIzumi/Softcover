package nl.rhaydus.softcover.core.corpus.domain.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.MoodMoment
import nl.rhaydus.softcover.core.domain.model.MoodWord

interface MoodMomentsRepository {
    fun observeAll(): Flow<Map<Int, List<MoodMoment>>>

    fun observe(bookId: Int): Flow<List<MoodMoment>>

    suspend fun add(
        bookId: Int,
        word: MoodWord,
        strength: Int?,
        percent: Int?,
        page: Int?,
        seconds: Int?,
    )

    suspend fun update(moment: MoodMoment)

    suspend fun delete(id: Long)
}
