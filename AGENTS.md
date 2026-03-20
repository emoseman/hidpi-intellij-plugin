# Repository Summary

## Purpose

This repository contains `Modern HiDPI Profiles`, an IntelliJ IDEA Community plugin for saving and applying font-oriented IDE profiles across different display setups. It supports manual profile switching and optional display-based auto-switching when the active window moves between monitors.

## Core Behavior

- Captures the current IntelliJ font-related settings into a `HidpiProfile`
- Applies saved profiles back to the IDE
- Stores profiles in persistent plugin state
- Detects the current display and matches it against saved auto-switch rules
- Exposes profile actions through the settings UI and IntelliJ actions

## Main Packages

- `src/main/java/com/emoseman/hidpi/model`
  Data classes for profiles, display rules, font settings, and persisted state.
- `src/main/java/com/emoseman/hidpi/services`
  Core application services:
  `HidpiProfilesService` persists profiles,
  `IntellijIdeSettingsAccessor` captures/applies IDE settings,
  `ProfileApplicationService` orchestrates application + notifications,
  `AutoSwitchService` evaluates rules,
  `DisplayEventBridgeService` and `StartupInitializationService` trigger auto-switch checks.
- `src/main/java/com/emoseman/hidpi/display`
  Display detection and display signature matching.
- `src/main/java/com/emoseman/hidpi/settings`
  IntelliJ settings/configurable UI for managing profiles and editing font values.
- `src/main/java/com/emoseman/hidpi/actions`
  User-invokable actions for applying, saving, cycling profiles, and toggling auto-switch.
- `src/main/java/com/emoseman/hidpi/util`
  Reflection-based helpers for IntelliJ UI settings that are not exposed through stable direct APIs.

## Current Configurable Settings

The plugin currently captures/applies these settings when IntelliJ APIs allow it:

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

- Display matching is best-effort, not true physical DPI detection.
- Rules use graphics device id, bounds, and scale.
- Newer detection code prefers native monitor resolution where available, while remaining compatible with older saved logical-bound rules.

## Persistence and UI Notes

- Profiles are stored in `modern-hidpi-profiles.xml` via `PersistentStateComponent`.
- The settings panel now supports manual editing of the selected profile's font values instead of only capturing the current IDE settings.
- Settings changes are staged in the configuration UI and persisted on Apply.

## Development

- Build: `gradle build`
- Test: `gradle test`
- Run in sandbox IDE: `gradle runIde`

## Constraints

- Some UI-level changes may require restart or may not be writable on all IntelliJ/JBR/platform combinations.
- Global IDE scale is intentionally not force-applied automatically.
- Display identifiers and scale behavior vary across Linux, Windows, macOS, and different JVM/JBR builds.
