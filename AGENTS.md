# AGENTS.md

## Core Requirement

- **Must use IDEA MCP for code operations** (e.g., rename, refactor, search, debug, format).  
  If IDEA MCP is not available or not started, **immediately stop and inform the user** that IDEA MCP support is
  required.

## Prohibited Actions

1. **Do not modify any files outside the project source code and build scripts** (e.g., system files, Gradle wrapper
   files, library caches).
2. **Do not add any dependencies without explicit user consent**. If needed, present options and await user decision.
3. **Do not change any project dependency definitions** (including but not limited to `build.gradle`,
   `build.gradle.kts`, `settings.gradle`, `settings.gradle.kts`, `gradle.properties`) without user approval. This
   includes NeoForge version, mod versions (e.g., Jade), etc.
4. **Do not download anything without user consent**. Show exact actions (e.g., using `gradlew` for dependency
   management) and await approval.
5. **Do not hand-write or manually edit generated resources under `src/generated/resources` (models, language files,
   tags, etc.). Update the corresponding providers under `src/datagen` (Java or Kotlin) and regenerate with datagen.**

## Workflow

1. **Before modification**: Read code and documentation, understand existing features and interfaces, make a plan.
2. **Ensure package nullability**: Every Java package must include a `package-info.java` annotated with `@NullMarked`
   (JSpecify). Use `$ensure-package-info` to create any missing files.
3. **After modification**:
    - Use IDEA MCP to analyze the project, check for errors, and fix them. Fix warnings where possible; ignore only if
      unavoidable (e.g., fixed Guava version requiring beta graph API).
    - Use IDEA MCP to format code.
    - Do **not** commit code; commits are only performed upon explicit user request.

## Knowledge Strategy

- **Priority order**:
    1. Javadoc (inline documentation)
    2. Code itself
    3. Official documentation
    4. Web search
    5. Model's internal knowledge
- **Must combine with documentation and code; never answer solely from internal knowledge.**
- Always first read inline Javadoc from project or dependency source code via IDEA MCP or other means. If Javadoc is
  insufficient or guidance is needed, perform web search. For specific targets, select the correct version from the
  official website and read documentation for that version. **If documentation is inaccessible, do not silently skip;
  inform the user**.
- **Mandatory official documentation sites** (choose the version matching your project dependency):
    - NeoForge: https://docs.neoforged.net/docs/gettingstarted/
    - Mixin: https://github.com/SpongePowered/Mixin/wiki

## Tool Usage (including but not limited to)

- All code operations (formatting, search, refactoring, debugging) must be executed through IDEA MCP.
- Use PowerShell when running terminal commands through IDEA MCP.
- Gradle tasks (e.g., build, test, datagen) should run through IDEA MCP first; fall back to the `gradlew` CLI only when
  IDEA MCP cannot run them.
