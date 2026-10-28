# Patterns — Reading

### Backdate a logged action

- The Update progress sheet's `WhenReadRow`: one icon-leading pill in the sheet's shared chrome, above the
  active unit tab, so every tab's submit carries the same moment (the host holds it as `readAt`).
- Tapping it starts a backdate at now and expands an inline day + time-of-day editor of `StepperCircle`s
  (day, hour ±1h, minute ±15m); a trailing clear glyph returns to "Just now". The host clamps to now.
- Reuse this pill-plus-stepper shape for any "log this in the past" affordance, never a platform
  date/time picker dialog.

### Reading-session peek bar

- `SessionPeekBar` (`feature/session`), mounted once at the app shell above the nav; renders nothing
  without an active session and reads `ActiveSessionController`. It slides and fades in and out.
- A 32dp `Cover`, a one-line `titleSmall` title, a live `H:MM:SS` timer (or `Paused · …`), inline
  pause/resume (`select`) and stop (`threshold`); the whole bar opens Focus Mode.
- `flush = true` on the wide shell: an edge-to-edge strip, square corners, top hairline, at the foot of the
  content pane. Compact shell: a rounded floating card above the bottom bar.
- Its lock-screen counterpart is `ReadingSessionService`'s plain ongoing notification (cover, chronometer,
  pause/resume, stop, an inline page reply), never a media session. No screen renders its own timer.

### Focus Mode

- Full-screen session surface over the shell, an editorial hero on the page `surface`: `NOW READING`
  eyebrow, cover, `display` italic title over a byline, then the timer as a hero stat (`READING TIME` or
  `PAUSED`, `statHero` `H:MM:SS`).
- Below: the page as an editable hero stat (`HeroStatNumberField` + `EditorialSuffix` + `ProgressBar`),
  committing on focus loss, read-only "% complete" without a page count; then pause/resume + stop buttons.
- `FocusModeScreenModel` drives it off `ActiveSessionController`; it pops once no session is active.
  Reached from the peek bar, the featured card's "Focus mode" button and the notification's content tap.

### Streak strip

- A 21-day row in the Reading header, most recent first in a horizontal scroll. A read day is a filled
  `primary` dot, an unread day a faint track dot, today slightly larger (ringed when unread).
- A "Most recent first" caption beside an italic "N days running" streak, shown while the current streak runs: one ending yesterday still counts until midnight.
- The dot is binary: a day with pages read in the on-device progress history, never reading sessions.
- The strip shows only while Settings' "Reading streak" is on.
- The row is one `tactileClickable` target (`select`) opening a per-day breakdown sheet. With no activity
  in the window the strip is omitted.

### Reading featured-hero card

- A 20dp-radius clipped card: an optional pace-nudge ribbon fused to its top, then one blurred-cover
  backdrop under everything else (`surfaceContainerHigh` base, 6dp blur, a graded veil; denser where
  `supportsBlur()` is false).
- Content: the cover (`CoverVariant.Spotlight`) in a deadline `CoverOverlay`, beside series eyebrow (or
  the retry line), italic title, byline and an optional "Your pace" line; then the page count and `NN%`,
  a `ProgressBar`, an optional `DeadlineSummaryLine`, a tonal "Update progress" pill, the session button
  (outlined "Start reading session", tonal "Focus mode" while this book's session runs) and a caption.
- Mark-as-read lives only in the Update progress sheet. Reserved for Reading's single featured book.

### Reading secondary row

- The "also reading" rows under a `SectionHeader` ("N more in motion" eyebrow + headline): each a
  `BookCard` row, flat with a top hairline on every row, a `titleMedium` roman title, progress with
  `NN%`, and a full-width `DeadlineSummaryLine` beneath when a deadline is tracked.
- A trailing pencil chip (no chevron, `DesktopTooltip`) opens the Update progress sheet.

### Reading on-hold row

- The paused books sit under a `SectionHeader` ("On hold" eyebrow, "Set aside for now" headline), below the
  "also reading" rows and above Pick up next. The section is absent when nothing is on hold.
- Each row is identical to the secondary row (flat, top hairline, `titleMedium` title, progress with `NN%`),
  with no deadline.
- Trailing: only the pencil chip, which opens the Update progress sheet; logging progress there resumes the
  book. There is no session chip; sessions start only from the featured card.
- With nothing in Currently Reading, the "Between books" empty state still shows, with the section beneath it.

### Mark-as-read celebration

- Only a transition to Read celebrates (book-detail Read row, Reading "Mark as Read", progress reaching
  100%); Want-to-read / Reading / DNF never do.
- `MarkAsReadBurst` (a `primary` particle burst) paired with one `Feedback.Milestone` haptic.
- Reading (`MarkAsReadController`, both layouts): the row slides down 96dp while fading, then
  `OnMarkBookAsReadClickAction` dispatches; `navPulse.pulse(LibraryNavPulseKey)` pulses the Library icon.
- One burst per finish: the progress-sheet path never repeats one an explicit affordance played.
- Reduced motion: no slide, the action dispatches at once.
