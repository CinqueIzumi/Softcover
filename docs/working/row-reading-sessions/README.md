# Reading sessions from every currently-reading row

User feedback: a reading session can only be started from the featured card. Every "Also reading" row gets a
start control too, without making the list busier.

## Now

- **State:** S1–S3 and T built, reviewed (looks good) and tested; V is with the user. Ships in 3.2.4: work on
  `row-reading-sessions`, PR into `release/3.2.4` (cut from `main`).
- **Next:** the user runs V on Medium_Phone; then `check koverXmlReport -x lint` once, then commit on request.
- **Open questions:** `start()` launches async, so two very fast taps can still open two sessions (the
  featured card had this before). A real guard is a repository-level "start only if none is open"; not built.
- **Verification:** `styleCheck :konsist:test :feature:reading:testAndroidHostTest
  :feature:reading:compileAndroidMain :core:component:androidMainStabilityCheck checkDocBudgets` passed.
- **Uncommitted:** all S1–S3 and T code, stability dumps, `components.md`, `patterns/reading.md`, tracker docs.

## Why

- `ActiveSessionController.start()` is only called from `ReadingFeaturedSection`; rows carry one trailing
  control, the `SetProgress` pill.
- `start()` inserts a new open session without stopping a running one, so no surface may offer a start while a
  session runs. `FeaturedSessionButton` already hides its start button then.

## Decisions

- Rows keep `main`'s single ✎ pill; starting is a "▶ Start session" text link under the progress bar.
- ▶ is hidden while any session runs, for any book, as on the featured card; the row then shows only ✎.
- ▶ is never offered for audiobooks or on loading stand-ins.
- The link hiding and returning animates the row's height; it never pops.
- Out of scope: moving the session's book into the featured slot, a "switch session" flow, book-detail start.

## Steps

| Step | Scope | Status |
|---|---|---|
| S1 | `:core:component`: `BookCardTrailing` split variant (start + set-progress segments, width animated when the start segment drops) and a `BookCardEvent` for the start segment; component contract, gallery preview | [x] |
| S2 | `:feature:reading` state: `ActiveSessionController` on `ReadingScreenDependencies`, a collector writing whether a session runs to `ReadingScreenUiState`, `toAlsoReadingCard` setting `BookCardVariant.Row.startSession` (a "Start session" link under the progress) (no session, not an audiobook, not a stand-in), `OnStartSessionClickAction` calling `start()` | [x] |
| S3 | `:feature:reading` render: `ReadingCardEventHandler` routes `StartSessionClicked`; one notification-permission requester at section level shared by rows and `FeaturedSessionButton`, which also moves onto `OnStartSessionClickAction`; mobile and jvm layouts; `docs/reference/design-system/patterns/reading.md:66` ("only the pencil chip") rewritten | [x] |
| T | test-writer: row mapper pill rules, session collector, start action | [x] |
| V | User checks on Medium_Phone: start from a row, ▶ disappears on every row and returns after stopping, audiobook rows, permission prompt on first start | [ ] |

S2 and S3 share a module and run in order after S1.

## Per-step routine

1. Implementer brief (Goal / Files / Verify / Report).
2. Build, then `rhaydus-kotlin:test-writer` where the step adds logic, then `styleCheck :konsist:test`, then
   `rhaydus-kotlin:reviewer`.
3. Stop for the user's go-ahead.

## Risks

- A wider trailing control on rows that also carry a deadline summary may squeeze the title; check the longest
  title and a deadline row in S1's preview.
- The start action fires `start()` after the permission result; a session that began between render and tap
  could still double-start. The action re-checks `activeSession` before calling `start()`.
