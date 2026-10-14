# FUTO theme format notes

The user-provided `animal-theme.zip` contained a working-looking theme package with:
- `theme.txt`
- PNG key/button assets such as `Button-default.png`, `Button-function.png`, `Button-space-press.png`
- icon assets such as `Icon-backspace.png`, `Icon-enter.png`
- `PetKeys-background.png`
- `Manosque-Regular.ttf`

The `theme.txt` structure included:
- metadata: `name`, `author`, `id`, `version`, `description`
- `[options]`
- `[colors]` using `#RRGGBBAA`
- `[[matchrules.border]]` with `selector` and `asset`
- `[[matchrules.icon]]` with `selector` and `asset`
- `[[asset.border]]` with `name`, tints, padding, slicing, gap, target density
- `[[asset.icon]]` with `name` and target density

## Before production release
1. Compare all fields/selectors with current official FUTO advanced-theme documentation.
2. Confirm supported `selector` values and rule ordering.
3. Confirm asset names and 9-slice values.
4. Export a ZIP and import it in the current FUTO Keyboard release.
5. Compare on-device appearance with the preview on Arabic and English layouts.
6. Test missing assets, malformed TOML, unusual names, and large images.
