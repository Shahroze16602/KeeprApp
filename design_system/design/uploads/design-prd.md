# Keepr — Design Product Requirements Document

> **Artifact:** `design-prd.md`  
> **Workflow step:** 8 — Design PRD  
> **Source boundary:** Validated Steps 2–7, reconciled on 2026-08-02 against the production design in `ui_kits/keepr-app/`, repository tokens/components, and the approved permanent-free product direction. Step 7b is skipped because `spike_required` is empty.  
> **Product:** Keepr, Android photo cleaning one calendar month at a time.

This document is the behavior and UX source of truth. It preserves stable upstream IDs (`P`, `SEG`, `R`, `F`, `US`, `AC-US`, `OD`, `PC`, and `RISK`) while reconciling outdated commercial assumptions to the production design, and adds design acceptance IDs (`DAC`). Visual guidance is intentionally descriptive rather than tokenized.

**Design authority:** The production design source (`ui_kits/keepr-app/`, repository tokens, and component specifications) is authoritative for routes, labels, hierarchy, interaction, visual behavior, and available product states. This Design PRD may add required privacy, accessibility, deletion-safety, recovery, localization, and adaptive-layout behavior where the prototype is less explicit, but it may not introduce a route, control, commercial state, or behavior that conflicts with the design.

**Permanent product constraint:** The entire app is free. There are no ads, subscriptions, in-app purchases, entitlements, locked months, time limits, swipe limits, commercial experiments, or promotional interruptions. This is not a launch experiment.

## 1. Product Vision, Goals & Success Metrics

### Vision

Keepr turns an intimidating camera-roll cleanup backlog into a bounded, trustworthy game: choose one calendar month, make fast keep-or-delete decisions, review every destructive choice, and finish with unmistakable closure. The experience must feel proprietary and physically satisfying without ever making deletion ambiguous, hiding recovery, or resembling a stock Google template.

### Structural differentiator — exact Step 4 copy

> Keepr guarantees “every month stays free”: every user can open and complete any accessible calendar month or selected-photo group—from the first card through undo, final keep/delete review, Android confirmation, reconciliation, and completion—without an ad, swipe cap, subscription gate, purchase prompt, entitlement check, or promotional interruption.

The tactile visual system is mandatory product expression. The structural commitment is permanent free access combined with truthful, user-controlled deletion and local-first privacy.

### Product goals

| Goal | Goal | Required behavior | Primary metric | Rationale |
| --- | --- | --- | --- | --- |
| G1 | Deliver unlimited bounded cleanup | A user can select and finish any accessible calendar month or selected-photo group, regardless of prior use or item count, without commercial interruption. | month_completion_rate | P1, P2, P3; Step 3 gap; approved production design |
| G2 | Make every photo decision fast and unmistakable | Right means keep and left means delete; non-gesture controls provide identical outcomes; motion never hides intent or safety controls. | month_completion_rate | P4, P6; SEG-2; Step 3 gap |
| G3 | Make destructive outcomes trustworthy | Undo, pre-commit review, explicit confirmation, system-mediated deletion, reconciliation, and partial/failure recovery are always available. | deletion_reconciliation_rate | P5, P6; SEG-2; gate constraints |
| G4 | Create visible momentum and closure | Progress is legible throughout; completion reports kept, confirmed deleted, unresolved, and estimated reclaimed space when known, without streak guilt or nagging. | repeat_month_activation_rate | P1, P2, P7; SEG-3; Step 3 gap |
| G5 | Protect privacy and scope | Photo handling remains on-device; no Keepr account or cloud-backup claim is introduced; analytics is optional/off by default and media-derived data never leaves the device. | access_recovery_rate | P5; R9; PC-1 through PC-4 |
| G6 | Make repeat use frictionless | After completion, every other accessible month remains immediately available and core cleanup continues in airplane mode. | repeat_month_activation_rate | P1, P2, P3; approved permanent-free product constraint |

### Success metrics

| Metric ID | Name | Formula | Events | Limitations / guardrails |
| --- | --- | --- | --- | --- |
| month_start_rate | Month start rate | Distinct consented app instances with `month_started` divided by distinct consented app instances with `month_picker_viewed`. | month_picker_viewed, month_started | Excludes users who decline analytics.; A reinstall may create a new app instance.; Permission denial before month_picker_viewed is outside this denominator. |
| month_completion_rate | Month completion rate | Distinct consented app instances with `month_completed` divided by distinct consented app instances with `month_started`. | month_started, month_completed | Excludes users who decline analytics.; Completion may span processes.; The event reflects reconciled app state, not inferred provider behavior. |
| repeat_month_activation_rate | Repeat month activation rate | Distinct consented app instances starting another month after completion divided by distinct consented app instances with a completion. | month_completed, additional_month_started | Not a retention guarantee.; Analytics is optional and off by default. |
| review_correction_rate | Review correction rate | Sessions with at least one `review_item_moved` divided by sessions with `review_opened`. | review_opened, review_item_moved | Measures observed correction behavior, not error severity or trust. |
| deletion_reconciliation_rate | Deletion reconciliation rate | Terminal requested URIs reconciled as deleted/still present/unavailable divided by requested URIs. | delete_request_finished, deletion_reconciled | Provider behavior may remain unresolved; unresolved items are reported separately. |
| access_recovery_rate | Access recovery rate | Consented app instances regaining usable access after opening recovery divided by access-recovery opens. | access_recovery_opened, access_recovered | Platform/user-choice dependent.; Selected access counts only for selected-photo mode. |

Analytics measurement is optional, off by default, and consented. Client analytics cannot be treated as a complete census. No metric may justify a feature gate, commercial surface, media-derived payload, or punitive retention.

## 2. Target Users & Personas

### Persona A — The Backlog Finisher

Has months or years of camera-roll clutter, distrusts subscriptions and ad-gated utilities, and needs a complete cleanup unit with no commercial uncertainty.

**Needs**
- Choose any accessible calendar month and finish it without an ad, swipe cap, trial prompt, or paywall.
- Understand progress, remaining work, and the exact moment a month is complete.
- Know that the same complete experience remains available for every month.

**Validated pains — verbatim Step 2 text**
- **P1:** “Now you can barely do 20 without 'purchasing' (or watching an ad) to get more.”
- **P2:** “I had barely opened the thing for the first time and I was already hit with an ad”
- **P3:** “I can't swipe to delete, I can't combine all albums into one, and my biggest pet peeve is that it's subscription-only.”

**Segment:** SEG-1

### Persona B — The Cautious Curator

Has a large personal photo library and wants speed, but fears accidental loss, frozen sessions, ambiguous deletion, and cloud-backed media reappearing.

**Needs**
- Make fast decisions while retaining a clear path to undo.
- Review every pending deletion before committing destructive changes.
- Receive explicit confirmed, partial, failed, and recoverable deletion outcomes.

**Validated pains — verbatim Step 2 text**
- **P4:** “sometimes the screen would just freeze and then I would have to refresh it”
- **P5:** “this app do not delete my photos lol scam”
- **P6:** “loads nothing, extremely slow load times if it does load anything.”

**Segment:** SEG-2

### Persona C — The Ritual Seeker

Avoids photo cleanup because it feels tedious and returns only when the interaction itself is satisfying, finishable, and free from nagging.

**Needs**
- Turn an intimidating backlog into one bounded month at a time.
- Feel responsive card physics, visible momentum, and meaningful closure.
- Return because progress feels rewarding, not because notifications punish inactivity.

**Validated pains — verbatim Step 2 text**
- **P1:** “Now you can barely do 20 without 'purchasing' (or watching an ad) to get more.”
- **P2:** “I had barely opened the thing for the first time and I was already hit with an ad”
- **P4:** “sometimes the screen would just freeze and then I would have to refresh it”
- **P7:** “it sends too many memories/themed notifications each day and there isn't an option to fine-tune this other than to disable them.”

**Segment:** SEG-3

## 3. Problem Statement & Value Proposition

### Problem

Camera-roll cleanup combines a repetitive chore with irreversible risk. Existing swipe cleaners prove demand, but user voice shows that early ads, tiny free allowances, subscription pressure, freezes, slow loading, and uncertain deletion can destroy momentum and trust. Broad utilities already dominate automated storage cleanup, so Keepr must not compete as another generic file manager.

### Value proposition

For Android users who postpone camera-roll cleanup because it feels tedious, risky, or monetization-hostile, Keepr is the permanently free month-by-month photo-cleaning game with tactile card decisions, reversible choices, Android-confirmed deletion, truthful reconciliation, and unmistakable closure—unlike swipe cleaners that interrupt the core loop with ads, limits, or subscription pressure.

### Validated market gap

A focused Android cleanup ritual that lets users finish any accessible month through tactile, game-like card decisions without commercial interruption, while making review, undo, final deletion, recovery, and reclaimed-space feedback unmistakably trustworthy.

**Design implications**
- Month-by-month left/delete and right/keep interaction is proven demand but already crowded, so the gesture itself is not the gap.
- High-confidence review evidence identifies ads, low free allowances, and subscription pressure as the strongest recurring break in the core loop.
- Medium-confidence review evidence identifies freezes, slow loading, and deletion uncertainty as product-breaking risks.
- Broad substitutes dominate automated cleanup and cloud management, leaving a narrower opportunity around deliberate ritual, closure, and interaction quality.
- Store creatives show colorful and gamified claims, but little public evidence of a deeply tactile custom interaction system.

### Wedge

**Finish-the-month cleanup ritual**

**Users**
- Subscription-averse backlog cleaners
- Deletion-anxious users with large camera rolls
- Occasional cleaners who need a finishable ritual rather than automated file management

**Jobs**
- Complete one meaningful unit of cleanup and immediately choose another.
- Make hundreds of keep/delete decisions without commercial interruption.
- Review and reverse decisions before committing deletion.
- End with visible month closure and reclaimed-space feedback.

**Required capabilities**
- Calendar-month selection and resumable progress
- High-performance right-to-keep and left-to-delete card interaction
- Undo and a complete pre-commit review queue
- Reliable deletion commit with explicit success, partial-failure, and recovery states
- Permanent free-access guarantee across first and repeated sessions
- Custom chunky visual language and spring motion that never obscure safety or progress

**Excluded workflows**
- Cloud backup and storage-plan management
- General file management
- Photo editing
- Album organization as a parallel primary loop
- AI-first automatic deletion
- Ads, swipe packs, paywalls, subscriptions, purchases, entitlements, and promotional interruptions anywhere in the product

## 4. Scope

### In-scope features

