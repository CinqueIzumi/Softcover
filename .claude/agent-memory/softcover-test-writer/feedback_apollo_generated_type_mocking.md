---
name: feedback_apollo_generated_type_mocking
description: Mocking Apollo-generated response types in data-source tests — taggable_counts shapes, fragment accessors, deeply nested interfaces, and Optional.Absent assertions.
metadata:
  type: feedback
---

## taggable_counts { book { ...BookDetailFragment } } queries

`GetBooksByMoodTagQuery`, `GetBooksByGenreTagQuery` (and similar) generate
`Data(taggable_counts: List<Taggable_count>)`, each `Taggable_count(book: Book?)` with `Book` implementing
`BookDetailFragment` via `Book.Companion.bookDetailFragment()`. Production maps
`taggable_counts.mapNotNull { it.book?.xBookDetailFragment()?.toBook() }.distinctBy { it.id }`.

- Mock like `GetBooksByIdsQuery`: `mockkObject(X.Data.Taggable_count.Book.Companion)` in setUp; per row,
  `mockk<X.Data.Taggable_count>()`, stub `.book`, stub
  `with(X.Data.Taggable_count.Book.Companion) { bookEntry.bookDetailFragment() }` to a mocked fragment,
  then stub `.toBook()`. Mock rather than build the real data classes, to match the file's style.
- The dedupe is load-bearing — test it per query.
- When the shape backs pagination, `hasMore` comes from the RAW row count, not the deduped count. Test a
  full page of rows that resolves to few distinct books and assert `hasMore` stays true.

## insert_user_book { user_book { ...UserBookFragment } } mutations

The fragment accessor (`userBookFragment()` or an alias) is an identity cast on these response types.
Stub only `insertUserBook.user_book` and `userBookEntry.toBook()`; never stub the accessor.

## Interfaces nested 3+ levels deep

`mockk<BookListTaggableCount.Tag_category>()` on a type alias doesn't compile. Add one `import ... as Alias`
per nesting level and use the flat alias. `Tag_category.category` clashes with `kotlin.text.CharCategory`:
write `every { this@mockk.category } returns "..."`.

## Optional inputs

When asserting a mutation input field that is absent, check `is Optional.Absent`, not `== null`
(`match<X> { it.input.field.getOrNull() == value }` for the present case).
