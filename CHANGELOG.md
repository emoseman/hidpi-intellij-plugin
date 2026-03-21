# Changelog

## 1.1.2

- Stabilized font-family filtering in the settings UI so typing and selecting a font works reliably

## 1.1.1

- Refreshed open editors after profile apply so editor font size changes take effect immediately

## 1.1.0

- Added `deployLocalIde`, `stopLocalIde`, `startLocalIde`, and `redeployLocalIde` Gradle tasks for faster local plugin iteration
- `redeployLocalIde` can stop a running local IntelliJ instance, replace the installed plugin, and relaunch the IDE

## 1.0.0

- Initial release of Modern HiDPI Profiles
- Profile CRUD (add current, apply, rename, duplicate, delete, set default)
- Action integration (save/apply/next/previous/toggle auto-switch)
- Display signature detection and rule-based auto-switch with debounce
- Persistent state with versioned storage
- Settings page with profile table and management controls
