# PF — Premium-feel audit

**Stage:** PF — app-wide, read-only until PF-3. Runs any session before S11; earlier is better, so S7–S9
can take contract-level findings while their components are being cut. **Delegation:** parallel
`general-purpose` agents per feature for PF-1 (the main conversation merges their findings), the user for
PF-2.

The app reads flat: state changes snap, presses give little feedback, and nothing responds to touch
beyond the ripple. PF catalogues every moment where motion, haptics or depth would make it feel
premium. It splits them into the ones that belong in the component contracts, which S11 builds in once,
and the surface polish that ships after this PR as its own issue. **PF changes no UI.** The migration
keeps today's UI unchanged so the S12 walkthrough against `main` stays a clean comparison.

## Sub-steps

| Sub | Scope | Runs after | Status |
|---|---|---|---|
| PF-1 | Audit every screen and sheet; write `premium-feel-audit.md` in this directory (shape below) | — | [ ] |
| PF-2 | Walk the audit with the user: approve, cut or re-tier each finding | PF-1 | [ ] |
| PF-3 | Write the approved contract-level findings into `s11-contract-retrofit.md` as new S11 sub-steps | PF-2 | [ ] |
| PF-4 | File the approved surface-level findings as a new GitHub issue (ask the user first), linked from #275 | PF-2 | [ ] |

## PF-1 — Audit

**Read first:** `docs/rhaydus/0.3.1/CAPABILITIES.md` and `design-system-foundations.md` for the motion,
press and haptic support the foundation already has, then what the app already does: `MotionDebugScreen`,
`MarkAsReadBurst`, `bookCoverTransitionKey`, the animated sheet closes from S6, `SkeletonCrossfade`. A
finding builds on these; it never proposes a second system next to one.

**Sweep, per screen and per sheet:** library (grid, list, selection mode), reading, explore, book
detail, profile and statistics, settings, every bottom sheet, the nav shells, search, the barcode scanner,
and the share flow. Look for:

- **State changes that snap:** content landing after loading, list insert / remove / reorder, tab and
  filter switches, counters and progress values changing, toggles, expanding sections.
- **Press and gesture feedback:** cards, rows, chips, buttons, long-press into selection, swipe, drag,
  pull to refresh, overscroll.
- **Navigation continuity:** shared elements beyond the cover (title, rating), predictive back, sheet
  and pane transitions, the wide-layout detail pane.
- **Moments worth marking:** finishing a book, hitting a goal or streak, rating, adding to a list,
  scanning a barcode, saving a share card.
- **Depth:** elevation and shadow on lifted or dragged items, scroll-linked top bar and hero effects,
  parallax on covers, blur behind sheets where the platform supports it.
- **Haptics:** which of the moments above earn one, and at which strength. Keep them rare. A haptic on
  every tap is noise.

**Each finding records:** the moment (in user terms, then `file:line`), what happens today, the proposed
touch, its tier (**contract**: a press modifier, a haptics `expect`/`actual`, motion tokens, a
transition key on a UI model; **surface**: one screen's polish), its effort (S / M / L), and platform
notes. Android, iOS and desktop differ on haptics, and desktop has none. Predictive back is Android
only. Every proposal honours the system reduce-motion setting.

**Output:** `premium-feel-audit.md`, ranked by impact over effort, contract-level first. A finding names
the moment and the touch. It does not design the API; S11 does that.

## PF-2 — Triage

Present the audit grouped by tier. The user approves, cuts or re-tiers each finding. Record the verdict on
each row of `premium-feel-audit.md`.

## PF-3 — Into S11

Each approved contract-level finding becomes an S11 sub-step (S11-6 onward) in
`s11-contract-retrofit.md`, with its scope, and its row in the `## Sub-steps` table.

## PF-4 — Follow-up issue

The surface-level findings outlive this directory, which S12 deletes. File them as one `kind:polish` GitHub issue
per `.claude/rules/roadmap.md`, with the user's approval of its title, labels and milestone first. Link it
from #275. Delete `premium-feel-audit.md` once both PF-3 and PF-4 have landed.
