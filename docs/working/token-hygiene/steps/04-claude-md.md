# Step 04 — CLAUDE.md catalogue + path-scoped rules

**Branch:** tooling. **Depends on:** P3 (whether rules load in subagents), Step 03 (agent names).
**Delegation:** main conversation.

Goal: `CLAUDE.md` loads in every session and every agent (7.3K tokens today). Keep only what applies to
every task; everything area-specific moves to `.claude/rules/*.md` with `paths:` so it loads only when
matching files are touched. **No normative rule may be dropped**, only moved or shortened; history and
rationale (e.g. "3,144 `FunctionNaming` findings") are removed.

## 1. Rule-by-rule mapping first

Before editing, go through the current `CLAUDE.md` and classify every rule as **root** / **rule file X** /
**history (delete)**. Show the table to the user and get approval before writing. This is the review point
that protects the normative rules.

## 2. Target root `CLAUDE.md` (≤ ~8KB, about 3K tokens)

1. **Project overview** — two lines.
2. **Engineering principles** — verbatim (normative).
3. **Working in this repo** (new):
   - Session loop: `docs/working/ACTIVE.md` → the tracker's `## Now` block (≤40 lines) → `/handoff` → the
     user runs `/clear`. One step per session.
   - Routing (D8): ≤3 existing files, no new file, no public API change → the main conversation does it.
     Otherwise `softcover-implementer`; tests always `softcover-test-writer`; review with
     `softcover-reviewer` before reporting substantial work done. Briefs follow
     `docs/reference/agent-briefs.md` (enforced by hook).
   - The main conversation directs: no multi-step Gradle loops or large-file paging in the main
     conversation. Gradle output is summarised automatically; the full log is at the printed path.
4. **Build & test** — the five commands, one line each.
5. **Tests** — the no-exceptions delegation rule and the "failing test → report verbatim and stop" rule,
   in ≤6 lines. The brief detail moves to `docs/reference/agent-briefs.md`.
6. **Commit messages** — the subject-only rule and its style bullets, condensed.
7. **Roadmap** — three lines: GitHub Issues are the source of truth, `ROADMAP.md` is generated, and the
   details are in `.claude/rules/roadmap.md`.
8. **Where things live** — a table of *"When you are … → read …"* (architecture, module structure,
   design system index, component contract, code style, capabilities index).
9. **Rhaydus block** — see §4.

## 3. Rule files (`.claude/rules/`, each with `paths:`, each ≤40 lines)

| File | `paths` | Content |
|---|---|---|
| `build-wiring.md` | `**/build.gradle.kts`, `build-logic/**`, `gradle/**`, `settings.gradle.kts` | CMP resources need `androidResources.enable = true` + the `checkResourcePackaging` gate; the version catalog; a pointer to module-structure § Build wiring |
| `kotlin-style.md` | `**/*.kt` | ktlint/detekt gates in brief: type resolution, test sources gated, no per-file `@Suppress`, `iosMain` not covered, never reintroduce a style script; a pointer to the review-only section |
| `compose-ui.md` | `**/presentation/**`, `core/component/**`, `core/designsystem/**` | design-system index + foundation doc pointers; the **maintenance rule** (a design-system change updates its doc in the same change); `:core:presentation` is not a component home |
| `architecture.md` | `feature/**`, `core/**`, `orchestration/**` | the current Quick reference, condensed |
| `tests.md` | `**/androidHostTest/**`, `**/commonTest/**` | test conventions pointer |
| `roadmap.md` | `ROADMAP.md`, `scripts/roadmap/**`, `.github/workflows/roadmap.yml` | the full roadmap rules from the current `CLAUDE.md` |

**If P3 = rules do not load in subagents:** keep the rule files (they still help the main conversation),
and make sure every `softcover-*` agent's "where the rules live" map names the rule file for its area.

Never create a rule file without `paths:`: it would load always, which is the same as `CLAUDE.md`.

## 4. Rhaydus block — local override (D6)

- Replace the managed block with the version proposed for the foundation, under the heading
  `## Rhaydus foundation (local override — pending foundation FU-6; re-running rhaydus-adopt reverts this)`.
- Content, catalogue style, ≤25 lines:
  - The version pin and where the vendored docs are.
  - One line each for the consumed libraries, grouped.
  - The routing (the `softcover-*` agents; `rhaydus-adopt` for foundation wiring changes).
  - Reuse-first → `CAPABILITIES.md`.
- Copy the exact text into `foundation-upstream.md` FU-6 as the proposed rhaydus-adopt template.

## 5. `docs/reference/agent-briefs.md`

If Step 03 didn't finish it: the three brief templates (`## Goal / ## Files / ## Contract (optional) /
## Constraints / ## Verify / ## Report`), plus the scoping advice now in `CLAUDE.md` § Test Writing (exact
paths and lines, one `--tests` filter, no re-audit, a report limit).

## Acceptance

- The user approved the §1 mapping table.
- `wc -c CLAUDE.md` ≤ 8500. `/context` → Memory files: `CLAUDE.md` ≤ 3.2K tokens.
- Touching a `build.gradle.kts` in a session loads `build-wiring.md` (check it shows up in `/context` or
  the model can quote it); a session that only edits docs doesn't load it.
- The P3 fallback is applied if needed.
- `foundation-upstream.md` FU-6 contains the exact proposed block → "done locally".
