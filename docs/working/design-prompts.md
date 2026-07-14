# Softcover — Claude Design prompt pack

For the **Claude Design** beta client. Two things to set up, then one chat per screen.

1. **Once:** create a Softcover **design system** (the "Design systems" tab) using Part 1 below. It carries the brand — palette, fonts, conventions — into every generation.
2. **Per screen:** open a new composer, pick the Softcover design system from the **Design system** dropdown, set **Template** to *Prototype* (or *Wireframe* to explore structure first), attach screenshots of the current screen with the **`+`**, and paste **the preamble (Part 2)** followed by **one screen brief (Part 3)**.

One screen per chat. A second screen in the same chat inherits the first one's compromises.

The framing is deliberate. The design system describes *what we have today* — a starting point, not a spec. The preamble is what gives Claude explicit licence to depart from it, which is the whole point: this is a redesign, and the current build is the thing being improved on.

> **Status.** The original sixteen screen briefs (Library, Explore, Reading, Book detail, Profile,
> Settings + its sub-screens, Focus mode, Onboarding, the modal-sheet pattern, Barcode scanner,
> Desktop, the add-to-list/change-edition sheets) have all been executed and their resulting spec
> sheets implemented — they were deleted from Part 3 rather than kept as stale descriptions of a
> UI that no longer exists. The pack stays alive for the next redesign: write a fresh brief with
> the Part 3 template whenever a screen earns another pass.

---

## Part 1 — the design system (fill in once)

The Design-system creation form has these fields.

**Company name and blurb**
```
Softcover — an editorial book-tracking app for iOS, Android and desktop (a third-party client for Hardcover.app). Kotlin Multiplatform / Compose Multiplatform. The design language borrows from print magazines and book covers, not from utility dashboards: type does the heavy lifting, chrome retreats, and the reader is the protagonist.
```

**Link code from your computer** → drag in `core/designsystem`. That's the whole brand layer: theme, color roles, the editorial type scale, the shared component catalog. If it accepts a second folder, add `feature/book_detail` as the reference for how a real screen composes those pieces. (It's Compose/Kotlin, not web — Claude Design reads it as design intent, not as something to reuse.)

**Add fonts, logos and assets** → drag in `core/designsystem/src/commonMain/composeResources/font` — Fraunces and Inter in every shipped weight. This is the highest-value field on the form: it means mockups come back in the real typefaces rather than a substitute serif, and the two-family voice is most of the brand. Add `composeResources/drawable` too if you want the icon set and illustrations.