| Feature | Label | Tier | Requirements | Design scope |
| --- | --- | --- | --- | --- |
| F1 | Month picker and progress ledger | P0 | R2, R7 | Users choose one calendar month, see item count and progress, and resume exactly where they left off. |
| F2 | Tactile swipe-card triage | P0 | R3, R8 | Each photo is presented as a responsive card with unambiguous keep-right and delete-left outcomes plus accessible non-gesture controls. |
| F3 | Session undo and decision revision | P0 | R4 | Users can reverse the latest choice during triage and revise any decision during final review. |
| F4 | Pre-commit review queue | P0 | R4, R5 | Before deletion, users can inspect all keep and delete decisions, move items between groups, and cancel without data loss. |
| F5 | Explicit deletion commit and result states | P0 | R5, R6 | Deletion requires explicit confirmation and reports confirmed, partial, failed, and recoverable outcomes without claiming success prematurely. |
| F6 | Durable session resume and recovery | P0 | R2, R6 | Backgrounding, process interruption, loading failure, and permission recovery preserve the user's month and prior decisions. |
| F7 | Month completion and reclaimed-space celebration | P0 | R7 | A month ends with unmistakable closure, decision totals, deletion outcome, and reclaimed-space feedback; no missed-day punishment is used. |
| F8 | Custom game-feel interaction shell | P0 | R8 | Chunky ink borders, hard offset shadows, oversized rounded forms, depth-bearing controls, enormous numerals, and spring motion form one coherent system rather than stock Material styling. |
| F9 | Permanent free-access invariant | P0 | R1, R10 | The product contains no commercial UI, SDK, route, copy, dependency, entitlement state, usage gate, or promotional interruption; every accessible cleanup scope remains usable. |
| F10 | Scoped local-library access and privacy explanation | P1 | R9 | Keepr explains why photo access is needed, avoids cloud-account scope, and keeps the product centered on the local camera roll. |

All ten features are in scope. `F10` is P1 but required for permission, privacy, and Play-policy integrity; it is not staged out.

### Permanent free-access boundary

- The complete app is permanently free.
- There is no price, product ID, purchase, restore, subscription, trial, entitlement, paywall, locked month, time limit, swipe limit, ad, or commercial experiment.
- Every accessible month and Android-selected photo group is usable based only on platform access and media availability.
- The permanent-free rule is enforced by absence: no disabled commercial subsystem, hidden route, remote flag, or dormant SDK ships.

### Account and data scope

Keepr has no custom user account. Photo cleanup, session history, access education, and preferences are local. Core cleanup has no backend dependency and no service receives media-library data.

**Local data:** MonthSession and MediaDecision ledger; DeletionBatch outcomes; analytics consent/local queue identifier only while consented; language, theme, motion, haptics, and access-education preferences.

**Remote data:** Optional consented anonymous analytics events and an analytics installation identifier that is reset on withdrawal. No account, media, purchase, or entitlement record exists.

**Reset/deletion:** “Reset Keepr” deletes all local session, preference, consent, and analytics identifier/queue data and calls the analytics reset mechanism when analytics has ever been enabled. Uninstall also removes app-private data. Reset never touches MediaStore.

**Export:** No photo export or cloud backup exists. A local diagnostics export may include app version, OS version, access state, and coarse error counts, but must exclude content URIs, media IDs, filenames, dates, thumbnails, photo metadata, exact library statistics, and analytics identifiers.

### Adversarial anti-scope

- **REFUSED — `monetization-blocks-core-loop`:** The product refuses ads, rewarded-ad continuation, swipe caps, trials, purchase prompts, paywalls, subscriptions, entitlements, locked months, and cross-promotion across the entire app—not only during cleanup.
- **P0 quality obligation — `cleanup-reliability-and-speed`:** The product does not refuse this problem; it treats loading speed, session durability, deletion truthfulness, and recovery as P0 requirements rather than background polish.
- **REFUSED — `secondary-prompts-crowd-primary-task`:** The product refuses notification nagging, themed prompts, missed-day punishment, and promotional surfaces that compete with the active cleanup task.

### Ordinary out of scope

| Item | Why excluded | Revisit signal |
| --- | --- | --- |
| Cloud backup, synchronization, and storage-plan management | These workflows broaden Keepr into a platform category dominated by established substitutes and conflict with the narrow local cleanup ritual. | Only revisit if validated demand shows users cannot complete local cleanup without a narrowly defined synchronization aid. |
| General file management | Broad storage utilities already serve this job at massive scale; it would dilute the month-by-month camera-roll wedge. | Only revisit if photo-cleanup users repeatedly request one adjacent file action that directly completes the same ritual. |
| Photo editing | Editing is a separate job with different interaction, quality, and retention requirements. | Only revisit if post-cleanup research identifies a repeated, high-confidence need for one constrained edit before keep/delete. |
| Album organization as a parallel primary loop | A second primary taxonomy workflow would compete with the simple keep/delete month ritual. | Only revisit when completion data shows a substantial share of kept photos need one lightweight destination action. |
| AI-first automatic deletion | Automatic destructive decisions conflict with the deliberate, reversible human-review wedge and increase trust risk. | Only revisit for non-destructive suggestions after measured evidence shows users want prioritization without surrendering final control. |
| Duplicate, blur, and large-file scanning as the core product | Automated cleanup is a crowded utility category and is not Keepr's structural differentiation. | Only revisit as an optional ordering aid if it improves month completion without hiding or auto-deleting media. |
| Daily streaks, leaderboards, and punitive retention mechanics | The gate requires progress and closure—not pressure—to drive return behavior. | Only revisit non-punitive milestones after evidence shows they improve completion without increasing notification or guilt complaints. |
| Social sharing or public profiles | These features do not trace to the validated pains, segments, gap, or structural wedge. | Only revisit if completion research demonstrates a privacy-safe sharing job with repeated demand. |

## 5. Information Architecture & Primary Navigation

### Navigation model

Keepr uses a single-task, single-activity route stack rather than persistent bottom navigation. `SCR-05 Main / Month Picker` is the durable root after first-run setup. Cleanup, review, deletion, and recovery are one flow; Settings is the only secondary app graph. The prototype registry defines SCR-01..SCR-22 and is the screen-number authority.

| Root / route | Purpose | Entry | Exit / back policy |
| --- | --- | --- | --- |
| SCR-01 Splash | Resolve first-run, permission, and recovery state | App launch | Automatic route; never remains indefinitely |
| SCR-02 Language Selection | Choose supported app language | First run and Settings | Continue to onboarding; back to Settings when opened there |
| SCR-03 Onboarding | Explain month ritual, safety, privacy, and permanent free access | First run | Continue to media access; back allowed within onboarding |
| SCR-04 Media Access | Explain and request photo access | Onboarding, denied state, Settings | Full access → Main; partial → Selected Photos Mode; not now → limited Main |
| SCR-05 Main / Month Picker | Choose or resume any accessible month; reach Settings | Post-setup root | System back exits app; unfinished sessions route through Resume |
| SCR-06 Selected Photos Mode | Operate honestly on Android-selected items | Android 14+ partial access | Reselect/full access/Main |
| SCR-07 Cleanup Session | Swipe or use buttons; undo; see progress | Chosen/resumed month or selected group | Review when no undecided items; back saves and returns Main |
| SCR-08 Media Load Recovery | Retry/skip unavailable item without a decision | Load failure in Cleanup/Review | Return to source flow |
| SCR-09 Pre-commit Review | Inspect and revise keep/delete groups | All actionable items decided | Confirm deletion, return Cleanup, or save and exit |
| SCR-10 Deletion Confirmation | State exact pending count, permanence, and batch behavior | Review commit action | Continue to system UI or cancel to Review |
| SCR-11 Deletion Progress & Recovery | Own system prompts and reconcile outcomes | After Keepr confirmation | Completion when terminal; Partial Deletion/Review/Retry on incomplete outcomes |
| SCR-12 Completion | Show truthful month/group closure and results | Terminal reconciled result | Main, Rate-Us, Partial Deletion when needed |
| SCR-13 Privacy | Explain on-device handling, optional analytics, retention, and reset | Settings, onboarding/access education | Back to Settings/caller |
| SCR-14 Settings | Language, access, theme, motion, haptics, privacy, analytics, feedback, rate, diagnostics, reset | Main | Back to Main |
| SCR-15 Analytics Consent | Opt in/out with bounded explanation | Onboarding optional step or Settings | Return to caller |
| SCR-16 Feedback | Compose privacy-safe feedback and diagnostics | Settings or Rate-Us alternative | Share sheet then return |
| SCR-17 Rate-Us | User-initiated rating route after completion or Settings | Completion secondary route or Settings | Launch Play review/store; return |
| SCR-18 Reset Keepr Confirmation | Explain local reset effects and exclusions | Settings | Confirm → Main setup state; cancel → Settings |
| SCR-19 Permission Denied | Recover from denial or permanent denial without confusing it with an empty library | Media permission result or restored session with lost access | Full/selected access, Settings, Privacy, or Not now |
| SCR-20 Empty Library / Month | Explain a genuinely empty accessible library or chosen month | Empty MediaStore query | Scan, Access, Settings, Privacy, or Main |
| SCR-21 Resume Session | Explain restored local progress before continuing | Unfinished local session | Resume, Start over with confirmation, or Main |
| SCR-22 Partial Deletion | Report and recover still-present/unavailable items truthfully | Incomplete post-system reconciliation | Review unresolved, retry eligible, or Main |

### Global back and interruption policy

- Back from active cleanup saves state and returns to Main; it never discards decisions.
- Back from review preserves all decisions and performs no deletion.
- Back is disabled only while a system deletion confirmation is in front; after return, Keepr reconciles and routes to a named result.
- Process death restores the latest non-terminal session, permission state, and deletion batch before choosing a destination.
- Splash, loading, and reconciliation screens have bounded automatic exits and explicit recovery; no transient screen may hang.

## 6. End-to-End User Journeys (`J1..Jn`)

### J1 — First run with full photo access

**Personas:** A, B, C  
**Preconditions:** Fresh install; no restored session

| Order | Screen | User action | Result state |
| --- | --- | --- | --- |
| 1 | SCR-01 | Launch Keepr | Resolve first-run state |
| 2 | SCR-02 | Choose language | Persist locale and continue |
| 3 | SCR-03 | Read ritual, privacy, deletion safety, and permanent free-access promise | Continue to access explanation |
| 4 | SCR-04 | Tap Allow photo access and approve full access | Query month summaries |
| 5 | SCR-05 | View available calendar months | Ready to select a month |

**Success end:** Month picker is usable and every accessible month is available  
**Failure exits:** Permission denial routes to SCR-19/J2; an empty query routes to SCR-20.

### J2 — Denied or selected-photos access recovery

**Personas:** A, B  
**Preconditions:** Photo access is denied or partial

| Order | Screen | User action | Result state |
| --- | --- | --- | --- |
| 1 | SCR-19 | Read denial-specific access explanation | Choose full access, selected photos, Settings, Privacy, or Not now |
| 2 | SCR-06 | Use selected-photos mode when partial | Review only selected items with partial-access label |
| 3 | SCR-04 | Choose Allow all photos or reselect | System permission flow opens |
| 4 | SCR-05 | Return after any usable access grant | Show full months or selected-photo entry truthfully |

**Success end:** Access state is truthful and reversible; no commercial state exists  
**Failure exits:** Not now routes to SCR-20; Settings denial remains recoverable.

### J3 — Start, swipe, undo, and resume any accessible cleanup scope

**Personas:** A, B, C  
**Preconditions:** Usable access; accessible month or selected-photo group; no matching resumable session

