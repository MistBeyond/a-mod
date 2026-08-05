---
name: feature-specs
description: Governs code-specific specs under docs/features/. They are active only when the user explicitly creates or maintains them. Use when touching code covered by a feature spec, deciding whether a doc belongs there, or creating/updating/removing one. Never invent, apply, or edit undocumented specs.
---

# Feature Specs

`docs/features/` stores user-authored, code-specific specifications. A spec is active only when the user explicitly
created or maintains it.

## Active-spec checklist

A spec is active only when all the following hold:

- The user explicitly created or maintains it.
- The spec declares its own scope at the top (which code it covers).
- The covered code's Javadoc references it with `{@code docs/features/<name>.md}`.

If any part is missing, or you are unsure, ask the user; do not guess.

## Javadoc references

- Reference a feature spec from the Javadoc of the entry types it covers (the interfaces or classes that guard the
  feature).
- Use the inline code tag exactly: `{@code docs/features/<name>.md}`.
- Add a sentence such as "Read {@code docs/features/<name>.md} before modifying this code. Any change to this code must
  be reflected in that document."
- When the spec file is moved or renamed, update every Javadoc reference in the same change.

## Boundaries

- Never create, update, or delete a feature spec on your own initiative. Propose the change and ask the user first; wait
  for approval.
- Do not treat a file under `docs/features/` as active merely because it exists.
- Apply a spec only to the code it explicitly covers. Never extend its rules to unrelated code.
- Do not enumerate specific feature specs in AGENTS.md, docs/architecture.md, or other meta documents; code Javadoc is
  the only pointer.

## Keeping specs in sync

- After the user approves a spec change, keep the spec, the covered code, and its Javadoc references in sync.

## Naming

- Name spec files `docs/features/<hyphen-case-name>.md`.
