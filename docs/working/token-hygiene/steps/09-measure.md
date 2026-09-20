# Step 09 — Measure and close out

**Branch:** migration. **Delegation:** main conversation.

## Actions

1. After at least **five** migration sessions have run under the new setup, run
   `python3 scripts/claude/token-usage.py --since <date Step 05 merged>`.
2. Compare against the `README.md` baseline table and write the before/after table into
   `foundation-upstream.md` § Evidence. Expected, as estimates to check rather than guarantees:
   - main-conversation mean context ≤150K, and ≤150 turns per session;
   - implementer / reviewer cost per run ≤5M, and ≤40 turns;
   - subagent starting context ≤35K.
3. Where a target is missed, find the cause with the same script (top reads, largest tool results), and
   **ask the user** before any follow-up change.
4. Close out:
   - Move `docs/working/token-hygiene/foundation-upstream.md` → `docs/working/foundation-token-hygiene.md`,
     and add a one-line pointer to it in `docs/working/foundation-upstream-candidates.md`.
   - Delete `docs/working/token-hygiene/` and remove its line from `docs/working/ACTIVE.md`.

## Acceptance

- The Evidence table is filled in; the plan directory is gone; the foundation doc remains and is linked.
