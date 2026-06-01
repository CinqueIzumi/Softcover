---
name: feedback_apollo_nested_interface_imports
description: Apollo-generated deeply-nested interface types need explicit import aliases; dot-notation on type aliases doesn't resolve nested members
metadata:
  type: feedback
---

When mocking Apollo-generated types that are interfaces nested 3+ levels deep (e.g. `BookListFragment.Taggable_count.Tag.Tag_category`), import each level via its own `import ... as` alias. Dot-notation access on a type alias (e.g. `BookListTaggableCount.Tag_category`) does not compile in Kotlin.

Also: the `category` property on `Tag_category` clashes with `kotlin.text.CharCategory`; use `every { this@mockk.category } returns "..."` to pin the stub to the mock receiver.

**Why:** Discovered during tag category/count test additions — compiler reported `Unresolved reference 'Tag_category'` when using `mockk<BookListTaggableCount.Tag_category>`, and `CharCategory` type mismatch when using bare `every { category }`.

**How to apply:** For any Apollo fragment interface nested 3+ levels deep, add one `import ... as Alias` per level needed, then use the flat alias directly in `mockk<Alias>`.