**Link code from GitHub** → skip (the local folder covers it). **Upload a .fig file** → skip (there's no Figma source).

**Any other notes**
```
Color is a warm book-paper palette with a terracotta accent (Material 3 roles).
Light: bg/surface #FFF8F6 · text #231917 · primary #8F4C38 · on-primary #FFFFFF · demoted #53433F · containers #FCEAE5/#F7E4E0/#F1DFDA · sheets #FFFFFF · hairline #D8C2BC · selected chips #FFDBD1 on #5D4037.
Dark: bg/surface #1A110F · text #F1DFDA · primary #FFB5A0 · on-primary #561F0F · demoted #D8C2BC · containers #271D1B/#322825/#3D322F · sheets #140C0A · hairline #53433F.
Rating stars are gold #FBBF23 in both themes, and nothing else uses that gold.

Primary is an accent (eyebrows, key stats, progress, active toggles, filled buttons, accent bars), never a background — the one exception is a single primary-filled hero stat card per screen. Elevation is expressed by tone (container shades), never by drop shadow; the only shadow in the app sits under a book cover so it reads as a physical object. Imagery-led cards keep a neutral container shade so the artwork carries the colour.

Fraunces (serif, true italics) carries the editorial voice: page titles, display headlines, body prose, hero stats, and a decorative quote glyph. Headlines and body prose are ITALIC; page titles are roman; stats use tabular figures. Inter carries the chrome voice: ALL-CAPS eyebrows with wide tracking, card and list titles, buttons, nav labels — never italic.
Roles: eyebrow · pageTitle (~30sp roman) · display (italic section headline) · body (italic) · statHero (72sp italic, autosized) · quoteGlyph (a “ at ~120sp, very low alpha).

Signature pattern — the section rhythm: every content region opens with a 32×4dp primary accent bar in the gutter → an ALL-CAPS primary eyebrow → an italic display headline → the body (carousel, list, grid or block), then a generous 40–48dp gap before the next section. A 20×1dp hairline bar is the variant for an eyebrow living inside a card.

Layout: 24dp page gutters; spacing rhythm 4/8 · 12/16 · 20/24 · 28/32 · 40/48; small radii on cards; covers strictly 2:3. Book collections are horizontal carousels of fixed-width cards, and the partially-clipped rightmost card is the only "there's more" cue (no scrollbars, chevrons, fades or page dots). Progress is always a WAVY (sine-wave) indicator, never a flat bar. Icons are Material outline, never mixed with filled.
Empty states are editorial flourishes, not errors: an oversized low-alpha “ glyph, an italic encouraging headline, one line of prose, at most one pill button. No sad illustrations, no big centred icons.

This is the system as it exists today, not a spec to obey. I'm actively trying to improve the app — depart from any of it where a departure makes the product better, and say what you changed and why. A generic result is the only unacceptable one.
```

---

## Part 2 — the preamble (paste above every screen brief)

```
You are redesigning a screen for Softcover, using the attached design system. Design for a 393×852 phone screen unless the brief says otherwise.

BEFORE YOU DESIGN ANYTHING — ASK FIRST
Every time I paste this, in every new chat, begin by asking me your setup questions and then STOP and wait for my answers. At minimum ask: how many alternatives I want you to explore, whether this is a single static pass or an iterative back-and-forth, wireframe vs. prototype fidelity, and anything in the brief ambiguous enough that you'd otherwise have to guess. Ask these EVERY time, even when it feels obvious or we settled the same questions in an earlier chat — never assume a previous chat's answers carry over, and never skip straight to planning or designing. Only start once I've answered (if I say "your call" on something, take it from there).

This is a REDESIGN, not an implementation spec. The design system describes decisions we already made — it's there so you understand the product and don't accidentally regress it, NOT so you reproduce it. I am actively trying to make this app better, so:

- Treat the current system as a strong starting point with no hard boundaries.
- If a specific decision — a layout, a component, an ornament, a piece of copy, an information hierarchy — is holding the screen back, CHANGE IT. A short "what I kept / what I changed and why" note at the end is worth more to me than obedience.
- Push on what's weak. Fill in what's missing. If a screen is doing too much, take something away.
- The one thing I don't want is a generic result: if your redesign could belong to any app, it's wrong.

WHAT TO PROTECT
The EDITORIAL register — print magazines and book covers, never utility dashboards or SaaS admin panels. Surfaces read like spreads. Type does the heavy lifting; chrome retreats. The reader is the protagonist: their books, progress and stats are foregrounded, app affordances get out of the way. Decoration is sparing and always serves a hierarchy decision — no gradients, no glassmorphism, no cards-inside-cards, no dashboard widgets, no icon-and-label grids.
The mood is warm paper and terracotta ink, not cool tech. Reading is a slow, physical, personal pleasure and the app should feel like it respects that.
The palette and the two-family type split are the strongest things we have. Keep the idea; the specific role assignments are fair game. If a bolder accent or a richer set of tones would serve the editorial mood better, propose it.

DELIVERABLE
A polished, pixel-accurate design of the screen. Use real, plausible book titles, authors and numbers — never lorem ipsum. Finish with the "what I kept / what I changed and why" note.
```

---

## Part 3 — screen briefs

All previously-authored briefs have been executed and removed (see the status note at the top).
When a screen earns a new pass, write its brief here using the template below, then paste it
under the preamble in a fresh chat.

### Brief template

Each brief describes the screen *as it exists* and then names where it's weak — the honest
critique is what steers the redesign, so don't soften it.

```
SCREEN: {Name} — {one-line role of the screen}. Today:
- {Bullet-by-bullet walkthrough of the current surface, top to bottom: chrome, sections,
  components, copy. Describe what is actually rendered — real labels, real layouts — so the
  design tool can't regress behavior it doesn't know exists.}
- {Include secondary states worth designing in the same pass: empty state, loading, edit mode,
  bulk-select, desktop/tablet variant — each with its current anatomy.}

WHERE IT'S WEAK: {The honest critique. What's flat, bloated, generic, or working against the
"reader is the protagonist" principle. Name the specific decisions the designer is licensed to
overturn, and say which parts you most want reimagined. End with the degree of liberty granted —
e.g. "you may restructure the controls entirely" or "keep the anatomy, fix the weight".}
```

Writing tips that paid off across the first sixteen briefs:

- **Describe the build, not the intention.** The walkthrough should read like a screen-reader
  transcript of the current app, including copy verbatim ("48 titles · 14,220 pages"), not like a
  PRD. The tool designs against what it can see.
- **Bundle sibling states into one brief** (a screen + its empty state + its selection mode)
  rather than opening more chats — they need to share one visual answer.
- **Grant liberty explicitly.** The briefs that produced the best results said exactly where the
  designer could break things ("the blurred-cover backdrop is a cliché we'd happily lose").
- **One screen per chat**, always — and attach current screenshots (light + dark).

---

## Follow-ups worth asking for in the same chat

- "Now the dark-mode variant of exactly this layout." (Always ask for light first, then this — you get one layout in two themes rather than two different designs.)
- "Now the empty state and the loading (shimmer) state."
- "Now the 600–840dp tablet width."
- "What did you deliberately reject from our current system, and what would it cost us to keep it?" — usually where the actual product insight shows up, more than in the mockup.
- "Pull the reusable pieces out as a component sheet: section header, book card, chip, progress indicator, hero stat, empty state."

## Notes

- **Attach screenshots.** A design tool reasoning from your real pixels gives far sharper judgement than one reasoning from a prose description. Light and dark, plus the empty state where it's interesting. Two or three editorial reference images (a magazine spread, a book cover, a print layout you admire) close the gap between stating the direction and hitting it.
- **If a generated prototype's type looks wrong,** the webfont probably didn't load. Ask for a fallback stack — `Fraunces, "Iowan Old Style", Georgia, serif` and `Inter, -apple-system, system-ui, sans-serif` — so a blocked font degrades into something still recognisably editorial rather than Times New Roman.
- **Implementing a resulting spec sheet:** the `/redesign <screen>` skill in this repo fetches the
  spec sheet from the "# Softcover redesigns" claude.ai project and drives the implementation
  through the rhaydus-logic / rhaydus-ui / code-reviewer / unit-test-writer pipeline.
