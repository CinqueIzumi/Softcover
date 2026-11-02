---
paths:
  - "foundation/**"
---

# Working in the foundation submodule

- Work on a branch inside `foundation/`, never on a detached HEAD.
- Changes are generic and name no consuming app (`foundation/CLAUDE.md`).
- The `rhaydus-kotlin` plugin agents do the work there.
- Build with `foundation/gradlew -p foundation <task>`; the Gradle hook rewrites a bare `./gradlew` to
  Softcover's root.
- Commit in the foundation first, then record the pointer in Softcover with `git add foundation`.
- Push the foundation commit before any Softcover commit that points at it (`git push
  --recurse-submodules=check` refuses otherwise).
- Softcover merges to `main` only with the pointer on a tagged foundation commit.
- Ask before committing in either repo.
- The `rhaydus-kotlin` plugin installs from `foundation/` through the project-scoped `rhaydus` marketplace in
  `.claude/settings.json` (`foundation/claude/plugins/rhaydus-kotlin/README.md` § Install). Claude Code runs a
  copy cached per plugin version, so an edit to its agents, hooks or skills applies only after
  `claude plugin uninstall rhaydus-kotlin@rhaydus --scope project`, the matching `install`, and
  `/reload-plugins`; `claude plugin update` is a no-op while the version is unchanged.
