# PF — Premium-feel audit

**Stage:** PF — app-wide, read-only until PF-3. PF-7 runs before S12-1. **Delegation:** parallel
`general-purpose` agents per feature for PF-1 (the main conversation merges their findings), the user for
PF-2.

The app reads flat: state changes snap, presses give little feedback, and nothing responds to touch
beyond the ripple. PF catalogues every moment where motion, haptics or depth would make it feel
premium. It splits them into the ones that belong in the component contracts, which S11 builds in once,
and the surface polish for single screens. **Every finding stays local and lands in this branch.** None
becomes a GitHub issue. **PF-1 to PF-3, PF-5 and PF-6 change no UI.** Surface polish for single screens is
not applied. The copy swaps (PF-7) land before S12, so its budget, docs and walkthrough see the final UI once.

Much of the app's copy also reads as machine-written. PF-5 to PF-7 audit every user-facing string and
swap the ones that sound AI-generated for plain, natural text.

## Sub-steps

| Sub | Scope | Runs after | Status |
|---|---|---|---|
| PF-1 | Audit every screen and sheet, plus open suggestions for what's missing; write `premium-feel-audit.md` in this directory (shape below) | — | [x] |
| PF-2 | Walk the audit with the user: approve, cut or re-tier each finding | PF-1 | [x] |
| PF-3 | Write the approved contract-level findings into S11 as new sub-steps | PF-2 | [x] |
| PF-5 | Audit every user-facing string for AI-sounding copy; write `copy-audit.md` in this directory | — | [x] |
| PF-6 | Walk the copy audit with the user: approve, cut or reword each proposed replacement | PF-5 | [x] |
| PF-7 | Apply the approved replacements | PF-6 | [ ] |

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
- **Open suggestions:** anything else that would make the app feel more complete or premium, beyond
  the categories above: missing features, states or affordances a polished reading app would have,
  rough edges, and additions worth making. These go in their own section of the audit, tiered like the
  rest.

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

Each approved contract-level finding becomes an S11 sub-step (S11-4 onward), with its scope.

## PF-5 — Copy audit

**Sweep:** every user-facing string, wherever it lives: `compose.resources` strings, literals in mappers,
collectors and composables, empty states, errors, snackbars, sheet copy, onboarding, settings, loading
and refresh copy, and content descriptions. Parallel `general-purpose` agents per feature; the main
conversation merges.

**Flag copy that reads as AI-generated:** em-dash asides and "X — Y" constructions, tidy triplets,
whimsical or literary flourishes ("the page is still open", "your story starts here"), forced metaphors,
cheerleading and exclamation, filler ("simply", "seamlessly", "effortlessly"), over-explaining, and
headings or labels that are cute rather than clear.

**Each finding records:** where it appears (in user terms, then `file:line`), the current text, why it
reads as AI, and one plain replacement in the app's voice — short, concrete, and how a person would
say it. Text that already reads naturally is left out.

**Output:** `copy-audit.md`, grouped by screen.

## PF-6 — Copy triage

Present `copy-audit.md` screen by screen. The user approves, cuts or rewrites each replacement. Record the
verdict on each row.

## PF-7 — Apply the copy

Before S12, one `softcover-implementer` brief per module group applies the
approved replacements, then the tests that assert on copy are updated through `softcover-test-writer`.
