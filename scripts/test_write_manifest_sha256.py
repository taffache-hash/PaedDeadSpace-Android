from __future__ import annotations

from pathlib import Path

import pytest

from scripts.write_manifest_sha256 import _validate_relative_path


def test_nested_core_manifest_is_an_allowed_release_file() -> None:
    _validate_relative_path(Path("provenance/core-v1.0.0/MANIFEST_SHA256.txt"))


def test_output_manifest_cannot_checksum_itself() -> None:
    with pytest.raises(ValueError, match="cannot checksum itself"):
        _validate_relative_path(Path("MANIFEST_SHA256.txt"))


@pytest.mark.parametrize(
    "path",
    [Path("../secret"), Path("release-key.jks"), Path(".gradle/cache.bin")],
)
def test_local_or_sensitive_paths_are_rejected(path: Path) -> None:
    with pytest.raises(ValueError):
        _validate_relative_path(path)
