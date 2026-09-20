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
  component, or pattern in the design system MUST update the relevant section file under
  `docs/reference/design-system/` in the same change. `softcover-reviewer` treats a missing update as a
  blocker. This covers a new shared component, a new editorial typography role, a new color role usage, a
  new layout pattern other screens should adopt, and retiring or renaming any of these. A localized tweak to
  one screen that does not change the system needs no update.
- `:core:presentation` is not a component home; shared components go in `:core:component`.
- Check `docs/rhaydus/0.3.1/CAPABILITIES.md` before hand-rolling a component, modifier or util.
