---
paths:
  - "ROADMAP.md"
  - "scripts/roadmap/**"
  - ".github/workflows/roadmap.yml"
---

# Roadmap

**The roadmap lives in GitHub Issues, not in this repo.** The issue tracker is the one source of truth.

- **Issue** = one unit of work. **Milestone** = the release it lands in. Clusters use **sub-issues**; blocking
  relationships are **native issue dependencies**, not prose.
- Labels: `area:*`, `scope:S|M|L`, `kind:feature|polish|tech|bug`, `needs-design`.
- Every item carries a stable tag in a hidden `<!-- sc-tag: B.4.3 -->` marker. Older commits and docs reference
  these tags, so search by tag to find an item's issue.
- Engineering work outside the release cadence is `kind:tech` with **no milestone**.
- Work with issues via `gh`. Close an item's issue from the PR (`Closes #123`) rather than editing any file.

**`ROADMAP.md` is generated — never hand-edit it.** Its content comes from the `description` field of each open
milestone.

- To change what it says, **edit the milestone description**, not the file.
- `.github/workflows/roadmap.yml` regenerates it on any milestone change and opens a single pull request
  (`chore/roadmap-sync`); it never pushes to `main`, and further edits update that PR in place. Merging it
  publishes: the in-app Roadmap screen fetches the file from the default branch at runtime.
- A pull request touching `ROADMAP.md` runs `generate_roadmap.py --check` and **fails if the file was
  hand-edited**.
- **Closing a milestone removes its section** from the public roadmap, so closing one is a user-visible act.
- The only hand-written parts are `scripts/roadmap/header.md` (static caveats plus what has *shipped*) and the
  "Under consideration" footer in `scripts/roadmap/generate_roadmap.py`.
- Regenerate locally with `python3 scripts/roadmap/generate_roadmap.py --write`.
