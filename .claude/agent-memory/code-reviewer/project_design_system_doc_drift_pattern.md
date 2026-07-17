---
name: project_design_system_doc_drift_pattern
description: docs/reference/design-system.md drifts when a later edit changes a UI detail but the author only re-edits one paragraph, not every mention
type: project
---

Recurring failure mode found in `docs/reference/design-system.md`: a feature is built in stages
(e.g. Profile's genre-stack / reading-life share card), and the doc paragraph describing the most
recently touched surface (e.g. the Share card entry, ~line 256) gets updated in place, but the SAME
fact is also asserted in one or two OTHER bullets elsewhere in the doc (a `§4` component-catalogue
entry and a `§5` "Profile screen" recipe bullet that names the same section by its old headline/
detail) — and those don't get touched.

Concretely found 2026-07-20: `ProfileShelf.kt`'s genre stack was reworked (remainder "Everything
else" slice removed, `GENRE_STACK_ALPHAS` trimmed 6→5, headline reworded "The shelves of your
taste" → "The genres you read most"). The reading-life share-card paragraph (§4, ~line 256) was
correctly updated. But:
- §4 "Genre proportion stack" bullet (~line 251) still said "six stepping alphas
  (100/82/64/48/34/24%)".
- §5 "Profile screen" bullet (~line 325) still named the section "'The shelves of your taste'".

**Why:** the doc describes the same UI fact from 3 angles (component catalogue entry, recipe/screen
bullet, and — for share cards — the share-card variant paragraph), and CLAUDE.md's maintenance rule
technically only gets enforced against whichever paragraph the author remembered to open.

**How to apply:** whenever a design-system-relevant code change touches copy, alpha/count constants,
or a section's shape, `grep` the OLD value/name across the whole `design-system.md` (not just the
paragraph that seems most relevant) before signing off — e.g. `grep -n "old headline text\|old alpha
list"`. Treat every hit as a doc-accuracy finding, not just the first one found.
