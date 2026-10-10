#!/usr/bin/env python3
"""Structural validator for FUTO Advanced Theme ZIPs (Python 3.11+)."""
import sys
import zipfile
import tomllib
import struct

PNG = b"\x89PNG\r\n\x1a\n"
WEBP_RIFF = b"RIFF"
JPEG = b"\xff\xd8\xff"
GIF87 = b"GIF87a"
GIF89 = b"GIF89a"


def image_problem(filename: str, data: bytes) -> str | None:
    # FUTO themes may contain WebP data in a file named .png (as in Pet Keys).
    if data.startswith(PNG):
        if len(data) < 24:
            return f"Truncated PNG image: {filename}"
        width, height = struct.unpack(">II", data[16:24])
        if width < 1 or height < 1:
            return f"Invalid PNG dimensions: {filename}"
        return None
    if len(data) >= 12 and data.startswith(WEBP_RIFF) and data[8:12] == b"WEBP":
        return None
    if data.startswith(JPEG) or data.startswith(GIF87) or data.startswith(GIF89):
        return None
    return f"Unrecognized image data: {filename}"


def main(path: str) -> int:
    errors = []
    try:
        with zipfile.ZipFile(path) as archive:
            file_list = [n for n in archive.namelist() if not n.endswith("/")]
            names = set(file_list)
            if len(file_list) != len(names):
                errors.append("ZIP contains duplicate file names")
            if "theme.txt" not in names:
                errors.append("Missing root theme.txt (FUTO Advanced Theme uses TOML in this file)")
                config = {}
            else:
                try:
                    config = tomllib.loads(archive.read("theme.txt").decode("utf-8-sig"))
                except Exception as exc:
                    config = {}
                    errors.append(f"theme.txt is not valid TOML: {exc}")

            for field in ("name", "author", "id", "version", "description"):
                if not config.get(field):
                    errors.append(f"Missing required metadata field: {field}")
            if not isinstance(config.get("version"), int):
                errors.append("Metadata 'version' must be an integer")

            matchrules = config.get("matchrules", {})
            assets = config.get("asset", {})
            border_rules = matchrules.get("border", [])
            icon_rules = matchrules.get("icon", [])
            border_assets = assets.get("border", [])
            icon_assets = assets.get("icon", [])
            if not border_rules:
                errors.append("No [[matchrules.border]] rules found")
            if not border_assets:
                errors.append("No [[asset.border]] definitions found")

            referenced = set()
            for kind, rules in (("border", border_rules), ("icon", icon_rules)):
                for index, rule in enumerate(rules, 1):
                    selector = rule.get("selector")
                    asset_name = rule.get("asset")
                    if not isinstance(selector, str) or not selector.strip():
                        errors.append(f"{kind} rule #{index} is missing required 'selector'")
                    if not isinstance(asset_name, str) or not asset_name:
                        errors.append(f"{kind} rule #{index} is missing 'asset'")
                    elif asset_name not in names:
                        errors.append(f"{kind} rule references missing file: {asset_name}")
                    else:
                        referenced.add(asset_name)

            declared = set()
            for kind, definitions in (("border", border_assets), ("icon", icon_assets)):
                for index, definition in enumerate(definitions, 1):
                    asset_name = definition.get("name")
                    if not isinstance(asset_name, str) or not asset_name:
                        errors.append(f"{kind} asset definition #{index} has no name")
                    elif asset_name not in names:
                        errors.append(f"Declared {kind} asset missing from ZIP: {asset_name}")
                    declared.add(asset_name)
            for filename in referenced:
                if filename not in declared:
                    errors.append(f"Referenced asset has no [[asset.border]]/[[asset.icon]] declaration: {filename}")

            # Verify paths used by optional font/background configuration.
            font_section = config.get("options", {}).get("font", {})
            font_name = font_section.get("font") if isinstance(font_section, dict) else None
            if font_name and font_name not in names:
                errors.append(f"Selected font file is missing from ZIP: {font_name}")
            background_section = config.get("options", {}).get("background", {})
            bg_name = background_section.get("image") if isinstance(background_section, dict) else None
            if bg_name and bg_name not in names:
                errors.append(f"Background image file is missing from ZIP: {bg_name}")

            for filename in file_list:
                lower = filename.lower()
                if lower.endswith((".png", ".webp", ".jpg", ".jpeg", ".gif")):
                    problem = image_problem(filename, archive.read(filename))
                    if problem:
                        errors.append(problem)
    except (OSError, zipfile.BadZipFile) as exc:
        print(f"FAIL: cannot read ZIP: {exc}")
        return 2

    if errors:
        print("FAIL — found issues:")
        for error in errors:
            print(f" - {error}")
        return 1
    print("PASS — ZIP, TOML, metadata, selector rules, declared assets, referenced files, fonts/backgrounds and image signatures look structurally valid.")
    print("Note: only importing the ZIP into the target FUTO Keyboard build can confirm full runtime compatibility.")
    return 0


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: python verify_futo_theme_zip.py path/to/theme.zip")
        raise SystemExit(2)
    raise SystemExit(main(sys.argv[1]))