| Order | Screen | User action | Result state |
| --- | --- | --- | --- |
| 1 | SCR-05 | Select a calendar month | Open new or saved session |
| 2 | SCR-07 | Wait for first actionable card | Persist the session snapshot only when the card is usable |
| 3 | SCR-07 | Swipe right/keep or left/delete, or use buttons | Commit exactly one reversible decision |
| 4 | SCR-07 | Tap Undo when needed | Restore latest card with no duplicate decision |
| 5 | SCR-21 | Return to an unfinished session | Explain and restore exact next undecided item and progress |

**Success end:** All actionable items have one decision and the review route opens  
**Failure exits:** Load failures route to SCR-08; permission loss routes to SCR-19; back saves.

### J4 — Review and revise before deletion

**Personas:** B  
**Preconditions:** Month has no undecided actionable items

| Order | Screen | User action | Result state |
| --- | --- | --- | --- |
| 1 | SCR-09 | Inspect Keep and Delete groups | Totals equal loaded actionable items |
| 2 | SCR-09 | Move items between groups | Counts and pending bytes update immediately |
| 3 | SCR-09 | Choose Continue cleaning when an item needs reinspection | Return to that item in Cleanup |
| 4 | SCR-09 | Tap Delete selected | Open explicit confirmation |

**Success end:** User reaches confirmation with final delete set  
**Failure exits:** Back or Save for later performs no destructive operation.

### J5 — System-mediated deletion and reconciliation

**Personas:** B  
**Preconditions:** Final delete set confirmed in Keepr

| Order | Screen | User action | Result state |
| --- | --- | --- | --- |
| 1 | SCR-10 | Review exact count, permanence, and number of system batches | Confirm or cancel |
| 2 | SCR-11 | Approve each Android system deletion request | Persist batch state before and after each prompt |
| 3 | SCR-11 | Allow reconciliation | Re-query every requested item |
| 4 | SCR-11 | Acknowledge partial/failed/canceled items or retry | Reach terminal result |

**Success end:** Every item is confirmed deleted, unresolved, or failed; no inferred success  
**Failure exits:** Cancel returns to recoverable review; API 29 may require serial consent; API 36 may require multiple prompts.

### J6 — Truthful completion and next cleanup

**Personas:** A, C  
**Preconditions:** J5 reached a terminal reconciled result

| Order | Screen | User action | Result state |
| --- | --- | --- | --- |
| 1 | SCR-12 | View kept, confirmed deleted, unresolved, and estimated reclaimed space | Current month/group becomes complete |
| 2 | SCR-12 | Choose Back to months | Return Main with every accessible month available |
| 3 | SCR-12 | Choose Resolve remaining when applicable | Open SCR-22 recovery |
| 4 | SCR-17 | Optionally choose Rate Keepr | Open user-initiated rating route |

**Success end:** User has a truthful result and can immediately clean another accessible month  
**Failure exits:** Unknown size omits estimate; unresolved outcomes remain explicit and route to SCR-22.

### J7 — Verify permanent free access and repeat cleanup

**Personas:** A  
**Preconditions:** Month Picker or Completion is visible

| Order | Screen | User action | Result state |
| --- | --- | --- | --- |
| 1 | SCR-05 | Inspect the month catalog | Every month tile is interactive; no locked/commercial state exists |
| 2 | SCR-05 | Open any new month | Start local session without network or product lookup |
| 3 | SCR-21 | Open any unfinished month | Resume without a feature gate |
| 4 | SCR-12 | Return after finishing | Another month remains immediately available |

**Success end:** Unlimited repeat cleanup is available by construction  
**Failure exits:** Airplane mode does not affect core cleanup; any commercial artifact is a release-blocking defect.

### J8 — Privacy, analytics, feedback, language, rate, and reset

**Personas:** A, B, C  
**Preconditions:** Main is available and no destructive system UI is active

| Order | Screen | User action | Result state |
| --- | --- | --- | --- |
| 1 | SCR-14 | Open Settings | View explicit secondary controls |
| 2 | SCR-15 | Grant, decline, or withdraw analytics consent | Collection remains aligned with choice |
| 3 | SCR-02 | Change language | Return to Settings with locale applied |
| 4 | SCR-16 | Compose/share feedback | Exclude media identifiers and content |
| 5 | SCR-17 | Open user-initiated rating flow | Return without coercion |
| 6 | SCR-18 | Confirm Reset Keepr or cancel | Clear local Keepr state only when confirmed |

**Success end:** User retains control without a Keepr account  
**Failure exits:** Reset does not delete any MediaStore photo.

### J9 — Process-death and external-change recovery

**Personas:** B  
**Preconditions:** A non-terminal session or deletion batch exists at app start

| Order | Screen | User action | Result state |
| --- | --- | --- | --- |
| 1 | SCR-01 | Launch after interruption | Detect recovery state |
| 2 | SCR-22 | Resume partial deletion recovery when applicable | Re-query affected URIs and show exact unresolved counts |
| 3 | SCR-21 | Explain resumable cleanup | Resume exact next actionable item or return Main |
| 4 | SCR-19 | Recover permission when revoked | Preserve ledger and route to explanation |

**Success end:** A named actionable state is restored without duplicate decisions or false completion  
**Failure exits:** Missing media becomes partial/unavailable; no full-library blocking scan before first recovery frame.

## 7. Screen-by-Screen UX Specification

### Shared state catalog

| State ID | Definition |
| --- | --- |
| booting | App is resolving first-run, permission, and recoverable local session state. |
| loading | A bounded asynchronous operation is in progress; primary actions are disabled or replaced with a clear progress affordance. |
| empty | No eligible media or no results exist; explains why and offers a valid next action. |
| populated | Primary content is available and all visible controls are actionable. |
| permission_required | Required photo access is denied or revoked. |
| partial_access | Android selected-photos access is active; scope is visibly limited. |
| active | A cleanup session has an actionable current card. |
| load_error | One media item failed without recording a decision. |
| review_ready | All actionable items have decisions and can be revised. |
| confirming | Keepr is asking for explicit destructive confirmation. |
| system_prompt | Android owns the current deletion confirmation. |
| reconciling | Keepr is re-querying requested media and calculating truthfully confirmed outcomes. |
| partial_result | Some items are confirmed and some remain unresolved or failed. |
| complete | All session obligations have a terminal result and completion information is shown. |
| offline | Core cleanup remains fully usable; optional analytics upload is unavailable. |
| reduced_motion | Nonessential spring/travel effects are removed while all state changes remain clear. |

### SCR-01 — Splash

**Universal screen:** Yes  
**Feature IDs:** None  
**Entry points:** App launch  
**Back destination:** System back exits app.

| State | Entry condition | Presentation | Actions and destination | Automatic exit |
| --- | --- | --- | --- | --- |
| booting | Every cold or restored launch | Keepr mark in the custom visual language; no generic progress template | No user action required | Automatic route to SCR-02, SCR-03, SCR-04, SCR-05, SCR-07, or SCR-11 after bounded state resolution |
| load_error | State resolution exceeds bounded timeout or local state cannot be read | Clear recovery message with Retry and Reset Keepr route; no infinite spinner | Retry → booting; Reset → SCR-18 | None |

### SCR-02 — Language Selection

**Universal screen:** Yes  
**Feature IDs:** None  
**Entry points:** First run, Settings  
**Back destination:** Back returns to Settings when opened there; first-run back returns Splash/onboarding entry.

| State | Entry condition | Presentation | Actions and destination | Automatic exit |
| --- | --- | --- | --- | --- |
| populated | Supported language list is available | Current language is selected; device-language behavior is explained | Choose language → persist; Continue → SCR-03 or Back → SCR-14 | None |

### SCR-03 — Onboarding

**Universal screen:** Yes  
**Feature IDs:** F8, F9, F10  
**Entry points:** First run after language  
**Back destination:** Back moves to prior panel; final back returns Language Selection.

| State | Entry condition | Presentation | Actions and destination | Automatic exit |
| --- | --- | --- | --- | --- |
| populated | First-run education is required | Three concise panels: one month at a time; right keeps/left deletes; review before permanent deletion; every month is free with no ads, purchases, or swipe limits; photos stay on device | Continue → SCR-04; Privacy details → SCR-13 | None |

### SCR-04 — Media Access

**Universal screen:** Yes  
**Feature IDs:** F1, F6, F9, F10  
**Entry points:** Onboarding, Permission loss, Settings  
**Back destination:** Back returns to caller.

| State | Entry condition | Presentation | Actions and destination | Automatic exit |
| --- | --- | --- | --- | --- |
| permission_required | Full access is absent | Exact rationale copy; actions Allow photo access, Choose photos where supported, Not now, Open settings after repeated denial | Full grant → SCR-05; partial → SCR-06; Not now → SCR-05 limited; settings → Android settings then re-check | None |
| partial_access | Selected photos only | Clearly states that only Android-selected items are visible; no feature is commercially restricted | Choose more photos → system selection; Allow all photos → system permission; Continue selected → SCR-06 | None |
| populated | Full access granted | Confirmation that photos stay on device and month catalog can be built | Continue → SCR-05 | Optional automatic route after system result |

### SCR-05 — Main / Month Picker

**Universal screen:** Yes  
**Feature IDs:** F1, F6, F7, F9, F10  
**Entry points:** Post-onboarding root, Completion, Resume  
**Back destination:** System back exits. Active-session selection always resumes rather than creates a duplicate.

| State | Entry condition | Presentation | Actions and destination | Automatic exit |
| --- | --- | --- | --- | --- |
| loading | Month summaries are being queried | Skeleton/placeholder matching custom components; no full-library blocking spinner | Settings remains available; Retry appears on failure | Automatic to populated or empty |
| empty | No eligible images under current access | Route to SCR-20 rather than rendering a blank list | Empty details → SCR-20; Adjust → SCR-04; Refresh → loading; Settings → SCR-14 | None |
| permission_required | Access denied/revoked | Preserved session notice and denial-specific recovery | Recovery → SCR-19; Settings → SCR-14 | None |
| partial_access | Only selected items accessible | Persistent selected-photo badge; no unseen month is falsely described as cleared | Open selected mode → SCR-06; Allow all → SCR-04 | None |
| populated | Month summaries available | Every tile is interactive and shows month/year, sorted/total, Not started/In progress/Cleared state, and estimated reclaimed size when known; Settings is explicit | New month → SCR-07; saved month → SCR-21; Settings → SCR-14 | None |

### SCR-06 — Selected Photos Mode

**Universal screen:** No  
**Feature IDs:** F1, F6, F9, F10  
**Entry points:** Partial access  
**Back destination:** Back returns Main. Completion here applies only to the selected group and never claims the unseen calendar month is fully cleared.

| State | Entry condition | Presentation | Actions and destination | Automatic exit |
| --- | --- | --- | --- | --- |
| empty | No currently selected accessible images | Explain Android-selected scope and offer reselection/full access | Choose photos → system selection; Allow all photos → SCR-04; Main → SCR-05 | None |
| populated | Selected accessible images exist | Clearly labeled non-month cleanup; shows selected count and uses the same safety controls | Start selected cleanup → SCR-07 with partial scope; Reselect → system UI; Allow all → SCR-04 | None |

