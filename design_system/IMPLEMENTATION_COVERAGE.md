# Keepr design and behavior coverage

## Screen registry — 22 / 22

| ID | Screen | Native implementation | States covered |
|---|---|---|---|
| SCR-01 | Splash | SplashScreen + Keepr SplashScreenContent | booting, bounded progress, monetization handoff |
| SCR-02 | Language | KeeprLanguageScreen | 10 choices, selected, settings return |
| SCR-03 | Onboarding | KeeprOnboardingScreen | 3 panels, paging, privacy |
| SCR-04 | Media access | MediaAccessScreen | required, selected, full, denial |
| SCR-05 | Month picker | MonthPickerScreen | skeleton, empty, denied, partial, populated, error |
| SCR-06 | Selected photos | SelectedPhotosScreen | empty/disabled, populated, reselect/full access |
| SCR-07 | Cleanup | CleanupSessionScreen | loading, active, touch swipe, controls, reduced motion, access/load recovery |
| SCR-08 | Media recovery | MediaRecoveryScreen | retry, skip, return |
| SCR-09 | Review | ReviewScreen | keep/delete/unresolved, regroup, early review, zero-delete |
| SCR-10 | Confirmation | DeletionConfirmationScreen | exact count, batches, cancel |
| SCR-11 | Deletion | DeletionProgressScreen | system prompt, reconcile, error, complete/partial |
| SCR-12 | Completion | CompletionScreen | complete and unresolved recovery |
| SCR-13 | Privacy | KeeprPrivacyScreen | local data, deletion, analytics, monetization disclosure |
| SCR-14 | Settings | KeeprSettingsScreen | every designed row/toggle and diagnostics |
| SCR-15 | Analytics | AnalyticsConsentScreen | opt-in, off, withdrawal/reset |
| SCR-16 | Feedback | FeedbackScreen | validation, safe diagnostics, copy/share |
| SCR-17 | Rate | RateUsScreen | 1–5 choice, Play, feedback, dismiss |
| SCR-18 | Reset | ResetKeeprScreen | confirm/cancel, local-only reset |
| SCR-19 | Permission denied | PermissionDeniedScreen | denied/permanent, settings, selected, privacy |
| SCR-20 | Empty library/month | EmptyLibraryScreen | library and month copy/actions |
| SCR-21 | Resume | ResumeSessionScreen | resume, review, restart, picker |
| SCR-22 | Partial deletion | PartialDeletionScreen | truthful counts, review/retry/picker |

## Component registry — 12 / 12

Button, IconButton, SegmentedControl (review tabs), LevelCard, PhotoCard, PileTile, ProgressRing, StatNumber, StreakBadge, Badge, ComboCounter, and Stamp are implemented with Keepr tokens, Archivo, hard outlines/shadows, pressed semantics, and accessibility labels.

## Functional journeys — 9 / 9

First run; full/selected/denied media access; month catalog; resumable card triage; review/regroup; API 29 and API 30–36 deletion confirmation/reconciliation; completion/gamification; settings/privacy/analytics/feedback/rate/reset; denial/empty/resume/partial recovery.

## Deliberate product override

The starter app's ads, Premium, billing, RevenueCat, Remote Config, Firebase, and app-open flow are retained per user instruction. Premium removes ads only and never gates cleanup. Inline ads occupy reserved regions outside Keepr photo/control content. Six dummy Feature One/Two placements are retired.
