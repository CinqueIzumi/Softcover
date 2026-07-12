---
name: project_bottom_chrome_padding
description: bottomChromePadding()/rememberBottomBarPadding() contract for scroll-surface trailing padding under BottomBarScaffold/SessionPeekBar; recurring miss pattern is offline/error placeholders
type: project
---

Foundation contract: `BottomBarScaffold` (designsystem-core) provides `LocalBottomBarPadding` (default `0.dp`); a
shell-hosted scrolling surface must read `rememberBottomBarPadding()` and apply it as **trailing** content
padding (LazyColumn/LazyVerticalGrid `contentPadding`, or `Modifier.verticalScroll(state).padding(bottom = ...)`
— padding must come AFTER `verticalScroll` in the chain so it scrolls with content, not clip the viewport).

`core/designsystem/.../presentation/layout/BottomChromePadding.kt` adds `bottomChromePadding()` =
`max(rememberBottomBarPadding(), WindowInsets.navigationBars bottom inset)` — for surfaces that render BOTH
inside the shell (expanded two-pane) and pushed outside it (compact), today only book detail. Summing instead
of max would double-count the nav inset when hosted in the overlay shell (whose measured footprint already
bakes the inset in). A surface that only ever renders inside the shell reads `rememberBottomBarPadding()`
directly; a surface that never renders inside the shell needs neither (reads 0.dp harmlessly).

**Why:** as of 2026-07-13 a hotfix (`hotfix/3.0.3`) closed most gaps but two review passes have now caught the
same recurring miss pattern: **offline/error placeholder branches** (`OfflineScreenContent`, rendered via an
early `if (isOnline.not()) { ...; return }` short-circuit before the main scroll content) get forgotten even
when the loaded-content path in the very same file is fixed in the same diff. Confirmed misses found 2026-07-13:
`ExploreScreenLayout.jvm.kt:79` (sibling `DesktopDiscovery`/`DesktopSearchResults` fixed, offline branch above
them skipped) and `BookDetailScreenLayout.jvm.kt:91` / `BookDetailScreenLayout.mobile.kt:104` (loaded-content
paths fixed via `bottomChromePadding()`, offline branch skipped in both platforms).

**How to apply:** whenever reviewing a diff that adds `rememberBottomBarPadding()`/`bottomChromePadding()` to
scroll surfaces, explicitly grep the same feature's files for `OfflineScreenContent`/`isOnline.not()` short-circuit
branches and verify they got the same treatment — the fix pass tends to touch the "happy path" scroll containers
and skip the early-return error states in the same function. Also check: (1) padding is *added* to an existing
static bottom margin (`24.dp + rememberBottomBarPadding()`), never substituted for it; (2) pinned/non-scrolling
bottom footers (desktop sidebar version text, `LibraryVisibilitySaveBar`) get the padding as ordinary
`Modifier.padding(bottom = ...)` since they aren't scroll containers; (3) a shared composable like
`LibraryVisibilitySaveBar` used both inside the shell (desktop Settings tab-root pane) and on pushed sub-pages
(outside the shell) is fine reading `rememberBottomBarPadding()` unconditionally — it resolves to `0.dp` outside
the shell by design (harmless, not a bug).
