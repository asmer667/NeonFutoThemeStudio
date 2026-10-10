# FUTO theme ZIP compatibility audit

Reference fixture: user-supplied `animal-theme.zip` (Pet Keys theme).

## Reference structure verified
- `theme.txt` is at the ZIP root and contains TOML.
- The reference has 27 `[[matchrules.border]]` rules, 5 `[[matchrules.icon]]` rules, 15 `[[asset.border]]` definitions, and 5 `[[asset.icon]]` definitions.
- Each rule points to a ZIP asset by its `asset` name; declarations include image slicing/tint settings and target density.
- The archive includes 15 border-image assets, 5 icon assets, a font and a background image.
- `PetKeys-background.png` contains WebP data despite its `.png` extension, so image validation must inspect signatures rather than extensions alone.
- `python tools/verify_futo_theme_zip.py animal-theme.zip` passes for the supplied fixture.

## Implemented project changes
- Full import archive retention for all file entries, `theme.txt`, custom icon/border assets and all original matching/declaration rules.
- Import rejects traversal paths, duplicate entries, more than 512 entries, files above 32 MiB each or total extracted content above 100 MiB.
- Export of imported themes keeps all original rules, selector order, asset declarations, icon rules and unrelated extra files. The copy receives an updated name, author and collision-resistant ID; the available Material 3/keyboard colors, roundedness and background opacity are updated.
- When the user explicitly edits the palette, shape family/variant or corner radius, every declared border asset keeps its original filename and its image bytes are regenerated for the new style. If the user chooses a custom border image, that image overrides the generated image for all existing border assets matching the selected role without deleting the rules or declarations.
- Independent icon-image pickers allow replacing the referenced icon asset while preserving its selector and all other theme configuration.
- If a theme is created from scratch, the exporter starts from the bundled complete Animal-shaped TOML template and writes all 27 border selectors, all 15 border asset declarations/images, all 5 icon selectors/declarations/images, plus the selected font and optional background. Generated border assets use neutral white tint declarations so their rendered colors are not unexpectedly multiplied by the theme engine.
- TOML values for name/description are escaped safely, and the validator checks required metadata, selectors, declarations, references, fonts/backgrounds and PNG/WebP/JPEG/GIF signatures.

## Boundaries and testing
- The four-role UI does not expose every arbitrary custom selector in an imported theme as a separate editable control. All custom selectors/rules and declarations remain intact; when a visual redesign is requested, assets referenced by the original border declarations are regenerated in place, including assets used by specialized selectors.
- Color controls edit the fields currently exposed in the UI; other original color fields and theme options remain untouched.
- The archive validator is structural, not a runtime compatibility guarantee. Import the exported ZIP into the exact FUTO Keyboard build for final confirmation.
- Android SDK/Gradle are not installed in this environment; no successful APK build or device test is claimed.

## Final source changes
- Export now builds a temporary archive, validates required metadata and every border/icon reference, checks ZIP readability/CRC, verifies font/background references, and displays a summary of files/rules/assets before the user chooses a save location.
- Bottom navigation visibly marks the active page.
- Imported themes regenerate existing border-image assets when the palette or shape controls are changed, while preserving asset filenames and all selector/configuration rules.
- Build verification and device runtime import testing still require the GitHub Actions runner and the target Android/FUTO Keyboard installation.
