# Design System
The visual and interaction language for Softcover. This document is purely descriptive — it does not point at code. When a screen is being designed or redesigned, a contributor should be able to read this and know how the surface should look and behave without consulting other screens.

> **Maintenance rule.** Any change that introduces, retires, or alters a foundation, component, or pattern in this system must update this file in the same change. If the doc and the code disagree, the code is wrong, the doc is wrong, or both — none of those is acceptable to merge.

---

## 1. Tone

The system is **editorial**: it borrows from print magazines and book covers rather than from utility dashboards. Surfaces feel like spreads. Type does heavy lifting; chrome is restrained. Italic display faces and a single primary accent carry character. Decoration is sparing — a thin accent bar, a quote glyph, a wavy progress indicator — and always serves a hierarchy decision, not ornament for its own sake.

The reader is the protagonist. Their books, progress, and stats are foregrounded; app affordances retreat.

> **Read only the section you need.** This doc is split so agents and contributors pull just the relevant part instead of the whole file. Grep this index for the component/role/pattern you're touching, then open that one section file.

## Sections

- [Foundations](design-system/foundations.md) — color roles, typography, shape & elevation, spacing, motion, iconography, window size & breakpoints (§2).
- [Layout primitives](design-system/layout.md) — page scaffold, section rhythm, hero region, carousels & cards, modal sheets, adaptive content width (§3).
- [Components](design-system/components.md) — the catalogue of shared components and which tool to reach for (§4).
- [Patterns](design-system/patterns.md) — recurring recipes that compose the primitives above (§5).
- [Decision rules](design-system/decision-rules.md) — the checklist to walk before reaching for novelty (§6).
- [Component contract](design-system/component-contract.md) — the UI-model contract every `:core:component` component obeys: signature, model rules (R1–R8), mapper placement, the build gates, and the Component Gallery (§7).
