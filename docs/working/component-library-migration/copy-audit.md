# Copy audit

PF-5 output (see `steps/pf-premium-feel.md`). Every user-facing string that reads as AI-generated, with
one plain replacement each: 140 rows. Text that already reads naturally is left out. PF-6 walks this
screen by screen and records a verdict per row (**approve**, **cut**, or the user's own rewrite); PF-7
applies the approved rows after S12-4, then deletes this file.

`path:line` is where the string lived on 2026-10-07; re-find by the quoted text if it has drifted.

## Onboarding and profile

Paths are relative to `feature/<module>/src/<sourceSet>/kotlin/nl/rhaydus/softcover/feature/<module>/presentation/`.

### Onboarding: welcome page (mobile page 1, desktop hero)

| Where | Current | Why | Replacement | Verdict |
|---|---|---|---|---|
| Welcome body, mobile — `onboarding/mobileMain/screen/OnboardingScreenLayout.mobile.kt:248` | "Track every book you read, share the ones worth sharing — or don't — and find your next life-changing read." | em-dash aside, tidy triplet, hype ("life-changing") | "Track what you read and find what to read next." | |
| Welcome body, desktop — `onboarding/jvmMain/screen/OnboardingScreenLayout.jvm.kt:87` | (same as above) | same | (same as above) | |

### Onboarding: connect page (mobile page 2)

| Where | Current | Why | Replacement | Verdict |
|---|---|---|---|---|
| Connect body — `onboarding/mobileMain/screen/OnboardingScreenLayout.mobile.kt:304` | "To sync your reading, Softcover connects to your Hardcover account — the open book database your library actually lives in." | em-dash appositive, "actually lives in" flourish | "Softcover syncs with your Hardcover account, where your library is stored." | |

### Onboarding: API key page / form

| Where | Current | Why | Replacement | Verdict |
|---|---|---|---|---|
| API key body — `onboarding/mobileMain/screen/OnboardingScreenLayout.mobile.kt:455` | "One key, once. It only syncs your reading progress — never your password." | punchy fragment, em-dash contrast | "You only add it once. It's used to sync your reading, and Softcover never sees your password." | |
| Paste row subtitle — `onboarding/commonMain/screen/OnboardingShelf.kt:227` | "The quick way in" | cute rather than clear | "Paste the key you copied" | |
| "Where to find it" step 2 subtitle — `onboarding/commonMain/screen/OnboardingShelf.kt:332` | "Your access token lives here." | personification; calls the key a "token" | "Your API key is on this page." | |

### Onboarding: loading sheet

| Where | Current | Why | Replacement | Verdict |
|---|---|---|---|---|
| Loading sheet headline — `onboarding/commonMain/state/OnboardingUiState.kt:29` | "Pulling your library together." | literary flourish | "Loading your library" | |

### Profile: identity and section headers

| Where | Current | Why | Replacement | Verdict |
|---|---|---|---|---|
| Identity section eyebrow — `profile/commonMain/state/ProfileUiState.kt:79` | "The reader" | cute label | "Profile" | |
| Stats section eyebrow — `profile/commonMain/state/ProfileUiState.kt:78` | "Reading atlas" | forced metaphor (nothing is mapped) | "Stats" | |
| Authors section headline — `profile/commonMain/state/ProfileUiState.kt:82` | "The authors behind your shelf" | metaphor | "Who wrote what you read" | |
| Genres section eyebrow — `profile/commonMain/state/ProfileUiState.kt:85` | "What you reach for" | cute label | "Genres" | |
| Recently loved headline — `profile/commonMain/state/ProfileUiState.kt:90` | "The last few that landed" | cute, vague | "Your latest favorites" | |
| Year history eyebrow — `profile/commonMain/state/ProfileUiState.kt:93` | "The shape of the years" | literary flourish | "By year" | |

### Profile: stats tiles

| Where | Current | Why | Replacement | Verdict |
|---|---|---|---|---|
| Total pages hero caption — `profile/commonMain/mapper/ReadingAtlasMapper.kt:21` | "Every page a step further into the story." | literary flourish, says nothing | "Across all your books." | |
| Books tile eyebrow — `profile/commonMain/mapper/ReadingAtlasMapper.kt:24` | "Volumes" | cute substitute for the plain word | "Books" | |

### Profile: ratings histogram ("How you score")

| Where | Current | Why | Replacement | Verdict |
|---|---|---|---|---|
| Headline, generous band — `profile/commonMain/mapper/RatingsHistogramMapper.kt:122` | "A generous but honest hand" | literary flourish | "You rate generously" | |
| Headline, superfan band — `RatingsHistogramMapper.kt:123` | "Easily, happily impressed" | whimsical | "You rate most books highly" | |
| Headline, centrist band — `RatingsHistogramMapper.kt:124` | "You keep to the middle of the shelf" | forced shelf metaphor | "You rate most books in the middle" | |
| Headline, exacting band — `RatingsHistogramMapper.kt:125` | "A hard star to earn" | literary flourish | "You rate strictly" | |
| Headline, polarised band — `RatingsHistogramMapper.kt:126` | "Love it or leave it" | cute idiom | "You rate books high or low" | |
| Headline, new-reader band — `RatingsHistogramMapper.kt:127` | "Still finding your scale" | cute | "Not enough ratings yet" | |
| Caption, generous band — `RatingsHistogramMapper.kt:99` | "Half-stars count. Most of yours land between four and five — you round up when a book earns it." | em-dash, invented character read | "Half stars included. Most of your ratings are between 4 and 5." | |
| Caption, superfan band — `RatingsHistogramMapper.kt:102` | "Your average sits north of four and a half — you finish what you start, and it usually earns the top shelf." | em-dash, invented claim, "top shelf" metaphor | "Your average rating is above 4.5." | |
| Caption, centrist band — `RatingsHistogramMapper.kt:106` | "Your ratings cluster around three and a half — steady and considered, rarely swinging to an extreme." | em-dash, flattering pair | "Most of your ratings are around 3.5, with few very high or very low." | |
| Caption, exacting band — `RatingsHistogramMapper.kt:110` | "Your average runs low — a five-star read is rare here, which makes the ones you give out mean something." | em-dash, over-explaining flourish | "Your average is low, and five-star ratings are rare." | |
| Caption, polarised band — `RatingsHistogramMapper.kt:114` | "Your ratings split into two camps, low and high, with little in between — a book either wins you over or it doesn't." | em-dash, restates itself | "Most of your ratings are either low or high, with few in between." | |
| Caption, new-reader band — `RatingsHistogramMapper.kt:118` | "Not enough ratings yet to say much about your taste — a few more books will fill this in." | em-dash | "Rate a few more books to see a pattern here." | |

### Profile: authors section

| Where | Current | Why | Replacement | Verdict |
|---|---|---|---|---|
| Empty caption, untagged hidden — `profile/commonMain/screen/section/AuthorRepresentationSection.kt:141` | "None of the authors you've read carry this data yet — switch off to see the untagged share." | em-dash, vague "switch off" | "None of your authors have this data yet. Turn off Hide untagged authors to see them." | |
| Gender caption, no data — `AuthorRepresentationSection.kt:158` | "Gender breakdown will fill in as your tagged authors are identified." | muddled ("tagged authors are identified") | "This fills in as your authors get gender data on Hardcover." | |
| BIPOC / LGBTQ+ caption, no data — `AuthorRepresentationSection.kt:217` | "This breakdown will fill in as your tagged authors are identified." | same | "This fills in as your authors get tagged on Hardcover." | |
| Scope caption, untagged hidden — `AuthorRepresentationSection.kt:168` | "Shares of the authors each one is tagged for — untagged authors left out." | em-dash, hard to parse | "Each bar counts only authors tagged for it." | |
| Hide-untagged toggle gloss — `profile/commonMain/state/HideUntaggedAuthorsToggleModel.kt:8` | "Read each bar over the authors it has data for." | awkward, over-literal | "Only count authors with data for each bar." | |

### Profile: genres section

| Where | Current | Why | Replacement | Verdict |
|---|---|---|---|---|
| Genre caption — `profile/commonMain/screen/section/GenreRankingSection.kt:87` (scope from lines 82–84) | "Your five most-read genres, each as a share of your books — counted over the {count} finished books of yours that carry genre tags. A book tagged with several counts toward each, so these overlap and don't total 100%." | em-dash, over-explaining | "Your top five genres, out of {count} finished books with genre tags. Books can have more than one genre, so these add up to more than 100%." | |
| Empty state — `GenreRankingSection.kt:51` | "Once a few books settle onto your shelves, this fills in." | flourish ("settle onto your shelves") | "Finish a few books to see your top genres." | |

### Profile: recently loved

| Where | Current | Why | Replacement | Verdict |
|---|---|---|---|---|
| Empty state — `profile/commonMain/screen/section/RecentlyLovedSection.kt:43` | "Your next favorite is still ahead of you." | literary, cheerleading | "Books you rate highly will show up here." | |

### Profile: year history

| Where | Current | Why | Replacement | Verdict |
|---|---|---|---|---|
| Empty caption — `profile/commonMain/mapper/YearHistoryMapper.kt:19` | "Your reading years will chart themselves in as books finish." | personification flourish | "Finish a book to start this chart." | |
| Peak caption — `YearHistoryMapper.kt:95` | "{year} was your busiest year yet — {count} {unit}." | em-dash construction | "{year} was your busiest year: {count} {unit}." | |

### Profile: log out

| Where | Current | Why | Replacement | Verdict |
|---|---|---|---|---|
| Log-out failure snackbar — `profile/commonMain/action/OnLogOutClickAction.kt:20` | "Couldn't log out — try again." | em-dash | "Couldn't log out. Try again." | |

## Library and lists

### Library masthead (empty shelf subtitle)

| Where | Current | Why | Replacement | Verdict |
|---|---|---|---|---|
| Masthead subtitle, empty Read shelf — `feature/library/src/commonMain/kotlin/nl/rhaydus/softcover/feature/library/presentation/collector/MastheadSnapshot.kt:60` | "Nothing finished yet — the page is still open." | em-dash aside, literary flourish | "No finished books yet" | |
| Masthead subtitle, empty Want to read shelf — `.../collector/MastheadSnapshot.kt:62` | "No titles set aside — discover one next." | em-dash construction, nudging tagline | "No books to read yet" | |
| Masthead subtitle, empty Currently reading shelf — `.../collector/MastheadSnapshot.kt:64` | "No book open — pick one up." | em-dash construction, cute wordplay | "Not reading anything right now" | |
| Masthead subtitle, any other empty shelf/list — `.../collector/MastheadSnapshot.kt:65` | "No titles yet — your story starts here." | em-dash, "your story starts here" flourish | "No books yet" | |

### Library shelf empty state

| Where | Current | Why | Replacement | Verdict |
|---|---|---|---|---|
| Empty Did-not-finish shelf, title — `feature/library/src/commonMain/kotlin/nl/rhaydus/softcover/feature/library/presentation/mapper/LibraryEmptyStateMapper.kt:12` | "Nothing set aside" | cute rather than clear | "No unfinished books" | |
| Empty Did-not-finish shelf, body — `.../mapper/LibraryEmptyStateMapper.kt:13` | "No books abandoned here — long may it stay that way." | em-dash aside, literary flourish | "Books you stop reading will show up here." | |
| Empty shelf/list, title — `.../mapper/LibraryEmptyStateMapper.kt:17` | "An empty shelf" | literary heading | "No books yet" | |
| Empty shelf/list, body — `.../mapper/LibraryEmptyStateMapper.kt:18-19` | "Nothing rests on your {label} list yet. Find a title worth keeping and it will live here." | forced metaphor ("rests", "will live here"), over-explaining | "Books you add to {label} will show up here." | |

### Filter sheet

| Where | Current | Why | Replacement | Verdict |
|---|---|---|---|---|
| Filter sheet header description — `feature/library/src/commonMain/kotlin/nl/rhaydus/softcover/feature/library/presentation/state/LibrarySheetScaffoldBuilder.kt:44` | "Combine facets to find the corner of your library you're after." | forced metaphor ("corner of your library"), jargon ("facets") | "Combine filters to narrow down this shelf." | |
| Filter sheet, shelf too small to filter — `feature/library/src/commonMain/kotlin/nl/rhaydus/softcover/feature/library/presentation/component/LibraryFilterSheet.kt:306` | "This shelf is too sparse for filters to matter — add a few books first." | em-dash construction, over-explaining | "Add a few books to this shelf to filter it." | |

### Library control line (accessibility)

| Where | Current | Why | Replacement | Verdict |
|---|---|---|---|---|
| Sort-direction button content description — `feature/library/src/commonMain/kotlin/nl/rhaydus/softcover/feature/library/presentation/component/LibraryControlLine.kt:139` | "Sorted {direction} — tap to change" | em-dash construction | "Sorted {direction}. Tap to change." | |

### Library snackbars (bulk actions and list ordering)

| Where | Current | Why | Replacement | Verdict |
|---|---|---|---|---|
| Bulk "Add to new list" failure snackbar — `feature/library/src/commonMain/kotlin/nl/rhaydus/softcover/feature/library/presentation/action/OnBulkAddToNewListAction.kt:74` | "Couldn't add {count} {book/books} to “{listName}” — try again." | "X — Y" em-dash construction | "Couldn't add {count} {book/books} to “{listName}”. Try again." | |
| Bulk Move failure snackbar — `.../action/OnBulkMoveShelfAction.kt:61` | "Couldn't move {count} {book/books} — try again." | "X — Y" em-dash construction | "Couldn't move {count} {book/books}. Try again." | |
| Bulk add/remove list membership failure snackbar — `.../action/OnBulkToggleListMembershipAction.kt:106` | "Couldn't {add/remove} {count} {book/books} {to/from} "{listName}" — try again." | "X — Y" em-dash construction | "Couldn't {add/remove} {count} {book/books} {to/from} "{listName}". Try again." | |
| List reorder failure snackbar — `.../action/OnReorderListBooksAction.kt:49` | "Couldn't save the new order{ in "{listName}"} — try again." | "X — Y" em-dash construction | "Couldn't save the new order{ in "{listName}"}. Try again." | |
| "Make this list ordered" failure snackbar — `.../action/OnSetListRankedAction.kt:97` | "Couldn't {enable/disable} manual ordering{ for "{listName}"} — try again." | "X — Y" em-dash construction | "Couldn't {turn on/turn off} manual ordering{ for "{listName}"}. Try again." | |

## Reading, Focus Mode and notifications

Paths are relative to the repo root. `R` = `feature/reading/src/commonMain/kotlin/nl/rhaydus/softcover/feature/reading/presentation`.

### Reading screen: masthead

| Where | Current | Why | Replacement | Verdict |
|---|---|---|---|---|
| Masthead eyebrow while loading/refreshing. `R/collector/ReadingHeaderCopy.kt:15`, also `feature/reading/src/mobileMain/.../screen/section/ReadingMasthead.kt:39` | "Catching up on your reading…" | cute loading copy instead of a plain status | "Refreshing…" | |
| Masthead greeting, 22:00 to 04:59. `R/collector/ReadingHeaderCopy.kt:23` | "Late hours." | literary flourish, and not really a greeting | "Good evening." | |
| Masthead subtitle, book count. `R/collector/ReadingHeaderCopy.kt:32-33` (initial-state default at `R/state/ReadingScreenUiState.kt:126,132`) | "One title in motion" / "{count} titles in motion" | forced metaphor | "Reading 1 book" / "Reading {count} books" | |
| Masthead subtitle, average progress. `R/collector/ReadingHeaderCopy.kt:36` | "{percent}% along, on average" | wordy, slightly literary | "{percent}% done on average" | |

### Reading screen: featured book card

| Where | Current | Why | Replacement | Verdict |
|---|---|---|---|---|
| Hero caption, 05:00 to 11:59. `R/screen/section/FeaturedBackdropCard.kt:244` | "Mornings are made for a few quiet pages before the day gets loud." | literary flourish | "Pick up where you left off." | |
| Hero caption, 12:00 to 17:59. `R/screen/section/FeaturedBackdropCard.kt:245` | "A good afternoon for picking up where you left off." | filler framing around the actual message | "Pick up where you left off." | |
| Hero caption, 18:00 to 21:59. `R/screen/section/FeaturedBackdropCard.kt:246` | "Evenings were made for this. Pick up where you stopped." | literary flourish and cheerleading | "Pick up where you left off." | |
| Hero caption, 22:00 to 04:59. `R/screen/section/FeaturedBackdropCard.kt:247` | "The late hours are yours. Pick up where you stopped." | literary flourish | "Pick up where you left off." | |
| "Your pace" line. `R/mapper/ReadingPaceLineMapper.kt:14` | "{pace} — about {days} day(s) to go" | em-dash construction | "{pace} · about {days} day(s) left" | |
| Series eyebrow after a failed save. `R/screen/section/FeaturedBackdropMeta.kt:50` | "COULDN'T SAVE — TAP TO RETRY" | em-dash construction | "COULDN'T SAVE. TAP TO RETRY" | |

Note: the four hero captions all say the same thing once the flourish is removed, so one plain line is enough. If the caption only exists to vary by time of day, dropping it is the other option.

### Reading screen: "Also reading" list

| Where | Current | Why | Replacement | Verdict |
|---|---|---|---|---|
| Section headline. `R/collector/CurrentlyReadingBooksCollector.kt:26` (initial-state default at `R/state/ReadingScreenUiState.kt:39`) | "Also between your fingers" | forced metaphor, cute heading | "Also reading" | |
| Section eyebrow. `R/collector/CurrentlyReadingBooksCollector.kt:25` (initial-state default at `R/state/ReadingScreenUiState.kt:38`) | "One more in motion" / "{count} more in motion" | forced metaphor | "1 more book" / "{count} more books" | |
| Row eyebrow after a failed save. `R/mapper/ReadingBookCardMapper.kt:38` | "COULDN'T SAVE — TAP TO RETRY" | em-dash construction | "COULDN'T SAVE. TAP TO RETRY" | |

### Reading screen: nothing-in-progress state

| Where | Current | Why | Replacement | Verdict |
|---|---|---|---|---|
| Empty-state title. `R/state/ReadingScreenUiState.kt:66` | "An open page awaits" | literary flourish, cute heading | "You're not reading anything" | |
| Empty-state body. `R/state/ReadingScreenUiState.kt:67` | "Nothing is currently between your fingers. Find a title worth losing an evening to." | forced metaphor and literary flourish | "Start a book and it will show up here." | |

### Desktop tray notification

| Where | Current | Why | Replacement | Verdict |
|---|---|---|---|---|
| Tray notification caption when the notification has an eyebrow. `core/notification/src/jvmMain/kotlin/nl/rhaydus/softcover/core/notification/JvmSoftcoverNotifier.kt:34` | "{eyebrow} — {title}" | "X — Y" em-dash construction | "{eyebrow}: {title}" | |

### Android notification settings (channel descriptions)

| Where | Current | Why | Replacement | Verdict |
|---|---|---|---|---|
| "Milestones" channel description in system notification settings. `core/notification/src/androidMain/kotlin/nl/rhaydus/softcover/core/notification/SoftcoverNotificationChannel.kt:20` | "Celebrations when you finish a series, hit a streak, or complete a yearly goal." | cheerleading ("Celebrations") on a tidy triplet | "When you finish a series, reach a streak or hit your yearly goal." | |

## Explore

### Explore feed

| Where | Current | Why | Replacement | Verdict |
|---|---|---|---|---|
| Masthead eyebrow while pull-to-refresh runs — `feature/explore/src/commonMain/kotlin/nl/rhaydus/softcover/feature/explore/presentation/collector/MastheadCollector.kt:30` | "Finding fresh picks…" | cute rather than clear ("fresh picks") | "Refreshing…" | |
| Masthead subtitle — `feature/explore/src/commonMain/kotlin/nl/rhaydus/softcover/feature/explore/presentation/collector/MastheadCollector.kt:32` (same default in `presentation/state/ExploreScreenUiState.kt:80`) | "Trending books, the next in your series, and anything you search for." | tidy triplet | "Trending books and the next book in each of your series." | |

### Hidden suggestions screen

| Where | Current | Why | Replacement | Verdict |
|---|---|---|---|---|
| "Hidden books" group subline — `feature/explore/src/commonMain/kotlin/nl/rhaydus/softcover/feature/explore/presentation/state/HiddenSuggestionsUiState.kt:20` | "Set aside — tap Restore to shelve them again." | em-dash aside, literary wording ("set aside", "shelve") | "Tap Restore to show them in Up next again." | |
| Empty state body — `feature/explore/src/commonMain/kotlin/nl/rhaydus/softcover/feature/explore/presentation/state/HiddenSuggestionsUiState.kt:29` | "Books and series you mute from “Up next in your series” land here, so you can bring any of them back whenever you like." | over-explaining, soft filler ("land here", "whenever you like") | "Books and series you hide from “Up next in your series” show up here." | |

## Book detail and scan

Paths are relative to `feature/book_detail/src/commonMain/kotlin/nl/rhaydus/softcover/feature/book_detail/` (BD) and `feature/scan/src/commonMain/kotlin/nl/rhaydus/softcover/feature/scan/` (SC).

### Book detail: status callout (Yours lens)

| Where | Current | Why | Replacement | Verdict |
|---|---|---|---|---|
| Read callout, read date known: `BD/presentation/mapper/StatusCalloutMapper.kt:28` | "You finished this on {readDate}. A great one to revisit." | tacked-on cheerleading (it praises every finished book, liked or not) | "You finished this on {readDate}." | |
| Want to read callout: `BD/presentation/mapper/StatusCalloutMapper.kt:44` | "On your shelf since {date}. Ready when you are." | literary flourish | "On your shelf since {date}." | |
| Did not finish callout, date known: `BD/presentation/mapper/StatusCalloutMapper.kt:37` | "You set this aside on {dnfDate}." | soft euphemism for a plain status | "You stopped reading this on {dnfDate}." | |

### Book detail: shelve control

| Where | Current | Why | Replacement | Verdict |
|---|---|---|---|---|
| Shelve card header after a failed write: `BD/presentation/collector/ShelveControlHeaderCollector.kt:27` | "Couldn't save — tap to retry" | em-dash "X — Y" construction | "Couldn't save. Tap to retry" | |

### Book detail: reviews section (The Book lens)

| Where | Current | Why | Replacement | Verdict |
|---|---|---|---|---|
| Reviews section eyebrow: `BD/presentation/state/BookDetailUiState.kt:95` | "Voices" | cute label rather than clear | "Reviews" | |
| Reviews section headline: `BD/presentation/screen/section/ReviewsSection.kt:67` | "What readers think" | literary headline layered on the label | "Reader reviews" (or drop it once the eyebrow says "Reviews") | |

### Book detail: scanned-edition update

| Where | Current | Why | Replacement | Verdict |
|---|---|---|---|---|
| Snackbar when switching to the scanned edition fails: `BD/presentation/action/OnUpdateToScannedEditionClickAction.kt:37` | "Couldn't update the edition — try again." | em-dash "X — Y" construction | "Couldn't update the edition. Try again." | |

### Tag editor sheet

| Where | Current | Why | Replacement | Verdict |
|---|---|---|---|---|
| Sheet headline: `BD/presentation/state/BookDetailSheetScaffoldBuilder.kt:27` | "Tag it your way." | slogan / cheerleading | "Add your own tags." | |
| Empty state title: `BD/presentation/state/BookDetailUiState.kt:229` | "Nothing here yet." | stock filler title | "No tags yet" | |
| Empty state body: `BD/presentation/state/BookDetailUiState.kt:230` | "Name the first thing this book is to you." | literary flourish | "Add a tag to start." | |
| Spoiler toggle on a tag chip (accessibility label): `BD/presentation/collector/TagChipModelsCollector.kt:97` | "Marked as spoiler — tap to unmark" | em-dash "X — Y" construction | "Marked as spoiler. Tap to unmark" | |

### Share sheet

| Where | Current | Why | Replacement | Verdict |
|---|---|---|---|---|
| Sheet description for the Book card: `BD/presentation/state/BookDetailSheetScaffoldBuilder.kt:110` | "An editorial card of {bookTitle}" | design jargon ("editorial") | "A card for {bookTitle}" | |

### Scan screen

| Where | Current | Why | Replacement | Verdict |
|---|---|---|---|---|
| Snackbar when the ISBN lookup fails: `SC/presentation/screen/BarcodeScannerScreen.kt:85` | "Couldn't look that up — try again." | em-dash "X — Y" construction | "Couldn't look up that ISBN. Try again." | |
| Snackbar when adding to Hardcover fails: `SC/presentation/screen/BarcodeScannerScreen.kt:86` | "Couldn't add it — try again." | em-dash "X — Y" construction | "Couldn't add the book. Try again." | |

### Unknown ISBN sheet

| Where | Current | Why | Replacement | Verdict |
|---|---|---|---|---|
| Sheet headline: `SC/presentation/state/UnknownIsbnSheetScaffoldBuilder.kt:14` | "We don't have this one yet." | vague "we" voice (Softcover doesn't own the catalogue) | "Hardcover doesn't have this book yet." | |
| Sheet description: `SC/presentation/state/UnknownIsbnSheetScaffoldBuilder.kt:15` | "Double-check the ISBN below — if it's right, add it to Hardcover so you can track it." | em-dash aside, over-explains | "Check the ISBN below. If it's right, add it to Hardcover to track it." | |

## Settings

Paths are abbreviated. `S/` = `feature/settings/src/commonMain/kotlin/nl/rhaydus/softcover/feature/settings/presentation/`, `SM/` = `feature/settings/src/mobileMain/kotlin/nl/rhaydus/softcover/feature/settings/presentation/`.

`feature/app_update` has no user-facing copy: its strings are log messages and installer arguments. The update callout copy lives in settings.

### Settings menu (mobile) and sidebar (desktop)

| Where | Current | Why | Replacement | Verdict |
|---|---|---|---|---|
| Settings masthead subtitle (mobile and desktop) — `S/state/SettingsScreenUiState.kt:39`, `:45` | "Tune Softcover to match how you read." | cheerleading flourish | "Appearance, library tabs and app info." | |
| Account section headline — `S/state/SettingsScreenUiState.kt:55` | "Your reader" | cute label, unclear | "Your account" | |
| Personalisation section headline — `S/state/SettingsScreenUiState.kt:59` | "Make it yours" | marketing cliché | "Look and layout" | |
| About section headline — `S/state/SettingsScreenUiState.kt:63` | "The fine print" | cute label | "About the app" | |
| Roadmap row subtitle — `SM/screen/SettingsScreenLayout.mobile.kt:182` | "What we're building next, in the order we plan to ship it" | over-explaining | "What's coming next" | |

### App update callout (Settings)

| Where | Current | Why | Replacement | Verdict |
|---|---|---|---|---|
| Downloading title — `S/mapper/AppUpdateCalloutMapper.kt:9` | "Bringing the update down" | whimsical phrasing | "Downloading the update" | |
| Downloading body — `S/mapper/AppUpdateCalloutMapper.kt:10` | "You can keep reading — it'll finish in the background." | em-dash aside | "This continues in the background." | |
| Downloaded body — `S/mapper/AppUpdateCalloutMapper.kt:14` | "Downloaded and waiting. Softcover will restart once." | clipped and stilted | "Softcover restarts to install it." | |
| Failed title — `S/mapper/AppUpdateCalloutMapper.kt:18` | "That didn't go through" | vague, chatty | "Couldn't download the update" | |
| Failed body — `S/mapper/AppUpdateCalloutMapper.kt:19` | "Tap to try again." | repeats the "Try again" button (and desktop users click) | "Check your connection and try again." | |
| Available body — `S/mapper/AppUpdateCalloutMapper.kt:24` | "A newer Softcover is ready whenever you are." | "whenever you are" flourish | "A new version of Softcover is available." | |

### Appearance

| Where | Current | Why | Replacement | Verdict |
|---|---|---|---|---|
| Intro line — `S/screen/AppearanceSettingsContent.kt:43` | "How Softcover looks in your hands, and how it reads dates back to you." | literary flourish | "Theme, colours, text size and date format." | |
| Appearance masthead subtitle (desktop pane) — `S/state/SettingsScreenUiState.kt:94` | "Make Softcover yours." | marketing cliché | "Change how Softcover looks." | |
| Theme section headline — `S/state/SettingsScreenUiState.kt:98` | "The coat it wears" | forced metaphor | "Light or dark" | |
| Spine colour section headline — `S/state/SettingsScreenUiState.kt:102` | "The paper and the ink" | literary metaphor | "App colours" | |
| Date notation section headline — `S/state/SettingsScreenUiState.kt:111` | "How dates read" | cute rather than clear | "Date format" | |
| Display section headline — `S/state/SettingsScreenUiState.kt:115` | "The look" | vague, cute | "Display options" | |
| Spine colour note while dynamic colour is on — `S/screen/section/SpineColourSection.kt:92` | "Dynamic colour is painting the app — pick a spine colour to take it back." | em-dash plus metaphor | "Dynamic colour is on. Pick a spine colour to turn it off." | |
| Display scale note — `S/screen/section/UiScaleSection.kt:43` | "Applies immediately, across the whole app." | over-explaining (the section description already says it scales everything) | "Changes apply right away." | |
| Floating bottom bar subtitle — `S/mapper/DisplayToggleRowsMapper.kt:26` | "Lift the nav off the edge, with rounded corners." | figurative, jargon "nav" | "Show the navigation bar as a rounded bar above the screen edge." | |

### Library tabs

| Where | Current | Why | Replacement | Verdict |
|---|---|---|---|---|
| Page header description — `S/state/LibraryVisibilitySettingsUiState.kt:18` | "Choose which shelves ride along the top of your library — and the order they sit in." | em-dash plus "ride along" metaphor | "Choose which shelves show as tabs in your library, and in what order." | |
| Tabs group subhead — `S/screen/section/LibraryTabsGroupHeader.kt:58`–`63` | "Drag to reorder — shelves and lists sit in one line. A • marks a list." | em-dash construction | "Drag to reorder. Shelves and lists share one order. A • marks a list." | |
| Save failure snackbar — `S/action/OnSaveLibraryVisibilityAction.kt:58` | "Couldn't save your tabs — try again." | em-dash | "Couldn't save your tabs. Try again." | |

### About

| Where | Current | Why | Replacement | Verdict |
|---|---|---|---|---|
| Source section headline — `S/state/SettingsScreenUiState.kt:79` | "Built in the open" | slogan | "Open source" | |
| Contact section headline — `S/state/SettingsScreenUiState.kt:83` | "Say hello" | cute | "Get in touch" | |
| GitHub row subtitle — `S/screen/AboutContent.kt:137` | "Browse the source, file a pull request, or fork it." | tidy triplet | "Browse the source code." | |
| Roadmap row subtitle — `S/screen/AboutContent.kt:145` | "What we're building next, in the order we plan to ship it." | over-explaining | "What's coming next." | |
| Report an issue subtitle — `S/screen/AboutContent.kt:172` | "Found a bug, or something feels off? Tell us on GitHub." | chatty rhetorical question | "Report bugs and problems on GitHub." | |

### Roadmap

| Where | Current | Why | Replacement | Verdict |
|---|---|---|---|---|
| Pull-to-refresh eyebrow (mobile) — `SM/screen/RoadmapScreenLayout.mobile.kt:71` | "The plan" | cute label | "Roadmap" | |
| Offline (bundled copy) note — `S/screen/RoadmapContent.kt:116`–`117` | "Showing the roadmap that shipped with this build — reconnect to see what's changed since." | em-dash construction | "This is the roadmap included with this version. Go online to see the latest." | |

## Shared components (several screens)

### Verdict sheet (rating + review sheet)

| Where | Current | Why | Replacement | Verdict |
|---|---|---|---|---|
| Verdict sheet header after finishing a book (Book detail, Reading) — `core/uibinding/src/commonMain/kotlin/nl/rhaydus/softcover/core/uibinding/verdict/VerdictSheetMapper.kt:40` | "The last page" | literary flourish as a label | "Finished" | |
| Verdict sheet header after finishing a book (Book detail, Reading) — `core/uibinding/.../verdict/VerdictSheetMapper.kt:42` | "A rating and a few words, if you like — both are optional." | em-dash aside, over-explaining | "Rating and review are both optional." | |
| Verdict sheet score caption under the stars (Book detail, Reading) — `core/component/src/commonMain/kotlin/nl/rhaydus/softcover/core/component/verdict/VerdictSheet.kt:76-85` | "A hard finish." / "Not for me." / "We didn't click." / "Mostly patience." / "Fair, in the end." / "Quietly worthwhile." / "A good one." / "One that lingers." / "Hard to put down." / "One of the greats." | whimsical, literary flourishes ("Quietly worthwhile", "One that lingers", "Mostly patience") | "Awful" / "Bad" / "Poor" / "Meh" / "Okay" / "Decent" / "Good" / "Very good" / "Great" / "A favourite" | |
| Verdict sheet review section label (Book detail, Reading) — `core/component/.../verdict/VerdictSheet.kt:195` | "In your words" | cute rather than clear heading | "Review" | |
| Verdict sheet review placeholder (Book detail, Reading) — `core/component/.../verdict/VerdictSheet.kt:237` | "What stayed with you? A line is plenty." | literary flourish, coaxing | "Write a review (optional)" | |

### Verdict block (empty state on a book's "Yours" tab)

| Where | Current | Why | Replacement | Verdict |
|---|---|---|---|---|
| Unrated verdict block hint (Book detail, Yours) — `core/component/src/commonMain/kotlin/nl/rhaydus/softcover/core/component/verdict/VerdictBlock.kt:194` | "Out of ten, your ten — spoilers optional." | em-dash construction, wordplay | "Rate out of 10. Reviews can hide spoilers." | |

### Choose lists sheet

| Where | Current | Why | Replacement | Verdict |
|---|---|---|---|---|
| Choose-lists sheet description, single book (Book detail) — `core/uibinding/src/commonMain/kotlin/nl/rhaydus/softcover/core/uibinding/lists/ChooseListsMapper.kt:38` | "Tap a list to file it here — tap a filled one to take it off. Saved as you go." | em-dash construction, tidy rhythm, "file it here" is vague | "Tap a list to add this book. Tap it again to remove it. Changes save automatically." | |
| Choose-lists sheet description, bulk selection (Library) — `core/uibinding/.../lists/ChooseListsMapper.kt:69` | "A part-filled list adds the rest first — then a second tap takes {takesOff} off." | em-dash construction, over-explaining | "Tapping a list that has some of these adds the rest. Tap again to remove {takesOff}." | |

### Deadline card

| Where | Current | Why | Replacement | Verdict |
|---|---|---|---|---|
| Deadline card goal line when behind (Book detail) — `core/uibinding/src/commonMain/kotlin/nl/rhaydus/softcover/core/uibinding/deadline/DeadlineMapper.kt:99` | "You're {behind} behind schedule — {base}" | "X — Y" construction | "You're {behind} behind schedule. {base}" | |

### Share sheet

| Where | Current | Why | Replacement | Verdict |
|---|---|---|---|---|
| Share failure snackbar (Book detail share, Profile share) — `core/component/src/commonMain/kotlin/nl/rhaydus/softcover/core/component/share/ShareCardSheetContent.kt:97` and `:108` | "Couldn't share — try again" | em-dash construction | "Couldn't share. Try again." | |

### Reading life share card

| Where | Current | Why | Replacement | Verdict |
|---|---|---|---|---|
| Card title (Profile share) — `core/component/src/commonMain/kotlin/nl/rhaydus/softcover/core/component/share/ReadingLifeShareCardBody.kt:59` | "A reading life" | literary flourish as a title | "My reading stats" | |
| Reader eyebrow (Profile share) — `core/component/.../share/ReadingLifeShareCardBody.kt:75` | "THE READER" | cute rather than clear | "READER" | |
| Monthly chart heading + subline (Profile share) — `core/component/.../share/ReadingLifeShareCardBody.kt:138` / `:147` | "THE READING YEAR" / "pages, month by month" | literary heading that needs a second line to explain it | "PAGES PER MONTH" (drop the subline) | |

### Book removal snackbar

| Where | Current | Why | Replacement | Verdict |
|---|---|---|---|---|
| Removal failure snackbar (Library, Book detail, Explore) — `core/presentation/src/commonMain/kotlin/nl/rhaydus/softcover/core/presentation/removal/UndoableBookRemover.kt:67` | "Couldn't remove {size} {noun} — try again." | em-dash construction | "Couldn't remove {size} {noun}. Try again." | |

### Spine colour picker

| Where | Current | Why | Replacement | Verdict |
|---|---|---|---|---|
| Palette description, Softcover (Settings > Appearance) — `core/designsystem/src/commonMain/kotlin/nl/rhaydus/softcover/core/designsystem/presentation/theme/SpinePalette.kt:37` | "The house look — warm clay and old gold, on warm white." | em-dash aside, literary flourish | "The default. Clay and gold on warm white." | |
| Palette description, Vellum (Settings > Appearance) — `core/designsystem/.../theme/SpinePalette.kt:41` | "Aged paper — a golden ink with a sage note, on cream." | em-dash aside, literary flourish | "Gold and sage on cream." | |
| Palette description, Ink (Settings > Appearance) — `core/designsystem/.../theme/SpinePalette.kt:45` | "Printer's slate blue and an oxblood second pass, on a cool page." | literary flourish, print jargon | "Slate blue and dark red on cool white." | |
| Palette description, Foxed (Settings > Appearance) — `core/designsystem/.../theme/SpinePalette.kt:49` | "The walnut brown of a browning page, on tanned paper." | literary flourish | "Walnut brown on tan." | |
| Palette description, Sea (Settings > Appearance) — `core/designsystem/.../theme/SpinePalette.kt:53` | "Deep sea teal and warm sand, on a page washed pale blue." | literary flourish | "Teal and sand on pale blue." | |

## Notes

- **No findings:** `feature/lists`, `feature/session` (Focus Mode, session bar), `feature/app_update`, the
  nav shells (`app`, `desktopApp`, `orchestration`: update snackbars, re-auth sheet, two-pane empty
  detail, window titles) and the core data modules (enum labels; no exception text reaches the UI).
- **Not AI copy, but worth a look in PF-6:** nav tab content descriptions are "{tabTitle} icon"
  (`NavDestinations.kt`); "icon" is redundant for screen readers. Explore uses both "mute" and "hide" for
  the same action. The four Reading hero captions collapse to one line once cleaned up; dropping the
  caption is the alternative.
- **Debug builds only (out of scope):** `DebugRoutesUiState.kt` "Behind the scenes.",
  `ShareCardDebugScreen.kt` "Storage permission denied — can't write to gallery."
