from __future__ import annotations

from pathlib import Path
import shutil

from scripts.verify_core_bundle import verify_core_bundle


def test_core_bundle_matches_the_declared_v1_release() -> None:
    report = verify_core_bundle(
        "app/src/main/python/paeddeadspace",
        "app/src/main/assets/core_release.json",
    )

    assert report.package_version == "1.0.0"
    assert report.changed_files == []
    assert report.missing_files == []
    assert report.unexpected_files == []


def test_core_bundle_reports_changed_missing_and_unexpected_files(tmp_path: Path) -> None:
    copied_bundle = tmp_path / "paeddeadspace"
    shutil.copytree("app/src/main/python/paeddeadspace", copied_bundle)
    (copied_bundle / "core.py").write_bytes(b"changed\n")
    (copied_bundle / "models.py").unlink()
    (copied_bundle / "extra.py").write_bytes(b"unexpected\n")

    report = verify_core_bundle(copied_bundle, "app/src/main/assets/core_release.json")

    assert report.changed_files == ["core.py"]
    assert report.missing_files == ["models.py"]
    assert report.unexpected_files == ["extra.py"]
