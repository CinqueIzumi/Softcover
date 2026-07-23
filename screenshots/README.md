# Screenshots

Drop the images referenced by the root [`README.md`](../README.md) here. The filenames below are
what the README's Screenshots table expects — keep them exact so the table renders.

| Filename | Surface | Notes |
|---|---|---|
| `reading.png` | Reading | Greeting, streak strip, hero card with pace and progress |
| `library.png` | Library | A status shelf in the dense grid, control line and filter chips in frame |
| `book-detail.png` | Book Details | Title, author, rating, shelve selector, About drop cap |
| `explore.png` | Explore | Most-anticipated hero, then the Up-next-in-your-series shelf |
| `profile-stats.png` | Profile, Reading Atlas | Total pages, Volumes, Avg. rating, Streak |
| `profile-taste.png` | Profile, taste | Year bar chart plus the genre breakdown |

## Capture guidance

- **Phone shots:** portrait, a clean device frame or a bezelless capture. Keep every shot the
  same dimensions so the table columns line up (the current set is 1080×2400, a Galaxy S21).
- `adb exec-out screencap -p > screenshots/<name>.png` captures straight off a connected device.
- Stay in one theme across the whole set (the current set is dark).
- Prefer real catalogue data over empty states, and avoid zeroed stats where you can.
- Compress before committing (e.g. `pngquant` / `oxipng`) — these live in git history forever.

The root README shows all six in a 3×2 table. Keep it that way: every file here is referenced, and
anything the table stops using should be deleted rather than left behind. After adding or renaming
files, update that table to match.

## Play Store frames

The committed files above are cropped (status bar removed) and quantised for the README. The Play
listing frames are built from full-quality, uncropped captures kept in `store/`, which is gitignored
— a device mockup wants its status bar, and the originals are four times the size. Recreate that
folder when the listing next needs reshooting, and delete it again once the frames are built.

Frames carry the app's own palette, not a separate marketing one. From
`core/designsystem/.../theme/Color.kt`: background `#1A110F` dark / `#FFF8F6` light, accent
`#FFB5A0` dark / `#8F4C38` light, text `#F1DFDA` dark / `#231917` light. The bright red used before
the 3.1.0 redesign is no longer a brand colour — the only red left in the theme is the error role.

Eight frames, in this order. The first two are angled brand frames; the rest are straight device
shots. Captures are deliberately reused between the two groups, so the angled pair teases what
frames 4 and 5 then pay off in full.

| # | Style | Capture | Headline |
|---|---|---|---|
| 1 | angled | `reading.png` | Softcover |
| 2 | angled duo | `book-detail.png` + `library.png` | Book smart. |
| 3 | straight | `reading.png` | Pick up / where you left off |
| 4 | straight | `library.png` | Your whole shelf, / at a glance |
| 5 | straight | `book-detail.png` | Every book, / in full |
| 6 | straight | `explore.png` | Find your / next book |
| 7 | straight | `profile-stats.png` | See the shape / of your reading |
| 8 | straight | `profile-taste.png` | Learn what / you reach for |

Frame 3 carries the search preview on its own, since 1 and 2 are branding — it needs the clearest
statement of the core job, which is why Reading sits there rather than the prettier book detail.

Second line takes the accent colour. Keep each line to four words or fewer so it holds at thumbnail
size, and write in the app's own second-person editorial voice rather than store-generic feature
labels.

Second line takes the accent colour. Keep each line to four words or fewer so it holds at thumbnail
size, and write in the app's own second-person editorial voice rather than store-generic feature
labels.
