package nl.rhaydus.softcover.core.corpus.data.mapper

import nl.rhaydus.softcover.core.database.model.BookMoodEntity
import nl.rhaydus.softcover.core.database.model.MoodMomentEntity
import nl.rhaydus.softcover.core.domain.model.BookMood
import nl.rhaydus.softcover.core.domain.model.Mood
import nl.rhaydus.softcover.core.domain.model.MoodMoment
import nl.rhaydus.softcover.core.domain.model.MoodWord

internal fun moodWordOf(
    word: String,
    isOwn: Boolean,
): MoodWord? = if (isOwn) {
    MoodWord.Own(text = word)
} else {
    Mood.fromStorageKey(key = word)?.let { MoodWord.Preset(mood = it) }
}

internal val MoodWord.storageKey: String
    get() = when (this) {
        is MoodWord.Preset -> mood.storageKey
        is MoodWord.Own -> text
    }

internal val MoodWord.isOwn: Boolean
    get() = this is MoodWord.Own

internal fun BookMoodEntity.toDomain(): BookMood? {
    val word = moodWordOf(
        word = word,
        isOwn = isOwn,
    ) ?: return null

    return BookMood(
        word = word,
        strength = strength,
    )
}

internal fun BookMood.toEntity(bookId: Int): BookMoodEntity = BookMoodEntity(
    bookId = bookId,
    word = word.storageKey,
    isOwn = word.isOwn,
    strength = strength,
)

internal fun MoodMomentEntity.toDomain(): MoodMoment? {
    val word = moodWordOf(
        word = word,
        isOwn = isOwn,
    ) ?: return null

    return MoodMoment(
        id = id,
        bookId = bookId,
        word = word,
        strength = strength,
        percent = percent,
        page = page,
        seconds = seconds,
        recordedAt = recordedAt,
    )
}

internal fun MoodMoment.toEntity(): MoodMomentEntity = MoodMomentEntity(
    id = id,
    bookId = bookId,
    word = word.storageKey,
    isOwn = word.isOwn,
    strength = strength,
    percent = percent,
    page = page,
    seconds = seconds,
    recordedAt = recordedAt,
)
