---
name: architecture_reading_1a_redesign
description: Reading 1a redesign (2026-07-21) — core/personal promotion of the reading-pace forecast, featured-hero card anatomy, and the confirmed Roman-titleMedium-for-list-titles convention.
metadata:
  type: project
---

Reviewed the "Reading 1a" screen redesign (release/3.1.0 branch, uncommitted at review time): Phase 1
(logic) promoted `ReadingPaceForecast`/`GetReadingJournalHistoryUseCase`/
`ReadingJournalHistoryRepository(Impl)`/`ReadingJournalHistoryRemoteDataSource`/`ReadingJournalEntry`
out of `feature/book_detail` into `core/personal` (byte-identical move, package rename only, verified
via diff against the deleted originals — no behavior drift, book_detail's own screenmodel/state/DI only
repointed imports). Phase 2 (UI) restyled `feature/reading`'s `ReadingShelf.kt`/`StreakStrip.kt` and both
platform layouts into a hero-fused pace-nudge ribbon, dark fixed-ink backdrop card, wavy-everywhere
progress, and a Roman-titled secondary-row list.

**Confirmed pattern: card/list titles are Roman `titleMedium`, never italic**, per
`docs/reference/design-system.md` §2.2's typography-role table (`titleLarge`/`titleMedium`/`titleSmall`
= "Card titles, list item titles, grouped row headers" = Roman semibold; italic is reserved for
`display`/`headlineMedium`/`headlineSmall`/`body*`/`stat*` roles). The Reading redesign's "also reading"
secondary-row titles deliberately deviate from the Figma redline (which sets them in italic Fraunces) to
stay Roman — this is documented explicitly in the new "Reading secondary row" design-system.md entry as
an intentional, reviewed deviation from the mockup, not an oversight. Treat this as the settled answer
if a future redesign's secondary-row titles come up italic vs Roman again — Roman wins per the app's
established editorial-role contract.

**core/personal now depends on `core:network` + `core:identity`** (both `implementation`, not `api` —
correctly respecting the "domain-area data module" api-visibility rule in
`docs/reference/module-structure.md`). No cycle: neither `core:network` nor `core:identity` depends back
on `core:personal`.

See also [[style_trailing_lambda_glomming_recurs]] for a regression found in this same review (two
`LinearWavyProgressIndicator` call sites un-fixed the glomming when replacing the old flat
`LinearProgressIndicator`).
