from __future__ import annotations

import argparse
import hashlib
from pathlib import Path
import re


ROOT = Path(__file__).resolve().parents[1]
RELEASE_MANIFEST = ROOT / "RELEASE_MANIFEST.md"
OUTPUT = ROOT / "MANIFEST_SHA256.txt"
START = "<!-- sha256-files:start -->"
END = "<!-- sha256-files:end -->"
PATH_PATTERN = re.compile(r"^- `([^`]+)`$")
FORBIDDEN_PARTS = {".gradle", ".idea", "build-cache", "keystores", "credentials"}
FORBIDDEN_SUFFIXES = {".jks", ".keystore"}


def selected_files() -> list[str]:
    lines = RELEASE_MANIFEST.read_text(encoding="utf-8").splitlines()
    try:
        start = lines.index(START) + 1
        end = lines.index(END, start)
    except ValueError as error:
        raise ValueError("RELEASE_MANIFEST.md has no valid SHA-256 file block") from error

    paths: list[str] = []
    for line in lines[start:end]:
        if not line.strip():
            continue
        match = PATH_PATTERN.fullmatch(line.strip())
        if match is None:
            raise ValueError(f"Invalid release-file entry: {line}")
        relative = Path(match.group(1))
        _validate_relative_path(relative)
        paths.append(relative.as_posix())
    if not paths or len(paths) != len(set(paths)):
        raise ValueError("Release-file block must be nonempty and contain unique paths")
    return sorted(paths)


def render_manifest() -> str:
    entries: list[str] = []
    for relative in selected_files():
        path = ROOT / relative
        if not path.is_file():
            raise FileNotFoundError(relative)
        digest = hashlib.sha256(path.read_bytes()).hexdigest()
        entries.append(f"{digest}  {relative}")
    return "\n".join(entries) + "\n"


def _validate_relative_path(path: Path) -> None:
    if path.is_absolute() or ".." in path.parts:
        raise ValueError(f"Release path must remain inside the repository: {path}")
    lowered_parts = {part.lower() for part in path.parts}
    if lowered_parts & FORBIDDEN_PARTS or path.suffix.lower() in FORBIDDEN_SUFFIXES:
        raise ValueError(f"Sensitive or local path is not allowed: {path}")
    if path.as_posix() == OUTPUT.relative_to(ROOT).as_posix():
        raise ValueError("The checksum manifest cannot checksum itself")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    rendered = render_manifest()
    if args.check:
        if not OUTPUT.is_file() or OUTPUT.read_text(encoding="utf-8") != rendered:
            print("MANIFEST_SHA256.txt is missing or stale.")
            return 1
        print("MANIFEST_SHA256.txt matches all selected release files.")
        return 0
    OUTPUT.write_text(rendered, encoding="utf-8", newline="\n")
    print(f"Wrote {OUTPUT.name} for {len(selected_files())} release files.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
