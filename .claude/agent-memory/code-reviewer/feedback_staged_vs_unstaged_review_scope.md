---
name: feedback_staged_vs_unstaged_review_scope
description: how to interpret "review the uncommitted working-tree changes, HEAD already had a large staged redesign" review requests in this repo
type: feedback
---

When asked to review "uncommitted working-tree changes" with a caveat like "HEAD already had a
large staged redesign, focus only on the unstaged/working changes described below" — do not take
"unstaged" literally as `git diff` (working tree vs index) only. In this repo the actual intent was:
review the full set of changes described in the prose (which may span both staged and unstaged
files, since the user works by `git add`-ing a feature as they go and then continues editing some
of those same files further). "HEAD already had a large staged redesign" referred to unrelated
already-committed work visible in `git log` (e.g. prior settings/library redesign commits), not to
the index.

**Why:** a first pass using `git diff --name-status` (unstaged only) surfaced just 2-3 files, but
the task's own 3-part description clearly covered ~49 files worth of change (mapper, GraphQL,
domain models, dead-code removal, docs). Cross-checking `git status --porcelain` staged-vs-unstaged
column against the task description resolved the ambiguity.

**How to apply:** when a review request's file scope doesn't match what `git diff` (unstaged-only)
shows, compare against `git diff HEAD` (everything uncommitted) and reconcile against the prose
description before narrowing scope. Where a specific composable/type is said to have been "further
edited" or "replaced" within a broader staged feature, use plain `git diff -- <file>` (unstaged vs
index) on JUST that file to see the actual delta being asked about (e.g. old pill-row → new
genre-ranking composables lived entirely in the unstaged layer of `ShareCard.kt`, while surrounding
context like `ReadingLifeRidgeline`/`MiniScallopPortrait` was already staged).