### SCR-07 — Cleanup Session

**Universal screen:** No  
**Feature IDs:** F1, F2, F3, F6, F8, F9  
**Entry points:** Month selection, Resume, Selected photos mode  
**Back destination:** Back saves the session and returns Main. It never abandons or deletes.

| State | Entry condition | Presentation | Actions and destination | Automatic exit |
| --- | --- | --- | --- | --- |
| loading | Current thumbnail is not yet ready | Card shell, bounded loading indication, progress and exit remain visible | Back → save and SCR-05; timeout → SCR-08 | Automatic to active or load_error |
| active | Current item loaded | Single dominant photo card, enormous progress numeral, explicit KEEP and DELETE semantics, visible Undo when applicable, tactile pressed states | Swipe right/Keep → next active; swipe left/Delete → next active; Undo → restore previous; Back → save/Main | Automatic to SCR-09 when no undecided actionable items |
| load_error | Current item unavailable | Do not classify the item; show reason without exposing path/filename | Retry → loading; Skip for now → next active with unavailable status; Details → SCR-08 | None |
| permission_required | Access lost during session | Progress-preserved explanation | Recover access → SCR-19; Save and exit → SCR-05 | None |
| reduced_motion | System motion reduction active | Same card and controls; immediate/short state transitions, no decorative travel | All controls identical to active | Same destinations as active |

### SCR-08 — Media Load Recovery

**Universal screen:** No  
**Feature IDs:** F6  
**Entry points:** Cleanup/Review load failure  
**Back destination:** Back returns to source screen with ledger intact.

| State | Entry condition | Presentation | Actions and destination | Automatic exit |
| --- | --- | --- | --- | --- |
| load_error | A specific item is missing, unreadable, corrupt, or permission-lost | Explain that no keep/delete decision was recorded; show Retry, Skip for now, and Return | Retry → source loading; Skip → source next item with unavailable status; Return → source screen | None |

### SCR-09 — Pre-commit Review

**Universal screen:** No  
**Feature IDs:** F3, F4, F5, F6, F9  
**Entry points:** Cleanup exhausted actionable items  
**Back destination:** Back/save performs no destructive operation and preserves all decisions.

| State | Entry condition | Presentation | Actions and destination | Automatic exit |
| --- | --- | --- | --- | --- |
| loading | Review groups are being reconciled with MediaStore | Keep/Delete tabs and totals unavailable until reconciliation completes | Back → save/Main; Retry on failure | Automatic to review_ready or partial_result |
| review_ready | All loaded actionable items have one current decision | Separate Keep and Delete groups; exact counts; estimated selected bytes only when known; item actions move groups or reopen card | Move group → remain; Reinspect → SCR-07 item; Delete selected → SCR-10; Save for later → SCR-05 | None |
| partial_result | Some snapshot items are unavailable or permission-lost | Visible unresolved group and explanation; commit includes only confirmed accessible delete items | Recover access → SCR-04; Retry items → SCR-08; Continue with accessible → SCR-10; Save → SCR-05 | None |

### SCR-10 — Deletion Confirmation

**Universal screen:** No  
**Feature IDs:** F4, F5, F9  
**Entry points:** Review commit  
**Back destination:** Back/cancel returns Review.

| State | Entry condition | Presentation | Actions and destination | Automatic exit |
| --- | --- | --- | --- | --- |
| confirming | At least one accessible item is marked delete | States exact pending count, permanent deletion, Android confirmation, and number of batches when more than platform limit; Android 10 serial-prompt note when applicable | Delete permanently → SCR-11/system UI; Cancel → SCR-09 | None |
| empty | Delete group is empty | No destructive CTA; explain that nothing will be deleted | Finish month → SCR-12; Back → SCR-09 | None |

### SCR-11 — Deletion Progress & Recovery

**Universal screen:** No  
**Feature IDs:** F5, F6, F7  
**Entry points:** Keepr confirmation, Process recovery  
**Back destination:** Back is unavailable during system prompt/reconciliation; at recoverable terminal states it returns Main with state preserved.

| State | Entry condition | Presentation | Actions and destination | Automatic exit |
| --- | --- | --- | --- | --- |
| system_prompt | Android confirmation is in foreground | Keepr background state is persisted; no competing UI | Android approve/cancel controls | Return → reconciling |
| reconciling | System result returned or app recovered | Batch progress, no premature success copy | No destructive repeat action; Cancel between remaining batches → partial_result | Automatic to next system prompt, partial_result, or complete |
| partial_result | Canceled, failed, missing, or permission-lost items remain | Separate confirmed, failed, canceled, and unresolved counts; exact next steps | Partial deletion details → SCR-22; Review remaining → SCR-09; Retry eligible → system_prompt | None |
| load_error | Reconciliation cannot complete | State remains recoverable and pending batch is not duplicated | Retry → reconciling; Return later → SCR-05 | None |
| complete | All batches terminal | Brief confirmed result summary | Continue → SCR-12 | Optional automatic route after acknowledgement |

### SCR-12 — Completion

**Universal screen:** No  
**Feature IDs:** F7, F9  
**Entry points:** Terminal month result  
**Back destination:** Back returns Main.

| State | Entry condition | Presentation | Actions and destination | Automatic exit |
| --- | --- | --- | --- | --- |
| complete | A month or selected-photo group reached a terminal reconciled result | Celebratory but truthful closure; kept, confirmed deleted, unresolved, and estimated space removed only when known; no guilt/streak language | Back to months → SCR-05; Rate Keepr → SCR-17; Resolve remaining → SCR-22 when applicable | None |
| partial_result | Completion acknowledged with unresolved items | Celebration is restrained; unresolved count and recovery remain more prominent than reward | Partial deletion details → SCR-22; Back to months → SCR-05 | None |

### SCR-13 — Privacy

**Universal screen:** Yes  
**Feature IDs:** F10  
**Entry points:** Settings, Onboarding/access education  
**Back destination:** Back returns Settings/caller.

| State | Entry condition | Presentation | Actions and destination | Automatic exit |
| --- | --- | --- | --- | --- |
| populated | Privacy screen opens | Implemented local policy states on-device photo handling, no account/upload, optional analytics boundary, local storage/retention, diagnostics exclusions, reset behavior, and Play Data Safety parity | Back → SCR-14/caller | None |

### SCR-14 — Settings

**Universal screen:** Yes  
**Feature IDs:** F9, F10  
**Entry points:** Main  
**Back destination:** Back returns Main.

| State | Entry condition | Presentation | Actions and destination | Automatic exit |
| --- | --- | --- | --- | --- |
| populated | Settings available | Language, photo access, dark theme, motion, haptics, analytics, privacy, feedback, rate, diagnostics, and Reset Keepr; no commercial row | Each row opens SCR-02/04/13/15/16/17/18 or its platform settings surface | None |

### SCR-15 — Analytics Consent

**Universal screen:** Yes  
**Feature IDs:** F9, F10  
**Entry points:** Onboarding optional step, Settings  
**Back destination:** Back leaves current choice unchanged and returns caller.

| State | Entry condition | Presentation | Actions and destination | Automatic exit |
| --- | --- | --- | --- | --- |
| populated | Consent choice can be changed | Explains bounded event collection, forbidden media data, optional status, and withdrawal/reset behavior | Share anonymous usage → enable and return; No thanks/Turn off → disable/reset and return | None |

### SCR-16 — Feedback

**Universal screen:** Yes  
**Feature IDs:** None  
**Entry points:** Settings, Rate-Us alternative  
**Back destination:** Back returns caller. Diagnostics exclude content URIs, media IDs, filenames, dates, thumbnails, photo metadata, exact library statistics, and analytics IDs.

| State | Entry condition | Presentation | Actions and destination | Automatic exit |
| --- | --- | --- | --- | --- |
| populated | Feedback route opened | Category, required user-authored text, privacy-safe diagnostic preview, and system-share action; no photo attachment path | Share feedback → Android share sheet only after text validates; Copy diagnostics → remain; Cancel → caller | None |

### SCR-17 — Rate-Us

**Universal screen:** Yes  
**Feature IDs:** None  
**Entry points:** Completion secondary action, Settings  
**Back destination:** Back returns caller; no repeated automatic nagging.

| State | Entry condition | Presentation | Actions and destination | Automatic exit |
| --- | --- | --- | --- | --- |
| populated | User explicitly opens rating route | Neutral request with Rate Keepr, Maybe later, and Send feedback; no reward or guilt | Rate Keepr → Play review/store then return; Maybe later → caller; Send feedback → SCR-16 | None |

### SCR-18 — Reset Keepr Confirmation

**Universal screen:** Yes  
**Feature IDs:** F6, F9, F10  
**Entry points:** Settings  
**Back destination:** Back equals Cancel.

| State | Entry condition | Presentation | Actions and destination | Automatic exit |
| --- | --- | --- | --- | --- |
| confirming | User taps Reset Keepr | Explains that local sessions, decisions, preferences, consent, and analytics identifier/queue are cleared; MediaStore photos are never touched | Reset Keepr → clear local state and SCR-01/SCR-03; Cancel → SCR-14 | None |

### SCR-19 — Permission Denied

**Universal screen:** Yes  
**Feature IDs:** F6, F10  
**Entry points:** Permission denial, restored session with revoked access  
**Back destination:** Back/Not now routes to SCR-20 without claiming the library is empty.

| State | Entry condition | Presentation | Actions and destination | Automatic exit |
| --- | --- | --- | --- | --- |
| denied | User denies photo access | Explain why access is needed; distinguish denial from empty; preserve session | Retry/full access → SCR-04; selected → SCR-06; Privacy → SCR-13; Not now → SCR-20 | None |
| permanently_denied | System prompt is no longer available | Explain Settings recovery without coercion; preserved sessions remain intact | Open Settings → re-check; selected → SCR-06; Privacy → SCR-13; Not now → SCR-20 | None |

### SCR-20 — Empty Library / Month

**Universal screen:** Yes  
**Feature IDs:** F1, F10  
**Entry points:** Empty accessible query, empty chosen month, Not now after denial  
**Back destination:** Back returns Main when available.

| State | Entry condition | Presentation | Actions and destination | Automatic exit |
| --- | --- | --- | --- | --- |
| empty_library | Accessible MediaStore query returns zero images | Explain a genuinely empty accessible library; do not imply permission denial | Scan → loading/Main; Access → SCR-04; Settings → SCR-14; Privacy → SCR-13 | None |
| empty_month | Chosen month contains no accessible images | Explain that this month is empty and keep other months reachable | Back to months → SCR-05; Access → SCR-04 | None |

### SCR-21 — Resume Session

**Universal screen:** No  
**Feature IDs:** F1, F6  
**Entry points:** Unfinished local session detected  
**Back destination:** Back returns Month Picker without discarding progress.

| State | Entry condition | Presentation | Actions and destination | Automatic exit |
| --- | --- | --- | --- | --- |
| available | Restorable session and current scope exist | Explain restored month/group, sorted/total progress, and local persistence | Resume → SCR-07; Start over → explicit confirmation then SCR-07; Month Picker → SCR-05 | None |
| load_error | Ledger cannot resolve the current media item | Preserve decisions; explain the mismatch without identifiers | Retry → available; Review available → SCR-09; Month Picker → SCR-05 | None |

