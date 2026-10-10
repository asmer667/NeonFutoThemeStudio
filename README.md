# FUTO Theme Studio Offline — Source Project

An offline-first Android companion project for creating and exporting FUTO Keyboard Advanced Theme ZIP files. It is a separate studio app; it does not replace the keyboard UI and is not merged into the user's `Ky` keyboard fork.

## Included

- 6,000 generated color palettes with numeric navigation.
- 5,000 editable theme recipes.
- **500 shape variants in five families:** 100 classic, 100 geometric, 100 neon, 100 3D-inspired, and 100 creative.
- Shape library is paged (20 choices per page), with category selection, numeric jump, visual thumbnails, and live Arabic/English keyboard preview. The full 500-item index is also in `SHAPE_CATALOG.csv`.
- Shape controls include corner radius, key size, opacity, text size, gradients, and custom color selection.
- 20 bundled font files with license notices.
- Local image selection for keyboard background and normal/functional/action/pressed key-border assets.
- FUTO ZIP import/export with `theme.txt`, selected font, border PNG assets, and optional background image.
- Before saving, export builds a temporary ZIP, validates required metadata and every rule/asset/font/background reference, then displays an archive summary (file count, rule counts, declared assets, font and background).
- When an imported theme's palette, shape family, shape variant, or corner radius is edited, existing border asset filenames are kept but their image contents are regenerated using the new palette/shape. Imported custom rules, selectors, icon files, font and unrelated extra files remain in the archive.
- No Internet permission is declared; normal operation is offline.

## FUTO compatibility boundaries

The exporter follows the documented Advanced Theme structure: `theme.txt`, supported color fields, font/background options, border assets, and border matchrules. FUTO uses nine-slice border assets, so each generated shape is exported as a PNG border and may be stretched to fit keys. The actual appearance depends on FUTO's renderer and the keyboard version. This companion project does not modify the keyboard engine.

The FUTO format documents colors, roundedness, fonts, icons, background images, and border assets selected by matchrules. It does **not** document a separate color for Arabic vs Latin glyphs or an independent image for every individual letter key. Those two options are preview-only in this studio unless the keyboard engine is extended.

## Build automatically on GitHub

The project includes `.github/workflows/android-build.yml`. When the **contents of this project folder** are placed at the root of a GitHub repository and pushed, GitHub Actions installs Java 17, Gradle 8.9, and Android SDK 35, then runs `clean assembleDebug`. On success, download `NeonFutoThemeStudio-debug-apk` from the workflow run's **Artifacts** section. A manual run is also available from **Actions → Android APK build → Run workflow**.

Do not upload only the `app/` directory: the root `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, and `.github/workflows/android-build.yml` must be included. Open the project in Android Studio with JDK 17 if you prefer to build locally.

## Startup diagnostics and testing limits

If an exception occurs while the main screen is being initialized, the app now shows a diagnostic screen with the stack trace and a **Copy error report** button instead of immediately hiding the cause. This is diagnostic handling, not proof that every device-specific crash has been fixed. The source has not been compiled in this environment because Android SDK/Gradle tooling is unavailable. The first GitHub Actions run is the build verification step; after a successful build, install the APK and test startup, image selection, import/export, and theme import in the exact FUTO Keyboard version you use.

## ZIP validation helper

`tools/verify_futo_theme_zip.py` checks TOML syntax, required metadata, `selector` fields, referenced and declared border/icon assets, font/background paths, and common image signatures (including WebP content even when the filename ends in `.png`). It has been checked against the supplied working Pet Keys / Animal theme ZIP. Passing this check is a structural check only; always import the ZIP into the exact FUTO Keyboard build you use for final compatibility verification.

## Startup crash diagnosis

The app now renders a small boot screen before constructing the full editor. Startup failures thrown while constructing the editor are caught as `Throwable` and displayed in a diagnostic page with a copy-report button. This is a diagnostic safeguard, not proof that every possible native/process crash is fixed. If the app still disappears without this page, collect Android Logcat for package `com.futo.themestudio` and share the `FATAL EXCEPTION` block.

## Crash fix (2026-10-10)
The startup crash report on Android 15 showed `IllegalArgumentException: needs >= 2 number of colors` while drawing a `GradientDrawable`. The preview background now always supplies at least two colors when gradients are disabled (the same color twice), avoiding the one-color shader crash. This is a targeted source fix; a full Android build/device run still must be verified by GitHub Actions and on-device testing.

## Full theme round-trip and icon assets

Importing an existing Advanced Theme ZIP now retains the archive's file entries and original TOML configuration as the editing base. Export gives the copy a new name, author and ID, updates the available Material 3/keyboard color fields and roundedness, and keeps the source selectors and asset declarations. If the user changes the palette or shape, the existing border filenames are retained while their image data is regenerated for the chosen design. User-selected replacement border images override generated images for their matched role; selected icon images update only the matching icon asset. The original font/background and unrelated extra files are retained unless the user explicitly changes those options. The importer rejects unsafe ZIP paths, duplicate filenames and unusually large archives to reduce the risk of malformed or hostile input.

For themes created from scratch, the exporter uses the bundled full Advanced Theme template based on the supplied working Animal/Pet Keys structure. It emits `theme.txt` (TOML), all 27 specialized border rules, all 15 border asset declarations/images, all 5 icon rules/declarations/images, a selected bundled font, and an optional background. The images are generated from the selected palette/shape or replaced with user-selected images. When an existing theme is imported, its own rules/assets are retained instead of being replaced by the starter template. Custom icon images can be chosen independently.

## Compatibility test scope

`tools/verify_futo_theme_zip.py animal-theme.zip` checks the supplied Animal reference fixture successfully. It validates archive structure and references, not the keyboard renderer itself. The app now performs a structural validation and shows a ZIP contents summary before asking where to save. The exact generated ZIP must still be imported into the target FUTO Keyboard build for final runtime confirmation. This environment has no configured Android SDK/Gradle build tool, so this source ZIP is not represented as a compiled or device-tested APK. The GitHub Actions workflow remains the reproducible APK build route.
