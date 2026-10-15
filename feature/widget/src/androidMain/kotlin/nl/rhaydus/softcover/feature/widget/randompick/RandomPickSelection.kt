package nl.rhaydus.softcover.feature.widget.randompick

import kotlin.random.Random
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.BookStatus

internal fun resolveSource(
    source: RandomPickSource,
    lists: List<BookList>,
): RandomPickSource = if (source is RandomPickSource.UserList && lists.none { it.id == source.listId }) {
    RandomPickSource.WantToRead
} else {
    source
}

internal fun buildPool(
    source: RandomPickSource,
    wantToRead: List<Book>,
    lists: List<BookList>,
): List<Book> {
    val resolved = resolveSource(
        source = source,
        lists = lists,
    )

    val books = when (resolved) {
        RandomPickSource.WantToRead -> wantToRead
        RandomPickSource.Owned -> lists.firstOrNull { it.isOwned }.unreadBooks()
        is RandomPickSource.UserList -> lists.firstOrNull { it.id == resolved.listId }.unreadBooks()
    }

    return books.distinctBy { it.id }
}

private fun BookList?.unreadBooks(): List<Book> = this?.books
    .orEmpty()
    .mapNotNull { it.book }
    .filter { it.status != BookStatus.Read && it.status != BookStatus.Reading }

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
        RandomPickCadence.Weekly -> now - pickedAt < 7.days
        RandomPickCadence.OnShuffleOnly -> true
    }
}
