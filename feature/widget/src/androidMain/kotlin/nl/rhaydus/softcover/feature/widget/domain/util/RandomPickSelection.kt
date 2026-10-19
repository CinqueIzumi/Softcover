package nl.rhaydus.softcover.feature.widget.domain.util

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.feature.widget.domain.model.RandomPickCadence
import nl.rhaydus.softcover.feature.widget.domain.model.RandomPickSettings
import nl.rhaydus.softcover.feature.widget.domain.model.RandomPickSource
import nl.rhaydus.softcover.feature.widget.domain.model.RandomPickState
import kotlin.random.Random
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

private val WEEKLY_PICK_LIFETIME = 7.days

internal fun isSourceAvailable(
    source: RandomPickSource,
    lists: List<BookList>,
): Boolean = when (source) {
    RandomPickSource.WantToRead -> true
    RandomPickSource.Owned -> lists.any { it.isOwned }
    is RandomPickSource.UserList -> lists.any { it.id == source.listId }
}

/** Empty when [source] is not [isSourceAvailable]. */
internal fun buildPool(
    source: RandomPickSource,
    wantToRead: List<Book>,
    lists: List<BookList>,
): List<Book> {
    val books = when (source) {
        RandomPickSource.WantToRead -> wantToRead
        RandomPickSource.Owned -> lists.firstOrNull { it.isOwned }.unreadBooks()
        is RandomPickSource.UserList -> lists.firstOrNull { it.id == source.listId }.unreadBooks()
    }

    return books.distinctBy { it.id }
}

private fun BookList?.unreadBooks(): List<Book> = this?.books
    .orEmpty()
    .mapNotNull { it.book }
    .filter { it.status == BookStatus.WantToRead || it.status == BookStatus.None }

internal fun resolvePick(
    state: RandomPickState,
    cadence: RandomPickCadence,
    pool: List<Book>,
    now: Instant,
    timeZone: TimeZone,
    random: Random,
): RandomPickState {
    val stillValid = state.pickedBookId != null &&
        pool.any { it.id == state.pickedBookId } &&
        isFresh(
            state = state,
            cadence = cadence,
            now = now,
            timeZone = timeZone,
        )

    return if (stillValid) {
        state
    } else {
        shufflePick(
            state = state,
            pool = pool,
            now = now,
            random = random,
        )
    }
}

internal fun shufflePick(
    state: RandomPickState,
    pool: List<Book>,
    now: Instant,
    random: Random,
): RandomPickState {
    val candidates = pool.filter { it.id != state.pickedBookId }.ifEmpty { pool }

    val picked = candidates.takeIf { it.isNotEmpty() }?.random(random)
        ?: return RandomPickState()

    return RandomPickState(
        pickedBookId = picked.id,
        pickedAtMillis = now.toEpochMilliseconds(),
    )
}

private fun isFresh(
    state: RandomPickState,
    cadence: RandomPickCadence,
    now: Instant,
    timeZone: TimeZone,
): Boolean {
    val pickedAt = state.pickedAtMillis?.let { Instant.fromEpochMilliseconds(it) } ?: return false

    return when (cadence) {
        RandomPickCadence.Daily -> pickedAt.toLocalDateTime(timeZone).date == now.toLocalDateTime(timeZone).date
        RandomPickCadence.Weekly -> now - pickedAt < WEEKLY_PICK_LIFETIME
        RandomPickCadence.OnShuffleOnly -> true
    }
}

internal fun weeklyPickExpiry(
    settings: RandomPickSettings,
    state: RandomPickState,
): Instant? {
    if (settings.cadence != RandomPickCadence.Weekly) return null

    val pickedAt = state.pickedAtMillis?.let { Instant.fromEpochMilliseconds(it) } ?: return null

    return pickedAt + WEEKLY_PICK_LIFETIME
}