### SCR-22 — Partial Deletion

**Universal screen:** No  
**Feature IDs:** F5, F6, F7  
**Entry points:** Incomplete deletion reconciliation, restored pending batch  
**Back destination:** Back returns Month Picker with unresolved state persisted.

| State | Entry condition | Presentation | Actions and destination | Automatic exit |
| --- | --- | --- | --- | --- |
| partial_result | Requested items remain present or unavailable | Report confirmed deleted, still present, unavailable, and unresolved counts; never over-claim freed bytes | Review unresolved → SCR-09; Retry eligible → SCR-11; Month Picker → SCR-05 | None |

## 8. Interaction & Control Inventory

| Control ID | Screen | Control | Behavior | States |
| --- | --- | --- | --- | --- |
| CTL-01 | SCR-05 | Month card | Open any represented month or route to SCR-21 when unfinished; never render a commercial locked state. | loading, not-started, in-progress, cleared |
| CTL-02 | SCR-07 | Photo card drag | Horizontal drag previews KEEP on right and DELETE on left; sub-threshold/canceled drag records nothing. | rest, dragging-right, dragging-left, settling, committed |
| CTL-03 | SCR-07 | Keep button | Records exactly the same outcome as a completed right swipe. | enabled, pressed, disabled/loading |
| CTL-04 | SCR-07 | Delete button | Records exactly the same reversible outcome as a completed left swipe; does not delete from MediaStore. | enabled, pressed, disabled/loading |
| CTL-05 | SCR-07 | Undo | Restores latest decided item and removes its prior decision. | hidden/no-history, enabled, pressed |
| CTL-06 | SCR-07 | Progress ledger | Communicates decided and remaining counts; unavailable items are not counted complete. | loading, active, complete |
| CTL-07 | SCR-08 | Retry media | Retries only the unavailable item without changing its decision. | enabled, loading, failed |
| CTL-08 | SCR-08 | Skip for now | Moves forward while marking item unavailable, not kept/deleted. | enabled, pressed |
| CTL-09 | SCR-09 | Keep/Delete group selector | Switches review group without changing decisions. | keep-selected, delete-selected |
| CTL-10 | SCR-09 | Review item action | Moves item between groups or reopens it for inspection. | available, unavailable, pressed |
| CTL-11 | SCR-09 | Delete selected | Opens Keepr confirmation only when accessible delete count is positive. | enabled, disabled |
| CTL-12 | SCR-10 | Delete permanently | Persists prepared batch and launches Android system confirmation. | enabled, pressed, disabled |
| CTL-13 | SCR-10 | Cancel | Returns Review with all decisions intact. | enabled, pressed |
| CTL-14 | SCR-11 | Retry unresolved | Creates/continues only the unresolved eligible batch; never duplicates confirmed deletions. | enabled, pending, disabled |
| CTL-15 | SCR-12 | Back to months | Returns Main after result is visible and persisted. | enabled, pressed |
| CTL-16 | SCR-12/22 | Resolve remaining | Opens Partial Deletion or Review for unresolved items without hiding truthful result counts. | hidden/no-unresolved, enabled |
| CTL-17 | SCR-13 | Privacy navigation | Opens local privacy sections/links and returns to Settings/caller. | enabled, focused |
| CTL-18 | SCR-14 | Theme/motion/haptics controls | Persist preference immediately while respecting stronger system accessibility settings. | on, off, system-reduced, disabled-by-system |
| CTL-19 | SCR-19 | Access recovery actions | Retry permission, open Settings, choose selected photos, Privacy, or Not now without coercion. | denied, permanently-denied, system-prompt-active |
| CTL-20 | SCR-04 | Allow photo access | Launches version-appropriate Android permission request. | enabled, system-prompt-active |
| CTL-21 | SCR-04/06 | Choose/reselect photos | Launches selected-photos flow on supported Android versions. | enabled, system-prompt-active |
| CTL-22 | SCR-14/15 | Analytics consent toggle/actions | Enable only after affirmative consent; withdrawal disables and resets analytics data. | not-asked, granted, declined, withdrawn |
| CTL-23 | SCR-16 | Share feedback | Enabled only after the user authors text; opens Android share sheet with previewed privacy-safe diagnostics. | disabled-empty, enabled, sheet-open, share-error |
| CTL-24 | SCR-17 | Rate Keepr | Launches Play in-app review/store when available; no success claim is required. | enabled, returning |
| CTL-25 | SCR-18 | Reset Keepr | Clears Keepr-local state only after confirmation. | enabled, pressed, processing |

### Gesture and motion rules

- Direction is invariant: right = keep; left = delete. The mapping never changes by locale, handedness, or experiment.
- A card decision commits only after a completed threshold/velocity settle; canceled or sub-threshold gestures return to rest with no ledger write.
- Gesture tracking performs no database, analytics upload, bitmap decode, EXIF read, or allocation-heavy work.
- Haptics are supportive and optional; text/icon semantics remain sufficient.
- Reduced-motion behavior preserves every control and destination while removing nonessential spring travel.

## 9. Permissions & System-Access Design

### Pre-permission copy

> **Photo access:** “Keepr needs photo access to show your camera roll one month at a time and delete only the photos you choose. Your photos stay on this device.”

> **Android 14+ scope choice:** “Choose full photo access to clean an entire month, or choose selected photos to clean only those items. You can change this later.”

> **Internet disclosure:** “Keepr works offline. If you opt in, it may use the internet to send limited anonymous usage analytics. Photos, filenames, dates, and media IDs are never uploaded.”

| Permission | Features | Type | Rationale | Denial/offline fallback |
| --- | --- | --- | --- | --- |
| android.permission.READ_EXTERNAL_STORAGE | F1, F2, F4, F5, F10 | Runtime | Keepr needs photo access to show your camera roll one month at a time and delete only the photos you choose. Your photos stay on this device. | On Android 10–12L, show SCR-19 with Not now and Allow photo access. Do not scan without access or confuse denial with an empty library. Keep Privacy and Settings available. |
| android.permission.READ_MEDIA_IMAGES | F1, F2, F4, F5, F10 | Runtime | Keepr needs photo access to show your camera roll one month at a time and delete only the photos you choose. Your photos stay on this device. | If denied, show SCR-19. If Android grants selected photos only, enter SCR-06 and offer reselection/full access. |
| android.permission.READ_MEDIA_VISUAL_USER_SELECTED | F1, F6, F9, F10 | Runtime | Choose full photo access to clean an entire month, or choose selected photos to clean only those items. You can change this later. | Resolve Selected, show only granted items, and offer Choose photos, Allow all photos, and Not now. No product feature is commercially restricted. |
| android.permission.INTERNET | Optional analytics only | Manifest; omit when analytics adapter is absent | Keepr works offline. If you opt in, it may send limited anonymous usage analytics. Photos, filenames, dates, and media IDs are never uploaded. | Core cleanup remains complete offline. Consent precedes initialization/upload; withdrawal clears identifier/queue. |

### Access-state behavior

- Android 10–12L: request `READ_EXTERNAL_STORAGE`; denial shows SCR-19 with Not now and Allow photo access. Do not scan without access or confuse denial with empty.
- Android 13+: request `READ_MEDIA_IMAGES`; never request video, location metadata, write access, all-files access, or media-management privileges.
- Android 14+: distinguish Unknown, Full, Selected, Denied, and PermanentlyDenied. Selected-photo mode shows only granted items and never claims the unseen calendar month is cleared.
- Repeated denial routes to system Settings without coercion. Returning from Settings re-evaluates access.
- Permission loss during cleanup preserves the session ledger and routes to SCR-19; it does not reset progress.

### Deletion system access

- Keepr's left-swipe/delete decision is reversible intent, not immediate deletion.
- API 30+: SCR-11 launches `MediaStore.createDeleteRequest` from the foreground Activity.
- API 29: Keepr handles per-item `RecoverableSecurityException` and explains repeated consent.
- Target API 36+: each system request contains at most 2,000 MediaStore URIs. Keepr shows the number of batches before commit, persists each batch, allows cancellation between prompts, and withholds completion until all batches are reconciled.
- Keepr requires both its own explicit confirmation and Android's system confirmation. After return, it re-queries each requested URI and never infers deletion solely from `RESULT_OK`.

## 10. Platform Behavior & Interaction

| Constraint | Mechanism | API levels | Required behavior | Features |
| --- | --- | --- | --- | --- |
| PC-1 | Versioned MediaStore read-permission matrix | 29–32: READ_EXTERNAL_STORAGE; 33+: READ_MEDIA_IMAGES; targetSdk 36 | On Android 10–12L (API 29–32), request READ_EXTERNAL_STORAGE and query only MediaStore.Images. On Android 13+ (API 33+), request READ_MEDIA_IMAGES. Do not request READ_MEDIA_VIDEO, ACCESS_MEDIA_LOCATION, WRITE_EXTERNAL_STORAGE, MANAGE_EXTERNAL_STORAGE, or MANAGE_MEDIA. | F1, F2, F4, F5, F10 |
| PC-2 | Android 14 Selected Photos Access via READ_MEDIA_VISUAL_USER_SELECTED | 34+ | Detect Full, Selected, Denied, and PermanentlyDenied. Selected access runs a clearly labeled selected-photo mode, shows only granted items, and never claims an unseen month is cleared. Provide reselection and a route to request full access. | F1, F6, F9, F10 |
| PC-3 | MediaStore.Images query through ContentResolver | 29+ | Build a UTC-safe local calendar-month range, query image rows by DATE_TAKEN when present and DATE_ADDED as fallback, exclude trashed/pending rows, and store only stable row identity, volume, MIME type, size, and timestamps needed for the session ledger. | F1, F2, F6, F10 |
| PC-4 | ContentResolver.loadThumbnail with cancellation and bounded prefetch | 29+ | Load the current card plus at most the next two thumbnails off the main thread. Cancel work when a card leaves the stack, never copy originals into app storage, and surface unavailable-media states instead of blocking the swipe loop. | F2, F4, F6, F8 |
| PC-5 | System-confirmed permanent deletion | 29: per-item RecoverableSecurityException; 30+: createDeleteRequest | On API 30+, call MediaStore.createDeleteRequest from the foreground and launch its IntentSender. On API 29, attempt ContentResolver.delete and handle RecoverableSecurityException for each item. Card-level undo ends before this system-confirmed commit; after approval, reconcile every URI and report confirmed, partial, canceled, and failed outcomes. | F3, F4, F5, F6, F7 |
| PC-6 | API 36 MediaStore request limit | Targeting API 36+ | Chunk delete requests to no more than 2,000 MediaStore URIs per system prompt. Persist batch progress and require the user to approve each remaining batch; month completion occurs only after every batch reaches a terminal reconciled state. | F5, F6, F7 |
| PC-7 | Google Play target API requirement | Google Play submissions from 2026-08-31 | Build and release with targetSdk 36. Keep compileSdk at least 36 and run behavior-change testing before every target-SDK increase. | F1, F2, F3, F4, F5, F6, F7, F8, F9, F10 |
| PC-8 | Foreground ownership of destructive system UI | 29+ | Deletion may not be scheduled as unattended background work. The review screen owns the system confirmation launcher, persists an intent-to-delete record before launch, and resumes reconciliation after activity recreation or process death. | F4, F5, F6 |

