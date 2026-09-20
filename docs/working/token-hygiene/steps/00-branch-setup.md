# Step 00 — Branch setup

**Branch:** starts on `275-migrate-…`, ends on the new tooling branch.
**Delegation:** none; main conversation, with the user.

## Actions

1. `git status --short` on the migration branch. The S5-1 chip work is staged (see
   `docs/working/s5-1-chips-plan.md`). **Ask the user** whether to commit it (they give or approve the
   subject line) or stash it. Do not choose for them.
2. Confirm `docs/working/token-hygiene/` is untracked, so it carries across the checkout.
3. **Ask the user** for the tooling branch name (suggest `token-hygiene-tooling`), then
   `git fetch && git checkout -b <name> origin/main`.
4. Confirm `docs/working/token-hygiene/` is present on the new branch.

## Acceptance

- The migration branch has no uncommitted S5-1 work left unaccounted for (committed or stashed, as the
  user chose).
- The tooling branch exists off an up-to-date `main`, with the plan directory present.

Update `README.md` `## Now` → Next: Step 01.
