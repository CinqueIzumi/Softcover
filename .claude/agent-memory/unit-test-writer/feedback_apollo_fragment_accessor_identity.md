---
name: feedback-apollo-fragment-accessor-identity
description: Apollo insert_user_book/update_user_book fragment accessor extensions (e.g. userBookFragment()) are identity casts on these response types — tests stub .toBook() directly on the User_book mock, never the fragment accessor itself
metadata:
  type: feedback
---

For mutations shaped like `insert_user_book { user_book { ...UserBookFragment } }` (e.g. `MarkBookAsReadMutation`, `MarkBookAsWantToReadMutation`, `MarkBookAsReadingViaInsertMutation`), production code calls `.insert_user_book?.user_book?.userBookFragment()?.toBook()` (or an aliased fragment accessor when multiple mutations are imported in the same file). Existing tests for these paths never stub the fragment accessor (`.userBookFragment()` / aliased equivalent) — they only stub `.toBook()` directly on the `User_book` mock instance. This works because the generated fragment-accessor extension is effectively an identity cast (the response type already implements the fragment interface), so calling it on a mock returns the same mock instance, and `.toBook()` on that instance is what's actually stubbed.

**Why:** Confirmed while adding `MarkBookAsReadingViaInsertMutation` create-path tests to `BooksRemoteDataSourceImplTest.kt` (mirroring the existing `markBookAsRead`/`markBookAsWantToRead` insert-path tests) — attempting to also stub the fragment accessor would be both unnecessary and a guess at a symbol/behavior that isn't actually exercised by the existing pattern.

**How to apply:** When writing a new "insert_user_book create path" data-source test for any mutation with this `insert_user_book { user_book { ...Fragment } }` shape, only stub `insertUserBook.user_book` (returns the `User_book` mock) and `userBookEntry.toBook()` (returns the expected `Book`). Do not stub the fragment accessor extension — it isn't needed and has no separate mock hook in these tests.
