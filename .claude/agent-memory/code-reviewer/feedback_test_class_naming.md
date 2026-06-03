---
name: test-class-naming
description: Test file names must match the class name they contain. BookEntityMapperTest.kt was found to contain class BookMapperTest — a mismatch that makes test discovery and navigation unreliable.
metadata:
  type: feedback
---

File `BookEntityMapperTest.kt` in `core/data/database/mapper/` contains a class named `BookMapperTest` instead of `BookEntityMapperTest`. The style guide and project convention require the file name to match the primary class name.

**Why:** Mismatched file/class names break IDE navigation ("go to test"), confuse test runners when filtering by class name, and make the split between the two mapper test files ambiguous.

**How to apply:** Always rename the class when renaming the file, or rename the file when creating a new class from a split.
