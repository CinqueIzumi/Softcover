# Step 05 — Land the tooling branch

**Branch:** tooling → `main` → migration. **Delegation:** main conversation, with the user.

## Actions

1. Final review of the tooling diff with `softcover-reviewer` (`## Scope: origin/main..HEAD`). This
   doubles as the first real smoke test of the new reviewer.
2. **Ask the user** before pushing and before opening the PR. PR body: what changed, the baseline numbers
   from `README.md`, a link to `foundation-upstream.md`, ending with the attribution line.
3. After the merge (the user merges): on the migration branch, merge `main` in.
   - **Ask** whether they want a merge or a rebase; the tracker says one branch, one PR.
   - Resolve conflicts with the `resolve-conflicts` skill if any appear (most likely in `CLAUDE.md` and
     `.claude/settings.json`).
4. On the migration branch, set `docs/working/ACTIVE.md` to list **both**
   `docs/working/token-hygiene/README.md` and `docs/working/component-library-migration.md`, and move
   `docs/working/token-hygiene/` over (it was committed on the tooling branch, so it arrives with the
   merge).

## Acceptance

- `main` has the tooling; the migration branch has `main` merged in and builds
  (`scripts/gradle-quiet.sh :core:component:compileKotlinJvm` plus one feature module).
- On the migration branch, a fresh session shows both Now blocks.
