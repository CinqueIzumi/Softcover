---
name: feedback_apollo_taggable_counts_mocking
description: How to mock Apollo-generated queries shaped like taggable_counts { book { ...BookDetailFragment } } (GetBooksByMoodTagQuery, GetBooksByGenreTagQuery)
metadata:
  type: feedback
---

Several explore-feature Apollo queries (`GetBooksByMoodTagQuery`, `GetBooksByGenreTagQuery`, and
likely future similar ones) share this generated shape: `Data(taggable_counts: List<Taggable_count>)`
where each `Taggable_count(book: Book?)` and `Book` implements `BookDetailFragment` via a
`Book.Companion.bookDetailFragment()` extension (same pattern as `GetBooksByIdsQuery.Data.Book`).
Production code does `response.taggable_counts.mapNotNull { it.book?.xBookDetailFragment()?.toBook() }`
then `.distinctBy { it.id }` — a book can appear in more than one `taggable_counts` row, so dedupe
is load-bearing and worth a dedicated test per query.

**How to apply:** mock each such query the same way the file already mocks `GetBooksByIdsQuery`:
`mockkObject(X.Data.Taggable_count.Book.Companion)` in setUp, then per-row helpers that
`mockk<X.Data.Taggable_count>()`, stub `.book`, and stub
`with(X.Data.Taggable_count.Book.Companion) { bookEntry.bookDetailFragment() }` to return a mocked
`BookDetailFragment`, then stub `.toBook()` on that. Don't hand-construct real `Data`/`Taggable_count`
data classes even though they're simple (single/double-field) — mocking keeps the whole file's
mocking style consistent, which matters more here than the small win of using real data classes.
Applied in `SearchRemoteDataSourceImplTest` (`feature/explore`) for `searchByMood` and
`fetchBooksByGenre`.

`hasMore`/pagination detail: when this shape backs pagination (as `searchByMood` does via
`MOOD_BOOKS_LIMIT`), `hasMore` must be computed from the RAW `taggable_counts` row count, not the
deduped book count — a full page can dedupe below the page size and would otherwise falsely signal
end-of-results. Test this with a page of rows that all resolve to a handful of distinct books and
assert `hasMore` is still true.
