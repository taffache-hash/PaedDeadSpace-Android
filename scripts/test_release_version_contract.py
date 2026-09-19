from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]


def test_android_v101_release_metadata_is_consistent() -> None:
    build_file = (ROOT / "app" / "build.gradle.kts").read_text(encoding="utf-8")
    citation = (ROOT / "CITATION.cff").read_text(encoding="utf-8")
    changelog = (ROOT / "CHANGELOG.md").read_text(encoding="utf-8")
    manifest = (ROOT / "RELEASE_MANIFEST.md").read_text(encoding="utf-8")
    readme = (ROOT / "README.md").read_text(encoding="utf-8")

    assert 'versionCode = 2' in build_file
    assert 'versionName = "1.0.1"' in build_file
    assert 'version: "1.0.1"' in citation
    assert '## [1.0.1] - 2026-09-19' in changelog
    assert 'Current app version: `1.0.1` (`versionCode` 2)' in manifest
    assert 'Version 1.0.1 is the reviewed Android source release.' in readme
