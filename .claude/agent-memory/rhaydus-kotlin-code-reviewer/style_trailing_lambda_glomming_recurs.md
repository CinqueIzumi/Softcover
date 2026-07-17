---
name: style_trailing_lambda_glomming_recurs
description: The Softcover-delta "trailing lambda multi-line body must not glom `) }`" rule (docs/reference/code-style.md) is NOT ktlint-enforced — keep checking new files by eye.
metadata:
  type: feedback
---

`docs/reference/code-style.md` documents a Softcover-specific tightening of the foundation's trailing-
lambda exemption: when a trailing lambda's body is a multi-line construct, the lambda's `{`/`}` must
each be on their own line — never `) }` glommed. The doc says explicitly this is **not yet tool-
enforced** (ktlint's multi-arg wrapping rule exempts trailing-lambda calls and even produces the
glommed form itself), so it is review-only.

**Why this matters:** found a fresh instance in the Profile 1a-redesign Phase 0+1 pass
(`core/profile/.../ProfileRemoteDataSource.kt`, `val monthWindows = (1..MONTHS_IN_YEAR).map { month ->
monthWindow(\n    currentYear,\n    month,\n) }`) — `ktlintCheck` passed clean on it (confirmed by
running the task), so this is exactly the kind of violation that only a manual read catches. A known
pre-existing instance also lives in `feature/profile/.../UserInformationCollector.kt`
(`scope.setState { it.copy(\n    userProfileData = profileData,\n    isLoading = false,\n) }`) — out of
scope unless that file is itself touched (on-touch policy), but a useful worked example to recognize
the pattern.

**How to apply:** in every reviewed file, explicitly scan trailing-lambda calls (`.map {`, `.let {`,
`scope.setState {`, `.also {`, etc.) whose body spans multiple lines — grep for `) }` glommed patterns
literally, since `ktlintCheck`/`ktlintFormat` will not surface them.
