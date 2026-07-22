---
name: architecture_appearance_1a_redesign
description: Appearance settings section rebuilt from boxed ToggleCard/SelectionIndicator cards to flat hairline-divided rows; design-system.md line 300 was left describing the removed SelectionIndicator anatomy — a real doc-sync blocker, not hypothetical.
metadata:
  type: project
---

Reviewed 2026-07-21 (uncommitted, `release/3.1.0`). `SettingsShelf.kt`'s `// region Appearance content`
was reworked: the three switches (dynamic colour / floating bar / reading streak) collapsed from
separate boxed `ToggleCard` sections into one flat `DisplaySection` toggle-row stack; the Date-notation
and desktop UI-scale pickers moved from a boxed `Surface` + circular `SelectionIndicator` radio anatomy
to a shared flat `SettingsSelectableRow` (primary-tinted label + trailing `SoftcoverIcon.Check`, no
circle). `DateStyle.label` values changed from `"DD/MM/YYYY"` etc. to prose (`"Day, month, year"`), and
`DateStyle` gained `fun format(date: LocalDate)`; today's formatted example per style is now precomputed
once into `SettingsScreenUiState.dateStyleExamples` at ScreenModel construction (same one-shot-in-
`initialState` pattern as `appVersionName`/`appVersionCode` — acceptable precedent, though it means the
example date goes stale if the screen is left open across midnight).

**Confirmed doc-sync blocker:** `docs/reference/design-system.md` line 300 (the "Desktop display scale"
entry) still says the Date-notation and UI-scale rows use "the standard selectable-row +
`SelectionIndicator` anatomy" — but `SelectionIndicator` was deleted in this same diff. This is the
canonical case CLAUDE.md's maintenance rule exists for (a component retired without a same-change doc
update) — flagged as a blocker in review, not fixed (review-only task).

**Also confirmed a third instance of the one-type-per-file colocated-data-class pattern** — see
[[style_one_type_per_file_colocated_support_class]] (`ToggleRowSpec` in the same file).

This continues the direction set by [[architecture_settings_1a_redesign]] (flat rows over boxed
`SettingsGroup` cards) — `SettingsGroup`/`SettingsRowDivider` remain correctly scoped to the
Library-tabs control list only; Appearance's flat rows intentionally use a bare
`HorizontalDivider(color = outlineVariant)` with no horizontal inset (correct for edge-to-edge flat rows
vs. the inset divider inside a boxed card), just hand-repeated 3x instead of extracted to a shared
private composable (nit, not a blocker).
