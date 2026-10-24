---
paths:
  - "foundation/**"
---

# Working in the foundation submodule

- Work on a branch inside `foundation/`, never on a detached HEAD.
- Changes are generic and name no consuming app (`foundation/CLAUDE.md`).
- Softcover's agents do the work there (`softcover-implementer` / `-reviewer` / `-test-writer`), not the
  foundation CLAUDE.md's `code-reviewer` / `unit-test-writer`.
- Build with `foundation/gradlew -p foundation <task>`; the Gradle hook rewrites a bare `./gradlew` to
  Softcover's root.
- Commit in the foundation first, then record the pointer in Softcover with `git add foundation`.
- Push the foundation commit before any Softcover commit that points at it (`git push
  --recurse-submodules=check` refuses otherwise).
- Softcover merges to `main` only with the pointer on a tagged foundation commit; pushing a `v*` tag publishes
  to Maven Central.
- Ask before committing in either repo.
- The `rhaydus-kotlin` plugin loads in place from `foundation/` through the project-scoped `rhaydus` marketplace
  in `.claude/settings.json`, registered once per machine (`foundation/claude/plugins/rhaydus-kotlin/README.md`
  § Install); an edit to its agents, hooks or skills applies after `/reload-plugins` or in the
  next session, with no version bump.
