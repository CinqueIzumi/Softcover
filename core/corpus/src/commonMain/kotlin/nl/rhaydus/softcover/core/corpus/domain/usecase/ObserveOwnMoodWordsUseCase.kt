package nl.rhaydus.softcover.core.corpus.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import nl.rhaydus.softcover.core.corpus.domain.repository.BookMoodsRepository
import nl.rhaydus.softcover.core.corpus.domain.repository.MoodMomentsRepository
import nl.rhaydus.softcover.core.domain.model.MoodWord

class ObserveOwnMoodWordsUseCase(
    private val bookMoodsRepository: BookMoodsRepository,
    private val moodMomentsRepository: MoodMomentsRepository,
) {
    operator fun invoke(): Flow<List<String>> = combine(
        bookMoodsRepository.observeAll(),
        moodMomentsRepository.observeAll(),
    ) { bookMoods, moments ->
        val fromBooks = bookMoods.values.flatten().map { it.word }
        val fromMoments = moments.values.flatten().map { it.word }

        (fromBooks + fromMoments).filterIsInstance<MoodWord.Own>()
            .map { it.text.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }
    }
}
