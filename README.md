# Modern HiDPI Profiles

Modern HiDPI Profiles restores and modernizes the classic HiDPI profile workflow for IntelliJ IDEA Community.

## Installation

### Install from a plugin ZIP

1. Download the plugin ZIP artifact.
   For this project, the built artifact is typically `build/distributions/modern-hidpi-profiles-1.1.4.zip`.
2. In IntelliJ IDEA, open **Settings / Preferences > Plugins**.
3. Click the gear icon and choose **Install Plugin from Disk...**.
4. Select the ZIP file and restart the IDE if prompted.

### Build and install from source

1. Build the plugin ZIP:

```bash
gradle buildPlugin
```

2. Find the generated ZIP in `build/distributions/`.
3. In IntelliJ IDEA, open **Settings / Preferences > Plugins**.
4. Click the gear icon and choose **Install Plugin from Disk...**.
5. Select the generated ZIP and restart the IDE if prompted.

### Fast local redeploy into your installed IDE

If you want to iterate against your normal IntelliJ install instead of the Gradle sandbox, use the local deploy tasks.

1. Set the IntelliJ launcher path once per shell or pass it inline:

```bash
export LOCAL_IDE_EXECUTABLE="$HOME/.local/share/JetBrains/Toolbox/apps/idea-community/ch-0/243.25659.59/bin/idea"
```

2. Optionally set a process match string if the default executable name is not specific enough:

```bash
export LOCAL_IDE_PROCESS_MATCH="idea-community"
```

3. Redeploy and restart IntelliJ:

```bash
./gradlew redeployLocalIde
```

You can also override paths with Gradle properties:

```bash
./gradlew redeployLocalIde \
  -PlocalIdeExecutable=/path/to/idea \
  -PlocalIdePluginsDir=/path/to/JetBrains/IdeaIC2024.3/plugins
```

Available tasks:

- `deployLocalIde` builds the plugin ZIP and installs it into the local IDE plugins directory
- `stopLocalIde` stops a running IntelliJ process matched by `localIdeProcessMatch` or `LOCAL_IDE_PROCESS_MATCH`
- `startLocalIde` launches IntelliJ from `localIdeExecutable` or `LOCAL_IDE_EXECUTABLE`
- `redeployLocalIde` stops IntelliJ, installs the fresh plugin build, and launches IntelliJ again

Notes:

- `localIdePluginsDir` still defaults automatically based on `platformType` and `platformVersion`
- On macOS, `localIdeExecutable` may be either the `.app` bundle path or a launcher binary
- On Windows, set `localIdeProcessMatch` to the executable name used by `taskkill`, for example `idea64.exe`

### Run it in a sandbox IDE during development

1. Start a sandbox IntelliJ instance:

```bash
gradle runIde
```

2. Wait for the sandbox IDE to launch with the plugin already installed.
3. Test the plugin from:
   - **Settings / Preferences > Appearance & Behavior > HiDPI Profiles**
   - **Tools > HiDPI Profiles**
   - **Find Action**

### Uninstall or update

- To update, install a newer plugin ZIP from disk and restart IntelliJ when prompted.
- To remove the plugin, open **Settings / Preferences > Plugins**, locate **Modern HiDPI Profiles**, uninstall it, and restart the IDE.

## What problem this plugin solves

Many developers switch between displays (4K external, 1080p laptop, projector, remote desktop) and need different font sizes and families for comfortable readability. This plugin lets you save complete font-oriented profiles and switch quickly.

## Manual profile workflow

1. Tune IDE/editor/console fonts to your preferred setup.
2. Open **Settings / Preferences > Appearance & Behavior > HiDPI Profiles**.
3. Click **Add Current** to capture the current settings as a named profile.
4. Later apply from:
   - Settings page **Apply** button, or
   - **Tools > HiDPI Profiles** action group, or
   - **Find Action** (Search Everywhere).

## Auto-switch workflow

1. Create or select a profile.
2. Click **Detect Current Display** to inspect active monitor signature.
3. Click **Create Rule from Current Display** to bind selected profile to that display.
4. Enable **display-based auto-switch**.
5. Moving/focusing windows across displays triggers debounced evaluation and applies matching profile.

## Supported settings

- Editor font family and size
- Editor line spacing
- Console font family and size
- Console line spacing
- UI font family/size where current platform APIs expose writable values
- Presentation mode font size where writable

## Unsupported or limited settings

- True cross-platform physical DPI is not reliable from IntelliJ/JVM APIs on all OS/JBR combinations
- Rule matching therefore uses monitor id, display bounds, and transform scale as best-effort approximation
- IDE/JVM global scale is intentionally not force-applied automatically for safety and may require restart/manual handling

## OS caveats (Linux/Windows/macOS)

- Linux (X11/Wayland) may expose different monitor identifiers or scale behavior depending on WM/compositor/JBR
- Windows per-monitor scale and mixed DPI setups can report transform scales that are close but not exact
- macOS scaling may produce logical resolutions instead of physical pixel dimensions

## Restart-required caveats

Some UI-level changes may not fully propagate live on all platform versions. The plugin surfaces this as warning/restart-required where applicable.

## Development

### Build

```bash
gradle build
```

### Run in IntelliJ sandbox

```bash
gradle runIde
```

### Test

```bash
gradle test
```
