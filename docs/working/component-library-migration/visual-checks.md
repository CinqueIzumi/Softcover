# Visual checks — final UI pass (S12)

Accepted visual shifts to confirm on device and in the gallery. Append one line per step that lands a
shift; tick each during S12.

- [ ] The Arrange / Filter sheets no longer render empty or stale for a frame on open.
- [ ] The Arrange sheet's "Show titles" row does not pop in a frame after open (collector-only writer now).
- [ ] S5-6: 4dp eyebrow gap, 1-line titles, reading's mobile masthead line height.
- [ ] S5-C-headers: "Your tabs" tracking (2.2 → 1.2sp); `FeaturedCard`'s eyebrow gap (10 → 8dp).
- [ ] The whole badge family in the gallery and on device (never visually checked since S4-5b): SemiBold
  everywhere, TrackingTag pill → 6dp, Owned 14 → 12dp icon, cover selection mark 26 → 24dp, Expired on
  `surfaceContainerHigh`.
- [ ] S5-C-dividers: quote-glyph gap (14 → 12dp), the "Or type it" label on plain `eyebrowSmall`, the
  version footer on `eyebrowSmall`.
- [ ] S6-C-rows: settings rows (16dp vertical everywhere, 4dp title gap, top rule and press feedback on
  toggle / choice lists).
- [ ] S6-3: sheet header gaps (12/8/8), 56dp jackets, sheet padding (top 8 / bottom 16, LoadingSheet
  32 → 16), the progress sheet's tap-to-clear-focus covering the body only.
- [ ] S6-4: verdict cover as a 56dp header jacket (was above the header).
- [ ] S6-5: Filter footer inset (22 → 24), bulk-remove and re-auth as sheets, the animated close on
  Apply / Save / Delete / top action.
- [ ] S6-6: tag editor and edition jackets (56dp), the edition footer divider, ShareBook's eyebrow bar,
  the unknown-ISBN footer.
- [ ] S6-7: streak sheet insets, log-out no longer centred, the genre / Continue-series headers.
- [ ] S6-C-sheets: header trailing gap (16 → 12), verdict jacket radius 4, the TEXT dismiss label's right
  edge, ShareBook's TEXT Cancel, animated state-driven closes.
- [ ] SL-5: the hero title block while loading.
- [ ] S7-2/4/5/6/7: cards.
- [ ] SL-10b/11: loading.
- [ ] Desktop chip gap (24dp).
- [ ] S7-C2: bars. S7-C3: eyebrows, bylines. S7-C5: uncrossfaded swap.
- [ ] S8-1: quote glyph now on the offline screen and the desktop detail pane; Library and tag-editor glyph
  tint (`primary` → `onSurfaceVariant`) and type one step up; Reading's glyph at standard size and its
  "Find a book" pill as a `RhaydusButton`; tag editor gaps 6/4 → 8/12; Library's empty state top-aligned.
