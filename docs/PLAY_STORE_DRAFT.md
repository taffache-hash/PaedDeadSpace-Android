# Google Play listing and declaration draft

This is a pre-release drafting source, not a submitted declaration. Recheck every field against the final AAB and current Google Play policy immediately before submission.

## Main store listing — English

**App name (30-character limit)**  
PaedDeadSpace

**Short description (80-character limit)**  
Offline educational dead-space model for healthcare professionals.

**Full description (4,000-character limit)**

PaedDeadSpace is an offline educational and research application for healthcare professionals. It presents a reproducible mathematical model for exploring how explicitly entered apparatus dead space contributes to a hypothetical or de-identified ventilation scenario.

Users select an age unit and patient dead-space model, enter scenario values, and label the source of the apparatus dead-space value. The app then displays the structured outputs and warnings returned by the embedded PaedDeadSpace-Core v1.0.0 scientific package. Apparatus dead space is shown separately from patient dead space, and source-domain warnings appear before numerical results.

PaedDeadSpace is not a medical device. It does not diagnose or treat any condition, recommend ventilator settings, or provide patient-specific clinical decision support. Results describe a mathematical scenario and do not establish that any setting is appropriate, safe, normal, therapeutic, or individualized.

Version 1 works entirely offline. It has no accounts, advertising, analytics, telemetry, cloud services, stored case history, sharing, or export. It requests no runtime permissions. Do not enter patient names, record numbers, clinical narratives, or other identifying information.

Scientific provenance, the embedded Core version, GitHub source, and DOI are available in the app's Details and provenance section.

## Classification and declarations — working answers

- App/game: App.
- Category: Medical.
- Intended audience: healthcare professionals; not designed for children.
- Ads: No.
- App access: all features available without login or special access.
- Health apps declaration: **Medical Reference and Education**. Do not select “My app doesn't provide any health features.”
- Medical-device status: not a medical device, based on the frozen educational, descriptive, non-prescriptive intended purpose; re-evaluate if features or claims change.
- Data safety: expected final answer is no data collected and no data shared; verify against the exact dependencies and final AAB.
- Sensitive permissions: none expected. AndroidX may declare an app-scoped signature permission for internal receiver protection; it is not a runtime or sensitive-data permission.
- Privacy-policy URL: **TBD — required before any Play testing or production submission.** Must be active, public, non-geofenced, non-editable, and not a PDF.

## Visual assets still requiring human approval

- 512 x 512 final high-resolution launcher icon matching the packaged icon.
- 1,024 x 500 feature graphic without efficacy, safety, ranking, or promotional claims.
- At least two phone screenshots from the final build, including the educational-use notice and a representative result with warnings/provenance where applicable.
- Localized screenshots and listing text for every supported Play Store language.

## Copy guardrails

Do not add claims about clinical validation, diagnosis, treatment, safety, improved outcomes, performance superiority, regulatory approval, or suitability of any user-entered setting. Do not use recommendation language, target zones, alarm language, or patient-specific examples.
