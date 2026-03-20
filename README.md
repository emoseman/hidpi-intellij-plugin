# Modern HiDPI Profiles

Modern HiDPI Profiles restores and modernizes the classic HiDPI profile workflow for IntelliJ IDEA Community.

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
