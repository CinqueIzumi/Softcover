# Design preset

Softcover's design system as a Claude Design System artifact: tokens, fonts, icons, a brand book and every
`:core:component` component, so Claude can design new Softcover UI from the real system.

## What lives where

- `design/project/README.md` — the brand book, hand-written. Usage rules that name tokens.
- `design/project/components/` — `bundle.css`, `bundle.js` (an empty `window.Softcover`: the page runs no
  preview without one), the hand-written components (`<Comp>/preview.html` +
  `README.md`) and the system cover (`Cover/preview.html`).
- `screens/` is not in `design/project`: the bundle copies the screen pages from
  `docs/reference/design-system/patterns/` (`settings.md`, `explore-profile.md`, `book-detail.md`, `library.md`,
  `reading.md`, `widgets.md`). Edit them there, never in the bundle.
- Everything else is generated, never committed: tokens (`tokens.json`, `components/softcover-tokens.css`), fonts
  and vector assets from `:core:designsystem:exportDesignTokens`; PNG renders of every `GalleryRegistry` fixture from
  `:showcase:renderShowcase`; a preview and README for each component without a hand-written folder.

## Changing it

- Edit `design/project/**`. Cite tokens by their `tokens.json` name in backticks and never hard-code a colour:
  `./gradlew :core:designsystem:checkDesignPresetContent` fails on either.
- A new component needs only its `GalleryRegistry` entry; it then ships as rendered images. Hand-write a folder
  only for a component designers reach for constantly.
- Build the bundle with `./gradlew designPresetBundle` (output in `build/design-preset/project/`).

## Publishing

The code is the source of truth; the artifact is its generated view. The project has one shared artifact, its url
committed as `artifactUrl` in `design/design-system.properties`. After a merge that changes tokens, components,
gallery fixtures or `design/project/**`, run `/publish-design-system` in Claude Code to update it in place. Edits
made on the page are overwritten by the next publish. Edit access is granted from the artifact's Share menu.
