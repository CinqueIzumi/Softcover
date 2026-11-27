package nl.rhaydus.softcover.core.corpus.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.corpus.data.datasource.MoodMomentsLocalDataSource
import nl.rhaydus.softcover.core.corpus.data.mapper.clampedStrength
import nl.rhaydus.softcover.core.corpus.data.mapper.cleaned
import nl.rhaydus.softcover.core.corpus.data.mapper.isAllowedIn
import nl.rhaydus.softcover.core.corpus.data.mapper.toDomain
import nl.rhaydus.softcover.core.corpus.data.mapper.toEntity
import nl.rhaydus.softcover.core.corpus.domain.repository.MoodMomentsRepository
import nl.rhaydus.softcover.core.domain.model.Mood
import nl.rhaydus.softcover.core.domain.model.MoodMoment
import nl.rhaydus.softcover.core.domain.model.MoodWord
import kotlin.time.Clock

internal class MoodMomentsRepositoryImpl(
    private val localDataSource: MoodMomentsLocalDataSource,
    private val clock: Clock = Clock.System,
) : MoodMomentsRepository {
    override fun observeAll(): Flow<Map<Int, List<MoodMoment>>> = localDataSource.observeAll().map { entities ->
        entities.mapNotNull { it.toDomain() }
            .groupBy { it.bookId }
            .mapValues { (_, group) -> group.sortedWith(PositionOrder) }
    }

    override fun observe(bookId: Int): Flow<List<MoodMoment>> = localDataSource.observe(bookId = bookId).map { entities ->
        entities.mapNotNull { it.toDomain() }.sortedWith(PositionOrder)
    }

    override suspend fun add(
        bookId: Int,
        word: MoodWord,
        strength: Int?,
        percent: Int?,
        page: Int?,
        seconds: Int?,
    ) {
        val moment = MoodMoment(
            id = 0,
            bookId = bookId,
            word = word,
            strength = strength,
            percent = percent,
            page = page,
            seconds = seconds,
            recordedAt = clock.now().toEpochMilliseconds(),
        )

        localDataSource.insert(entity = moment.validated().toEntity())
    }

    override suspend fun update(moment: MoodMoment) {
        localDataSource.update(entity = moment.validated().toEntity())
    }

    override suspend fun delete(id: Long) {
        localDataSource.delete(id = id)
    }

    private fun MoodMoment.validated(): MoodMoment {
        require(word.isAllowedIn(presets = Mood.Moment)) { "A moment mood must be one of the moment words" }
        val cleanedWord = requireNotNull(word.cleaned()) { "A moment mood must not be blank" }

        return copy(
            word = cleanedWord,
            strength = strength.clampedStrength(),
        )
    }

    private companion object {
        val PositionOrder: Comparator<MoodMoment> = compareBy<MoodMoment, Int?>(nullsLast()) { it.percent }
            .thenBy(nullsLast()) { it.page }
            .thenBy(nullsLast()) { it.seconds }
            .thenBy { it.recordedAt }
    }
}
