---
name: project_usertag_count_overload
description: UserTag.count means global popularity or personal usage depending on which query produced it; and a DAO clear-then-insert must be one @Transaction method.
metadata:
  type: project
---

**1. `UserTag.count` is contextually overloaded — a real footgun.** `UserTag.count` means the tag's
*global site-wide popularity* when mapped from `SaveTagsMutation`/`FindTagsByUserAndTaggableQuery`
(`UserTagMapper.kt`), but the *user's personal usage frequency* when mapped from `FindTagsByUserQuery`
after client-side `groupBy` aggregation (`UserTagsRemoteDataSource.fetchUserVocabulary`).

Found in the local tag vocabulary cache work (2026-07-22): `RecordAppliedTagsUseCase` →
`UserTagVocabularyRepositoryImpl.record()` → `UserTag.toVocabularyEntity()` mapped `usageCount = count`
from the *save-mutation echo* (global popularity) into the personal-usage-cache column via a plain
`@Upsert`. Since `SaveUserTagsUseCase` re-sends the entire tag list on every add/remove/spoiler-toggle,
this clobbered the personal usage count for every tag on the book on every interaction — contradicting
`TagSuggestionDerivation.kt`'s "ranks by personal usage frequency" contract.

**How to apply:** when reviewing tag/vocabulary code, always trace which query a `UserTag.count` value
came from before trusting it as "the user's own usage count."

**2. Clear-then-insert belongs in one `@Transaction` DAO method.** The same diff composed
`dao.clearForUser()` + `dao.upsertAll()` as two separate suspend calls in
`UserTagVocabularyLocalDataSourceImpl.replaceAll()`. The codebase convention is a single
`@Transaction`-annotated DAO method (see `BookDao.cacheBookList`). Check every new "replace all rows for
this key" data-source method for it.
