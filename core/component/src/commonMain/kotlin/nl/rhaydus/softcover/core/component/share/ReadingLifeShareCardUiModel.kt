package nl.rhaydus.softcover.core.component.share

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
/**
 * The exportable "reading life" share card content — a reader's whole-history snapshot (total pages,
 * their most-read genres, a pages-by-month ridgeline, and a rating / streak / tenure footer). The call
 * site (feature/profile) maps `core:profile`'s `ReadingLife` domain model onto this plain primitive
 * shape; this type deliberately carries no `core:profile` dependency so the card stays self-contained
 * here in `core:designsystem`, matching the other [ShareCardUiModel] variants.
 *
 * Every field a sparse reader might not have yet is nullable or list-shaped so [ShareCard] can render
 * without special-casing at the call site: [avatarUrl] falls back to initials, [pagesByMonth] is
 * padded/truncated to twelve months, and [topGenres] simply omits its section when empty.
 *
 * [topGenres] arrives already ranked, highest share first — the card renders the leading entry larger
 * than the rest rather than re-sorting.
 */
data class ReadingLifeShareCardUiModel(
    val readerName: String,
    val avatarUrl: String?,
    val totalPagesRead: Int,
    val totalBooksRead: Int,
    val topGenres: ImmutableList<ReadingLifeGenre> = persistentListOf(),
    val pagesByMonth: ImmutableList<Int>,
    val averageRating: Double,
    val dayStreak: Int,
    val trackedYears: Int,
) : ShareCardUiModel