### Lifecycle and recovery

- The latest non-terminal session and deletion batch are authoritative on process start.
- Recovery re-evaluates media access and MediaStore availability, then restores an exact actionable screen. It does not perform a full blocking library validation before the first recovery frame.
- External deletion, cloud-provider changes, missing media, or permission loss become named unavailable/partial outcomes.
- MediaStore remains the source of truth before review and after every deletion result.

### Offline behavior

- **FULL — F1..F10:** Conditions: required media permission/scope remains usable and MediaStore items are locally readable. Core onboarding, cleanup, review, deletion, reconciliation, completion, recovery, privacy, and settings work in airplane mode. Fallback: mark only an unavailable item unresolved, keep the rest usable, and expose retry/skip without claiming deletion or completion. Optional analytics may queue/drop without affecting product state.

### Performance and resource behavior

- Cold-start TTID budget: 1200 ms at p95 on Pixel 6a, Android 16/API 36, release build with Baseline Profile, 500-image synthetic library.
- Frame budget: 16.67 ms, with percentile budget 22 ms.
- Local database ceiling: 26214400 bytes; bounded media-cache soft ceiling: 67108864 bytes.
- Battery budget: no more than 5% over 15 minutes on Pixel 6a, Android 16/API 36, 500-photo swipe session at 60 Hz, screen brightness 50%, analytics disabled.
- Current and next two thumbnails may be prefetched; originals are never copied into Keepr storage.
- Hard offset shadows are drawn without soft blur; bitmap transforms and storage work never occur during pointer movement.

### Device and layout adaptation

- Compact phones prioritize one dominant card and reachable keep/delete controls.
- Larger phones and tablets may place progress or review detail beside the card/grid, but the route and control semantics remain identical.
- Large text, display scaling, and multi-window must not hide the active photo, progress, undo, confirmation copy, or recovery actions.
- Support portrait phones from 320 dp, landscape, tablets, foldables, edge-to-edge insets, and display cutouts. Production layouts are adaptive and never reproduce the prototype's fixed phone canvas.

## 11. Reusable Component & Interaction Patterns

| Component | Name | States | Behavior |
| --- | --- | --- | --- |
| CMP-01 | Ink Surface | Resting, pressed, disabled, selected, error | Oversized rounded surface with a strong ink outline and hard offset depth; press visually compresses depth rather than adding soft blur. |
| CMP-02 | Depth Button | Resting, pressed, disabled, loading | Primary and secondary action family with visible physical depth, text/icon semantics, and deterministic pressed feedback. |
| CMP-03 | Photo Decision Card | Loading, rest, drag-left, drag-right, settle, unavailable | Displays media without cropping away essential review context; overlays KEEP/DELETE only during directional intent; exposes equivalent buttons. |
| CMP-04 | Progress Numeral | Loading, active, complete | Enormous decided/remaining or month-completion numeral with accessible full phrase; decorative scale never substitutes for text. |
| CMP-05 | Month Tile | Not started, in progress, cleared, selected-scope | Shows month/year, sorted/total progress, completion status, and estimated reclaimed size when known; every tile is interactive. |
| CMP-06 | Decision Badge | Keep, delete, unresolved, missing | Uses text/icon plus color; appears consistently in card, review, and completion surfaces. |
| CMP-07 | Review Grid/List Item | Keep, delete, unavailable, selected | Supports move-group and reinspection; unavailable items cannot be silently committed. |
| CMP-08 | Truthful Result Panel | Confirmed, partial, failed, canceled, estimated | Separates outcome categories and never uses celebration to hide unresolved items. |
| CMP-09 | Permission Explanation Panel | First ask, denied, partial, settings-required | Uses exact permission rationale and always offers a reversible next action. |
| CMP-10 | Privacy Panel | Summary, detail, analytics-off, analytics-on | Explains on-device photo handling, optional analytics, reset/retention, and diagnostics boundaries without legalistic ambiguity. |
| CMP-11 | Recovery Panel | Load failure, permission loss, process recovery, reconciliation error | Names what happened, confirms preserved state, and offers retry/skip/return without dead ends. |
| CMP-12 | Celebration Layer | Complete, partial, reduced-motion | Loud but bounded positive feedback only after truthful completion state is visible; never blocks actions or masks partial outcomes. |

### Shared interaction rules

- Every control has resting, pressed, disabled/loading where applicable, focus, and accessibility semantics.
- Destructive controls are visually and verbally distinct from reversible delete-intent controls.
- Loading placeholders retain layout to prevent card or button jumps.
- Error panels name what is preserved and provide a destination.
- No commercial or promotional component exists in any screen or information hierarchy.

## 12. Theming & Visual Consistency Requirements (guidance only; no tokens)

Keepr is not a Material 3 product. User-facing surfaces must use a custom, coherent visual language rather than stock component shapes, elevations, app bars, cards, dialogs, switches, or default motion.

### Required character

- Chunky ink borders define tactile surfaces and separate content from controls.
- Hard offset shadows create physical depth; tactile UI never uses soft blurred elevation.
- Primary shapes are oversized and generously rounded, with readable internal hierarchy.
- Buttons show real resting-versus-pressed depth rather than a simple opacity change.
- Numerals used for progress and completion are unapologetically large and confident.
- Motion is loud, springy, and responsive when enabled, but always subordinate to deletion accuracy, state legibility, and performance.
- Photo content remains the visual hero; decoration frames it rather than covering it.
- The store and in-app promise pair play with trust: every month is free, review before deletion, and see a truthful result.
- Both shipped light and dark themes preserve the same hierarchy and semantics. Production uses local Archivo assets and local Android vector drawables; prototype CDN/Picsum resources are not implementation dependencies.

### Prohibitions

- No stock Google template appearance.
- No Material 3 visual defaults on user-facing surfaces.
- No soft-blur shadow on tactile controls.
- No generic broom, sparkle, or photo-stack metaphor as the sole brand expression.
- No color-only keep/delete distinction.
- No decorative motion that delays input, hides controls, or makes a completed action uncertain.
- No visual token values are defined in this document; the implementation must derive a dedicated Keepr design system and test its consistency.

## 13. Content, Microcopy & Tone Guidelines

### Voice

- Confident, energetic, and playful during reversible triage.
- Calm, exact, and unambiguous around permissions, permanent deletion, partial failure, and privacy.
- Celebrate completed work; never shame unfinished work or missed days.
- Prefer plain verbs: Keep, Delete, Undo, Review, Retry, Resume, Not now.
- Do not call selected-photos mode a complete month.
- Do not call estimated reclaimed bytes an exact device-space increase.

### Required product and free-access copy

- “Every month is free.”
- “No ads, purchases, or swipe limits.”
- “Photos stay on your device.”
- “Review before anything goes.”
- “Not now”

The free-access promise appears in onboarding and may be reinforced in Privacy/Settings help, but it must never resemble a temporary offer, trial, countdown, or entitlement claim.

### Required deletion copy concepts

- “Delete **N** photos permanently?”
- “Android will ask you to confirm.”
- For multiple batches: explain how many system confirmations remain before the first prompt.
- “Estimated space removed” only when based on confirmed deletions with known size.
- Partial result copy separates confirmed deleted, not deleted, and still needing action.

### Forbidden language

- No “limited swipes,” “act now,” countdown, false scarcity, forced trial, or deceptive close language.
- No “streak broken,” missed-day guilt, or notification pressure.
- No “deleted successfully” before URI reconciliation.
- No “photos are private” claim that omits optional analytics; disclose it separately and accurately.
- No generic “AI cleaner” or cloud-backup promise.

## 14. Accessibility Requirements

| ID | Requirement |
| --- | --- |
| A11Y-1 | All keep/delete actions are available through labeled buttons in addition to swipe gestures. |
| A11Y-2 | TalkBack labels announce the photo decision action, progress, current group, and result; decorative layers are excluded from the accessibility tree. |
| A11Y-3 | Right/keep and left/delete are distinguishable by words or icons as well as color and motion. |
| A11Y-4 | Focus order follows task order: progress and photo context, keep/delete controls, undo, secondary navigation. |
| A11Y-5 | After a committed decision, focus and announcement move predictably to the next card; Undo announces the restored decision. |
| A11Y-6 | Large text and display scaling through 200% reflow content without clipping confirmation counts, permission copy, or recovery actions. |
| A11Y-7 | Touch targets meet Android platform accessibility guidance and retain separation when layouts compress. |
| A11Y-8 | Reduced-motion mode removes nonessential spring/travel effects while keeping state changes and outcomes immediately perceivable. |
| A11Y-9 | Haptics are optional and never the only feedback channel. |
| A11Y-10 | Partial, failed, and canceled deletion states use explicit headings and counts that screen readers can review. |
| A11Y-11 | System permission and deletion prompts are preceded by app copy that explains why the system surface is appearing. |
| A11Y-12 | High-contrast and color-vision testing must preserve border, text, icon, and state distinction. |
| A11Y-13 | Switch Access and keyboard/D-pad navigation can reach every action, including review revisions, Privacy, analytics, feedback, rating, reset, and recovery. |
| A11Y-14 | Completion celebration does not steal focus repeatedly, trap navigation, or obscure unresolved-result content. |
| A11Y-15 | Every control target is at least 48×48 dp; primary Keep/Delete actions remain at least 64 dp. |
| A11Y-16 | Body text targets 4.5:1 contrast; large text and UI graphics target 3:1 in both light and dark themes. |
| A11Y-17 | English, Spanish, French, German, Portuguese, Italian, Japanese, Korean, Hindi, and Arabic are complete and reviewed before release; no visible string falls back unexpectedly. |
| A11Y-18 | Arabic uses logical start/end layout and RTL mirroring while Keep/Delete remain explicitly labeled and the localized onboarding demonstrates their direction. |

## 15. Acceptance Criteria (pass/fail)

