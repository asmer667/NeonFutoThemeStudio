# Ultimate Theme Creator Studio

A Flutter/Dart Android-first project scaffold for authoring FUTO Keyboard theme packages.

## Current implementation

- Multi-screen studio shell with dashboard and editor sections.
- Central `ThemeProject` model used by the preview and exporter.
- Live Arabic keyboard preview with suggestion/action rows and functional keys.
- Editing for core colors, gradient presets, key shapes, background selection, font choices, metadata, and layout.
- Local saved-project list using `shared_preferences`.
- ZIP export built with `archive`; generated package contains `theme.txt`, `README.md`, `LICENSE.txt`, and starter PNG assets bundled from the supplied sample theme.
- ZIP import picker and basic archive validation.
- TOML writer based on the real structure seen in the supplied working Pet Keys theme (`[options]`, `[colors]`, `[[matchrules.border]]`, `[[matchrules.icon]]`, `[[asset.border]]`, `[[asset.icon]]`).

## Important compatibility note

This is a working foundation, not a claim of complete FUTO compatibility. FUTO's format is versioned and selectors/assets/options have exact semantics. Validate exported ZIPs with the official FUTO Keyboard advanced-theme importer on a device. The generator intentionally emits a conservative theme and validates that referenced assets exist in the generated archive. Do not assume Flutter-only visual effects (animation, arbitrary gradients, blur, sound) can be represented by FUTO's theme format unless translated to supported assets/options.

## Requirements

- Flutter stable (Dart 3.3+)
- Android Studio or VS Code with Flutter extension
- Android SDK

## Run

```bash
flutter pub get
flutter analyze
flutter test
flutter run
```

## Build APK

```bash
flutter build apk --release
```

Flutter is not installed in the generation environment, so this source package has not been compiled into an APK here.

## Folder structure

- `lib/models/theme_project.dart` — project state and serialization
- `lib/services/theme_export_service.dart` — TOML and ZIP generation
- `lib/services/project_store.dart` — local saved projects
- `lib/widgets/keyboard_preview.dart` — live Arabic/English keyboard preview
- `lib/screens/studio_shell.dart` — navigation and screen set
- `lib/screens/editor_screen.dart` — editing panels
- `test/` — unit tests for model and export text generation
- `docs/FUTO_FORMAT_NOTES.md` — format notes and validation checklist
