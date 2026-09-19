from __future__ import annotations

from dataclasses import dataclass
import hashlib
import json
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
OFFICIAL_MANIFEST = ROOT / "provenance" / "core-v1.0.0" / "MANIFEST_SHA256.txt"
CORE_PREFIX = "src/paeddeadspace/"
SUPPORTED_VERSION = "1.0.0"


@dataclass(frozen=True)
class CoreBundleReport:
    package_version: str
    changed_files: list[str]
    missing_files: list[str]
    unexpected_files: list[str]

    @property
    def ok(self) -> bool:
        return not (self.changed_files or self.missing_files or self.unexpected_files)


def verify_core_bundle(bundle_path: str | Path, metadata_path: str | Path) -> CoreBundleReport:
    bundle = _resolve(bundle_path)
    metadata_file = _resolve(metadata_path)
    metadata = json.loads(metadata_file.read_text(encoding="utf-8"))
    package_version = str(metadata["package_version"])
    if package_version != SUPPORTED_VERSION:
        raise ValueError(f"Unsupported Core package version: {package_version}")

    declared_manifest = str(metadata["source_manifest"])
    if declared_manifest != OFFICIAL_MANIFEST.name:
        raise ValueError(f"Unsupported source manifest: {declared_manifest}")

    expected = _core_manifest_entries(OFFICIAL_MANIFEST)
    actual_files = {
        path.relative_to(bundle).as_posix(): path
        for path in bundle.rglob("*")
        if path.is_file() and "__pycache__" not in path.parts
    }

    missing = sorted(set(expected) - set(actual_files))
    unexpected = sorted(set(actual_files) - set(expected))
    changed = sorted(
        filename
        for filename in set(expected) & set(actual_files)
        if _sha256(actual_files[filename]) != expected[filename]
    )
    return CoreBundleReport(
        package_version=package_version,
        changed_files=changed,
        missing_files=missing,
        unexpected_files=unexpected,
    )


def _resolve(path: str | Path) -> Path:
    candidate = Path(path)
    return candidate if candidate.is_absolute() else ROOT / candidate


def _core_manifest_entries(path: Path) -> dict[str, str]:
    entries: dict[str, str] = {}
    for line in path.read_text(encoding="utf-8").splitlines():
        if not line.strip():
            continue
        digest, relative_path = line.split(maxsplit=1)
        normalized = relative_path.replace("\\", "/")
        if normalized.startswith(CORE_PREFIX):
            entries[normalized.removeprefix(CORE_PREFIX)] = digest.lower()
    if not entries:
        raise ValueError("Official manifest contains no Core package entries")
    return entries


def _sha256(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main() -> int:
    report = verify_core_bundle(
        "app/src/main/python/paeddeadspace",
        "app/src/main/assets/core_release.json",
    )
    if report.ok:
        print(f"Core bundle matches published v{report.package_version} source bytes.")
        return 0
    print(f"Changed: {report.changed_files}")
    print(f"Missing: {report.missing_files}")
    print(f"Unexpected: {report.unexpected_files}")
    return 1


if __name__ == "__main__":
    raise SystemExit(main())