| Acceptance ID | Scope | Scope ID | Binary criterion | Test method |
| --- | --- | --- | --- | --- |
| DAC-1 | global | permanent-free | Every accessible month and selected-photo group is usable with no ad, cap, paywall, trial, purchase, subscription, entitlement, or cross-promotion anywhere in the app. | Free-access invariant test plus end-to-end traces across first, second, and later 501+ item sessions. |
| DAC-2 | journey | J3 | A new session snapshot becomes durable only when its first card is actionable; browsing, denial, and load failure do not create false progress. | Integration test over permission/access and session transitions. |
| DAC-3 | screen | SCR-05 | Every eligible full-access calendar month is listed with item count and saved progress; unavailable media is not counted complete. | MediaStore fixture UI test. |
| DAC-4 | journey | J3 | Selecting a saved month restores all prior decisions and the exact next undecided item after backgrounding and process recreation. | Process-death device test. |
| DAC-5 | screen | SCR-07 | A completed right swipe records keep and advances exactly one item; a completed left swipe records delete and advances exactly one item. | Gesture instrumentation test. |
| DAC-6 | screen | SCR-07 | A canceled or sub-threshold gesture returns the card to rest and records no decision. | Gesture threshold unit/UI test. |
| DAC-7 | screen | SCR-07 | Visible Keep and Delete controls produce the same ledger outcomes as swipes. | Accessibility/UI test. |
| DAC-8 | screen | SCR-07 | Undo restores the latest decided item, removes its prior decision, and remains available before commit. | Ledger integration and UI test. |
| DAC-9 | screen | SCR-09 | Keep and Delete group totals equal all successfully loaded actionable items; unavailable items are separately represented. | Review reconciliation test. |
| DAC-10 | screen | SCR-09 | Moving an item between groups updates both groups and totals immediately and leaves one final decision. | UI plus Room transaction test. |
| DAC-11 | screen | SCR-09 | Leaving review without confirmation preserves decisions and performs no MediaStore deletion. | Device test with resolver spy. |
| DAC-12 | screen | SCR-10 | Deletion cannot start until Keepr shows the exact pending count, permanence copy, and an explicit confirmation action. | UI semantics test. |
| DAC-13 | journey | J5 | Every destructive operation requires Android system confirmation in addition to Keepr confirmation. | API 29 and API 30+ device test. |
| DAC-14 | screen | SCR-11 | After each system result, Keepr re-queries every requested URI and reports confirmed, failed, canceled, and unresolved counts separately. | Device integration test with mixed outcomes. |
| DAC-15 | screen | SCR-11 | Canceling a system deletion request returns to a recoverable state with pending decisions intact. | Device UI test. |
| DAC-16 | journey | J5 | On target API 36+, more than 2,000 delete URIs are split into bounded batches, batch count is explained, and completion waits for all terminal results. | Android 16 device test with 2,101 images. |
| DAC-17 | screen | SCR-12 | A month is marked complete only after all eligible items have decisions and deletion outcomes are terminal or explicitly acknowledged. | State-machine property test. |
| DAC-18 | screen | SCR-12 | Completion displays kept, confirmed deleted, unresolved, and result status; partial outcomes are not presented as full success. | Snapshot and semantics test. |
| DAC-19 | screen | SCR-12 | Reclaimed space is labeled estimated, uses only confirmed deleted rows with known size, and is omitted when unknown. | Unit and UI test. |
| DAC-20 | screen | SCR-04 | Before the system permission prompt, the exact photo-access rationale is visible and understandable. | UI text assertion. |
| DAC-21 | journey | J2 | Selected-photo mode is labeled, shows only Android-granted items, offers reselection/full access, and never claims an unseen full month is cleared. | Android 14+ permission matrix test. |
| DAC-22 | screen | SCR-19 | Permission denial or Settings recovery preserves sessions, remains dismissible, and never masquerades as an empty library. | Navigation and process-restoration test. |
| DAC-23 | journey | J9 | Killing the process on every core route restores a named valid state without losing committed decisions or duplicating permission, deletion, share, rating, or navigation effects. | Route-by-route fault-injection device test. |
| DAC-24 | screen | SCR-08 | A media-load failure offers Retry and Skip for now and records neither keep nor delete. | Synthetic provider test. |
| DAC-25 | global | account | Core cleanup requires no Keepr account, makes no cloud-backup claim, and sends no photo content or identifiers off-device. | Network inspection and content audit. |
| DAC-26 | global | route-graph | The route graph contains SCR-01..SCR-22 and no commercial destination, deep link, CTA, Settings row, completion action, or automatic redirect. | Navigation graph and prototype-registry diff. |
| DAC-27 | global | dependency-boundary | The product specification requires no billing, ad, mediation, purchase, entitlement, or commercial experiment SDK/component. | Design inventory plus implementation dependency/SBOM review. |
| DAC-28 | global | state-and-content | All 22 design routes have valid states/exits and no executable or user-facing commercial state/copy. | Screen matrix, content inventory, and navigation traversal. |
| DAC-29 | journey | J7 | Core cleanup from access through reconciled completion remains fully usable in airplane mode for first and repeated sessions. | Offline end-to-end device test. |
| DAC-30 | screen | SCR-15 | Analytics starts disabled, requires affirmative consent, rejects forbidden parameters, and withdrawal disables collection and resets local analytics data. | Release configuration and event allowlist test. |
| DAC-31 | screen | SCR-18 | Reset Keepr clears local sessions, preferences, consent, and analytics identifier/queue but does not delete any MediaStore photo. | Integration test with local and media fixtures. |
| DAC-32 | global | visual-system | User-facing screens use the Keepr custom system and contain no stock Material 3 visual component/theme in the release dependency and screenshot allowlist. | Dependency audit plus golden screenshot review. |
| DAC-33 | global | motion | Reduced-motion mode preserves every action and state change while removing nonessential spring/travel effects. | Accessibility setting UI test. |
| DAC-34 | global | accessibility | Keep, delete, undo, review, confirm, recovery, privacy, analytics, feedback, rating, reset, and permission actions are operable by TalkBack and Switch Access with non-color semantics. | Accessibility device test. |
| DAC-35 | screen | SCR-01 | Splash routes to a valid destination within the bounded startup budget or exposes Retry/Reset; it never hangs. | Macrobenchmark and timeout test. |
| DAC-36 | screen | SCR-02 | The selected language persists, current choice is announced, and unsupported device locales fall back to English. | Locale recreation test. |
| DAC-37 | screen | SCR-03 | Onboarding states the month ritual, review-before-delete safety, on-device photo handling, and that every month is free with no ads, purchases, or swipe limits. | UI text and navigation test. |
| DAC-38 | screen | SCR-16 | Feedback export excludes content URIs, media IDs, filenames, dates, thumbnails, photo metadata, exact library statistics, and analytics identifiers. | Payload unit test. |
| DAC-39 | screen | SCR-17 | Rate-Us is user-initiated, dismissible, non-rewarded, and never appears during active cleanup or recovery. | Navigation/frequency policy test. |
| DAC-40 | global | retention | No missed-day punishment, broken-streak copy, notification nagging, or themed promotional interruption exists in the active product flow. | Content inventory and notification manifest audit. |
| DAC-41 | global | performance | Release build meets TTID and frame budgets on the Step 7 reference device and fixtures. | Macrobenchmark. |
| DAC-42 | global | resources | Database, media-cache, and battery budgets stay within Step 7 ceilings during the 500-photo reference session. | Performance and storage instrumentation. |
| DAC-43 | global | privacy | Network inspection finds no photo bytes, thumbnails, filenames, dates, content URIs, MediaStore IDs, exact counts, or exact byte totals leaving the device. | Signed-release proxy inspection. |
| DAC-44 | screen | SCR-14 | Settings exposes language, access scope, dark theme, motion, haptics, privacy, analytics, feedback, rate, diagnostics, and Reset Keepr with no commercial row. | UI inventory test. |
| DAC-45 | global | navigation-and-artifacts | Every SCR-01..SCR-22 state has a reachable action, automatic exit, or back destination; the prototype registry, design tokens/components, this Design PRD, Code PRD, generated bundle, and manifest agree. | Automated graph traversal, artifact diff, and manual review. |

## 16. Requirements & User-Story Coverage Map

| ID | Kind | Feature IDs | Journey IDs | Screen IDs | Acceptance IDs |
| --- | --- | --- | --- | --- | --- |
| R1 | requirement | F9 | J3, J6, J7 | SCR-05, SCR-06, SCR-07, SCR-12 | DAC-1, DAC-2, DAC-26 |
| R2 | requirement | F1, F6 | J3, J9 | SCR-05, SCR-07 | DAC-3, DAC-4, DAC-23 |
| R3 | requirement | F2 | J3 | SCR-07 | DAC-5, DAC-6, DAC-7 |
| R4 | requirement | F3, F4 | J3, J4 | SCR-07, SCR-09 | DAC-8, DAC-10, DAC-11 |
| R5 | requirement | F4, F5 | J4, J5 | SCR-09, SCR-10, SCR-11 | DAC-9, DAC-12, DAC-13, DAC-14, DAC-15 |
| R6 | requirement | F5, F6 | J5, J9 | SCR-08, SCR-11, SCR-19, SCR-21, SCR-22 | DAC-14, DAC-23, DAC-24 |
| R7 | requirement | F1, F7 | J3, J6 | SCR-05, SCR-12 | DAC-3, DAC-17, DAC-18, DAC-19 |
| R8 | requirement | F2, F8 | J3 | SCR-03, SCR-07 | DAC-32, DAC-33, DAC-34 |
| R9 | requirement | F10 | J1, J2, J8 | SCR-04, SCR-06, SCR-13, SCR-14, SCR-15, SCR-19, SCR-20 | DAC-20, DAC-21, DAC-22, DAC-25, DAC-43 |
| R10 | requirement | F9 | J3, J6, J7 | SCR-05, SCR-12, SCR-14 | DAC-1, DAC-26, DAC-27 |
| F1 | feature | F1 | J1, J3 | SCR-05, SCR-07 | DAC-2, DAC-3, DAC-4 |
| F2 | feature | F2 | J3 | SCR-07 | DAC-5, DAC-6, DAC-7 |
| F3 | feature | F3 | J3, J4 | SCR-07, SCR-09 | DAC-8, DAC-10 |
| F4 | feature | F4 | J4 | SCR-09, SCR-10 | DAC-9, DAC-10, DAC-11, DAC-12 |
| F5 | feature | F5 | J5 | SCR-10, SCR-11 | DAC-12, DAC-13, DAC-14, DAC-15, DAC-16 |
| F6 | feature | F6 | J2, J3, J5, J9 | SCR-07, SCR-08, SCR-11, SCR-19, SCR-21, SCR-22 | DAC-4, DAC-21, DAC-23, DAC-24 |
| F7 | feature | F7 | J6 | SCR-12 | DAC-17, DAC-18, DAC-19 |
| F8 | feature | F8 | J1, J3 | SCR-03, SCR-07 | DAC-32, DAC-33, DAC-34 |
| F9 | feature | F9 | J3, J6, J7 | SCR-05, SCR-06, SCR-07, SCR-12, SCR-14 | DAC-1, DAC-2, DAC-26, DAC-27, DAC-28, DAC-29 |
| F10 | feature | F10 | J1, J2, J8 | SCR-03, SCR-04, SCR-06, SCR-13, SCR-14, SCR-15, SCR-19, SCR-20 | DAC-20, DAC-21, DAC-22, DAC-25, DAC-30, DAC-43 |
| US-1 | user story | F1 | J3 | SCR-05, SCR-07 | DAC-2, DAC-3, DAC-4 |
| US-2 | user story | F2, F8 | J3 | SCR-07 | DAC-5, DAC-6, DAC-7, DAC-32, DAC-33 |
| US-3 | user story | F3 | J3 | SCR-07 | DAC-8 |
| US-4 | user story | F4 | J4 | SCR-09 | DAC-9, DAC-10, DAC-11 |
| US-5 | user story | F5 | J5 | SCR-10, SCR-11 | DAC-12, DAC-13, DAC-14, DAC-15 |
| US-6 | user story | F6 | J3, J9 | SCR-07, SCR-08, SCR-11, SCR-19, SCR-21, SCR-22 | DAC-4, DAC-23, DAC-24 |
| US-7 | user story | F7 | J6 | SCR-12 | DAC-17, DAC-18, DAC-19, DAC-40 |
| US-8 | user story | F9 | J3, J6, J7 | SCR-05, SCR-06, SCR-07, SCR-12, SCR-14 | DAC-1, DAC-2, DAC-26 |
| US-9 | user story | F10 | J1, J2 | SCR-03, SCR-04, SCR-06 | DAC-20, DAC-21, DAC-22, DAC-25 |
| US-10 | user story | F8 | J3 | SCR-07 | DAC-32, DAC-33, DAC-34 |

