# Design System
The visual and interaction language for Softcover, as deltas from the vendored [foundation design system](../rhaydus/0.3.1/design-system-foundations.md). Maintenance follows [`.claude/rules/docs.md`](../../.claude/rules/docs.md).

---

## 1. Tone

The system is **editorial**: it borrows from print magazines and book covers rather than from utility dashboards. Surfaces feel like spreads. Type does heavy lifting; chrome is restrained. Italic display faces and a single primary accent carry character. Decoration is sparing — a thin accent bar, a quote glyph, a wavy progress indicator — and always serves a hierarchy decision, not ornament for its own sake.

The reader is the protagonist. Their books, progress, and stats are foregrounded; app affordances retreat.

## Sections

- [Foundations](design-system/foundations.md) — color roles, typography, shape & elevation, spacing, motion, iconography, window size & breakpoints (§2).
- [Layout primitives](design-system/layout.md) — page scaffold, section rhythm, hero region, carousels & cards, modal sheets, adaptive content width (§3).
- [Components](design-system/components.md) — an index of shared components; each contract is KDoc on its UI model (§4).
- [Patterns](design-system/patterns.md) — an index of shared patterns, with the full catalogue split by surface under `design-system/patterns/` (§5).
- [Decision rules](design-system/decision-rules.md) — the checklist to walk before reaching for novelty (§6).
- [Component contract](design-system/component-contract.md) — the UI-model contract every `:core:component` component obeys: signature, model rules (R1–R12), mapper placement, the build gates, and the `:showcase` app (§7).
- [Voice and copy](design-system/voice.md) — the rules every user-facing string follows: voice, the tells to cut, and mechanics (§8).
