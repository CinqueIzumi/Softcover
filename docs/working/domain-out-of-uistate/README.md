# Domain data out of the UiState

Every screen's UiState holds only UI models and primitives. Repositories are the source of truth for domain
data; collectors map repository flows to UI models; actions take ids. Starts after the component-library
migration closes. The stability gate (`stabilityCheck`, per-module baselines) already exists; this plan
removes the UiState entries from those baselines.

## Now

- **State:** written 2026-10-12 from the Book Detail audits; nothing built.
- **Next:** phase 1, step 1a below: design the per-id book flow and the fetch-persists contract, stop for approval.
- **Open questions:** see `## Risks`.
- **Uncommitted:** this file.

## Why

Book Detail's UiState is the screen's domain cache. `fetchBookById` is a network one-shot whose result is
never stored, so for an off-shelf book `state.book` is the only copy. The book is assembled from that
snapshot, a Room overlay (`UserBooksFlowCollector` patches `userBook`, `userBookRead`, `owned`) and the
caller's `initialCover`. About 21 collectors derive from `state.book`, about 19 actions read domain objects
from the state, and composables hand `Book` / `BookEdition` back to actions as payloads. The repository's
optimistic writes are built from the caller's `Book`, so the write use cases take whole domain objects.

Consequence: the UiState holds types from Compose-free modules and can never be stable, and swapping them for
UI models is not mechanical, because the state also serves as data store and event bus.

## Target shape

- **Source of truth:** a book the app shows is in Room. `fetchBookById` persists the book and its editions
  (no `user_books` row). `BooksRepository.observeBook(id): Flow<Book?>` reads it back.
- **Writes by id:** write use cases take ids (`MarkBookAsRead(bookId, editionId, actionAt)`); the repository
  resolves the book from Room for its optimistic copy and rollback.
- **Collectors** observe repository / use-case flows by id and map straight to UI models. Nothing derives from
  a domain field on the state.
- **Actions** take ids or UI keys, never a domain object from a composable.
- **Selection-only state** is an id on the UiState (the previewed edition, the scanned edition).
- **`initialCover`** seeds the hero's UI model until the book flow emits; it is never merged into a domain
  object.
- **Sections:** the `*ScreenLayout` is the only composable taking the whole UiState; each section below it
  takes its own section UI model built by a collector.
- **Acceptance per screen:** the UiState dumps STABLE in its module's stability baseline; no domain type on
  it; no stability configuration file.

## Steps

| Phase | Step | Scope | Status |
|---|---|---|---|
| 1 | 1a | Per-id book flow; `fetchBookById` persists book + editions; design first, stop for approval | [ ] |
| 1 | 1b | Audit every consumer of the all-books stream (`observeBooks` returns non-user rows); filter where needed | [ ] |
| 1 | 1c | Book Detail refresh path: replace the never-sent `RefreshDetailBookEvent` with a real refresh | [ ] |
| 2 | 2a | Id-based shelf writes: `MarkBookAsWantToRead` / `Reading` / `Read`, all callers (Book Detail, Reading, Library, Explore) | [ ] |
| 2 | 2b | Id-based `RecordBookProgress` (incl. orchestration's active-session controller), `SaveBookVerdict`, `UpdateBookReview` | [ ] |
| 2 | 2c | Id-based `UndoableBookRemover.remove`, `AddBookToList`, `SetEditionAsOwned`, `UpdateBookEdition` | [ ] |
| 2 | 2d | Explore: shelving a search result Room does not have yet (fetch-and-cache before the write) | [ ] |
| 3 | 3a | User tags repository: flow by book id, optimistic updates and rollback in the repository | [ ] |
| 4 | 4a | Book Detail: collectors on repository flows, `UserBooksFlowCollector` overlay and `initialCover` merge retired | [ ] |
| 4 | 4b | Book Detail: actions by id, domain fields off the UiState, derived getters gone | [ ] |
| 4 | 4c | Book Detail: section UI models (hero, shelve card, overflow menu) | [ ] |
| 4 | 4d | Book Detail: section UI models (lenses, overlays); `DatePickerUiModel.initialDate` off `LocalDate`; re-dump baseline | [ ] |
| 5 | 5a | Reading | [ ] |
| 5 | 5b | Explore | [ ] |
| 5 | 5c | Library | [ ] |
| 5 | 5d | Profile | [ ] |
| 5 | 5e | Settings (states already stable; section models only) | [ ] |

Each step runs as one session through the usual loop: design → approval → `softcover-implementer` →
`softcover-test-writer` → `softcover-reviewer` → ask before committing → `/handoff`.

## Risks

- Persisting every fetched book makes the all-books stream carry more non-user rows (1b must land with 1a).
- A Room row hydrated by a list or a mark may hold stale or partial metadata; the per-id flow must refresh it.
- Editions are network-only today, and the previewed edition is deliberately never persisted; 1a decides
  whether editions get a table or ride on the book row.
- The reading-pace forecast is a network one-shot re-triggered by `state.book` progress; it moves onto the
  book flow in 4a.
- Book Detail has 107 test files, 76 of which build `BookDetailUiState`; phase 4 carries a large test round.

## Decisions

- Domain enums (`DateStyle`, `TagCategory`, shelf status) may stay on a UiState; they dump STABLE.
- No foundation change: collectors observe repositories, so `toad` needs no local-variables flow.
