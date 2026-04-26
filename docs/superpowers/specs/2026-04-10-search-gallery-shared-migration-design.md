# Search/Gallery Shared Screen Migration Design

## Summary

- Goal: continue the Compose Multiplatform migration by moving `search` and `gallery` screen implementations from `app` into `shared`.
- Scope is intentionally split into two phases:
  - Phase A: migrate `search` screen and only the minimum shared UI helpers it needs.
  - Phase B: extract reusable image/viewer UI building blocks, then migrate `gallery` screen.
- Android must remain buildable throughout. iOS compile checks for `shared` and `htmlText` must continue to pass.

## Design

### Phase A: Search Screen

- Move the full `SearchScreenRoute` and `SearchScreen` implementation into `shared`.
- Keep the public route contract already migrated into `shared`:
  - `SearchArgs`
  - `searchScreenNavigationRoute`
  - `navigateToSearch`
  - `SearchViewModel`
- Migrate only the minimum helper set required by `search`:
  - `LazyPagingItems.rememberLazyListState()`
  - `LazyListScope.pagingRefreshItem(...)`
  - `LazyListScope.pagingAppendMoreItem(...)`
  - paging load/error composables used by those helpers
  - `String.toTimeText()` and its `toDateTime()` helper
- Leave Android navigation registration in `app`, but make it call the `shared` `SearchScreenRoute`.
- Do not migrate unrelated helpers or adjacent screens in this phase.

### Phase B: Gallery Screen

- First migrate the smallest reusable UI dependencies needed by `gallery`:
  - `BackIcon`
  - `GalleryImage`
- `GalleryImage` must be rewritten to avoid Android-only screen code patterns in `shared`:
  - remove direct `LocalContext` coupling from the screen layer
  - keep current gestures and save-image affordance
  - continue to rely on `LocalImageSaver` for the actual save action
- After those helpers are in `shared`, move `GalleryScreenRoute` and `GalleryScreen` into `shared`.
- Leave Android navigation registration in `app`, but make it call the `shared` route/screen implementation.

## Public APIs / Interfaces

- No new user-facing behavior changes are intended.
- `shared` becomes the source of truth for:
  - `search` route contract + ViewModel + screen
  - `gallery` route contract + ViewModel + screen
  - the small shared UI helpers extracted during migration
- `app` keeps:
  - Android host wiring
  - platform-only services and implementations
  - navigation registration shells until the full NavHost is migrated

## Risks And Decisions

- `search` is low-risk because its remaining dependencies are mostly pure Compose helpers.
- `gallery` is the main uncertainty because `GalleryImage` currently mixes generic UI with Android-oriented image request setup.
- Decision taken:
  - Do not migrate `gallery` in the same patch as `search`.
  - Keep the work split so `search` can land independently.

## Validation

- Required after each phase:
  - `./gradlew :shared:compileKotlinIosSimulatorArm64 :app:compileFossDebugKotlin`
  - `./gradlew :app:assembleFossDebug :shared:compileKotlinIosSimulatorArm64 :htmlText:compileKotlinIosSimulatorArm64`
- Expected known failure remains unchanged:
  - `./gradlew build` may still fail on the existing Android test dependency lock conflict involving `androidx.concurrent`.

## Assumptions

- The migration remains incremental; duplicated registration shells in `app` are acceptable temporarily.
- We are not solving the global Android test dependency lock conflict in this slice.
- We prefer moving real code to `shared` over introducing wrapper abstractions with no reuse value.
