# Architecture Map

This document is the authoritative source for concrete package placement and the user's architecture rulings. Read it
before any structural change: new packages, moving or extracting classes, or changing dependency direction. General
design principles live in `design-principles.md`; this map records where things actually belong.

## How to use

- Before a structural change, check this map first.
- If the placement is documented, follow it without redesigning.
- If it is not documented, ask the user with concrete options (for example: "put this helper in `util` (recommended)
  or `core`?"). After the user confirms, add a row to "Decision log".
- A recorded user decision overrides generic best practice. Do not invent package boundaries.

## Package map

| Package            | Responsibility                                                                               | Belongs here                                                                 | Does not belong here                                                      |
|--------------------|----------------------------------------------------------------------------------------------|------------------------------------------------------------------------------|---------------------------------------------------------------------------|
| `block`            | Blocks, block entities, and block-level contracts (e.g. `WireBlock`, `IConnectableBlock`)    | Blocks, block entities, connection and shape contracts owned by blocks       | Energy network logic, generic transfer math                               |
| `block/entity`     | Block entity implementations by area (`logistic`, `machine`)                                 | Sync and ticking code for a block entity                                     | Shared domain logic                                                       |
| `client`           | Client-only UI and rendering                                                                 | Screens, client-side caches                                                  | Logic used by common or server code                                       |
| `config`           | Mod configuration                                                                            | Config types and loading                                                     | Feature logic                                                             |
| `core`             | Shared domain logic, registries, and contracts used across features                          | Cross-feature services and contracts                                         | Feature internals (e.g. `item.ElectricItem`, `integration` package names) |
| `core/logistic`    | Logistic and energy domain logic and interfaces; keep this area pure                         | Interfaces, pure data, and logic; implementations in `impl/`; `IConnectable` | Block- or item-specific behavior                                          |
| `core/registry`    | External `registry-lib` dependency (`com.mistbeyond.registry`); no longer lives in this repo | Registration contracts and implementations                                   | Feature registration glue                                                 |
| `integration`      | External integration glue (Jade, JEI)                                                        | Adapters that consume public APIs                                            | Feature logic                                                             |
| `inventory`        | Menus and inventory interaction                                                              | Menu classes and slot logic                                                  | Block or entity logic                                                     |
| `item`             | Item classes and components                                                                  | Item behavior and data components                                            | Shared domain logic                                                       |
| `recipe`           | Recipe types, serializers, ingredients, and displays                                         | Recipe domain types; `RecipeTypes` and `Ingredients` are shared contracts    | Framework glue                                                            |
| `util`             | Generic, feature-agnostic helpers                                                            | Helpers used by multiple packages                                            | Feature-specific logic                                                    |
| `data/*` (datagen) | Generated resource providers (`lang`, `model`, `recipe`, `tags`)                             | Providers and builders that generate resources                               | Hand-written data files; prefer datagen or vanilla resources              |

## Decision log

| Date       | Decision                                                                                                                                                                                                                                                                                                 | Reason                                                                                                                      |
|------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------|
| 2026-08-05 | `IConnectable` lives in `block`, not `core.logistic.energy`; connection state, update, and shape logic was extracted from `WireBlock` into `IConnectable`.                                                                                                                                               | `core.logistic.energy` stays a pure logic and interfaces package; block connection contracts belong with the block feature. |
| 2026-08-05 | Split `IConnectable`: pure connection contracts live in `core.logistic.IConnectable`; block connection state, placement/update state, and shapes live in `block.IConnectableBlock`. `WireBlock -> IConnectableBlock -> IConnectable` and `WireBlockEntity -> IWire -> IEnergyComponent -> IConnectable`. | User ruling: pure connection logic stays in logistic; block collision and state stay in the block package.                  |
| 2026-08-05 | `recipe.RecipeTypes` and `recipe.Ingredients` are cross-cutting contracts, not feature internals; using them from `core` is not dependency debt.                                                                                                                                                         | They are used by `util`, machines, menus, and JEI.                                                                          |
| 2026-08-05 | Implementations go in an `impl` subpackage under their contract package.                                                                                                                                                                                                                                 | Existing pattern in `core/registry` and `core/logistic`.                                                                    |
| 2026-08-05 | `core` must not gain new reverse dependencies on feature internals (`item`, `client`, `integration`).                                                                                                                                                                                                    | Keeps `core` stable; existing exceptions are listed in `design-principles.md`.                                              |
| 2026-08-05 | Prefer datagen over hand-written data files; use vanilla resources when possible; hand-write only as a last resort.                                                                                                                                                                                      | Keeps generated resources consistent and regenerable.                                                                       |
| 2026-08-09 | Extracted `core.registry` into the standalone local library `registry-lib` (`com.mistbeyond.registry`), consumed via `mavenLocal()` and bundled with jarJar.                                                                                                                                             | Reuse registry infrastructure across mods and keep the mod repo clean.                                                      |

## Feature specs

`docs/features/` holds user-authored, code-specific specs. A spec is active only when the user explicitly created or
maintains it; active specs declare their own scope and are referenced from the code they cover (e.g., code Javadoc).
Read an active spec before modifying the code it covers. Do not create, update, or delete feature specs without the
user's approval.
