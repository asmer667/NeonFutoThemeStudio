#!/usr/bin/env python3
"""Validate the bundled complete Advanced Theme TOML template using Python 3.11+."""
from pathlib import Path
import tomllib

ROOT = Path(__file__).resolve().parents[1]
TEMPLATE = ROOT / "app/src/main/assets/templates/advanced_theme_template.txt"

def main() -> int:
    try:
        config = tomllib.loads(TEMPLATE.read_text(encoding="utf-8"))
    except Exception as exc:
        print(f"FAIL: bundled template is not valid TOML: {exc}")
        return 1

    errors = []
    for key in ("name", "author", "id", "version", "description"):
        if not config.get(key):
            errors.append(f"missing top-level metadata: {key}")
    rules = config.get("matchrules", {})
    assets = config.get("asset", {})
    border_rules = rules.get("border", [])
    icon_rules = rules.get("icon", [])
    border_defs = assets.get("border", [])
    icon_defs = assets.get("icon", [])
    if len(border_rules) < 20:
        errors.append(f"expected full selector template, found only {len(border_rules)} border rules")
    if len(icon_rules) < 5:
        errors.append(f"expected standard icon rules, found {len(icon_rules)}")
    if len(border_defs) < 10:
        errors.append(f"expected multiple border assets, found {len(border_defs)}")
    if len(icon_defs) < 5:
        errors.append(f"expected five icon asset definitions, found {len(icon_defs)}")

    border_names = {item.get("name") for item in border_defs}
    icon_names = {item.get("name") for item in icon_defs}
    for kind, entries, declared in (("border", border_rules, border_names), ("icon", icon_rules, icon_names)):
        for i, entry in enumerate(entries, 1):
            if not entry.get("selector"):
                errors.append(f"{kind} rule #{i} missing selector")
            if not entry.get("asset"):
                errors.append(f"{kind} rule #{i} missing asset")
            elif entry["asset"] not in declared:
                errors.append(f"{kind} rule #{i} references undeclared asset {entry['asset']}")

    if errors:
        print("FAIL: bundled theme template checks failed:")
        for error in errors:
            print(f" - {error}")
        return 1
    print(f"PASS: complete template TOML; {len(border_rules)} border rules, {len(border_defs)} border assets, {len(icon_rules)} icon rules, {len(icon_defs)} icon assets.")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
