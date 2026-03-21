# Repository Summary

## Purpose

This repository contains `Modern HiDPI Profiles`, an IntelliJ Platform plugin for saving, editing, and applying IDE font-focused profiles across different monitor setups. It supports manual profile switching, default profiles, and optional display-based auto-switching when the active IDE window moves between displays.

## Tech Stack

- Java 21
- Gradle
- `org.jetbrains.intellij.platform` Gradle plugin
- IntelliJ Platform application services, startup activities, settings configurable UI, and actions
- JUnit 4 for tests

## Core Behavior

- Captures the current IDE font-related settings into a `HidpiProfile`
- Lets users manually edit saved profile values in the settings UI before persisting
- Applies a saved profile back to editor, console, and some UI settings
- Persists profiles and plugin flags with `PersistentStateComponent`
- Tracks the last applied profile to avoid redundant auto-switch reapplication
- Detects the current display and matches it against saved auto-switch rules
- Exposes profile management through both the settings panel and Tools menu actions
- Shows notifications for apply success, warnings, and failures

## Main Packages

- `src/main/java/com/emoseman/hidpi/model`
  Data objects for profiles, display rules, font settings, and persisted plugin state.
- `src/main/java/com/emoseman/hidpi/services`
  Core application services and startup hooks.
  `HidpiProfilesService` owns persistent state and profile lifecycle.
  `IntellijIdeSettingsAccessor` captures and applies IDE settings.
  `ProfileApplicationService` applies profiles, updates last-applied state, and emits notifications.
  `AutoSwitchService` debounces and evaluates display-based profile matching.
  `DisplayEventBridgeService` and `StartupInitializationService` trigger auto-switch evaluation from IDE lifecycle events.
- `src/main/java/com/emoseman/hidpi/display`
  Current-display detection plus display signature matching helpers.
- `src/main/java/com/emoseman/hidpi/settings`
  IntelliJ settings/configurable UI, table model, and staged editing behavior.
- `src/main/java/com/emoseman/hidpi/actions`
  User-invokable actions for save/apply/next/previous/toggle-auto-switch flows.
- `src/main/java/com/emoseman/hidpi/util`
  Reflection-based accessors for UI settings that are not exposed cleanly by stable APIs.

## Important Classes

- `HidpiProfile`
  Stores profile identity, name, default flag, restart flag, editor/console font settings, UI font fields, presentation mode size, and an optional `DisplayRule`.
- `HidpiProfilesState`
  Persists state version, `autoSwitchEnabled`, `lastAppliedProfileId`, and the profile list.
- `HidpiProfilesService`
  Validates unique profile names case-insensitively, enforces a single default profile, duplicates/renames/deletes profiles, and replaces the full profile list from staged settings UI edits.
- `IntellijIdeSettingsAccessor`
  Captures from `EditorColorsManager` and `UISettings`, applies changes with rollback on failure, and intentionally does not auto-apply IDE scale changes.
- `ProfileApplicationService`
  Central apply path. Updates `lastAppliedProfileId` only on success and reports warnings through IntelliJ notifications.
- `DisplayDetectionService`
  Resolves the focused or visible IDE window to a `GraphicsConfiguration`, derives scale, and prefers native monitor resolution when available.
- `DisplayInfo`
  Encodes the display signature and matches rules against either native bounds or legacy logical bounds.
- `HidpiProfilesSettingsPanel`
  Maintains an in-memory staged copy of profiles, supports direct editing of font values, and only persists on configurable Apply.

## Current Configurable Settings

The plugin currently captures and applies these settings when IntelliJ APIs allow it:

- Editor font family
- Editor font size
- Editor line spacing
- Console font family
- Console font size
- Console line spacing
- UI font family
- UI font size
- Presentation mode font size

## Display Matching Notes

- Display matching is best-effort. It is not true physical DPI detection.
- Rules may match on graphics device id, bounds, and scale.
- Bounds matching accepts either native monitor bounds or older logical bounds so saved legacy rules still work.
- Scale matching uses a tolerance of `0.05`.
- Auto-switch evaluation is debounced in `AutoSwitchService` before applying a matched profile.

## Persistence And UI Notes

- Persistent state is stored in `modern-hidpi-profiles.xml`.
- The persistent component name is `ModernHidpiProfiles`.
- Settings UI changes are staged in the table/editor model and persisted only when the configurable Apply action runs.
- The settings panel can apply the currently selected staged profile immediately without persisting the entire staged state first.
- Profile names must be non-blank and unique case-insensitively.
- Only one profile should be marked default at a time.

## Plugin Registration

- Plugin metadata and registrations live in `src/main/resources/META-INF/plugin.xml`.
- Application services are registered for profile storage, IDE settings access, profile application, auto-switching, and display detection.
- Startup activities initialize auto-switch at project startup and install the AWT event bridge for focus and move events.
- Actions are exposed under the Tools menu group `HiDPI Profiles`.

## Development

- Build: `gradle build`
- Test: `gradle test`
- Run in sandbox IDE: `gradle runIde`

## Test Coverage

Current tests are small unit tests around core model and service behavior:

- `ProfileSummaryTest`
  Verifies `HidpiProfile.summary()`.
- `DisplayInfoMatchTest`
  Verifies display rule matching, scale tolerance, and legacy logical-bounds compatibility.
- `HidpiProfilesStateTest`
  Verifies add, duplicate, rename, delete, default-profile lifecycle, and duplicate-name rejection.

When changing display matching, persistence, or profile lifecycle logic, extend these tests first.

## Constraints

- Some UI-level settings may not be writable on every IntelliJ, JBR, or platform combination.
- UI font changes may require restart depending on platform behavior.
- Presentation mode font size writes are best-effort and may produce warnings instead of hard failure.
- Global IDE scale is intentionally not force-applied automatically for safety.
- Display identifiers, transforms, and native resolution reporting vary across Linux, Windows, macOS, and different JVM or JBR builds.
- Reflection-based UI setting access is inherently fragile across platform versions.

## Change Guidance

- Keep profile persistence backward compatible when possible. Older saved rules may contain logical bounds instead of native bounds.
- Prefer routing profile application through `ProfileApplicationService` rather than calling the accessor directly from new UI or action code.
- Be careful not to break staged-edit behavior in the settings configurable by writing directly to persisted state from UI listeners.
- If adding new persisted fields, update copy logic and consider state loading defaults.


## Git Management
- Each tested, completed feature or bugfix gets committed to the git repository with meaningful summarizing git commit message

## Version Management
- Each tested, completed feature will increase the minor version number
  - When a new minor version is made, the patch version resets to 0
- Each tested, completed bugfix will increase the patch version number