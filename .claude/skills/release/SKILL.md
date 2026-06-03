---
name: release
description: Prepare a release — refresh the README, run all checks, then bump versionCode and set the new versionName. Stops with changes unstaged; never commits or tags.
---

Orchestrate a release-prep pass by composing the existing skills. README only;
never commit or tag; leave all changes unstaged for the user to review.

Steps:

1. Invoke the `update-readme` skill via the Skill tool to bring the README current with the latest features.
2. Invoke the `style-check` skill via the Skill tool to auto-fix style and run the full gate. If the gate is not green, **stop and report** — do not bump the version on a red build.
3. Read the current `versionCode` and `versionName` from `app/build.gradle.kts` and show them to the user. Ask for the new `versionName` (suggest the next semantic version). If the user provided one as an argument, use that.
4. Invoke the `set-version-name` skill via the Skill tool with the new version name. It sets `versionName` and bumps `versionCode` together — do not edit either field yourself.
5. Report a consolidated summary: README changes, the check result, and the version transition (e.g. `versionName: 2.4.0 → 2.5.0, versionCode: 29 → 30`).

Do not create a commit or tag. Do not touch `RELEASE_PLAN.md` or any Google Play release notes. Leave all changes unstaged in the working tree.