### Upstream story acceptance preservation

**US-1 — As a backlog finisher, I want to choose a calendar month and see its size before starting so that the cleanup feels bounded and achievable.**
- `AC-US1.1` — Given photo access is available, the month picker lists every month containing eligible local media with its item count.
- `AC-US1.2` — Selecting a month opens its existing saved progress when present; otherwise it starts at the first undecided item.
- `AC-US1.3` — The active month view displays completed decisions and remaining items without counting unavailable media as completed.

**US-2 — As a ritual seeker, I want each photo decision to feel immediate and playful so that I remain engaged through a large month.**
- `AC-US2.1` — A completed right swipe records keep and advances exactly one item; a completed left swipe records delete and advances exactly one item.
- `AC-US2.2` — A cancelled or sub-threshold gesture returns the card to rest and records no decision.
- `AC-US2.3` — Visible keep and delete controls provide the same outcomes without requiring a swipe gesture.
- `AC-US2.4` — Motion and depth effects never cover the active photo, decision labels, progress, or undo control at the moment a decision is committed.

**US-3 — As a cautious curator, I want to undo a mistaken swipe immediately so that speed does not create irreversible loss.**
- `AC-US3.1` — Undo restores the most recently decided item to the active card and removes its prior keep or delete state.
- `AC-US3.2` — Undo remains available to every user throughout active triage and before final deletion commit.
- `AC-US3.3` — Undoing and re-deciding an item leaves exactly one final decision for that item.

**US-4 — As a cautious curator, I want to review every pending deletion before committing so that I can catch mistakes.**
- `AC-US4.1` — After the last undecided item, the app opens a review state containing separate keep and delete groups whose totals equal all successfully loaded items in the month.
- `AC-US4.2` — Moving an item from delete to keep or keep to delete updates both groups and their totals immediately.
- `AC-US4.3` — Leaving review without confirming deletion preserves all decisions and performs no destructive media operation.

**US-5 — As a cautious curator, I want deletion results to distinguish success from partial or complete failure so that I know what actually happened.**
- `AC-US5.1` — Deletion begins only after an explicit confirmation that states the number of items marked for deletion.
- `AC-US5.2` — The result state reports confirmed deletions, failures, and items still requiring system or user action as separate counts.
- `AC-US5.3` — A partial or failed result offers a recovery path and does not mark affected items as deleted until the platform confirms them.
- `AC-US5.4` — Cancelling a platform deletion request returns to a recoverable state with the pending decisions intact.

**US-6 — As a cautious curator, I want my session to survive interruption or loading problems so that I never repeat completed decisions.**
- `AC-US6.1` — After backgrounding or process recreation, reopening the active month restores all prior decisions and the next undecided item.
- `AC-US6.2` — A media-load failure exposes retry and skip-for-now actions without silently recording keep or delete.
- `AC-US6.3` — If required photo access is unavailable, the app preserves the session and routes the user to a permission-recovery explanation rather than resetting progress.

**US-7 — As a ritual seeker, I want an unmistakable completion moment so that finishing a month feels rewarding and worth repeating.**
- `AC-US7.1` — A month is marked complete only after all eligible items have final decisions and the deletion result has resolved to confirmed, partial, or failed.
- `AC-US7.2` — The completion state displays kept count, confirmed deleted count, unresolved count, and reclaimed-space value when it can be measured.
- `AC-US7.3` — The completion state contains no missed-day penalty, broken-streak language, or notification pressure.

**US-8 — As a backlog finisher, I want every month permanently free so that I can repeat cleanup without commercial interruption or uncertainty.**
- `AC-US8.1` — Across onboarding, permission recovery, every cleanup session, review, deletion, completion, settings, and recovery, no ad, swipe cap, paywall, trial, purchase, subscription, entitlement, or cross-promotion is shown.
- `AC-US8.2` — Every accessible month or selected-photo group remains fully usable regardless of item count, prior sessions, elapsed time, connectivity, or reclaimed space.
- `AC-US8.3` — No commercial surface, route, dependency, state, string, SDK, or remote gate exists anywhere in the product.
- `AC-US8.4` — Product-facing copy states that every month is free with no ads, purchases, or swipe limits, with no hidden qualification.

**US-9 — As a cautious curator, I want to understand and control Keepr's photo access so that cleanup does not imply cloud backup or broad account access.**
- `AC-US9.1` — Before requesting photo access, the app explains that access is used to review and delete local camera-roll media.
- `AC-US9.2` — The core cleanup flow requires no Keepr account and presents no cloud-backup claim.
- `AC-US9.3` — When the platform supports limited photo access, the app explains the resulting scope and offers a path to adjust the selected media.

**US-10 — As a ritual seeker, I want the interface to feel uniquely Keepr without sacrificing clarity so that the chore feels playful but safe.**
- `AC-US10.1` — Primary tactile surfaces use the specified chunky borders, hard offset shadows, oversized rounded forms, and visible pressed-depth states rather than stock Material 3 component styling.
- `AC-US10.2` — Reduced-motion mode preserves every action and state change while replacing nonessential spring or travel effects with immediate transitions.
- `AC-US10.3` — Keep, delete, undo, review, confirm, and recovery actions remain distinguishable by text or iconography in addition to motion and color.

## 17. Open Decisions & Questions (every decision has an adopted default)

All decisions below are resolved defaults for downstream design and code planning. They remain reviewable, but none is unresolved.

### OD-1 — What commercial model ships?

- **a:** Permanently free with no commercial subsystem.
- **b:** Free at launch behind dormant billing/ads flags.

**Adopted default:** a — Permanently free with no commercial subsystem.  
**Rationale:** This is the approved production design. Absence of commercial code prevents hidden gating, accidental initialization, and future design drift.  
**Source:** production design override

### OD-2 — Which cleanup scopes are usable?

- **a:** Every accessible calendar month and selected-photo group.
- **b:** Only the current month.

**Adopted default:** a — Every accessible calendar month and selected-photo group.  
**Rationale:** Android access scope and actual media availability are the only access boundaries; no usage or commercial state is allowed.  
**Source:** production design

### OD-3 — What backend is required for core cleanup?

- **a:** None; all cleanup and recovery state is local.
- **b:** A remote access/feature service.

**Adopted default:** a — None; all cleanup and recovery state is local.  
**Rationale:** The no-account, airplane-mode, on-device privacy promise requires no remote product or access dependency.  
**Source:** production design

### OD-4 — What analytics implementation should launch?

- **a:** Firebase Analytics disabled by default, enabled only after explicit consent, with Advertising ID disabled and a strict bounded event allowlist.
- **b:** No network analytics; rely on local debugging counters and release-quality testing only.
- **c:** A self-hosted event endpoint with the same consent and parameter restrictions.

**Adopted default:** a — Firebase Analytics disabled by default, enabled only after explicit consent, with Advertising ID disabled and a strict bounded event allowlist.  
**Rationale:** The default supplies bounded product-health instrumentation while remaining optional, off by default, and excluding all media content, identifiers, dates, exact counts/bytes, and free text.  
**Source:** Step 6

### OD-5 — What primary navigation model should Keepr use?

- a — Persistent bottom navigation.
- b — Single-task route stack with Month Picker as root and Settings as the secondary graph.

**Adopted default:** b — Single-task route stack with Month Picker as root and Settings as the secondary graph.  
**Rationale:** The product has one dominant ritual. Persistent tabs would dilute focus and crowd the active cleanup task.  
**Source:** design

### OD-6 — How should language selection behave at launch?

- a — Support English, Spanish, French, German, Portuguese, Italian, Japanese, Korean, Hindi, and Arabic; follow a supported device locale and expose standalone selection with English fallback.
- b — Ship fewer locales or incomplete translations.

**Adopted default:** a — Support all ten named locales with standalone selection, persistence, English fallback, and Arabic RTL.  
**Rationale:** The production design explicitly names these locales; release is blocked until every visible string is translated and reviewed.  
**Source:** design

### OD-7 — How should feedback be delivered without introducing a new account or media-data backend?

- a — System share sheet using user-entered text and a previewed privacy-safe diagnostics block.
- b — Build a first-party support-ticket backend.

**Adopted default:** a — System share sheet using required user-authored text and a previewed privacy-safe diagnostics block.  
**Rationale:** It satisfies the Feedback surface while preserving the no-account boundary; no silent first-party upload exists.  
**Source:** design

### OD-8 — When may Rate-Us appear?

- a — Automatic modal during cleanup.
- b — User-initiated from Settings or a secondary action after truthful month completion.

**Adopted default:** b — User-initiated from Settings or a secondary action after truthful month completion.  
**Rationale:** This avoids secondary prompts crowding the primary task and prevents nagging or monetized review incentives.  
**Source:** design

### OD-9 — How should selected-photos access be represented?

- a — Treat it as a full month.
- b — Distinct Selected Photos Mode that shows only granted items and never claims an unseen calendar month is cleared.

**Adopted default:** b — Distinct Selected Photos Mode that shows only granted items and never claims an unseen calendar month is cleared.  
**Rationale:** This is required by PC-2 and keeps access scope and progress truthful.  
**Source:** design

### OD-10 — How should reclaimed space be worded?

- a — Exact space freed.
- b — Estimated space removed, omitted when source size is unknown.

**Adopted default:** b — Estimated space removed, omitted when source size is unknown.  
**Rationale:** MediaStore size may be null or stale, and device free space may not change exactly as predicted.  
**Source:** design

### OD-11 — What happens when a month contains no items marked for deletion?

- a — Force a deletion confirmation.
- b — Skip destructive system UI and show a valid completion result with zero deleted.

**Adopted default:** b — Skip destructive system UI and show a valid completion result with zero deleted.  
**Rationale:** No system delete request is needed, and completion must remain reachable without a dead end.  
**Source:** design

### OD-12 — How should haptics behave?

- a — Required for decision meaning.
- b — Optional supportive cue controlled by system/user preference; never the only feedback.

**Adopted default:** b — Optional supportive cue controlled by system/user preference; never the only feedback.  
**Rationale:** Accessibility and device variation prohibit making haptics semantic.  
**Source:** design

---

**Progression note:** Step 7b is conditionally skipped because Step 7 reports no required technical spike. This PRD is ready to serve as the Step 8 dependency for Step 9 Code PRD generation.
