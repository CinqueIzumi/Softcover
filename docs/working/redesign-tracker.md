# Redesign implementation tracker

Tracks which screen redesigns from the **"# Softcover redesigns"** claude.ai/design project
(projectId `e2100354-7268-4e98-8654-db1e2591639b`) have been implemented in the app.

Each redesign starts as a redline **spec sheet** in that project and is implemented via the
`/redesign <screen>` skill (rhaydus-logic → rhaydus-ui → code-reviewer → unit-test-writer
pipeline). When a screen's redesign is implemented, the `/redesign` skill marks its row here
as **✅ Implemented**. This document is the single source of truth for "is this screen's
redesign in the app yet?".

## Status legend

- ⬜ **Not started** — spec sheet exists, no implementation yet.
- ✅ **Implemented** — the redesign has been built into the app.

## Screens

| Screen | Spec sheet | Status |
| --- | --- | --- |
| Book detail | `Book Detail 2a - Spec Sheet.dc.html` (+ `Book Detail Sheets - Spec Sheet.dc.html`) | ✅ Implemented |
| Explore | `Explore 3a - Spec Sheet.dc.html` | ✅ Implemented |
| Reading | `Reading 1a - Spec Sheet.dc.html` | ✅ Implemented |
| Library | `Library Spec Sheet.dc.html` | ✅ Implemented |
| Profile | `Profile 1a - Spec Sheet.dc.html` | ✅ Implemented |
| Settings | `Settings 1a - Spec Sheet.dc.html` | ✅ Implemented |
| Appearance | `Appearance 1a - Spec Sheet.dc.html` | ✅ Implemented |
| Library tabs | `Library Tabs 1a - Spec Sheet.dc.html` | ⬜ Not started |
| Hidden suggestions | `Hidden Suggestions 1a - Spec Sheet.dc.html` | ⬜ Not started |
| List creation | `List Creation 1a - Spec Sheet.dc.html` | ✅ Implemented (Create only — the sheet's Edit and Delete surfaces await Step 5.3's list CRUD in 3.7.0) |
| Onboarding | `Onboarding 1b - Spec Sheet.dc.html` | ⬜ Not started |
| Tag editor | `Tag Editor - Spec Sheet.dc.html` | ✅ Implemented |

## Notes

- Spec-sheet file names carry a version tag (`2a`, `3a`, `1b`, …); the `/redesign` skill picks
  the highest matching version. If a newer version supersedes the one listed here, update the
  spec-sheet cell in the same pass that re-implements the screen.
- Library, book detail, and the tag editor were implemented ahead of this tracker's creation
  (commit `dcb67dbd` for the library redesign, `7cb4d43e` for the tag editor; book detail
  predates them).
- `Typography Option A - Newsreader.dc.html` in the design project is a typography
  exploration, not a screen redesign spec sheet, so it is intentionally not tracked here.
- **Explore 3a** was built with two approved deviations from the spec sheet: the featured
  card's *Remind me* affordance and its release-reminder bottom sheet were dropped (the app has
  no future-notification scheduling yet — the card ships with *Want to read* only), and the
  *"Because you read {book}"* rail was implemented as *"Because you read {genre}"* (the Hardcover
  API has no recommendation/similar-books capability, so the rail is driven by the reader's
  most-read genre with a user-selectable override persisted locally). The spec's search
  sort/filter is limited to the two proven Typesense sort keys (Relevance / Popularity). The
  monogram cover from the spec's unreleased card became the app-wide coverless fallback
  (`CoverlessTitleCover`), keyed on missing art rather than release status.
