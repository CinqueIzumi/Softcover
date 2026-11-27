package nl.rhaydus.softcover.core.corpus.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.corpus.data.datasource.BookMoodsLocalDataSource
import nl.rhaydus.softcover.core.corpus.data.mapper.clampedStrength
import nl.rhaydus.softcover.core.corpus.data.mapper.cleaned
import nl.rhaydus.softcover.core.corpus.data.mapper.isAllowedIn
import nl.rhaydus.softcover.core.corpus.data.mapper.toDomain
import nl.rhaydus.softcover.core.corpus.data.mapper.toEntity
import nl.rhaydus.softcover.core.corpus.domain.repository.BookMoodsRepository
import nl.rhaydus.softcover.core.domain.model.BookMood
import nl.rhaydus.softcover.core.domain.model.Mood
import nl.rhaydus.softcover.core.domain.model.MoodWord

internal class BookMoodsRepositoryImpl(
    private val localDataSource: BookMoodsLocalDataSource,
) : BookMoodsRepository {
    override fun observeAll(): Flow<Map<Int, List<BookMood>>> = localDataSource.observeAll().map { entities ->
        entities.groupBy { it.bookId }
            .mapValues { (_, group) -> group.mapNotNull { it.toDomain() } }
            .filterValues { it.isNotEmpty() }
    }

    override fun observe(bookId: Int): Flow<List<BookMood>> =
        localDataSource.observe(bookId = bookId).map { entities -> entities.mapNotNull { it.toDomain() } }

    override suspend fun save(
        bookId: Int,
        moods: List<BookMood>,
    ) {
        require(moods.all { it.word.isAllowedIn(presets = Mood.HowItFelt) }) {
            "A book mood must be one of the How it felt words"
        }

        val seen = mutableSetOf<Pair<Boolean, String>>()

        val entities = moods.mapNotNull { mood ->
            val word = mood.word.cleaned() ?: return@mapNotNull null

            val key = when (word) {
                is MoodWord.Preset -> false to word.mood.storageKey
                is MoodWord.Own -> true to word.text.lowercase()
            }

            if (seen.add(key)) {
                mood.copy(
                    word = word,
                    strength = mood.strength.clampedStrength(),
                ).toEntity(bookId = bookId)
            } else {
                null
            }
        }

        localDataSource.replaceForBook(
            bookId = bookId,
            entities = entities,
        )
    }
}
