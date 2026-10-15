---
paths:
  - "**/presentation/**"
  - "core/component/**"
  - "core/designsystem/**"
---

# Compose UI and the design system

- Before designing or modifying a UI surface, consult the foundation
  `docs/rhaydus/0.3.1/design-system-foundations.md` (theme plumbing, layout primitives, shared component
  catalog, editorial role contract) and Softcover's brand layer behind `docs/reference/design-system.md`
  (color roles, editorial typography, brand components, patterns, decision rules). Read only the section file
  under `docs/reference/design-system/` that you need.
- **Maintenance rule (enforced by review).** Any change that introduces, retires, or alters a foundation,
  component, or pattern in the design system MUST update its home in the same change, per
  `.claude/rules/docs.md`: a component's KDoc on its UI model plus its one line in `components.md`; a
  foundation or pattern in its section file under `docs/reference/design-system/`. `softcover-reviewer`
  treats a missing update as a blocker. A localized tweak to one screen that does not change the system
  needs no update.
- **Every user-facing string follows `docs/reference/design-system/voice.md`** (enforced by review). Read it
  before writing or changing any label, headline, gloss, caption, empty state, snackbar or dialog text.
- `:core:presentation` is not a component home; shared components go in `:core:component`.
- Check `docs/rhaydus/0.3.1/CAPABILITIES.md` before hand-rolling a component, modifier or util.
