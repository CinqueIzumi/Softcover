---
name: publish-design-system
description: Build the Softcover design preset and publish it to the project's one shared Claude Design System artifact, updating it in place. Use when asked to publish, sync or update the design system / design preset, and after a merge that changes tokens, components, gallery fixtures or design/project.
disable-model-invocation: true
---

# Publish the design system

Publishes `build/design-preset/project/` (built by `designPresetBundle`) to the project's one shared Claude
Design System artifact. The code is the source of truth and the artifact is its generated view: nothing flows
back from the page. The format the files follow is the Design System Artifact type
(`https://claude.ai/artifact/5M7UeXXcx16TP3vzVFNDzd`); `action: "read"` with that `type_url` shows its
`SKILL.md`, and its `artifact-type/reference/format.md` holds every cap.

## 1. Build

Run `scripts/gradle-quiet.sh designPresetBundle`. It renders the showcase, exports the tokens and assembles
`build/design-preset/project/`; stop on failure.

## 2. Find the target

Read `artifactUrl` from `design/design-system.properties` (committed; one artifact for the whole project).

- **Present:** Artifact `read` with `url` and `path` = `project/design-system.json`. The result must say the
  person can edit it ("writer"); if not, stop and tell them to ask the artifact's owner for edit access from its
  Share menu. Keep the result: step 5 merges into it. Also list `scope: "files"` to know what is already
  published.
- **Absent:** only when the user confirms the project has no system yet. Artifact `publish` with `type_url` =
  the type above, `title` = `Softcover`, `auto_open` = `after_first_write`, no files; write the returned url to
  `design/design-system.properties` as `artifactUrl=<url>` and tell the user to commit it and share the
  artifact with the team. This is a new system: its index gets `createdOnFiles` in step 5.

Never pass `type_url` to an existing system; that makes a second one.

## 3. Upload the assets

Every non-text file under `build/design-preset/project/assets/<Group>/` (SVG logo, icons, illustrations) is
an asset upload, not a file. Artifact `publish` with `url`, `asset: true` and `file_paths` (≤25 per call)
uploads them. From each result record `{"name": <file name>, "blob": <id from /_blob/<id>>, "size": <bytes>,
"type": <media type>}` under `assetGroups.<Group>.files.<name>` (a key escapes bytes outside
`[A-Za-z0-9_./-]` as `~` + two hex), and the names in tile order under `assetGroups.<Group>.order`. Each
entry is `{"name": <Group>, "tile": …, "order": […], "files": {…}}`; `groups` is `["Logos", "Icons",
"Illustrations"]` with tiles `l`, `xs`, `m`. On an update, re-upload only files whose bytes changed and keep the
other records as they are; never delete an upload.

## 4. Publish the files

Everything else under `build/design-preset/project/` goes as files, at `project/<path>`: `README.md`,
`Palettes.md`, `tokens.json`, `components/**`, `fonts/**`, `renders/**` and each `assets/<Group>/README.md`.
Artifact `publish` with `url`, `root` = `build/design-preset`, `file_path` = the absolute path of one file, and
`files` mapping `project/<path>` → `project/<path>` for the rest:

- at most 255 paths and 16 MiB per call; split by size, `renders/` in the middle calls, and
  `project/components/Cover/preview.html` in the last one (the cover is written last);
- on an update, also map `project/<path>` → `null` for every file the listing showed under `project/` that the
  bundle no longer has, except `project/design-system.json` and anything the page generates (`tokens.css`,
  `manifest.json`, `api/`);
- never send `project/design-system.json` here.

## 5. Write the index, last

Write `build/design-preset/project/design-system.json` and publish it alone, in the final call.

- **New system:** `{"v": 3, "layout": "files", "createdOnFiles": {"v": 1, "at": "<now, ISO-8601>"},
  "title": "Softcover", "namespace": "Softcover", "libraries": [], "sections": {}, "groups": [...],
  "assetGroups": {...}, "blobs": {}, "docs": {"sections": []}, "lastChange": {...}}`. `libraries` stays `[]`:
  the previews are static HTML and CSS, and `bundle.js` only defines an empty `window.Softcover`.
- **Update:** `read` the index again right before this call, keep every key it has (its marker, `title`,
  `sections`, `blobs`, `docs`, records a person added), and change only `groups`, the `assetGroups` records
  from step 3 and `lastChange`.
- `lastChange` = `{"by": "<the person's name, else Claude>", "at": "<now>", "via": "CI · /publish-design-system
  from <short commit sha>", "note": "<one line: what changed>"}`. The `CI` prefix makes the page warn that edits
  made there are overwritten by the next publish.

A refusal because someone saved meanwhile: read the index again, redo the merge once; a second refusal, stop and
say so.

## 6. Report

Give the artifact link, the counts (files, uploads, removals) and anything skipped. Don't open or pin it unless
asked.
