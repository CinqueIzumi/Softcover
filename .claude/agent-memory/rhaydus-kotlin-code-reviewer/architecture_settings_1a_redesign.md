---
name: architecture_settings_1a_redesign
description: Settings 1a redesign (mobile menu rows + update highlight card) added two design-system.md §4 entries; SettingsGroup/SettingsRowDivider retained for control lists only
metadata:
  type: project
---

The Settings 1a redesign (render-only; `AppUpdateState` model deliberately untouched — the update
card is intentionally version-less/percentage-less copy, not a defect) touched:
`feature/settings/src/mobileMain/.../SettingsScreenLayout.mobile.kt` (new `SettingsMenuRow`,
replacing the old boxed `SettingsRow`/`SettingsGroup` for navigational rows) and
`feature/settings/src/commonMain/.../SettingsShelf.kt` (restyled `AppUpdateSection` to a
`primaryContainer` highlight card + new `UpdatePillButton`, restyled `VersionFooter` to a
hairline-sandwich footer). `docs/reference/design-system.md` §4 gained two entries: "Settings menu
row" and "Update highlight card".

`SettingsGroup` / `SettingsRowDivider` (in `SettingsShelf.kt`) were deliberately **kept** — they
still back the reorderable Library-tabs list (`ReorderableTabsGroup`), which mixes switches and a
drag handle and isn't pure navigation. The boxed-card anatomy is retired only for **navigational**
menu rows, not for grouped **control** lists. See [[architecture_toad_init_side_effects]] for the
unrelated TOAD init convention noted from an earlier library-screen redesign pass.

One gap found on review: `SettingsMenuRow`'s gloss `Text` has no `maxLines = 1` /
`TextOverflow.Ellipsis` even though the new design-system.md entry explicitly documents it as a
"one-line ... gloss" — the longest current gloss string ("Books you've asked us to stop
recommending", 44 chars) is a wrap risk at larger `UiScale` settings or on narrow phones, which
would break row-height uniformity across the hairline list. Worth checking on any future settings-row
addition too.
