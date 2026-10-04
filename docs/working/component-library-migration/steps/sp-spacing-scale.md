# SP — Spacing scale

**Stage:** SP — app-wide, after S10 (the shelves are split, so the sweep touches their final files) and
before S11. **Delegation:** user (SP-1), `softcover-implementer` per module group (SP-2 onward).

Spacing is written as raw dp at every call site (`2.dp`, `7.dp`, `14.dp`, …), so the same gap drifts from
screen to screen. SP introduces one named spacing scale on a 4dp grid. Every padding, gap and spacer then
reads a token (`Spacing.md`-style) instead of a number. Off-grid values snap to the nearest step as accepted
shifts. The scale follows `design-system-foundations.md` § 5.1 (a small recurring set, roughly doubling)
and app `foundations.md` § 2.4 (4/8, 12/16, 20/24, 28/32, 40/48).

## Sub-steps

| Sub | Scope | Status |
|---|---|---|
| SP-1 | Inventory and approved shape (below) — stop for approval | [ ] |
| SP-2 | The token object, its KDoc, the code-style rule, and the gate | [ ] |
| SP-3 | Sweep `:core:component` and `:core:designsystem` | [ ] |
| SP-4 | Sweep the feature modules, one brief per module group | [ ] |
| SP-5 | Every file-level `private val X = N.dp` (`git grep -l "^private val .*\.dp$" -- '*.kt'`; 30 files, 66 vals at planning) and `BarSpec`-style dp holders becomes a token (spacing) or inline (sizes); done when the grep is empty | [ ] |
| SP-C | Convergence pass (family-procedure.md § Phase 3): the gutters and gaps that should now be one value | [ ] |

## SP-1 — inventory and decisions

1. **Inventory.** Tally every dp literal passed to `padding`, `PaddingValues`, `Arrangement.spacedBy`,
   `Spacer` (`height` / `width`), `offset` and `contentPadding`, by value and module. Flag off-grid values
   and the screens that use them.
2. **Decide with the user:**
   - Token names and steps, for example `Spacing.xxs` 4 … `Spacing.xxl` 48. Decide whether a 2dp
     hairline step exists.
   - Where the object lives: `:core:designsystem`, and an entry in
     `docs/working/foundation-upstream-candidates.md`, since § 5.1 describes the scale without shipping it.
   - Scope: spacing only. Component sizes (heights, icon sizes), strokes stay literal; corner radii belong to [SH](sh-shape-scale.md).
     The approved shape records the line between them.
   - The gate: a detekt or ktlint rule that fails on a dp literal in a spacing call outside the token
     file.
   - The code-style rule: "`.dp` values stay inline" becomes "spacing reads a token; sizes stay inline".
3. Record the outcome in `## Approved shape`. Each sweep sub-step adds its snapped values to
   `visual-checks.md`.

## Approved shape

Not yet approved.
