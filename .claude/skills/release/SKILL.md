---
name: release
description: Prepare a release — refresh the README, run all checks, set the new versionName across all platforms (Android/iOS/desktop), bump the build number, and bring the public roadmap up to date. Stops with changes unstaged; never commits or tags.
---

Orchestrate a release-prep pass by composing the existing skills. Never commit or tag;
leave all changes unstaged for the user to review.

Steps:

1. Invoke the `update-readme` skill via the Skill tool to bring the README current with the latest features.
2. Invoke the `style-check` skill via the Skill tool to auto-fix style and run the full gate. If the gate is not green, **stop and report** — do not bump the version on a red build.
3. Read the current `versionCode` and `versionName` from `app/build.gradle.kts` and show them to the user. Ask for the new `versionName` (suggest the next semantic version). If the user provided one as an argument, use that.
4. Invoke the `set-version-name` skill via the Skill tool with the new version name. It sets the version name across all platforms (Android `versionName`, iOS `MARKETING_VERSION`, desktop `packageVersion`) and bumps the build number together — do not edit any of those fields yourself.
5. **Close the shipped milestone.** If a GitHub milestone matches the version being released, ask the user to confirm, then close it (`gh api -X PATCH repos/{owner}/{repo}/milestones/{n} -f state=closed`). This is the one live GitHub write in an otherwise local skill, so confirm first; it is trivially reversible by reopening.

   **Why it belongs here:** the public roadmap is built from *open* milestones, so leaving it open makes `ROADMAP.md` name the new version as the current release **and** list it as a future release in the same file. Skipping this ships a self-contradictory roadmap to the in-app Roadmap screen.

6. **Update the "what shipped" sentence in `scripts/roadmap/header.md`.** This is the only line in the public roadmap that no machine can derive, and release time is the only moment it can be written.
   - Read the commits since the previous tag (`git log --no-merges --format=%s $(git describe --tags --abbrev=0)..HEAD`) and summarise the release in the public, editorial voice already used in that file — what a reader gets, not what changed internally.
   - **Lead with the new version and prune older detail** rather than letting the sentence accumulate every patch forever. One or two sentences total.
   - Leave `{{CURRENT_VERSION}}` alone — it is substituted from Gradle at generation time.

7. **Regenerate the public roadmap:** `python3 scripts/roadmap/generate_roadmap.py --write`.

   **This is mandatory, not optional.** `ROADMAP.md` is a generated file, and the Roadmap workflow runs `--check` on any pull request touching `ROADMAP.md` or `scripts/roadmap/**`. Editing `header.md` in step 6 without regenerating leaves the file stale and **fails CI on the release PR**. Verify with `--check` before reporting.

8. Report a consolidated summary: README changes, the check result, the version transition (e.g. `versionName / MARKETING_VERSION / packageVersion: 2.4.0 → 2.5.0, versionCode / CURRENT_PROJECT_VERSION: 29 → 30`), which milestone was closed, and the new "what shipped" sentence.

Do not create a commit or tag. Do not write Google Play release notes. Never hand-edit
`ROADMAP.md` — it is generated; change `header.md` or a milestone description and regenerate.
Leave all changes unstaged in the working tree.
