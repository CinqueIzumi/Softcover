# Now

The day-to-day working surface. This is the **only** planning doc you need open while working: what's getting attention right now, and the small things to clear ASAP. The long-horizon plan lives elsewhere and is unchanged — this file just points into it.

- **Focus** is the 1–2 topics being actively driven. Each is a pointer to its real place in the plan (`[[step]]` / roadmap tag), never a fork of it.
- **Fast-track fixes** is a flat, unordered backlog of small things to do soon, independent of the release cadence. They don't wait for a phase slot.

**How this relates to the rest:**
- [idea-catalogue.md](idea-catalogue.md) — the idea catalogue (the *what*).
- [roadmap-steps.md](roadmap-steps.md) — the sequenced steps (the *order*).
- [release-plan.md](release-plan.md) — steps bundled into versioned drops (the *when*).
- [../../ROADMAP.md](../../ROADMAP.md) — the **public**, user-facing projection of the release plan.
- [foundation-upstream-candidates.md](foundation-upstream-candidates.md) — findings to push upstream into the nl.rhaydus foundation (bugs, reusability gaps, gates to promote).

**Maintenance rules.**
- A **focus** item is a step pulled to the front — link it to its step number / roadmap tag so it stays anchored. When it ships, it follows its step's normal lifecycle (deleted from `roadmap-steps.md`); remove it from Focus here too.
- A **fast-track fix** is one line, checked off and **deleted** when it ships (same discipline as `roadmap-steps.md`). When a fix ships, fold it into the next release's notes in `release-plan.md` (the "+ fixes and polish" line) rather than listing it individually on the public roadmap.

---

## Focus

_The 1–2 topics being driven right now. Each links to its step / roadmap tag._

<!-- - Topic — `[[step N.N]]` / roadmap tag — one line on the current state -->

---

## Fast-track fixes

_Small things to clear ASAP, outside the release cadence. One line each; delete when shipped._

- [ ] Complete the AGP 9 migration: flip `android.builtInKotlin` / `android.newDsl` back to defaults in `gradle.properties` and drop the explicit `org.jetbrains.kotlin.android` plugin once KSP supports AGP 9's built-in Kotlin (currently blocked — see architecture-review B2). Verify the Room KSP path still works after the switch.
- [ ] Replace the deprecated `FlowRowOverflow.expandIndicator` overflow API in `ExpandableFlowRow` once Compose ships a maintained replacement (currently `@file:Suppress("DEPRECATION")`; the successor `ContextualFlowRow` is also deprecated, so there is no stable target yet).
