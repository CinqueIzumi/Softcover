---
paths:
  - "showcase/**"
---

# Showcase render runner

- `showcase/src/test` holds the render runner: a generator that draws every `GalleryRegistry` fixture and
  every screen showcase entry to PNG for the design preset. It asserts nothing, so it is not a test, and it
  is written by `softcover-implementer`, not `softcover-test-writer`.
- Robolectric and Roborazzi are allowed here and nowhere else. No other module takes them, in any source set.
- The runner runs only when its Gradle property is set. It never runs in `check`, `test` or
  `koverXmlReport`.
- A unit test of showcase logic follows `.claude/rules/tests.md` and goes to `softcover-test-writer`.
