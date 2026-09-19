# Intended use and scope

## Intended use

PaedDeadSpace Android is offline educational and research software for healthcare professionals. It presents the PaedDeadSpace mathematical model so that users can explore explicit, hypothetical, or de-identified apparatus dead-space scenarios in a reproducible way.

The application is not intended to diagnose, prevent, monitor, predict, prognose, treat, or alleviate disease. It is not intended to recommend ventilator settings or other clinical actions and does not provide patient-specific clinical decision support. Results describe a model scenario; they do not establish that a setting is appropriate, safe, normal, therapeutic, or individualized.

## Scientific boundary

All physiological outputs, source-domain warnings, provenance labels, and model-boundary handling come from the embedded, byte-verified `PaedDeadSpace-Core v1.0.0`. The Android interface validates explicit field formats and presents the structured response; it does not implement independent physiological equations.

The user explicitly selects the patient dead-space model and age unit. Required weight, age, tidal-volume, and respiratory-rate fields have no hidden defaults. Apparatus dead space is entered and labelled separately from patient dead space. If the Core reports a mathematical model boundary, the app suppresses nonphysical outputs and displays only the safe partial values supplied by the Core.

## Excluded uses and features

Version 1 does not include:

- diagnosis, treatment advice, alarms, decision rules, target zones, or recommendations;
- a disease mode or absolute arterial carbon-dioxide prediction;
- patient records, identifiers, accounts, synchronization, sharing, storage, or export;
- network access, analytics, telemetry, advertising, or device-sensor access.

Users must not enter patient names, record numbers, free-text clinical narratives, or other identifying information.
