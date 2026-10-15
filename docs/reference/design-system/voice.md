# 8. Voice and copy

Every user-facing string follows these rules: labels, headlines, eyebrows, glosses, captions, empty states,
snackbars, dialogs, notifications and share cards. Previews, the Component Gallery and debug routes are exempt.

## 8.1 Voice

- **Editorial, not decorative.** Copy reads like a well-edited magazine: short, specific sentences in plain
  words, written to the reader as "you". The character comes from the type and the layout, not from the
  wording.
- **Say what is there.** A headline names the content of its section ("Year by year", "Recent favourites"). A
  gloss or caption says what the reader sees or what happens next.
- **Be accurate.** Copy describes the behaviour the code actually has. A gloss that promises something the
  app does not do is a bug.
- **Name things by their labels.** Refer to a setting, theme or button by the label the reader sees on
  screen ("Turn off Hide untagged authors"), never by a nickname or a metaphor for it.
- **Errors and status stay plain.** Say what failed and what to do: "Couldn't log out. Try again."

## 8.2 Tells to cut

These read as generated text. Rewrite any line that has one.

| Tell | Before | After |
|---|---|---|
| Em-dash aside or contrast | "Couldn't log out — try again." | "Couldn't log out. Try again." |
| Personification | "Your access token lives here." | "Your API key is on this page." |
| Literary flourish | "Pulling your library together." | "Getting your shelves ready…" |
| Forced metaphor | "The authors behind your shelf" | "Who you've been reading" |
| Cute label | "Volumes", "The reader" | "Books", "Reader" |
| Tidy triplet | "Track every book, share the ones worth sharing, and find your next read." | "Keep a record of every book you read, and find the next one worth reading." |
| Hype or cheerleading | "Your next favorite is still ahead of you." | "Books you rate highly show up here." |
| Invented character read | "you round up when a book earns it" | (cut) |
| Restating or over-explaining | "a book either wins you over or it doesn't" | (cut) |

A plain list of the actual options ("Light, dark or dim") is not a tidy triplet.

## 8.3 Mechanics

- **Sentence case** for every headline, label and button. Write eyebrows in sentence case too; the
  component that renders one uppercases it.
- **British spelling:** colour, favourite, organise.
- **No em dashes or en dashes in sentences.** Use a full stop, a colon or a comma.
- **Numbers as digits** in data copy ("between 4 and 5", "above 4.5").
- **An ellipsis character (…)** for an action in progress ("Getting your shelves ready…"), never three dots.
- **No exclamation marks.**
- A full stop ends a gloss, caption, body line or snackbar. Headlines, labels and buttons take none.
