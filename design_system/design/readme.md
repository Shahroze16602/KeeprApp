# Keepr Design System

Keepr is a completely free Android photo-cleaning app. People sort photos one month at a time by swiping right to keep and left to delete. There are no ads, subscriptions, in-app purchases, entitlements, locked months, or usage limits.

This is a custom visual language, not Material 3: chunky ink borders, hard offset shadows, oversized rounded shapes, enormous numerals, and springy card physics. The current HTML prototype and the two handoff documents under `uploads/` are the design and implementation sources of truth.

## Product principles

- Local first: photos, filenames, dates, media identifiers, and decisions stay on-device.
- Explicit deletion: Android owns the final confirmation; Keepr reconciles the actual result before reporting success.
- Reversible until commit: every decision can be reviewed and changed before deletion.
- Every month is available. Selected-photo cleanup is available when Android grants partial access.
- Progress survives process death. Empty, denied, interrupted, unavailable-item, and partial-deletion states all have recovery paths.
- Analytics are anonymous, optional, and off by default.

## Voice

- Warm, concise, and encouraging: “March cleared” rather than clinical storage language.
- Buttons use verbs: Keep, Delete, Retry, Resume, Review.
- Irreversible actions say “delete permanently”; reassuring copy explains exactly when Android confirmation occurs.
- Color never carries meaning alone. Icons, labels, counts, and state descriptions reinforce every status.

## Foundations

- Keep: tangerine `--keep-500 #FF6B2C`.
- Delete: cyan-blue `--gone-500 #00B8F0`.
- Reward: gold `--reward-500 #FFC63C`; success: green `--win-500 #2ED77E`.
- Dark theme is `:root`; warm-paper light theme is `[data-theme="light"]`.
- Minimum interactive target is 48×48 px in the prototype and 48×48 dp in Android.
- Visible keyboard focus and reduced-motion behavior are required.
- Arabic uses RTL direction; English, Spanish, and French use LTR.

Archivo 400–900 is the selected type family. The web prototype loads it from Google Fonts. The Android implementation must vendor the font files in `res/font` and must not depend on a network font request.

Phosphor Fill is the selected icon language. The web prototype loads its preview font from a CDN. Android must map each used glyph to a reviewed local vector drawable. The authored Keepr wordmark and compact K mark in `ui_kits/keepr-app/logo.jsx` are the current brand marks.

## Components

Components live under `components/` and are exposed from `window.KeeprDesignSystem_30a628` by `_ds_bundle.js`.

- Controls: Button, IconButton, SegmentedControl.
- Surfaces: PhotoCard, PileTile, LevelCard. LevelCard supports active and done states; it never gates access.
- Progress: ProgressRing, StatNumber, StreakBadge.
- Feedback: Stamp, ComboCounter, Badge.

All pressable components include pointer-cancel recovery. Icon-only controls require an accessible name. PhotoCard accepts `imageAlt`; decorative stacked cards use an empty description.

## App prototype

Open `ui_kits/keepr-app/index.html`. The prototype covers:

- Splash, language, onboarding, and Android photo access.
- Permanent denial and partial-access recovery.
- Month picker, selected-photo mode, cleanup session, item-load recovery, review, confirmation, deletion progress, partial-deletion recovery, and completion.
- Empty library/month and process-restored session states.
- Settings, privacy policy, analytics consent, feedback, rating, and local reset.
- Dark/light themes, reduced motion, keyboard focus, accessible controls, responsive phone framing, and RTL direction.

The demo uses Picsum and CDN-hosted development libraries only to render the browser prototype. Those are not production dependencies and must not be copied into Android.

## Files

- `styles.css` and `tokens/`: global style entry and design tokens.
- `components/`: JSX primitives, TypeScript declarations, prompts, and specimen cards.
- `guidelines/`: foundation specimen cards.
- `ui_kits/keepr-app/`: the latest and only app flow.
- `uploads/design-prd.md`: behavior and UX source of truth.
- `uploads/code-prd.md`: Kotlin/Jetpack Compose implementation contract.
- `_ds_bundle.js`, `_ds_manifest.json`, `_adherence.oxlintrc.json`: generated component artifacts synchronized with the source files.
