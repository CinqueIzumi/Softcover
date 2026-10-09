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
| SP-1 | Inventory and approved shape (below) — stop for approval | [x] |
| SP-2 | The token object, its KDoc, the code-style rule, and the gate | [x] |
| SP-3 | Sweep `:core:component` and `:core:designsystem` | [x] |
| SP-4 | Sweep the feature modules, one brief per module group | [x] |
| SP-5 | Every file-level `private val X = N.dp` (`git grep -l "^private val .*\.dp$" -- '*.kt'`; 30 files, 66 vals at planning) and `BarSpec`-style dp holders becomes a token (spacing) or inline (sizes); done when the grep is empty | [x] |
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

- Inventory (SP-1): 852 spacing-call dp literals (Spacer 431, padding 320, spacedBy 85, PaddingValues 10,
  offset 6); 228 off-grid, mostly the 6 / 10 / 14 / 18 midpoints.
- Scale: the full 4dp grid plus a 2dp hairline, `object Spacing` in `:core:designsystem`
  `presentation/theme/Spacing.kt`: `xxxs` 2, `xxs` 4, `xs` 8, `sm` 12, `md` 16, `lg` 20, `xl` 24, `xxl` 28,
  `xxxl` 32, `x4l` 40, `x5l` 48. Entry in `foundation-upstream-candidates.md`.
- Snapping: nearest step; exact ties round down for inner padding (`padding`, `PaddingValues`,
  `contentPadding`) and up for gaps (`spacedBy`, `Spacer`, `offset`). 1 and 3 snap to `xxxs`; 36 to `x4l`.
  `0.dp` and the one-off 64 / 96 stay literal.
- Spacing means a value passed to `padding`, `PaddingValues`, `contentPadding`, `spacedBy`, `offset`, or a
  `Spacer`'s `height` / `width` / `size`. Component sizes, strokes and borders stay literal; radii are SH.
- Gate: new app-local `:detekt-rules` module with one `RawSpacingDp` rule, wired into `detektPlugins` beside
  the foundation ruleset; fails on a dp literal in those calls outside `Spacing.kt`, allowing 0 / 64 / 96.
  Also an upstream candidate. Active via `config/detekt/softcover-rules.yml`, a config layer the root build
  appends for every module but `:detekt-rules` (which cannot load its own plugin). Drop the stale `:ktlint-rules` reference in the root `build.gradle.kts`.
- Code style: "`.dp` values stay inline" becomes "spacing reads a `Spacing` token; sizes stay inline"; the
  inline-dp memory is updated in SP-2.
