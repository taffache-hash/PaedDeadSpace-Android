from __future__ import annotations

import hashlib
from pathlib import Path
import unittest


ROOT = Path(__file__).resolve().parents[1]
SOURCE_MANIFEST = ROOT / "provenance" / "core-v1.0.0" / "MANIFEST_SHA256.txt"
BUNDLE = ROOT / "app" / "src" / "main" / "python" / "paeddeadspace"
EXPECTED = {
    "__init__.py",
    "core.py",
    "models.py",
    "provenance.py",
}


class CoreBundleSourceTest(unittest.TestCase):
    def test_bundled_sources_match_the_official_v1_manifest(self) -> None:
        manifest_entries: dict[str, str] = {}
        for line in SOURCE_MANIFEST.read_text(encoding="utf-8").splitlines():
            if not line.strip():
                continue
            digest, relative_path = line.split(maxsplit=1)
            manifest_entries[relative_path.replace("\\", "/")] = digest.lower()

        self.assertEqual(EXPECTED, {path.name for path in BUNDLE.glob("*.py")})
        for filename in sorted(EXPECTED):
            bundled = BUNDLE / filename
            actual = hashlib.sha256(bundled.read_bytes()).hexdigest()
            expected = manifest_entries[f"src/paeddeadspace/{filename}"]
            self.assertEqual(expected, actual, filename)


if __name__ == "__main__":
    unittest.main()
