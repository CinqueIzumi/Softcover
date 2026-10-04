# SH — Shape scale

**Stage:** SH — app-wide, after S10 (the shelves are split, so the sweep touches their final files) and
before S11; independent of SP, either order. **Delegation:** user (SH-1), `softcover-implementer` per
module group (SH-2 onward).

Corner radii are written as raw dp at every call site, so the same card or tile corner drifts from screen
to screen. At planning, main source sets held 65 `RoundedCornerShape(…)` calls across 17 radii (2, 3, 4, 6,
7, 8, 10, 11, 12, 14, 15, 16, 18, 20, 22, 24, 28 dp), 4 stray `RoundedCornerShape(percent = 50)` beside
`PillShape`, 24 `CircleShape` and 2 reads of `MaterialTheme.shapes`. SH introduces one named shape scale, so
every corner reads a token instead of a number. Off-scale radii snap to the nearest step as accepted
shifts. The scale follows `design-system-foundations.md` § 4 (small radii on cards and tiles, medium on
buttons, fully rounded for pills and active toggles) and app `foundations.md` § 2.3.

## Sub-steps

| Sub | Scope | Status |
|---|---|---|
| SH-1 | Inventory and approved shape (below) — stop for approval | [ ] |
| SH-2 | The token object, its KDoc, the theme wiring, the code-style rule, and the gate | [ ] |
| SH-3 | Sweep `:core:component` and `:core:designsystem` | [ ] |
| SH-4 | Sweep the feature modules, one brief per module group | [ ] |
| SH-C | Convergence pass (family-procedure.md § Phase 3): the surfaces whose corners should now be one step | [ ] |

## SH-1 — inventory and decisions

1. **Inventory.** Tally every `RoundedCornerShape`, `CircleShape`, `CutCornerShape`, `clip(…)`,
   `border(…, shape)` and `shape =` argument, by radius, surface role (card, tile, sheet, dialog, chip,
   button, cover, bar, field) and module. Flag one-off radii and the screens that use them. Include radii
   held in metrics objects (`CoverDimensions.cornerRadius` and the like).
2. **Decide with the user:**
   - Token names and steps, for example `Shapes.xs` … `Shapes.xl` plus `Shapes.pill`, or role names
     (`card`, `tile`, `sheet`). Decide whether partial-corner shapes (top-only sheets, one-sided bars) get
     tokens or derive from a step.
   - Whether the scale populates Material's `Shapes` in `SoftcoverTheme`, so Material components pick it
     up, and where the object lives: `:core:designsystem`, with an entry in
     `docs/working/foundation-upstream-candidates.md`, since § 4 leaves the radii app-tunable.
   - Where `PillShape` and `CircleShape` sit in the scale, and whether cover radii join it or stay a
     cover-specific value.
   - The gate: a detekt or ktlint rule that fails on a dp literal in `RoundedCornerShape` outside the token
     file.
   - The code-style rule: corners read a token; sizes stay inline.
3. Record the outcome in `## Approved shape`. Each sweep sub-step adds its snapped values to
   `visual-checks.md`.

## Approved shape

Not yet approved.
