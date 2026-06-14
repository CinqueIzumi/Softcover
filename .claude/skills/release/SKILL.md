---
name: release
description: Prepare a release — refresh the README, run all checks, then set the new versionName across all platforms (Android/iOS/desktop) and bump the build number. Stops with changes unstaged; never commits or tags.
---

Orchestrate a release-prep pass by composing the existing skills. README only;
never commit or tag; leave all changes unstaged for the user to review.

Steps:

1. Invoke the `update-readme` skill via the Skill tool to bring the README current with the latest features.
2. Run `./gradlew createModuleGraph` to regenerate the embedded Mermaid module-dependency graph (the `### Module graph` section of the README) so it reflects the current module topology. This is distinct from the `checkModuleGraph` gate in step 3, which only *asserts* the tier DAG — `createModuleGraph` *redraws* the diagram.
3. Invoke the `style-check` skill via the Skill tool to auto-fix style and run the full gate. If the gate is not green, **stop and report** — do not bump the version on a red build.
4. Read the current `versionCode` and `versionName` from `app/build.gradle.kts` and show them to the user. Ask for the new `versionName` (suggest the next semantic version). If the user provided one as an argument, use that.
5. Invoke the `set-version-name` skill via the Skill tool with the new version name. It sets the version name across all platforms (Android `versionName`, iOS `MARKETING_VERSION`, desktop `packageVersion`) and bumps the build number together — do not edit any of those fields yourself.
6. Report a consolidated summary: README changes, the check result, and the version transition (e.g. `versionName / MARKETING_VERSION / packageVersion: 2.4.0 → 2.5.0, versionCode / CURRENT_PROJECT_VERSION: 29 → 30`).

Do not create a commit or tag. Do not touch `RELEASE_PLAN.md` or any Google Play release notes. Leave all changes unstaged in the working tree.
