# PaedDeadSpace Android design

## Purpose and intended use

PaedDeadSpace Android is an offline educational and research application for healthcare professionals. It presents the PaedDeadSpace apparatus dead-space model for reproducible exploration of predefined input scenarios.

It is not intended to diagnose, treat, recommend ventilator settings, or provide patient-specific clinical decision support. The app must not collect, store, transmit, or export patient-identifying information.

## Governing scientific release

The application uses PaedDeadSpace-Core v1.0.0 as its sole scientific authority. The Android project must not reproduce, alter, or independently reimplement Core equations, constants, provenance rules, source-domain warnings, or model-boundary logic.

Every Android release records:

- the embedded Core version;
- the Core GitHub release and Zenodo DOI;
- the Core package checksum or immutable source reference; and
- the Android application version and release date.

PaedDeadSpace-UI v1.0.0 is a presentation and interaction reference, not the Android runtime implementation.

## Runtime architecture

The app is a native Android application written in Kotlin with Jetpack Compose. A minimal Android-to-Python bridge invokes the bundled PaedDeadSpace-Core package locally. The bridge has one responsibility: translate validated UI values into Core objects and translate Core structured outputs, warnings, and known errors into a stable Android result contract.

The data flow is:

```text
Explicit user input -> Android view model -> Core bridge -> PaedDeadSpace-Core v1.0.0 -> structured result -> Compose result screen
```

The Compose UI, view model, and bridge must not calculate clinical or physiological outputs. Only non-scientific display formatting, navigation, and localization are permitted outside the Core.

The app operates without network access, authentication, analytics, telemetry, cloud services, case storage, or export. It requests no permissions beyond those demonstrably essential to the delivered application; version 1 requires none.

## Mobile user experience

The app has a deliberately small workflow:

1. An initial educational-use notice identifies the professional audience and the non-clinical scope.
2. A New scenario screen collects explicit inputs.
3. A Results screen shows the structured Core output and links to secondary provenance information.

The input screen requires explicit selection of patient dead-space model and age unit. Weight, age, tidal volume, and respiratory rate have no hidden defaults. Apparatus dead space may start at zero, but its name and qualification remain explicit. No patient name, record number, free-text clinical narrative, or data persistence is included.

The result screen prioritizes absolute tidal volume, patient-only dead space, apparatus dead space, total dead space, alveolar tidal volume, alveolar ventilation, relative carbon-dioxide burden, and respiratory rate required to preserve baseline alveolar ventilation. It contains no graphs in version 1.

Warnings appear before quantitative results. For Core validation failures or the `VD >= VT` model boundary, the app identifies the mathematical boundary, preserves any safe partial values supplied by the Core, and suppresses outputs that the Core marks as nonphysical. It never turns a model boundary into a clinical threshold.

Sources, provenance, software versions, and model limitations appear in a collapsible Details and provenance area. A New scenario action clears the in-memory screen state only.

## Scientific and language guardrails

All user-facing language follows these rules:

- describe the app as educational and research software for healthcare professionals;
- use exploratory, descriptive wording for model outputs;
- retain Core labels for benchmark, reference/sensitivity construction, source-domain warning, and model breakdown;
- state that apparatus dead space is separate from patient dead space;
- do not imply that user-entered settings are appropriate, safe, normal, therapeutic, or individualized;
- do not introduce disease mode, absolute PaCO2 output, decision rules, traffic-light states, target zones, or recommendation wording.

The intended-use statement and Play Store listing must remain consistent with these behavior constraints. Calling the app educational does not justify functionality that would act as clinical decision support.

## Verification

The Android release candidate must pass both bridge-level and UI-level tests.

Bridge-level equivalence tests compare structured Android results with expected Core v1.0.0 outputs for:

- the Pearsall 10 kg golden benchmark;
- each accepted patient dead-space model;
- source-domain warnings;
- invalid or incomplete input;
- zero apparatus dead space; and
- model-breakdown scenarios where total dead space is greater than or equal to tidal volume.

UI tests verify explicit selection behavior, error/warning visibility, lack of hidden defaults, correct units, results ordering, details/provenance access, and clearing of in-memory scenario data.

The release checklist includes manual tests on supported Android devices or emulators, an offline-mode run, and a documented equivalence report against the frozen Core data.

## Release sequence

The Android component is a distinct product:

1. Create and test `PaedDeadSpace-Android` with a release manifest linking Core v1.0.0.
2. Publish a tagged GitHub release with source code, release notes, license, citation metadata, checksums, and the relevant Android build artifact.
3. Archive that exact release in Zenodo and record the DOI.
4. Update the PaedDeadSpace manuscript after the Android GitHub and Zenodo records exist.
5. Prepare the Google Play listing, publicly accessible privacy policy, Health Apps declaration, data-safety disclosure, and educational-use notices.
6. Distribute through Play testing, then publish to production only after final release, manuscript, and listing checks are complete.

The manuscript is not edited during implementation. Its final Android update must cite the frozen Android repository and DOI and must preserve the current educational, non-prescriptive scope.

## Out of scope for version 1

- patient records, identifiers, accounts, synchronization, export, or sharing;
- medical-device claims, diagnosis, prescription, treatment recommendations, or clinical decision support;
- new physiological equations or Core changes;
- disease mode, absolute PaCO2 output, or clinical thresholds;
- graphing features beyond essential numerical result presentation;
- iOS, web hosting, and Windows changes.

## External publication requirements

Before any Google Play production release, the final feature set and listing must be rechecked against current Google Play health-content, privacy, and data-safety requirements. The project must provide a clear public privacy policy and only make claims supported by the published scientific scope.

Relevant publication references:

- Google Play Health Content and Services policy: https://support.google.com/googleplay/android-developer/answer/16679511
- Google Play Health Apps declaration guidance: https://support.google.com/googleplay/android-developer/answer/14738291
- PaedDeadSpace-Core v1.0.0: https://github.com/taffache-hash/PaedDeadSpace-Core
- PaedDeadSpace concept DOI: https://doi.org/10.5281/zenodo.22838224
- PaedDeadSpace-Core v1.0.0 DOI: https://doi.org/10.5281/zenodo.22838225
