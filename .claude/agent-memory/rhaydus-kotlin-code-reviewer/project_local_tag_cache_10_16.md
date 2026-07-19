---
name: project_local_tag_cache_10_16
description: Roadmap step 10.16 (local tag vocabulary cache + suggestions) review findings — a UserTag.count ambiguity bug and confirmed ktlintCheck gate failures.
metadata:
  type: project
---

Reviewed 2026-07-22, uncommitted on `release/3.1.0`. Two reusable lessons:

**1. `UserTag.count` is contextually overloaded — a real footgun.** `UserTag.count` means the tag's
*global site-wide popularity* when mapped from `SaveTagsMutation`/`FindTagsByUserAndTaggableQuery`
(`UserTagMapper.kt`), but the *user's personal usage frequency* when mapped from the new
`FindTagsByUserQuery` after client-side `groupBy` aggregation (`UserTagsRemoteDataSource.fetchUserVocabulary`).
The 10.16 diff's `RecordAppliedTagsUseCase` → `UserTagVocabularyRepositoryImpl.record()` →
`UserTag.toVocabularyEntity()` naively maps `usageCount = count` from the *save-mutation echo*
(global popularity) straight into the personal-usage-cache column, via a plain `@Upsert`. Since
`SaveUserTagsUseCase` re-sends the *entire* tag list on every add/remove/spoiler-toggle, this
clobbers the personal usage count for every tag on the book on every single tag interaction —
directly contradicting `TagSuggestionDerivation.kt`'s own documented "ranks by personal usage
frequency" contract. The author's own comment on the `FindTagsByUserQuery.Tagging.toUserTag()`
mapper function explicitly named this exact ambiguity, which makes the `record()` path's mistake
more surprising (the confusion was seen and documented in an adjacent function, then made anyway).
**When reviewing tag/vocabulary code in this codebase going forward: always trace which query a
`UserTag.count` value came from before trusting it as "the user's own usage count."**

**2. Always run `./gradlew ktlintCheck` (root, not per-module — no per-module ktlint task exists)
for any "substantial multi-file change" review, don't just eyeball for the known patterns.** It
directly caught (a) `TagSuggestionsCollector.kt`'s colocated `private data class TagEditorQuery` —
the same one-type-per-file shape as prior findings (see [[style_one_type_per_file_colocated_support_class]])
— and (b) a genuine multi-arg-wrapping miss in production (`TagSuggestionDerivation.kt`'s
`.contains(query, ignoreCase = true)` / `.startsWith(query, ignoreCase = true)`, nested inside a
trailing-lambda body — the trailing-lambda exemption only covers the *outer* call, not calls nested
inside its body) plus ~25 more instances spread across every new `androidHostTest` file in the
change. Unlike detekt (only `detektAndroidMain`/`detektJvmMain`/`detektMain` are gated —
see [[project_detekt_gate_scope]]), `ktlintCheck` gates test source sets too, so these test-file
hits are real blockers, not informational. All but the one-type-per-file case are auto-fixable via
`ktlintFormat` — recommend that as the fix rather than manual line-by-line rewrapping.

**Also confirmed sound in this review:** the `config/detekt/detekt.yml` `LongParameterList.excludes`
change (path-based exclude replacing a threshold bump) was pre-approved by the user in the same
task (see the rhaydus-logic agent's own memory,
`.claude/agent-memory/rhaydus-kotlin-rhaydus-logic/feedback_detekt_screenmodel_exclude_not_threshold_bump.md`)
— don't re-litigate that pattern if seen again elsewhere.

**And a DAO convention this diff missed:** `UserTagVocabularyLocalDataSourceImpl.replaceAll()` composed
`dao.clearForUser()` + `dao.upsertAll()` as two separate suspend DAO calls with no `@Transaction` —
this codebase's established convention for clear-then-insert is a single `@Transaction`-annotated
DAO method (see `BookDao.cacheBookList`). Worth checking for on every new "replace all rows for this
key" data source method: grep the target DAO/entity's sibling DAOs for the multi-statement
`@Transaction` pattern before accepting a data-source-layer composition of two separate calls.
