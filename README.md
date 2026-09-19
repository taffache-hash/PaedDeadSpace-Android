# PaedDeadSpace Android

PaedDeadSpace Android is an offline educational and research application for healthcare professionals. It supports reproducible exploration of predefined apparatus dead-space scenarios using the frozen `PaedDeadSpace-Core v1.0.0` scientific package.

The app does not diagnose or treat any condition, recommend ventilator settings, or provide patient-specific clinical decision support. Its outputs describe a mathematical model and are not clinical recommendations. Apparatus dead space is represented separately from patient dead space.

## Privacy and offline operation

Version 1 has no accounts, advertising, analytics, telemetry, network communication, case storage, or export. It requests no runtime permissions. Users must not enter names, record numbers, or other patient-identifying information.

See [PRIVACY_POLICY.md](PRIVACY_POLICY.md) and [docs/APP_INTENDED_USE.md](docs/APP_INTENDED_USE.md) for the complete boundaries.

## Scientific authority

The app invokes the bundled, byte-verified `PaedDeadSpace-Core v1.0.0`; Kotlin and Compose do not reproduce its physiological equations. Core provenance is pinned in `app/src/main/assets/core_release.json`:

- repository: <https://github.com/taffache-hash/PaedDeadSpace-Core>
- release DOI: <https://doi.org/10.5281/zenodo.22838225>
- concept DOI: <https://doi.org/10.5281/zenodo.22838224>

## Development status

This repository is under pre-release verification. Debug builds and unsigned release artifacts are not public clinical products and are not ready for Google Play production distribution.
