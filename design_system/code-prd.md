# Keepr — Code Product Requirements Document

> Step 9 implementation contract, reconciled on 2026-08-02 against the production design in `ui_kits/keepr-app/` and `uploads/design-prd.md` v1.0. Where older upstream commercial assumptions differ, the implemented design and current Design PRD take precedence. Step 7b was not active because Step 7 reported no required spike.

## 1. Implementation Overview & Guiding Principles

Keepr is a local-first Android photo-cleaning application whose primary transaction is a durable, reversible month session followed by explicit user review and Android-owned deletion confirmation. The implementation must make the swipe loop feel immediate and playful without weakening deletion truthfulness, permission transparency, accessibility, recovery, or the permanent free-access promise.

**Permanent product constraint:** There are no ads, subscriptions, in-app purchases, entitlements, locked months, time limits, swipe limits, or promotional interruptions. This is not a launch experiment and must not be implemented as a remotely switchable state.

**Implementation authority:** The production design source (`ui_kits/keepr-app/`, repository tokens, and component specifications) and the current Design PRD (`uploads/design-prd.md`) are authoritative. This Code PRD translates them into Android architecture and may not override them. When the visual prototype is less explicit than the Design PRD, the Design PRD's privacy, accessibility, deletion-safety, recovery, and adaptive-layout requirements remain mandatory; routes, labels, hierarchy, and visual behavior follow the production design.

### 1.1 Non-negotiable engineering principles

1. **MediaStore is authoritative.** Room stores a session ledger, not ownership of photo truth. Re-query before review and after every destructive system result.
2. **Intent is not deletion.** A left swipe records reversible delete intent only. Permanent deletion begins only after Keepr confirmation and Android system confirmation.
3. **Exactly-once state transitions.** Every decision, undo, batch preparation, batch terminal result, completion mark, and analytics emission has an idempotency key or transactional guard.
4. **Permanent free access is a product invariant.** Every accessible month and selected-photo group is usable without an account, payment, ad, quota, trial, entitlement, time limit, swipe limit, or promotional interruption. F9 is the compile-time and runtime policy authority.
5. **Local-first and privacy-bounded.** No photo bytes, thumbnails, filenames, content URIs, MediaStore IDs, capture dates, exact media counts, or exact byte totals leave the device.
6. **Custom UI, not Material 3.** Compose UI/Foundation may be used; stock Material 3 components, theme defaults, elevation, dialogs, app bars, cards, and motion are prohibited on user-facing surfaces.
7. **Main-thread austerity.** Cursor traversal, Room I/O, bitmap decode, JSON parsing, optional consented analytics upload, and deletion reconciliation never execute on main.
8. **Accessibility is functional parity.** Every swipe action has a labeled control; motion, color, and haptics never carry unique meaning.
9. **Recovery precedes convenience.** Process death, permission revocation, missing media, share/rating unavailability, and partial deletion always restore a named actionable state.
10. **No hidden scope reduction.** Every F, R, US, SCR, J, DAC, PC, and TEST identifier in the validated inputs remains in scope.

### 1.2 Acceptance-criterion adversarial audit

All reconciled acceptance criteria are technically achievable on the declared API 29–36 surface when implemented through the named mechanisms in this document. **The latest production design and current Design PRD supersede older commercial assumptions.** DAC-13 and DAC-16 are implemented through `RecoverableSecurityException`, `MediaStore.createDeleteRequest`, and the API 36 2,000-URI request limit rather than weakened. DAC-32 is implemented by excluding stock Material 3 user-facing components rather than approximating the visual system. The prototype route registry defines SCR-01..SCR-22 and is the screen-number authority.

### 1.3 KPI and benchmark instrumentation


| Metric ID | Events or test surface | Measurement | Not a guarantee |
| --- | --- | --- | --- |
| month_start_rate | month_picker_viewed, month_started | Distinct consented app instances with `month_started` divided by distinct consented app instances with `month_picker_viewed`. | Not a population guarantee; consented app instances only. Excludes users who decline analytics. A reinstall may create a new app instance. |
| month_completion_rate | month_started, month_completed | Distinct consented app instances with `month_completed` divided by distinct consented app instances with `month_started`. | Not a population guarantee; consented app instances only. Completion can span processes and the event reflects reconciled app state. |
| repeat_month_activation_rate | month_completed, additional_month_started | Distinct consented app instances starting another month after a completion divided by distinct consented app instances with a completion. | Not a retention guarantee; analytics is optional and off by default. |
| review_correction_rate | review_opened, review_item_moved | Sessions with at least one Keep/Delete revision divided by sessions that open Review. | Measures observed correction behavior, not error severity or user trust. |
| deletion_reconciliation_rate | delete_request_finished, deletion_reconciled | Terminal requested URIs reconciled as deleted/still present/unavailable divided by requested URIs. | Does not guarantee provider behavior; unresolved items remain separately reported. |
| access_recovery_rate | access_recovery_opened, access_recovered | Consented app instances that regain usable access after opening recovery divided by recovery opens. | Platform and user-choice dependent; selected access counts as recovered only for selected-photo mode. |
| TTID_p95 | Macrobenchmark startup iteration | Time to initial display at p95 must be ≤ 1,200 ms on the Step 7 reference device. | A lab benchmark, not a guarantee for every OEM, library size, or thermal state. |
| frame_time | Macrobenchmark/JankStats swipe trace | Normal frame budget 16.67 ms; percentile budget ≤ 22 ms during the reference swipe session. | Measured on the defined fixture and release build. |
| resource_budget | Storage and battery instrumentation | DB ≤ 25 MiB; media cache soft ceiling ≤ 64 MiB; battery use ≤ 5% over 15 minutes on the reference fixture. | Device- and workload-specific benchmark, not a universal user outcome. |

## 2. Repo Discovery & Adoption

> **Step 0 (mandatory, in PLAN MODE):** Inspect the repository at the working-directory root and record
> findings before planning any feature. Do not write code until this is complete.
>
> | Dimension | What to find | Where to look |
> |---|---|---|
> | Language(s) | Kotlin / Java / cross-platform | `*.kt/*.java`, build files |
> | Build tooling | Gradle (Groovy/KTS), version catalogs, AGP, modules | `settings.gradle(.kts)`, `gradle/libs.versions.toml` |
> | Min/target/compile SDK | minSdk / targetSdk / compileSdk | module `build.gradle` |
> | UI stack | Jetpack Compose / Views+XML | `MainActivity`, theme files, `@Composable` |
> | Navigation | Navigation-Compose / Fragment+Nav | nav host, route definitions |
> | State management | ViewModel+Flow / MVI | `*ViewModel`, state holders |
> | DI | Hilt / Koin / manual | `@HiltAndroidApp`, modules |
> | Persistence | Room / SQLDelight / DataStore | DAOs, `@Entity`, datastore |
> | Background/work | WorkManager / AlarmManager / services | manifest services |
> | Billing | Existing wrapper / none | billing dependency |
> | Lint/format | ktlint/detekt/spotless, naming, packages | config files |
> | Folder structure | feature-module vs layered | source tree |
>
> **Output of Step 0:** a short "Repo Adoption Note" mapping each dimension to a concrete decision.
> **Greenfield fallback:** if the repo is empty, defer stack choices to an Open Decision and use the
> recommended default (Kotlin + Compose + Material 3 + Koin/Hilt + Room + WorkManager) only if
> unanswered before coding.

### 2.1 Keepr-specific discovery rows


| Dimension | What to find | Required adoption rule |
| --- | --- | --- |
| Visual system | Existing custom tokens/components, Compose Material or Material 3 imports, screenshot baselines | Reuse compatible custom infrastructure; replace or isolate stock Material 3 user-facing surfaces. The Keepr design constraint overrides the generic greenfield Material 3 fallback. |
| Commercial dependency exclusion | Billing, purchase, entitlement, paywall, subscription, trial, ad SDK, ad placement, premium route, or locked-month code | Remove it from Keepr release source and dependency graphs. Any commercial UI, SDK, route, copy, dependency, or entitlement state is release-blocking. |

### 2.2 Required Repo Adoption Note

Before feature planning, record: discovered modules; current package namespace; existing min/target/compile SDK; architecture; navigation; DI; persistence; background work; commercial-dependency audit; lint/test tooling; custom design-system assets; migration obligations; and each deviation from the greenfield defaults below. Existing production architecture wins when it can satisfy this contract without parallel stacks. Any incompatible existing mechanism must be named in an engineering decision before code changes.

## 3. Open Questions & Decisions Required

All decisions have adopted defaults so implementation is not blocked. Stable decision identifiers are retained for traceability, but their values are reconciled to the latest design and Design PRD. Engineering decisions `OD-E1..OD-E8` may be replaced only by an equivalent repository-native mechanism documented in the Repo Adoption Note.

### 3.1 Preserved product decisions


| ID | Question | Adopted default | Rationale |
| --- | --- | --- | --- |
| OD-1 | What commercial model ships? | The complete app is permanently free. | The production design explicitly forbids ads, subscriptions, in-app purchases, entitlements, locked months, time limits, swipe limits, and promotional interruptions. |
| OD-2 | Which cleanup scopes are usable? | Every accessible month and Android-selected photo group is usable without gating. | Access is controlled only by Android media permission scope and actual media availability. |
| OD-3 | What backend is required? | None for core cleanup; all session and media work is local. | No account, purchase verification, or remote product service exists. Optional consented analytics is isolated and never required for cleanup. |
| OD-4 | What analytics implementation should launch? | Optional anonymous analytics, off by default, Advertising ID disabled, and a strict bounded event allowlist. | Consent can be declined or withdrawn without degrading any feature; withdrawal stops new collection and resets the local analytics identifier and queue. |
| OD-5 | What primary navigation model should Keepr use? | Single-task route stack with Month Picker as durable root and Settings as the secondary graph. | Matches the prototype registry and keeps the cleanup ritual focused. |
| OD-6 | How should language selection behave at launch? | Support English, Spanish, French, German, Portuguese, Italian, Japanese, Korean, Hindi, and Arabic; follow a supported device locale with English fallback. | The design requires ten reviewed locales, standalone selection, persistence, and RTL for Arabic. |
| OD-7 | How should feedback be delivered without introducing a new account or media-data backend? | a — System share sheet using user-entered text and a previewed privacy-safe diagnostics block. | It satisfies the required Feedback surface while preserving the no-account, minimal-remote-data model. |
| OD-8 | When may Rate-Us appear? | b — User-initiated from Settings or a secondary action after truthful month completion. | This avoids secondary prompts crowding the primary task and prevents nagging or monetized review incentives. |
| OD-9 | How should selected-photos access be represented? | Distinct Selected Photos Mode that never claims to represent an entire month. | Android access scope, not commercial policy, determines which items can be shown. Reselection and full-access actions remain available. |
| OD-10 | How should reclaimed space be worded? | b — Estimated space removed, omitted when source size is unknown. | MediaStore size may be null or stale, and device free space may not change exactly as predicted. |
| OD-11 | What happens when a month contains no items marked for deletion? | b — Skip destructive system UI and show a valid completion result with zero deleted. | No system delete request is needed, and completion must remain reachable without a dead end. |
| OD-12 | How should haptics behave? | b — Optional supportive cue controlled by system/user preference; never the only feedback. | Accessibility and device variation prohibit making haptics semantic. |

### 3.2 Engineering decisions


| ID | Question | Adopted default | Rationale |
| --- | --- | --- | --- |
| OD-E1 | What greenfield module and dependency baseline applies? | Kotlin + single-activity Compose UI/Foundation + Navigation Compose + MVVM/Clean + Hilt with KSP + Room with KSP + DataStore + WorkManager. No KAPT and no stock Material 3 user-facing dependency. | Matches the required architecture while honoring Keepr's non-Material visual system. Existing compatible repo choices may be adopted instead. |
| OD-E2 | How are UI collections exposed? | All persistent UI collections use `ImmutableList<T>` in immutable state models; mutable collections remain inside repositories/builders only. | Prevents accidental mutation and reduces unnecessary recomposition. |
| OD-E3 | How is thumbnail loading implemented? | A `ThumbnailRepository` wraps `ContentResolver.loadThumbnail`, cancellation, current-plus-two prefetch, an in-memory LRU, and an app-cache soft ceiling of 64 MiB; no original is copied. | Implements PC-4 and the media budget without tying feature modules to provider details. |
| OD-E4 | How is permanent free access enforced? | A release lint/dependency/navigation test forbids commercial SDKs, routes, states, strings, and feature flags. No billing adapter or purchase abstraction is created. | Absence is simpler and safer than a disabled commercial subsystem. |
| OD-E5 | How is the no-ads policy enforced? | No ad SDK, mediation SDK, manifest metadata, placement abstraction, format, or remote ad flag exists in the Keepr release graph. | Prevents accidental initialization or network traffic and matches the permanent product constraint. |
| OD-E6 | How are destructive commands made idempotent? | Persist `DeletionBatch(prepared)` before launching system UI, key every batch by `(sessionId,batchOrdinal)`, and reconcile by requested URI set before any retry. | Prevents duplicate system requests and false success after recreation. |
| OD-E7 | What sensitive local material may be persisted? | Structured session state, preferences, and consent only; never original bytes, thumbnails, EXIF, filenames, or media-derived analytics values. | App-private credential-encrypted storage and explicit backup rules satisfy the local privacy boundary without inventing token storage. |
| OD-E8 | What network boundary is adopted without a Keepr account? | Core cleanup has no network dependency. Optional consented analytics is the only app-originated service and accepts only compile-time allowlisted, non-media parameters. | Airplane-mode cleanup remains complete and network inspection can prove zero media-derived egress. |

## 4. Backend Specification

### 4.1 Boundary

Keepr's photo cleanup backend is **the device**: MediaStore, Room, DataStore, and foreground Android system UI. No Keepr account, photo-upload service, purchase service, entitlement service, or remote feature gate exists. Optional anonymous analytics is off by default, isolated from the core domain, and must reject media-derived fields at construction, logging, and transport boundaries.

### 4.2 Local entities


| Entity | Field | Kotlin/server type | Nullable | Constraint | Index | Residency |
| --- | --- | --- | --- | --- | --- | --- |
| MonthSession | sessionId | String (UUID) | No | primary key | primary | private_local |
| MonthSession | monthKey | String | No | local calendar year-month, immutable | unique | private_local |
| MonthSession | accessScope | enum class enum_full_selected_ | No | selected scope contains only Android-granted items | index | private_local |
| MonthSession | status | enum class enum_selecting_active_review_committing_partial_complete_abandoned_ | No | validated transition graph | index | private_local |
| MonthSession | totalSnapshotCount | Int | No | >= 0 | none | private_local |
| MonthSession | decidedCount | Int | No | 0..totalSnapshotCount | none | private_local |
| MonthSession | selectedDeleteBytes | Long | No | >= 0; estimate from MediaStore SIZE | none | private_local |
| MonthSession | createdAtEpochMs | Long | No | system time | none | private_local |
| MonthSession | updatedAtEpochMs | Long | No | monotonic per record | index | private_local |
| MediaDecision | decisionId | String (UUID) | No | primary key | primary | private_local |
| MediaDecision | sessionId | String (UUID) | No | foreign key MonthSession.sessionId, cascade delete | index | private_local |
| MediaDecision | volumeName | String | No | MediaStore volume | index | private_local |
| MediaDecision | mediaStoreId | Long | No | MediaStore _ID | composite_unique(sessionId,volumeName,mediaStoreId) | private_local |
| MediaDecision | contentUri | String | No | specific MediaStore item URI only | none | private_local |
| MediaDecision | mimeType | String | Yes | image/* when present | none | private_local |
| MediaDecision | capturedAtEpochMs | Long | Yes | DATE_TAKEN fallback DATE_ADDED | index | private_local |
| MediaDecision | sizeBytes | Long | Yes | >= 0 when known | none | private_local |
| MediaDecision | decision | enum class enum_undecided_keep_delete_ | No | default undecided | index | private_local |
| MediaDecision | displayOrder | Int | No | >= 0 and unique within session | composite_unique(sessionId,displayOrder) | private_local |
| MediaDecision | availability | enum class enum_available_missing_permission_lost_load_failed_deleted_ | No | default available | index | private_local |
| MediaDecision | updatedAtEpochMs | Long | No | system time | none | private_local |
| DeletionBatch | batchId | String (UUID) | No | primary key | primary | private_local |
| DeletionBatch | sessionId | String (UUID) | No | foreign key MonthSession.sessionId | index | private_local |
| DeletionBatch | batchOrdinal | Int | No | >= 1; unique within session | composite_unique(sessionId,batchOrdinal) | private_local |
| DeletionBatch | requestedCount | Int | No | 1..2000 on targetSdk 36+ | none | private_local |
| DeletionBatch | state | enum class enum_prepared_prompting_approved_canceled_reconciling_partial_failed_complete_ | No | validated transition graph | index | private_local |
| DeletionBatch | confirmedDeletedCount | Int | No | 0..requestedCount | none | private_local |
| DeletionBatch | failedCount | Int | No | 0..requestedCount | none | private_local |
| DeletionBatch | requestedAtEpochMs | Long | No | system time | none | private_local |
| DeletionBatch | completedAtEpochMs | Long | Yes | required for terminal states | none | private_local |
| PrivacyPreference | key | String | No | primary key | primary | private_local |
| PrivacyPreference | analyticsConsent | enum class enum_not_asked_declined_granted_withdrawn_ | No | default not_asked | none | private_local |
| PrivacyPreference | updatedAtEpochMs | Long | No | system time | none | private_local |
| AnalyticsLocalState | installationId | String (UUID) | Yes | exists only while consent is granted; never derived from media/device ad ID | primary | private_local |
| AnalyticsLocalState | queuedEventCount | Int | No | >= 0; queue cleared on withdrawal/reset | none | private_local |
| AnalyticsLocalState | lastUploadAttemptEpochMs | Long | Yes | null until consented upload attempt | none | private_local |

### 4.3 Room and DataStore requirements

- Room owns `MonthSession`, `MediaDecision`, and `DeletionBatch`; all state transitions use explicit transactions.
- Foreign keys and unique indexes must be represented in schema exports and migration tests.
- Database migrations may never silently drop session state. A destructive migration is allowed only for debug fixtures, never release.
- DataStore owns onboarding completion, locale choice, theme, motion/haptics preferences, access education state, and `PrivacyPreference`. Content URIs, filenames, photo metadata, and analytics payloads are prohibited.
- Room and DataStore stay in credential-encrypted app-private storage. Backup rules exclude session URI ledgers and every ephemeral thumbnail cache unless a migration-safe URI restoration policy is explicitly tested.
- `Reset Keepr` clears Room, DataStore, memory/disk thumbnails, and analytics local state in an ordered transaction-like reset flow; it does not invoke MediaStore deletion.

### 4.4 No commercial or entitlement API

The following are prohibited from the Keepr implementation and release infrastructure: purchase verification, entitlement reconciliation, Play billing notifications, remote price/product lookup, remote access decisions, ad configuration, and paywall experiments. There is no server-side state required to unlock or use a month.

| Boundary | Allowed | Prohibited |
| --- | --- | --- |
| Core cleanup | MediaStore, Room, DataStore, foreground Android confirmation | Any network dependency, account, upload, entitlement, or remote gate |
| Optional analytics | Consented allowlisted event name plus coarse enum/bucket values | Media URI/ID, filename, date, MIME type, thumbnail, exact media count, exact bytes, free text, Advertising ID |
| Feedback | User-authored text and previewed diagnostics handed to Android share sheet | Silent upload, automatic attachment, media metadata, analytics identifier |
| Rating | User action opening Play review/store UI | Incentive, blocking prompt, background request, feature gate |

### 4.5 On-device processing and security

1. Validate every Room transition in a transaction and treat MediaStore as authoritative before review and after deletion.
2. Store only the minimum identity and metadata needed to restore a session; never copy original photo bytes into the app sandbox.
3. Keep logs structured and allowlisted. Exclude content URIs, media IDs, filenames, dates, EXIF, MIME-derived content details, thumbnails, and exact library statistics.
4. Gate analytics locally on affirmative consent before SDK initialization, identifier creation, event construction, or upload.
5. On consent withdrawal, stop collection, reset the analytics SDK, and delete the local analytics identifier and queue before returning success.
6. Sanitize feedback into a previewed payload. The share sheet launches only after the user has authored text.
7. Protect exported components, use explicit intents where possible, and declare only necessary intent filters and providers.
8. Maintain release dependency, manifest, string, route, and generated-bundle checks that fail on any commercial implementation artifact.

### 4.6 Backend failure contract


| Failure | Client state | Retry behavior | User claim |
| --- | --- | --- | --- |
| No network / airplane mode | Core routes remain fully usable | Queue nothing unless analytics consent exists; retry optional analytics later | Cleanup, review, deletion, reconciliation, and settings continue locally. |
| Analytics endpoint unavailable | Consent remains granted; bounded local queue or drop policy applies | Backoff without blocking UI; never retry from main | No feature or completion claim depends on upload. |
| Analytics payload contains forbidden field | Reject locally before SDK call; debug assertion | Drop event and fix client defect | No media-derived payload is accepted. |
| Share target unavailable | SCR-16 `share_error` | Preserve authored text; allow copy/cancel/retry | Do not invent a backend fallback. |
| Play review/store unavailable | SCR-17 remains dismissible | Offer Feedback or return | Never block completion or settings. |

## 5. Frontend Specification

### 5.1 Module graph and responsibilities


| Module | Responsibility | Depends on |
| --- | --- | --- |
| :app | Application, single Activity, root navigation, startup/recovery routing, DI composition | All feature entry APIs and core contracts |
| :core:model | Immutable domain models, IDs, enums, errors, clocks | None |
| :core:database | Room entities, DAOs, transactions, migrations | `:core:model` |
| :core:datastore | Preferences and privacy state | `:core:model` |
| :core:mediastore | Queries, month boundaries, URI reconciliation, thumbnails, API-level deletion adapters | `:core:model` |
| :core:recovery | Startup/session/batch recovery use cases | `:core:database`, `:core:mediastore`, `:core:permissions` |
| :core:permissions | Versioned photo-access state and launch actions | Android framework only |
| :core:analytics | Consent gate, compile-time event/parameter allowlist, Firebase adapter | `:core:datastore` |
| :core:designsystem | Keepr primitives, motion, typography roles, accessibility semantics; no Material 3 visual components | Compose UI/Foundation |
| :core:policy | Permanent-free invariant and release scanners for routes, dependencies, strings, manifests, and generated bundles | `:core:model` |
| :core:testing | Synthetic MediaStore, clock, database, permission, locale, and navigation fixtures | Test configurations only |
| :feature:onboarding | SCR-02, SCR-03 | Core model/datastore/design system |
| :feature:permissions | SCR-04, SCR-06 | Core permissions/mediastore/design system |
| :feature:months | SCR-05 and F1 | Core database/mediastore/policy/design system |
| :feature:cleanup | SCR-07, SCR-08 and F2/F3 | Core database/mediastore/recovery/design system |
| :feature:review | SCR-09 and F4 | Core database/mediastore/design system |
| :feature:deletion | SCR-10, SCR-11 and F5 | Core database/mediastore/recovery/design system |
| :feature:completion | SCR-12 and F7 | Core database/analytics/design system |
| :feature:settings | SCR-13..SCR-18 | Core datastore/permissions/analytics/policy/design system |
| :feature:recovery | SCR-19..SCR-22 | Core recovery/permissions/mediastore/database/design system |

Feature modules expose route contracts and immutable UI models; they do not depend on one another's implementation modules. Cross-feature navigation is owned by `:app`.

### 5.2 Architecture and Compose state rules

- Use **MVVM + Clean Architecture** with presentation, domain/use-case, and data-adapter boundaries.
- Use **no KAPT**. Annotation processing must use KSP where required; prefer code-free interfaces where practical.
- Every screen exposes one immutable `UiState` plus explicit user intents and one-shot effects.
- Persistent collection properties in UI state use `ImmutableList<T>` from `kotlinx.collections.immutable`.
- Compose screens observe `StateFlow` with `collectAsStateWithLifecycle`; direct `collectAsState` for lifecycle-bound screen state is prohibited.
- ViewModels never hold `Activity`, `ContentResolver`, `Cursor`, `Bitmap`, navigation controller, or Compose state objects.
- Repository implementations own dispatchers. Public domain contracts identify I/O or default ownership; the UI never wraps unknown repository calls in ad hoc dispatchers.
- One-shot system launches use effect IDs persisted or acknowledged so recomposition and recreation cannot launch permission, deletion, share, rating, or review surfaces twice.
- Hilt with KSP is the greenfield DI default. Existing repository DI may be adopted when scopes and test replacement remain explicit.
- Compose UI/Foundation and custom Keepr components are allowed. `androidx.compose.material3` visual components/themes are prohibited from user-facing release code.

### 5.3 Core interfaces and dispatcher ownership


| Interface | Module | Kotlin signature | Dispatcher owner |
| --- | --- | --- | --- |
| MonthRepository | :core:database / :core:mediastore | `@IoDispatcher fun observeMonths(access: MediaPermissionState): Flow<ImmutableList<MonthSummary>>` | I/O |
| MonthRepository | :core:database / :core:mediastore | `@IoDispatcher suspend fun startOrResume(month: YearMonth, scope: AccessScope): SessionId` | I/O transaction |
| DecisionRepository | :core:database | `@IoDispatcher suspend fun commit(sessionId: SessionId, mediaId: DecisionId, decision: Decision): DecisionResult` | I/O transaction |
| DecisionRepository | :core:database | `@IoDispatcher suspend fun undoLatest(sessionId: SessionId): UndoResult` | I/O transaction |
| DecisionRepository | :core:database | `@IoDispatcher fun observeLedger(sessionId: SessionId): Flow<ProgressLedger>` | I/O upstream; main delivery |
| ThumbnailRepository | :core:mediastore | `@IoDispatcher suspend fun load(uri: Uri, size: IntSize, signal: CancellationSignal): ThumbnailResult` | I/O decode |
| ReviewRepository | :core:database / :core:mediastore | `@IoDispatcher fun observeReview(sessionId: SessionId): Flow<ReviewState>` | I/O |
| ReviewRepository | :core:database | `@IoDispatcher suspend fun revise(decisionId: DecisionId, to: Decision): RevisionResult` | I/O transaction |
| DeletionRepository | :core:mediastore / :core:database | `@IoDispatcher suspend fun prepare(sessionId: SessionId): ImmutableList<PreparedDeletionBatch>` | I/O transaction |
| DeletionRepository | :core:mediastore / :core:database | `@IoDispatcher suspend fun reconcile(batchId: BatchId, result: SystemDeleteResult): DeletionOutcome` | I/O query + transaction |
| RecoveryRepository | :core:recovery | `@IoDispatcher fun observeRecovery(): Flow<RecoveryState>` | I/O |
| FreeAccessPolicy | :core:policy | `fun observeInvariant(): Flow<FreeAccessInvariant>` | In-memory/static; no I/O |
| FreeAccessPolicy | :core:policy | `suspend fun verifyReleaseSurface(snapshot: ReleaseSurfaceSnapshot): FreeAccessAuditResult` | Default dispatcher |
| AnalyticsReporter | :core:analytics | `@DefaultDispatcher suspend fun record(event: AllowedAnalyticsEvent): AnalyticsRecordResult` | Default; upload delegated by SDK |
| AnalyticsConsentRepository | :core:analytics / :core:datastore | `@IoDispatcher fun observeConsent(): Flow<AnalyticsConsent>` | I/O |
| AnalyticsConsentRepository | :core:analytics / :core:datastore | `@IoDispatcher suspend fun setConsent(consent: AnalyticsConsent)` | I/O |
| FeedbackSanitizer | :feature:settings | `@DefaultDispatcher suspend fun buildPreview(input: FeedbackInput): SanitizedFeedbackPayload` | Default |
| ResetKeeprUseCase | :feature:settings | `@IoDispatcher suspend fun execute(): ResetResult` | I/O, ordered cleanup |

### 5.4 Feature service ownership


| Feature | Service/engine | Owning module | Inputs | Outputs | Threading |
| --- | --- | --- | --- | --- | --- |
| F1 | MonthCatalogService | :feature:months | permissionState: MediaPermissionState; selectedMonth: YearMonth?; clock: Clock | months: Flow<List<MonthSummary>>; session: Flow<MonthSessionState> | io → main_flow |
| F2 | SwipeDecisionEngine | :feature:cleanup | sessionId: UUID; gesture: SwipeGesture; currentMedia: MediaDecision | cardState: StateFlow<CardStackState>; decisionCommitted: Flow<DecisionResult> | render → main_flow |
| F3 | DecisionRevisionService | :feature:cleanup | sessionId: UUID; command: UndoOrReviseCommand | result: DecisionRevisionResult; ledger: Flow<ProgressLedger> | io → main_flow |
| F4 | ReviewQueueService | :feature:review | sessionId: UUID; filter: ReviewFilter; revision: DecisionRevision? | queue: Flow<List<ReviewItem>>; commitReadiness: Flow<CommitReadiness> | io → main_flow |
| F5 | MediaDeletionService | :feature:deletion | sessionId: UUID; approvedUris: List<Uri>; activityResult: DeleteRequestResult? | launchRequest: DeleteIntentSenderRequest?; outcome: Flow<DeletionOutcome> | io → main_flow |
| F6 | SessionRecoveryService | :core:recovery | processStart: ProcessStart; permissionState: MediaPermissionState; mediaStoreVersion: String? | recoveryState: Flow<RecoveryState>; resumeDestination: ScreenRoute | io → main_flow |
| F7 | CompletionMetricsService | :feature:completion | sessionId: UUID; deletionOutcome: DeletionOutcome | completion: Flow<MonthCompletion>; reclaimedBytesEstimate: Long? | default → main_flow |
| F8 | GameFeelRenderEngine | :core:designsystem | interactionState: InteractionState; motionScale: Float; windowSize: WindowSizeClass | visualState: StateFlow<GameFeelVisualState>; hapticCue: Flow<HapticCue> | render → main_flow |
| F9 | FreeAccessInvariantService | :core:policy | routeGraph: RouteGraph; dependencyGraph: DependencyGraph; resourceIndex: ResourceIndex | invariant: StateFlow<FreeAccessInvariant>; audit: Flow<FreeAccessAuditResult> | default → main_flow |
| F10 | MediaAccessCoordinator | :feature:permissions | sdkInt: Int; permissionResults: Map<String,Boolean>; selectionChanged: Boolean | accessState: StateFlow<MediaPermissionState>; permissionAction: PermissionAction | main → main_flow |

### 5.5 Navigation and effect ownership

- One Activity owns `ActivityResultRegistry`, permission launchers, `IntentSenderRequest`, system share sheet, Play review/store launch, and app Settings intents.
- ViewModels emit typed effects such as `RequestMediaPermission(effectId)`, `LaunchDeleteBatch(effectId,batchId)`, `ShareFeedback(effectId,payload)`, and `OpenPlayStore(effectId)`.
- The Activity acknowledges an effect before or atomically with launch; persisted pending system work is reconciled on recreation.
- Month Picker (`SCR-05`) is the durable post-setup root. Cleanup/review/deletion/recovery form one nested graph. Settings is the only secondary app graph; privacy, analytics, feedback, rate, and reset are its children.
- Back behavior follows the Design PRD exactly; system-prompt/reconciling states cannot be popped into an inconsistent route.

### 5.6 Keepr design system implementation

- Primitives: `InkSurface`, `DepthButton`, `PhotoDecisionCard`, `ProgressNumeral`, `MonthTile`, `DecisionBadge`, `ReviewItem`, `TruthfulResultPanel`, `PermissionPanel`, `RecoveryPanel`, `CelebrationLayer`.
- Hard offset shadows use draw operations and geometric offsets, not blurred elevation.
- Press states compress depth/offset and preserve layout; opacity alone is insufficient.
- Gesture transforms live in draw/graphics state. Database and analytics work begins only after settle.
- Reduced motion is resolved from system animator duration scale plus user preference and maps to immediate/short deterministic transitions.
- Golden screenshots cover compact, medium, and expanded widths; portrait and landscape; foldable postures; 200% font scale; high contrast; Arabic RTL; and both shipped light/dark themes.
- Production Compose uses adaptive constraints/`WindowSizeClass`, edge-to-edge insets, display-cutout handling, and no fixed prototype canvas. Support portrait phones from 320 dp, landscape, tablets, and foldables.
- Every control target is at least 48×48 dp; primary Keep/Delete actions remain at least 64 dp. Body text targets 4.5:1 contrast and large text/UI graphics target 3:1.
- Use the repository's local Archivo assets and Android vector drawables. Browser CDN fonts, Picsum images, and prototype-only icon delivery are not production dependencies.

## 6. Screen State Matrix

Every Design PRD screen/state is implemented below. State names are preserved exactly.

| Screen | Module | ViewModel | UI state type | State | Entry | Implementation/presentation | Effects | Exits |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| SCR-01 Splash | :app | BootstrapViewModel | BootstrapUiState | booting | Every cold or restored launch | Keepr mark in the custom visual language; no generic progress template | typed navigation or none | No user action required; Auto: Automatic route to SCR-02, SCR-03, SCR-04, SCR-05, SCR-07, or SCR-11 after bounded state resolution |
| SCR-01 Splash | :app | BootstrapViewModel | BootstrapUiState | load_error | State resolution exceeds bounded timeout or local state cannot be read | Clear recovery message with Retry and Reset Keepr route; no infinite spinner | typed navigation or none | Retry → booting; Reset → SCR-18 |
| SCR-02 Language Selection | :feature:onboarding | LanguageViewModel | LanguageUiState | populated | Supported language list is available | Current language is selected; device-language behavior is explained | typed navigation or none | Choose language → persist; Continue → SCR-03 or Back → SCR-14 |
| SCR-03 Onboarding | :feature:onboarding | OnboardingViewModel | OnboardingUiState | populated | First-run education is required | Three concise panels: one month at a time; right keeps/left deletes; review before permanent deletion; every month is free with no ads, purchases, or swipe limits; photos stay on device | typed navigation or none | Continue → SCR-04; Privacy details → SCR-13 |
| SCR-04 Media Access | :feature:permissions | MediaAccessViewModel | MediaAccessUiState | permission_required | Full access is absent | Exact rationale copy; actions Allow photo access, Choose photos where supported, Not now, Open settings after repeated denial | typed navigation or none | Full grant → SCR-05; partial → SCR-06; Not now → SCR-05 limited; settings → Android settings then re-check |
| SCR-04 Media Access | :feature:permissions | MediaAccessViewModel | MediaAccessUiState | partial_access | Selected photos only | Clearly states that only Android-selected items are visible; no feature is commercially restricted | permission/settings Activity result | Choose more photos → system selection; Allow all photos → system permission; Continue selected → SCR-06 |
| SCR-04 Media Access | :feature:permissions | MediaAccessViewModel | MediaAccessUiState | populated | Full access granted | Confirmation that photos stay on device and month catalog can be built | typed navigation or none | Continue → SCR-05; Auto: Optional automatic route after system result |
| SCR-05 Main / Month Picker | :feature:months | MonthPickerViewModel | MonthPickerUiState | loading | Month summaries are being queried | Skeleton/placeholder matching custom components; no full-library blocking spinner | typed navigation or none | Settings remains available; Retry appears on failure; Auto: Automatic to populated or empty |
| SCR-05 Main / Month Picker | :feature:months | MonthPickerViewModel | MonthPickerUiState | empty | No eligible images under current access | Route to SCR-20 rather than rendering a blank list | typed navigation or none | Empty details → SCR-20; Adjust → SCR-04; Refresh → loading; Settings → SCR-14 |
| SCR-05 Main / Month Picker | :feature:months | MonthPickerViewModel | MonthPickerUiState | permission_required | Access denied/revoked | Preserved-session notice and access explanation; route to the dedicated denial recovery | typed navigation or none | Recovery → SCR-19; Settings → SCR-14 |
| SCR-05 Main / Month Picker | :feature:months | MonthPickerViewModel | MonthPickerUiState | partial_access | Only selected items accessible | Persistent selected-photos badge; no month is falsely described as fully visible or cleared | typed navigation or none | Open selected mode → SCR-06; Allow all → SCR-04 |
| SCR-05 Main / Month Picker | :feature:months | MonthPickerViewModel | MonthPickerUiState | populated | Month summaries available | Every tile is interactive and shows month/year, sorted/total, Not started/In progress/Cleared status, and estimated reclaimed size when known; Settings is explicit | typed navigation or none | New month → SCR-07; saved month → SCR-21; Settings → SCR-14 |
| SCR-06 Selected Photos Mode | :feature:permissions | SelectedPhotosViewModel | SelectedPhotosUiState | empty | No currently selected accessible images | Explain Android-selected scope and offer reselection/full access | permission/settings Activity result | Choose photos → system selection; Allow all photos → SCR-04; Main → SCR-05 |
| SCR-06 Selected Photos Mode | :feature:permissions | SelectedPhotosViewModel | SelectedPhotosUiState | populated | Selected accessible images exist | Clearly labeled non-month cleanup; shows selected count and uses the same safety controls | typed navigation or none | Start selected cleanup → SCR-07 with partial scope; Reselect → system UI; Allow all → SCR-04 |
| SCR-07 Cleanup Session | :feature:cleanup | CleanupViewModel | CleanupUiState | loading | Current thumbnail is not yet ready | Card shell, bounded loading indication, progress and exit remain visible | typed navigation or none | Back → save and SCR-05; timeout → SCR-08; Auto: Automatic to active or load_error |
| SCR-07 Cleanup Session | :feature:cleanup | CleanupViewModel | CleanupUiState | active | Current item loaded | Single dominant photo card, enormous progress numeral, explicit KEEP and DELETE semantics, visible Undo when applicable, tactile pressed states | typed navigation or none | Swipe right/Keep → next active; swipe left/Delete → next active; Undo → restore previous; Back → save/Main; Auto: Automatic to SCR-09 when no undecided actionable items |
| SCR-07 Cleanup Session | :feature:cleanup | CleanupViewModel | CleanupUiState | load_error | Current item unavailable | Do not classify the item; show reason without exposing path/filename | typed navigation or none | Retry → loading; Skip for now → next active with unavailable status; Details → SCR-08 |
| SCR-07 Cleanup Session | :feature:cleanup | CleanupViewModel | CleanupUiState | permission_required | Access lost during session | Progress-preserved explanation | typed navigation or none | Recover access → SCR-04; Save and exit → SCR-05 |
| SCR-07 Cleanup Session | :feature:cleanup | CleanupViewModel | CleanupUiState | reduced_motion | System motion reduction active | Same card and controls; immediate/short state transitions, no decorative travel | typed navigation or none | All controls identical to active; Auto: Same destinations as active |
| SCR-08 Media Load Recovery | :feature:cleanup | MediaRecoveryViewModel | MediaRecoveryUiState | load_error | A specific item is missing, unreadable, corrupt, or permission-lost | Explain that no keep/delete decision was recorded; show Retry, Skip for now, and Return | typed navigation or none | Retry → source loading; Skip → source next item with unavailable status; Return → source screen |
| SCR-09 Pre-commit Review | :feature:review | ReviewViewModel | ReviewUiState | loading | Review groups are being reconciled with MediaStore | Keep/Delete tabs and totals unavailable until reconciliation completes | typed navigation or none | Back → save/Main; Retry on failure; Auto: Automatic to review_ready or partial_result |
| SCR-09 Pre-commit Review | :feature:review | ReviewViewModel | ReviewUiState | review_ready | All loaded actionable items have one current decision | Separate Keep and Delete groups; exact counts; estimated selected bytes only when known; item actions move groups or reopen card | typed navigation or none | Move group → remain; Reinspect → SCR-07 item; Delete selected → SCR-10; Save for later → SCR-05 |
| SCR-09 Pre-commit Review | :feature:review | ReviewViewModel | ReviewUiState | partial_result | Some snapshot items are unavailable or permission-lost | Visible unresolved group and explanation; commit includes only confirmed accessible delete items | typed navigation or none | Recover access → SCR-04; Retry items → SCR-08; Continue with accessible → SCR-10; Save → SCR-05 |
| SCR-10 Deletion Confirmation | :feature:deletion | DeletionConfirmationViewModel | DeletionConfirmationUiState | confirming | At least one accessible item is marked delete | States exact pending count, permanent deletion, Android confirmation, and number of batches when more than platform limit; Android 10 serial-prompt note when applicable | typed navigation or none | Delete permanently → SCR-11/system UI; Cancel → SCR-09 |
| SCR-10 Deletion Confirmation | :feature:deletion | DeletionConfirmationViewModel | DeletionConfirmationUiState | empty | Delete group is empty | No destructive CTA; explain that nothing will be deleted | typed navigation or none | Finish month → SCR-12; Back → SCR-09 |
| SCR-11 Deletion Progress & Recovery | :feature:deletion | DeletionProgressViewModel | DeletionProgressUiState | system_prompt | Android confirmation is in foreground | Keepr background state is persisted; no competing UI | Android deletion IntentSender | Android approve/cancel controls; Auto: Return → reconciling |
| SCR-11 Deletion Progress & Recovery | :feature:deletion | DeletionProgressViewModel | DeletionProgressUiState | reconciling | System result returned or app recovered | Batch progress, no premature success copy | typed navigation or none | No destructive repeat action; Cancel between remaining batches → partial_result; Auto: Automatic to next system prompt, partial_result, or complete |
| SCR-11 Deletion Progress & Recovery | :feature:deletion | DeletionProgressViewModel | DeletionProgressUiState | partial_result | Canceled, failed, missing, or permission-lost items remain | Separate confirmed, failed, canceled, and unresolved counts; exact next steps | typed navigation or none | Partial deletion details → SCR-22; Review remaining → SCR-09; Retry eligible → system_prompt |
| SCR-11 Deletion Progress & Recovery | :feature:deletion | DeletionProgressViewModel | DeletionProgressUiState | load_error | Reconciliation cannot complete | State remains recoverable and pending batch is not duplicated | typed navigation or none | Retry → reconciling; Return later → SCR-05 |
| SCR-11 Deletion Progress & Recovery | :feature:deletion | DeletionProgressViewModel | DeletionProgressUiState | complete | All batches terminal | Brief confirmed result summary | typed navigation or none | Continue → SCR-12; Auto: Optional automatic route after acknowledgement |
| SCR-12 Completion | :feature:completion | CompletionViewModel | CompletionUiState | complete | A month or selected-photo group reached a terminal reconciled result | Celebratory but truthful closure; kept, confirmed deleted, unresolved, and estimated space removed only when known; no guilt or streak punishment | typed navigation or none | Back to months → SCR-05; Rate Keepr → SCR-17; Resolve remaining → SCR-22 when applicable |
| SCR-12 Completion | :feature:completion | CompletionViewModel | CompletionUiState | partial_result | Completion acknowledged with unresolved items | Celebration is restrained; unresolved count and recovery are more prominent than reward | typed navigation or none | Partial deletion details → SCR-22; Back to months → SCR-05 |
| SCR-13 Privacy | :feature:settings | PrivacyViewModel | PrivacyUiState | populated | User opens Privacy from Settings or access education | Local implemented privacy screen states on-device photo handling, no account/upload, optional analytics boundary, retention/reset, and Data Safety parity | typed navigation or none | Back → SCR-14 |
| SCR-14 Settings | :feature:settings | SettingsViewModel | SettingsUiState | populated | Settings available | Language, photo access, dark theme, motion, haptics, analytics, privacy, feedback, rate, diagnostics, and Reset Keepr; no commercial row | typed navigation or none | Each row opens SCR-02/04/13/15/16/17/18 or its platform settings surface |
| SCR-15 Analytics Consent | :feature:settings | AnalyticsConsentViewModel | AnalyticsConsentUiState | populated | Consent choice can be changed | Explains bounded event collection, forbidden media data, optional status, and withdrawal/reset behavior | typed navigation or none | Share anonymous usage → enable and return; No thanks/Turn off → disable/reset and return |
| SCR-16 Feedback | :feature:settings | FeedbackViewModel | FeedbackUiState | populated | Feedback route opened | Category, required user-authored text, privacy-safe diagnostic preview, and system-share action; no photo attachment path | share sheet | Share feedback → Android share sheet only after text validates; Copy diagnostics → remain; Cancel → caller |
| SCR-17 Rate-Us | :feature:settings | RateUsViewModel | RateUsUiState | populated | User explicitly opens rating route | Neutral request with Rate Keepr, Maybe later, and Send feedback; no reward or guilt | Play review/store | Rate Keepr → Play review/store then return; Maybe later → caller; Send feedback → SCR-16 |
| SCR-18 Reset Keepr Confirmation | :feature:settings | ResetKeeprViewModel | ResetKeeprUiState | confirming | User taps Reset Keepr | Explains that local sessions, decisions, preferences, consent, and analytics identifier/queue are cleared; photos in MediaStore are never touched | ordered local reset | Reset Keepr → clear local state and SCR-01/SCR-03; Cancel → SCR-14 |
| SCR-19 Permission Denied | :feature:recovery | PermissionDeniedViewModel | PermissionDeniedUiState | denied | User denies photo access | Explain why access is needed and offer retry, selected photos, Not now, Privacy, and Settings when appropriate | permission/settings Activity result | Retry/full access → SCR-04; selected → SCR-06; Privacy → SCR-13; Not now → SCR-20 |
| SCR-19 Permission Denied | :feature:recovery | PermissionDeniedViewModel | PermissionDeniedUiState | permanently_denied | System prompt is no longer available | Explain Settings recovery without coercion; preserved sessions remain intact | app Settings intent | Open Settings → re-check; selected → SCR-06; Back/Not now → SCR-20 |
| SCR-20 Empty Library / Month | :feature:recovery | EmptyLibraryViewModel | EmptyLibraryUiState | empty_library | Accessible MediaStore query returns zero images | Distinguish empty access from denial; offer Scan again, access options, Settings, and Privacy | typed navigation or none | Scan → loading/Main; Access → SCR-04; Settings → SCR-14 |
| SCR-20 Empty Library / Month | :feature:recovery | EmptyLibraryViewModel | EmptyLibraryUiState | empty_month | Chosen month contains no accessible images | Explain that this month is empty and keep other months reachable | typed navigation or none | Back to months → SCR-05; Access → SCR-04 |
| SCR-21 Resume Session | :feature:recovery | ResumeSessionViewModel | ResumeSessionUiState | available | An unfinished local session exists | Explain restored month/group, progress, and privacy-safe local persistence before resuming | typed navigation or none | Resume → SCR-07; Start over → explicit confirmation then SCR-07; Month Picker → SCR-05 |
| SCR-21 Resume Session | :feature:recovery | ResumeSessionViewModel | ResumeSessionUiState | load_error | Restored ledger cannot resolve current media | Preserve decisions and expose Retry, review available items, or Month Picker | typed navigation or none | Retry → available; Review → SCR-09; Month Picker → SCR-05 |
| SCR-22 Partial Deletion | :feature:recovery | PartialDeletionViewModel | PartialDeletionUiState | partial_result | Reconciliation leaves still-present or unavailable items | Report confirmed deleted, still present, unavailable, and unresolved counts without over-claiming freed bytes | typed navigation or delete effect | Review unresolved → SCR-09; Retry eligible → SCR-11; Month Picker → SCR-05 |

Screen tests must instantiate every row directly, assert all visible controls, and traverse every listed exit. DAC-45 fails if any row lacks a reachable action, automatic exit, or valid back route. The implementation inventory must match the prototype registry: SCR-01..SCR-22 with no commercial route.

## 7. Flow Implementation

### 7.1 J1 — First run with full photo access

**Preconditions:** Fresh install; no restored session


| Order | Screen | Owner | Action | Result | Durability boundary |
| --- | --- | --- | --- | --- | --- |
| 1 | SCR-01 | :app | Launch Keepr | Resolve first-run state | No durable mutation unless action explicitly commits. |
| 2 | SCR-02 | :feature:onboarding | Choose language | Persist locale and continue | No durable mutation unless action explicitly commits. |
| 3 | SCR-03 | :feature:onboarding | Read ritual, privacy, deletion safety, and permanent free-access promise | Continue to access explanation | No durable mutation unless action explicitly commits. |
| 4 | SCR-04 | :feature:permissions | Tap Allow photo access and approve full access | Query month summaries | No durable mutation unless action explicitly commits. |
| 5 | SCR-05 | :feature:months | View available calendar months | Ready to select a month | No durable mutation unless action explicitly commits. |


**Success end:** Month picker is usable and every accessible month is available  


**Failure boundaries:** Permission denial routes to SCR-19/J2; an empty query routes to SCR-20. All retries reuse persisted IDs and never duplicate decisions, deletion batches, system launches, or analytics events.


### 7.2 J2 — Denied or selected-photos access recovery

**Preconditions:** Photo access is denied or partial


| Order | Screen | Owner | Action | Result | Durability boundary |
| --- | --- | --- | --- | --- | --- |
| 1 | SCR-19 | :feature:recovery | Read denial-specific access explanation | Choose full access, selected photos, Settings, Privacy, or Not now | No durable mutation unless action explicitly commits. |
| 2 | SCR-06 | :feature:permissions | Use selected-photos mode when partial | Review only selected items with partial-access label | No durable mutation unless action explicitly commits. |
| 3 | SCR-04 | :feature:permissions | Choose Allow all photos or reselect | System permission flow opens | No durable mutation unless action explicitly commits. |
| 4 | SCR-05 | :feature:months | Return after any usable access grant | Show full months or the selected-photo entry truthfully | No durable mutation unless action explicitly commits. |


**Success end:** Access state is truthful and reversible; no commercial state exists  


**Failure boundaries:** Not now routes to SCR-20; Settings denial remains recoverable. All retries reuse persisted IDs and never duplicate decisions, deletion batches, system launches, or analytics events.


### 7.3 J3 — Start, swipe, undo, and resume any accessible month

**Preconditions:** Usable access; an accessible month or selected-photo group; no matching resumable session


| Order | Screen | Owner | Action | Result | Durability boundary |
| --- | --- | --- | --- | --- | --- |
| 1 | SCR-05 | :feature:months | Select a calendar month | Open new or saved session | No durable mutation unless action explicitly commits. |
| 2 | SCR-07 | :feature:cleanup | Wait for first actionable card | Persist the session snapshot only when the card is usable | Room session transaction. |
| 3 | SCR-07 | :feature:cleanup | Swipe right/keep or left/delete, or use buttons | Commit exactly one reversible decision | Room decision/undo transaction after settle. |
| 4 | SCR-07 | :feature:cleanup | Tap Undo when needed | Restore latest card with no duplicate decision | Room decision/undo transaction after settle. |
| 5 | SCR-21 | :feature:recovery | Return to an unfinished session | Explain and restore the exact next undecided item and progress | No durable mutation unless resume/start-over explicitly commits. |


**Success end:** All actionable items have one decision and the review route opens  


**Failure boundaries:** Load failures route to SCR-08; permission loss routes to SCR-19; back saves. All retries reuse persisted IDs and never duplicate decisions, deletion batches, system launches, or analytics events.


### 7.4 J4 — Review and revise before deletion

**Preconditions:** Month has no undecided actionable items


| Order | Screen | Owner | Action | Result | Durability boundary |
| --- | --- | --- | --- | --- | --- |
| 1 | SCR-09 | :feature:review | Inspect Keep and Delete groups | Totals equal loaded actionable items | Review mutation or prepared-batch transaction; no MediaStore deletion yet. |
| 2 | SCR-09 | :feature:review | Move items between groups | Counts and pending bytes update immediately | Review mutation or prepared-batch transaction; no MediaStore deletion yet. |
| 3 | SCR-09 | :feature:review | Choose Continue cleaning when an item needs reinspection | Return to that item in Cleanup | Review mutation or prepared-batch transaction; no MediaStore deletion yet. |
| 4 | SCR-09 | :feature:review | Tap Delete selected | Open explicit confirmation | Review mutation or prepared-batch transaction; no MediaStore deletion yet. |


**Success end:** User reaches confirmation with final delete set  


**Failure boundaries:** Back or Save for later performs no destructive operation. All retries reuse persisted IDs and never duplicate decisions, deletion batches, system launches, or analytics events.


### 7.5 J5 — System-mediated deletion and reconciliation

**Preconditions:** Final delete set confirmed in Keepr


| Order | Screen | Owner | Action | Result | Durability boundary |
| --- | --- | --- | --- | --- | --- |
| 1 | SCR-10 | :feature:deletion | Review exact count, permanence, and number of system batches | Confirm or cancel | Review mutation or prepared-batch transaction; no MediaStore deletion yet. |
| 2 | SCR-11 | :feature:deletion | Approve each Android system deletion request | Persist batch state before and after each prompt | Persist batch state before prompt; reconcile requested URIs after result. |
| 3 | SCR-11 | :feature:deletion | Allow reconciliation | Re-query every requested item | Persist batch state before prompt; reconcile requested URIs after result. |
| 4 | SCR-11 | :feature:deletion | Acknowledge partial/failed/canceled items or retry | Reach terminal result | Persist batch state before prompt; reconcile requested URIs after result. |


**Success end:** Every item is confirmed deleted, unresolved, or failed; no inferred success  


**Failure boundaries:** Cancel returns to recoverable review; API 29 may require serial consent; API 36 may require multiple prompts. All retries reuse persisted IDs and never duplicate decisions, deletion batches, system launches, or analytics events.


### 7.6 J6 — Truthful completion and next cleanup

**Preconditions:** J5 reached a terminal reconciled result


| Order | Screen | Owner | Action | Result | Durability boundary |
| --- | --- | --- | --- | --- | --- |
| 1 | SCR-12 | :feature:completion | View kept, confirmed deleted, unresolved, and estimated reclaimed space | Current session becomes complete | Completion transaction after reconciliation. |
| 2 | SCR-12 | :feature:completion | Choose Back to months | Return to SCR-05 with every month available | No durable mutation unless action explicitly commits. |
| 3 | SCR-12 | :feature:completion | Choose Resolve remaining when applicable | Open SCR-22 recovery | No durable mutation unless action explicitly commits. |
| 4 | SCR-17 | :feature:settings | Optionally choose Rate Keepr | Open user-initiated rating route | No durable mutation unless action explicitly commits. |


**Success end:** User has a truthful result and can immediately clean another accessible month  


**Failure boundaries:** Unknown size omits estimate; unresolved outcomes remain explicit and route to SCR-22. All retries reuse persisted IDs and never duplicate decisions, deletion batches, system launches, or analytics events.


### 7.7 J7 — Verify permanent free access and repeat cleanup

**Preconditions:** Month Picker or Completion is visible


| Order | Screen | Owner | Action | Result | Durability boundary |
| --- | --- | --- | --- | --- | --- |
| 1 | SCR-05 | :feature:months | Inspect the month catalog | Every tile is interactive; no locked/commercial state is representable | Static/runtime free-access invariant. |
| 2 | SCR-05 | :feature:months | Open any new month | Start the local session without network or product lookup | Room session transaction. |
| 3 | SCR-21 | :feature:recovery | Open any unfinished month | Resume without a feature gate | No commercial state read or write. |
| 4 | SCR-12 | :feature:completion | Return after finishing | Another month remains immediately available | No durable mutation unless action explicitly commits. |


**Success end:** Unlimited repeat cleanup is available by construction  


**Failure boundaries:** Airplane mode has no effect on core cleanup. A route/dependency/resource audit failure blocks the build rather than degrading at runtime.


### 7.8 J8 — Privacy, analytics, feedback, language, rate, and reset

**Preconditions:** Main is available and no destructive system UI is active


| Order | Screen | Owner | Action | Result | Durability boundary |
| --- | --- | --- | --- | --- | --- |
| 1 | SCR-14 | :feature:settings | Open Settings | View explicit non-commercial secondary controls | No durable mutation unless action explicitly commits. |
| 2 | SCR-15 | :feature:settings | Grant, decline, or withdraw analytics consent | Collection remains aligned with choice | Persist consent before changing analytics collection. |
| 3 | SCR-02 | :feature:onboarding | Change language | Return to Settings with locale applied | No durable mutation unless action explicitly commits. |
| 4 | SCR-16 | :feature:settings | Compose/share feedback | Exclude media identifiers and content | No durable mutation unless action explicitly commits. |
| 5 | SCR-17 | :feature:settings | Open user-initiated rating flow | Return without coercion | No durable mutation unless action explicitly commits. |
| 6 | SCR-18 | :feature:settings | Confirm Reset Keepr or cancel | Clear local Keepr state only when confirmed | Ordered local reset. |


**Success end:** User retains control without a Keepr account  


**Failure boundaries:** Reset does not delete any MediaStore photo. All retries reuse persisted IDs and never duplicate decisions, deletion batches, system launches, or analytics events.


### 7.9 J9 — Process-death and external-change recovery

**Preconditions:** A non-terminal session or deletion batch exists at app start


| Order | Screen | Owner | Action | Result | Durability boundary |
| --- | --- | --- | --- | --- | --- |
| 1 | SCR-01 | :app | Launch after interruption | Detect recovery state | No durable mutation unless action explicitly commits. |
| 2 | SCR-22 | :feature:recovery | Resume partial deletion recovery when applicable | Re-query affected URIs and present exact unresolved counts | Persist batch state before prompt; reconcile requested URIs after result. |
| 3 | SCR-21 | :feature:recovery | Explain resumable cleanup | Resume to the exact next actionable item or return to Month Picker | No durable mutation unless action explicitly commits. |
| 4 | SCR-19 | :feature:recovery | Recover permission when revoked | Preserve ledger and route to explanation | No durable mutation unless action explicitly commits. |


**Success end:** A named actionable state is restored without duplicate decisions or false completion  


**Failure boundaries:** Missing media becomes partial/unavailable; no full-library blocking scan before first recovery frame. All retries reuse persisted IDs and never duplicate decisions, deletion batches, system launches, or analytics events.


## 8. Permission & System-Access Implementation

### 8.1 Exact pre-permission copy

> **Photo access:** “Keepr needs photo access to show your camera roll one month at a time and delete only the photos you choose. Your photos stay on this device.”

> **Android 14+ scope choice:** “Choose full photo access to clean an entire month, or choose selected photos to clean only those items. You can change this later.”

> **Internet disclosure:** “Keepr works offline. If you opt in, it may use the internet to send limited anonymous usage analytics. Photos, filenames, dates, and media IDs are never uploaded.”

### 8.2 Permission matrix


| Permission | Features | Type | Exact rationale | Fallback |
| --- | --- | --- | --- | --- |
| android.permission.READ_EXTERNAL_STORAGE | F1, F2, F4, F5, F10 | Runtime | Keepr needs photo access to show your camera roll one month at a time and delete only the photos you choose. Your photos stay on this device. | On Android 10–12L, show SCR-19 with “Not now” and “Allow photo access.” Do not scan without access or confuse denial with an empty library. Keep Privacy and Settings available. |
| android.permission.READ_MEDIA_IMAGES | F1, F2, F4, F5, F10 | Runtime | Keepr needs photo access to show your camera roll one month at a time and delete only the photos you choose. Your photos stay on this device. | If denied, show SCR-19. If Android grants selected photos only, enter clearly labeled SCR-06 and offer reselection/full access. |
| android.permission.READ_MEDIA_VISUAL_USER_SELECTED | F1, F6, F9, F10 | Runtime | Choose full photo access to clean an entire month, or choose selected photos to clean only those items. You can change this later. | Resolve `Selected`, show only granted items, and offer “Choose photos,” “Allow all photos,” and “Not now.” No product feature is commercially restricted. |
| android.permission.INTERNET | Optional analytics only | Manifest, omit when analytics adapter is absent | Keepr works offline. If you opt in, it may send limited anonymous usage analytics. Photos, filenames, dates, and media IDs are never uploaded. | Core cleanup remains complete offline. Consent must precede initialization and upload; withdrawal clears identifier/queue. |

### 8.3 Access-state algorithm

1. On API 29–32, full access means `READ_EXTERNAL_STORAGE == granted`.
2. On API 33, full image access means `READ_MEDIA_IMAGES == granted`.
3. On API 34+, resolve a sealed state: `Full`, `Selected`, or `Denied`. Never infer `Full` from a non-empty query result.
4. `Selected` routes to SCR-06, labels the scope partial, and never claims that an unseen month has been fully cleared.
5. Re-selection and full-access requests are explicit user actions. Repeated denial routes to SCR-19 and offers Settings, Privacy, selected photos, and Not now without coercion.
6. `onResume` re-evaluates permission state after system Settings or selection UI.
7. Permission revocation during a session sets affected media availability to `permission_lost`, preserves all ledger rows, and routes through SCR-04/SCR-08.
8. The manifest must not contain `READ_MEDIA_VIDEO`, `ACCESS_MEDIA_LOCATION`, `WRITE_EXTERNAL_STORAGE`, `MANAGE_EXTERNAL_STORAGE`, `MANAGE_MEDIA`, notification permission, or background media privileges.

### 8.4 Deletion system access

- A delete swipe updates only `MediaDecision.decision=delete`.
- SCR-10 freezes review edits and calls `prepare(sessionId)`; Room creates ordered `DeletionBatch` rows before any system surface.
- API 30+: build specific URI batches of at most 2,000 and call `MediaStore.createDeleteRequest(contentResolver, uris)` from the foreground Activity.
- API 29: attempt item deletion and handle `RecoverableSecurityException`; persist per-item/serial progress before launching its `userAction.actionIntent.intentSender`.
- The Activity result alone is not success. SCR-11 re-queries every requested URI. Absent rows become confirmed deleted only after reconciliation; readable rows remain unresolved/failed/canceled according to the persisted result.
- Back is unavailable while Android owns the prompt or reconciliation is committing. Between batches, Cancel is allowed and yields `partial_result`.
- WorkManager must never scan media or launch deletion. If retained for consented analytics delivery, it must be constrained, bounded, and incapable of blocking any feature.

## 9. Platform-Specific Implementation Requirements

| ID | Mechanism | API levels | Required behavior | Features |
| --- | --- | --- | --- | --- |
| PC-1 | Versioned MediaStore read-permission matrix | 29–32: READ_EXTERNAL_STORAGE; 33+: READ_MEDIA_IMAGES; targetSdk 36 | On Android 10–12L (API 29–32), request READ_EXTERNAL_STORAGE and query only MediaStore.Images. On Android 13+ (API 33+), request READ_MEDIA_IMAGES. Do not request READ_MEDIA_VIDEO, ACCESS_MEDIA_LOCATION, WRITE_EXTERNAL_STORAGE, MANAGE_EXTERNAL_STORAGE, or MANAGE_MEDIA. | F1, F2, F4, F5, F10 |
| PC-2 | Android 14 Selected Photos Access via READ_MEDIA_VISUAL_USER_SELECTED | 34+ | Detect full, partial, and denied access. Partial access runs a clearly labeled selected-photos mode, shows only granted items, and never claims a full unseen month is cleared. Provide in-app reselection and a route to request full access. | F1, F6, F9, F10 |
| PC-3 | MediaStore.Images query through ContentResolver | 29+ | Build a UTC-safe local calendar-month range, query image rows by DATE_TAKEN when present and DATE_ADDED as fallback, exclude trashed/pending rows, and store only stable row identity, volume, MIME type, size, and timestamps needed for the session ledger. | F1, F2, F6, F10 |
| PC-4 | ContentResolver.loadThumbnail with cancellation and bounded prefetch | 29+ | Load the current card plus at most the next two thumbnails off the main thread. Cancel work when a card leaves the stack, never copy originals into app storage, and surface unavailable-media states instead of blocking the swipe loop. | F2, F4, F6, F8 |
| PC-5 | System-confirmed permanent deletion | 29: per-item RecoverableSecurityException; 30+: createDeleteRequest | On API 30+, call MediaStore.createDeleteRequest from the foreground and launch its IntentSender. On API 29, attempt ContentResolver.delete and handle RecoverableSecurityException for each item. Card-level undo ends before this system-confirmed commit; after approval, reconcile every URI and report confirmed, partial, canceled, and failed outcomes. | F3, F4, F5, F6, F7 |
| PC-6 | API 36 MediaStore request limit | Targeting API 36+ | Chunk delete requests to no more than 2,000 MediaStore URIs per system prompt. Persist batch progress and require the user to approve each remaining batch; month completion occurs only after every batch reaches a terminal reconciled state. | F5, F6, F7 |
| PC-7 | Google Play target API requirement | Google Play submissions from 2026-08-31 | Build and release with targetSdk 36. Keep compileSdk at least 36 and run behavior-change testing before every target-SDK increase. | F1, F2, F3, F4, F5, F6, F7, F8, F9, F10 |
| PC-8 | Foreground ownership of destructive system UI | 29+ | Deletion may not be scheduled as unattended background work. The review screen owns the system confirmation launcher, persists an intent-to-delete record before launch, and resumes reconciliation after activity recreation or process death. | F4, F5, F6 |

### 9.1 SDK and build configuration

- `minSdk = 29`.
- `targetSdk = 36` and `compileSdk >= 36` before production release.
- Release CI fails when manifest permissions exceed the allowlist, target/compile SDK drift, Room schema export changes lack migration tests, or stock Material 3 user-facing dependencies enter the release graph.
- Baseline Profiles include startup to Main/recovery and first cleanup-card presentation.
- Use R8/ProGuard rules that preserve required Room, optional consented analytics adapter, and serialization behavior while stripping unused code.

### 9.2 Permanent free-access and commercial-dependency policy

- The entire app is permanently free. There is no billing, subscription, purchase, trial, entitlement, paywall, premium, locked-month, swipe-limit, time-limit, or ad behavior.
- Release source and dependency graphs must not contain BillingClient, Play Billing, `inapppurchasebillingutil`, AdMob/GMA, mediation, `adlibraryutil`, RevenueCat, product IDs, purchase tokens, entitlement models, or commercial feature flags.
- The typed navigation graph must contain only SCR-01..SCR-22 from the prototype registry. No commercial destination or deep link may exist.
- User-facing strings and generated resources must contain no price, unlock, restore purchase, free trial, paid access, or ad copy. The only permitted use of commercial terms is in developer documentation/tests asserting their absence.
- Every month tile and selected-photo cleanup entry is interactive based only on Android access, local availability, and recoverable session state.
- CI scans source, resources, manifests, Gradle/version catalogs, merged dependencies, navigation routes, generated bundle, and prototype strings. A match requires explicit review and blocks release when executable or user-facing.

### 9.3 Analytics and privacy

- Firebase Analytics collection, Advertising ID collection, and ad personalization are disabled in the release manifest/config by default.
- Consent is affirmative and revocable. `resetAnalyticsData` runs on withdrawal/reset.
- Only the Step 6 event names and enum/bucket parameters compile. Unknown event/parameter construction must be impossible outside `:core:analytics`.
- No user ID is set. No free text is an analytics parameter.
- Release proxy inspection must find no media-derived values or unknown SDK destinations.

### 9.4 Play release evidence

The release package includes: broad-photo-access declaration; core-functionality demonstration video; privacy policy matching shipped SDK behavior; Data Safety review for photos, app interactions, and device/other IDs when analytics is enabled; signed AAB manifest diff; target API check; dependency/SBOM review proving no commercial SDK; network capture; and deletion-flow screenshots/video. A mismatch blocks release.

## 10. Feature-by-Feature Build Spec with Acceptance Criteria

### 10.1 F1 — Month picker and progress ledger

**Tier:** P0  
**Requirement IDs:** R2, R7  
**User stories:** US-1  
**Primary module:** `:feature:months`


**Build spec:** Users choose one calendar month, see item count and progress, and resume exactly where they left off. Implement through `MonthCatalogService` with io work ownership and main-flow UI delivery. The service consumes permissionState, selectedMonth, clock and produces months, session. All state mutations are durable and idempotent where applicable.


| Acceptance ID | Pass/fail criterion | Test type | Preserves |
| --- | --- | --- | --- |
| AC-F1.1 | With full photo access, every month tile is interactive and lists month/year, item count, decided/total progress, Not started/In progress/Cleared state, and estimated reclaimed size when known; unavailable media is not counted complete. | integration | DAC-3; AC-US1.1; AC-US1.3 |
| AC-F1.2 | Opening a month routes through SCR-21 when a session exists; otherwise the session snapshot becomes durable only when the first actionable card is ready. | integration | DAC-2; DAC-4; AC-US1.2 |
| AC-F1.3 | Partial selected-photo scope is represented as its own group, shows only granted items, offers reselection/full access, and never falsely completes an unseen calendar month. | device | DAC-21; AC-US9.3 |


### 10.2 F2 — Tactile swipe-card triage

**Tier:** P0  
**Requirement IDs:** R3, R8  
**User stories:** US-2  
**Primary module:** `:feature:cleanup`


**Build spec:** Each photo is presented as a responsive card with unambiguous keep-right and delete-left outcomes plus accessible non-gesture controls. Implement through `SwipeDecisionEngine` with render work ownership and main-flow UI delivery. The service consumes sessionId, gesture, currentMedia and produces cardState, decisionCommitted. All state mutations are durable and idempotent where applicable.


| Acceptance ID | Pass/fail criterion | Test type | Preserves |
| --- | --- | --- | --- |
| AC-F2.1 | A settled right swipe commits exactly one keep decision and advances once; a settled left swipe commits exactly one delete-intent decision and advances once. | ui | DAC-5; AC-US2.1 |
| AC-F2.2 | A canceled or sub-threshold drag returns to rest and performs no ledger write. | ui | DAC-6; AC-US2.2 |
| AC-F2.3 | Visible Keep and Delete controls call the same decision command path and produce the same ledger result as swipes. | ui | DAC-7; AC-US2.3 |
| AC-F2.4 | Pointer movement performs no database write, bitmap decode, analytics upload, EXIF read, or allocation-heavy list copy. | performance | PATH-2; AC-US2.4 |


### 10.3 F3 — Session undo and decision revision

**Tier:** P0  
**Requirement IDs:** R4  
**User stories:** US-3  
**Primary module:** `:feature:cleanup`


**Build spec:** Users can reverse the latest choice during triage and revise any decision during final review. Implement through `DecisionRevisionService` with io work ownership and main-flow UI delivery. The service consumes sessionId, command and produces result, ledger. All state mutations are durable and idempotent where applicable.


| Acceptance ID | Pass/fail criterion | Test type | Preserves |
| --- | --- | --- | --- |
| AC-F3.1 | Undo atomically clears the latest reversible decision and restores that item as the current card. | integration | DAC-8; AC-US3.1 |
| AC-F3.2 | Undo and review revisions remain available to every user until the system-confirmed deletion commit begins. | ui | DAC-8; AC-US3.2 |
| AC-F3.3 | Undo followed by a new decision leaves exactly one final decision for the item and correct ledger totals. | unit | DAC-10; AC-US3.3 |


### 10.4 F4 — Pre-commit review queue

**Tier:** P0  
**Requirement IDs:** R4, R5  
**User stories:** US-4  
**Primary module:** `:feature:review`


**Build spec:** Before deletion, users can inspect all keep and delete decisions, move items between groups, and cancel without data loss. Implement through `ReviewQueueService` with io work ownership and main-flow UI delivery. The service consumes sessionId, filter, revision and produces queue, commitReadiness. All state mutations are durable and idempotent where applicable.


| Acceptance ID | Pass/fail criterion | Test type | Preserves |
| --- | --- | --- | --- |
| AC-F4.1 | Review exposes separate Keep, Delete, and Unavailable groups whose totals reconcile to all snapshot rows. | integration | DAC-9; AC-US4.1 |
| AC-F4.2 | Moving an item between Keep and Delete is atomic and updates both groups and pending-byte estimate immediately. | integration | DAC-10; AC-US4.2 |
| AC-F4.3 | Leaving review or saving for later performs no MediaStore deletion and preserves every decision. | integration | DAC-11; AC-US4.3 |
| AC-F4.4 | Delete selected is enabled only for a positive accessible delete count and opens Keepr confirmation, never system UI directly. | ui | DAC-12 |


### 10.5 F5 — Explicit deletion commit and result states

**Tier:** P0  
**Requirement IDs:** R5, R6  
**User stories:** US-5  
**Primary module:** `:feature:deletion`


**Build spec:** Deletion requires explicit confirmation and reports confirmed, partial, failed, and recoverable outcomes without claiming success prematurely. Implement through `MediaDeletionService` with io work ownership and main-flow UI delivery. The service consumes sessionId, approvedUris, activityResult and produces launchRequest, outcome. All state mutations are durable and idempotent where applicable.


| Acceptance ID | Pass/fail criterion | Test type | Preserves |
| --- | --- | --- | --- |
| AC-F5.1 | Keepr confirmation states exact accessible pending count, permanent-deletion meaning, and system batch count before any destructive request. | ui | DAC-12; AC-US5.1 |
| AC-F5.2 | Every destructive operation also receives Android system confirmation: per-item RecoverableSecurityException handling on API 29 and MediaStore.createDeleteRequest on API 30+. | device | DAC-13; PC-5 |
| AC-F5.3 | On target API 36+, requests contain no more than 2,000 URIs and prepared batches are persisted before each system prompt. | device | DAC-16; PC-6 |
| AC-F5.4 | After every system result, Keepr re-queries every requested URI and persists confirmed, failed, canceled, and unresolved counts separately. | integration | DAC-14; AC-US5.2; AC-US5.3 |
| AC-F5.5 | Cancellation returns to a recoverable state with pending decisions intact and never duplicates an already terminal batch. | device | DAC-15; AC-US5.4 |
| AC-F5.6 | A zero-delete month bypasses destructive system UI and reaches a valid completion result with zero confirmed deletions. | ui | OD-11 |


### 10.6 F6 — Durable session resume and recovery

**Tier:** P0  
**Requirement IDs:** R2, R6  
**User stories:** US-6  
**Primary module:** `:core:recovery`


**Build spec:** Backgrounding, process interruption, loading failure, and permission recovery preserve the user's month and prior decisions. Implement through `SessionRecoveryService` with io work ownership and main-flow UI delivery. The service consumes processStart, permissionState, mediaStoreVersion and produces recoveryState, resumeDestination. All state mutations are durable and idempotent where applicable.


| Acceptance ID | Pass/fail criterion | Test type | Preserves |
| --- | --- | --- | --- |
| AC-F6.1 | Process recreation restores the latest non-terminal session, exact next undecided item, and any prepared deletion batch without duplicate commands. | device | DAC-4; DAC-23; AC-US6.1 |
| AC-F6.2 | A media-load failure offers Retry and Skip for now and records neither keep nor delete. | integration | DAC-24; AC-US6.2 |
| AC-F6.3 | Permission loss preserves the ledger and routes to SCR-19 media-access recovery rather than resetting progress or showing an empty library. | device | DAC-22; AC-US6.3 |
| AC-F6.4 | Recovery performs bounded state resolution before first content frame; full-library reconciliation runs incrementally off main. | performance | DAC-35; PATH-5 |


### 10.7 F7 — Month completion and reclaimed-space celebration

**Tier:** P0  
**Requirement IDs:** R7  
**User stories:** US-7  
**Primary module:** `:feature:completion`


**Build spec:** A month ends with unmistakable closure, decision totals, deletion outcome, and reclaimed-space feedback; no missed-day punishment is used. Implement through `CompletionMetricsService` with default work ownership and main-flow UI delivery. The service consumes sessionId, deletionOutcome and produces completion, reclaimedBytesEstimate. All state mutations are durable and idempotent where applicable.


| Acceptance ID | Pass/fail criterion | Test type | Preserves |
| --- | --- | --- | --- |
| AC-F7.1 | Completion is persisted only after all actionable items have decisions and every deletion batch is terminal or the user explicitly acknowledges unresolved results. | integration | DAC-17; AC-US7.1 |
| AC-F7.2 | Completion displays kept, confirmed deleted, unresolved, and result status; partial results are not presented as full success. | ui | DAC-18; AC-US7.2 |
| AC-F7.3 | Estimated space removed sums only confirmed deleted rows with known size and is omitted when no trustworthy estimate exists. | unit | DAC-19 |
| AC-F7.4 | Completion contains no missed-day punishment, broken-streak copy, notification pressure, commercial promotion, or feature gate. | ui | DAC-40; AC-US7.3 |


### 10.8 F8 — Custom game-feel interaction shell

**Tier:** P0  
**Requirement IDs:** R8  
**User stories:** US-2, US-10  
**Primary module:** `:core:designsystem`


**Build spec:** Chunky ink borders, hard offset shadows, oversized rounded forms, depth-bearing controls, enormous numerals, and spring motion form one coherent system rather than stock Material styling. Implement through `GameFeelRenderEngine` with render work ownership and main-flow UI delivery. The service consumes interactionState, motionScale, windowSize and produces visualState, hapticCue. All state mutations are durable and idempotent where applicable.


| Acceptance ID | Pass/fail criterion | Test type | Preserves |
| --- | --- | --- | --- |
| AC-F8.1 | All user-facing tactile surfaces use the Keepr design system: chunky ink borders, hard offset shadows, oversized rounded forms, enormous progress numerals, and real pressed depth. | ui | DAC-32; AC-US10.1 |
| AC-F8.2 | The release dependency and screenshot allowlists contain no stock Material 3 visual component or theme on user-facing screens. | release | DAC-32 |
| AC-F8.3 | Reduced motion removes nonessential spring/travel effects while preserving every action, destination, and state change. | ui | DAC-33; AC-US10.2 |
| AC-F8.4 | Keep, delete, undo, review, confirmation, and recovery remain distinguishable by text/icon semantics in addition to color and motion. | device | DAC-34; AC-US10.3 |
| AC-F8.5 | Swipe and animation frame performance meets the Step 7 frame budget on the reference device. | performance | DAC-41; TEST-11 |


### 10.9 F9 — Permanent free-access invariant

**Tier:** P0  
**Requirement IDs:** R1, R10  
**User stories:** US-8  
**Primary module:** `:core:policy`


**Build spec:** The product permanently excludes every ad, cap, purchase, subscription, entitlement, paywall, trial, locked month, and cross-promotion. Implement `FreeAccessInvariantService` as a release-audit boundary, not as a runtime gate. It consumes the route graph, dependency graph, manifest/resources index, and generated bundle snapshot and produces a pass/fail audit. Every accessible cleanup scope is allowed by construction; Android permission and media availability are the only access constraints.


| Acceptance ID | Pass/fail criterion | Test type | Preserves |
| --- | --- | --- | --- |
| AC-F9.1 | Every accessible month and selected-photo group can be opened regardless of prior usage, item count, elapsed time, reclaimed bytes, connectivity, or completion history. | integration | DAC-1; AC-US8.1 |
| AC-F9.2 | No swipe-count, media-count, month-count, time, reclaimed-space, streak, remote-config, experiment, or analytics state changes feature access. | unit | AC-US8.2 |
| AC-F9.3 | The route graph contains SCR-01..SCR-22 and no commercial destination, deep link, CTA, row, or automatic redirect. | ui/release | DAC-26; AC-US8.3 |
| AC-F9.4 | The release dependency graph and merged manifest contain no billing, ad, mediation, purchase, entitlement, or commercial experimentation SDK/component. | release | DAC-27 |
| AC-F9.5 | Source, localized strings, resources, tests, generated bundle, and prototype contain no executable/user-facing commercial state or copy. | release | DAC-28 |
| AC-F9.6 | Core cleanup from access through reconciled completion passes in airplane mode with analytics declined. | device | DAC-29 |
| AC-F9.7 | A legacy repository containing commercial artifacts fails the adoption audit until those artifacts are removed from Keepr release variants. | release | Permanent product constraint |


### 10.10 F10 — Scoped local-library access and privacy explanation

**Tier:** P1  
**Requirement IDs:** R9  
**User stories:** US-9  
**Primary module:** `:feature:permissions`


**Build spec:** Keepr explains why photo access is needed, avoids cloud-account scope, and keeps the product centered on the local camera roll. Implement through `MediaAccessCoordinator` with main work ownership and main-flow UI delivery. The service consumes sdkInt, permissionResults, selectionChanged and produces accessState, permissionAction. All state mutations are durable and idempotent where applicable.


| Acceptance ID | Pass/fail criterion | Test type | Preserves |
| --- | --- | --- | --- |
| AC-F10.1 | Exact photo-access rationale appears before the version-appropriate system permission prompt. | ui | DAC-20; AC-US9.1 |
| AC-F10.2 | Android 14+ Full, Selected, Denied, and PermanentlyDenied states are distinguished; selected-photo mode is labeled and shows only granted items. | device | DAC-21; AC-US9.3 |
| AC-F10.3 | Permission denial and Settings recovery remain dismissible, preserve sessions, and never masquerade as an empty library. | ui | DAC-22 |
| AC-F10.4 | Core cleanup requires no Keepr account, makes no cloud-backup claim, and sends no photo content or identifiers off device. | release | DAC-25; DAC-43; AC-US9.2 |
| AC-F10.5 | Analytics remains disabled until affirmative consent; forbidden parameters are rejected and withdrawal disables collection and resets local analytics data. | release | DAC-30 |


## 11. Validation, Error Handling & Edge Cases

### 11.1 Global validation rules


| Rule | Invariant | Failure behavior |
| --- | --- | --- |
| Month key | ISO-like local `YYYY-MM`; derive query instants using explicit `ZoneId`; end is exclusive. | Reject/repair malformed persisted key; never query an unbounded library. |
| Session transition | Only selecting→active→review→committing→partial/complete, plus explicit abandoned/reset edges. | Fail closed to recovery; log transition code without media identifiers. |
| Decision uniqueness | Unique `(sessionId,volumeName,mediaStoreId)` and `(sessionId,displayOrder)`; one of undecided/keep/delete. | Transaction conflict returns current authoritative row; never duplicate. |
| Progress | `0 ≤ decidedCount ≤ totalSnapshotCount`; unavailable rows do not count complete. | Recalculate from indexed rows if counters disagree. |
| Deletion batch | `1..2000` requested URIs on target API 36+; unique `(sessionId,batchOrdinal)`. | Do not launch malformed/empty batch; return review or zero-delete completion. |
| Completion | Every actionable row decided and every batch terminal/acknowledged. | Remain in review/recovery; never mark complete optimistically. |
| Free access | Access depends only on Android permission scope, media availability, and recoverable session state. | A commercial state/route/dependency is a build failure; never hide or lock a month. |
| Commercial dependency | Release graph has no billing, ad, mediation, entitlement, purchase, paywall, or commercial experiment implementation. | Fail CI and block release until removed. |
| Analytics | Event and parameter allowlist; enum/bucket only; consent must be granted. | Drop event locally and assert in debug; never coerce values into free text. |
| Feedback payload | Field allowlist only; diagnostics preview equals shared payload. | Omit unsafe fields and block share until payload passes sanitizer. |

### 11.2 Error taxonomy and user handling


| Code | Trigger | Surface | Recovery |
| --- | --- | --- | --- |
| MEDIA_PERMISSION_DENIED | No usable read permission | SCR-19 denied/permanently_denied | Allow access, selected photos where supported, Not now, Privacy, Settings; preserve session. |
| MEDIA_PARTIAL_ACCESS | Selected photos only | SCR-04 partial_access / SCR-06 | Label selected scope; reselect/full access; never claim an unseen month is cleared. |
| MEDIA_MISSING | URI no longer resolves | SCR-08 or SCR-09 partial_result | Retry, skip, recover access; no keep/delete inference. |
| THUMBNAIL_TIMEOUT | Bounded thumbnail load exceeded | SCR-07 load_error / SCR-08 | Cancel request; Retry or Skip for now. |
| DB_READ_OR_MIGRATION | Local state cannot be read safely | SCR-01 load_error | Retry; explicit Reset Keepr only after explanation. |
| DELETE_CANCELED | System deletion canceled | SCR-11 partial_result | Review remaining, retry eligible, acknowledge; preserve decisions. |
| DELETE_RECONCILE_ERROR | Post-result URI query failed | SCR-11 load_error | Retry reconciliation; no repeat delete launch until state known. |
| COMMERCIAL_ARTIFACT_FOUND | CI finds a prohibited SDK, route, state, string, manifest entry, or generated-bundle artifact | Build/release report | Fail the release variant and identify exact file/dependency; do not add a runtime feature flag. |
| ANALYTICS_DISABLED | No consent or withdrawal | No user error surface required | Drop/no-op event; settings reflects disabled. |
| SHARE_TARGET_MISSING | No share handler | SCR-16 populated with error | Copy diagnostics or cancel; no backend fallback invented. |

### 11.3 Edge cases

- Month boundaries across DST, leap years, locale changes, device timezone changes, missing `DATE_TAKEN`, and multiple MediaStore volumes.
- Zero eligible images; zero delete-intent items; 1 item; 501+ items; 2,001+ delete intents; 2,101-item recovery fixture.
- Duplicate query rows, externally deleted rows, cloud-placeholder/unreadable rows, null MIME/size/timestamp, corrupt thumbnail, removable-volume loss.
- Permission granted then revoked while backgrounded; Android 14 selected set changed; Settings return with unchanged denial.
- Rapid double tap, simultaneous swipe/button input, gesture cancel, pointer interruption, animation scale zero, 200% font, Switch Access.
- Process death after decision commit, before navigation, after batch prepared, while system UI is open, after Activity result, and before reconciliation transaction.
- Airplane mode; analytics declined/withdrawn; analytics endpoint unavailable; share target absent; Play Store/review unavailable.
- Reset during no active system prompt only; reset is unavailable while Android deletion UI owns control.
- Unsupported device locale falls back to English without corrupting persisted preference.

## 12. Non-Functional Requirements

| Area | Budget/requirement | Reference surface | Enforcement |
| --- | --- | --- | --- |
| Cold start | TTID p95 ≤ 1200 ms | Pixel 6a, Android 16/API 36, release build with Baseline Profile, 500-image synthetic library | Macrobenchmark; SCR-01 must route or expose recovery. |
| Frame timing | Nominal 16.67 ms; percentile ≤ 22 ms | Pixel 6a API 36, 500-photo 60 Hz session | No decode/DB/analytics during pointer movement. |
| Database | ≤ 26214400 bytes (25 MiB) | Reference and stress libraries | Indexed ledgers, pruning after explicit reset/retention policy. |
| Media cache | Soft ceiling ≤ 67108864 bytes (64 MiB) | Current + next two thumbnails, stress navigation | LRU eviction; no originals. |
| Battery | ≤ 5% over 15 minutes | Pixel 6a, Android 16/API 36, 500-photo swipe session at 60 Hz, screen brightness 50%, analytics disabled | Battery Historian/benchmark fixture. |
| SDK | minSdk 29; targetSdk 36; compileSdk ≥36 | All release variants | Android 10 is the minimum because it provides scoped-storage MediaStore access, RecoverableSecurityException for user-mediated edits, and ContentResolver.loadThumbnail; excluding pre-29 devices avoids legacy WRITE_EXTERNAL_STORAGE and a separate unsafe deletion model. |
| Encryption | At rest required | Room/DataStore app-private | Room and DataStore live in credential-encrypted app-private storage; no original photo bytes, thumbnails, or media-derived analytics payloads are copied into backups. |
| Offline | F1–F10 fully usable | Permission and local media available | Optional consented analytics may queue/drop; no product state or feature depends on network. |
| Accessibility | TalkBack, Switch Access, D-pad/keyboard, 200% font, high contrast, reduced motion | Small/large/tablet API 34+ | All actions preserve semantic parity. |
| Privacy | Zero media-content or media-identifier egress | Signed release proxy capture | DAC-43 is release-blocking. |

Additional NFRs: bounded startup and loading timeouts; no ANR during 2,100-item fixtures; crash-safe database transactions; deterministic replay of pending deletion work; no silent state reset; stable screen-reader focus after card changes; ten fully reviewed locales; RTL-safe layout; and release-grade structured logging that excludes media-derived data.

## 13. Testing Strategy & Definition of Done

### 13.1 Required testing surface


| ID | Layer | Scope | Device/fixture |
| --- | --- | --- | --- |
| TEST-1 | unit | Calendar-month boundary calculation across timezone changes, leap years, DST transitions, and DATE_TAKEN fallback. | Deterministic clocks and zones including Asia/Karachi, America/Los_Angeles, and UTC. |
| TEST-2 | unit/release | Permanent-free invariant proves no commercial state, route, event, SDK, string, dependency, or gate exists and every accessible scope is usable. | Property-based access transitions plus source/resource/manifest/dependency/generated-bundle scans. |
| TEST-3 | unit | Swipe thresholds, velocity, cancel, right=keep, left=delete, reduced-motion settle, and one-decision-per-gesture invariants. | Compose gesture clock and synthetic pointer traces. |
| TEST-4 | integration | Room transactions, process-death restoration, migration, deletion-batch idempotency, and no silent ledger loss. | Room on-device database plus fault injection at every state transition. |
| TEST-5 | device | API 29 RecoverableSecurityException deletion, user denial, repeated consent, missing URI, and app recreation. | Android 10 physical or CTS-compatible device with 250 synthetic images. |
| TEST-6 | device | API 30–33 createDeleteRequest approval, cancellation, mixed missing items, and post-result reconciliation. | Android 11, 12L, and 13 physical devices from at least Google and Samsung. |
| TEST-7 | device | Android 14+ Full, Selected, Denied, PermanentlyDenied, reselected, and Settings-revoked media access; selected mode shows only granted items and remains fully usable. | Android 14, 15, and 16 devices with system permission UI. |
| TEST-8 | device | API 36 deletion of 2,001+ selected images is split into bounded prompts and resumes after process death between batches. | Android 16 device with 2,101 synthetic MediaStore images. |
| TEST-9 | device | Unavailable, corrupt, externally deleted, and provider-delayed thumbnails do not freeze swiping or misstate progress. | Synthetic provider fixtures and removable-volume interruption. |
| TEST-10 | release | Commercial-absence audit across Gradle/version catalogs, merged manifest, navigation, strings, resources, source, generated bundle, and transitive dependencies. | Signed release AAB plus CI scanners; seeded forbidden fixtures prove the audit fails closed. |
| TEST-11 | performance | Cold TTID, swipe frame timing, thumbnail prefetch latency, review of 2,000 items, and deletion reconciliation. | Release build on Pixel 6a API 36 with Macrobenchmark and synthetic 500-photo and 2,100-photo libraries. |
| TEST-12 | device | TalkBack labels, focus order, switch access, 200% font scale, high contrast, touch-target size, color-independent decisions, and reduced motion. | Small phone, large phone, and tablet on API 34+. |
| TEST-13 | release | Manifest permission audit, network traffic inspection, no media payload egress, privacy-policy parity, Data Safety parity, and broad-photo-permission declaration evidence. | Signed release AAB, Play pre-launch report, proxy capture, and automated manifest diff. |
| TEST-14 | release | Custom UI screenshot and semantics regression proves no Material 3 component/theme enters user-facing surfaces. | Golden images and dependency graph allowlist across compact, medium, and expanded widths. |

### 13.2 Design acceptance verification


| DAC ID | Scope | Scope ID | Pass/fail criterion | Required verification |
| --- | --- | --- | --- | --- |
| DAC-1 | global | permanent-free | Every accessible month and selected-photo group is usable with no ad, cap, paywall, trial, purchase, entitlement, or cross-promotion anywhere in the app. | Free-access invariant test plus end-to-end traces across first, second, and later 501+ item sessions. |
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
| DAC-21 | journey | J2 | Selected-photo mode is clearly labeled, shows only Android-granted items, offers reselection/full access, and never claims an unseen full month is cleared. | Android 14+ permission matrix test. |
| DAC-22 | screen | SCR-19 | Permission denial or Settings recovery preserves sessions, remains dismissible, and never masquerades as an empty library. | Navigation and process-restoration test. |
| DAC-23 | journey | J9 | Process death during cleanup, review, or between deletion batches restores a named actionable state without duplicate decisions or batch requests. | Fault-injection device test. |
| DAC-24 | screen | SCR-08 | A media-load failure offers Retry and Skip for now and records neither keep nor delete. | Synthetic provider test. |
| DAC-25 | global | account | Core cleanup requires no Keepr account, makes no cloud-backup claim, and sends no photo content or identifiers off-device. | Network inspection and content audit. |
| DAC-26 | global | route-graph | The route graph contains SCR-01..SCR-22 and no commercial destination, deep link, CTA, Settings row, or completion action. | Navigation graph and prototype-registry diff. |
| DAC-27 | global | dependency-graph | The release dependency graph and merged manifest contain no billing, ad, mediation, purchase, entitlement, or commercial experiment component. | Dependency/SBOM and merged-manifest audit. |
| DAC-28 | global | content-inventory | Source, localized strings, resources, tests, and generated bundle contain no executable or user-facing commercial state/copy; all 22 design routes have valid states and exits. | Static scan plus screen-matrix test. |
| DAC-29 | journey | J7 | Core cleanup from permission through reconciled completion remains fully usable in airplane mode for first and repeated sessions. | Offline end-to-end device test. |
| DAC-30 | screen | SCR-15 | Analytics starts disabled, requires affirmative consent, rejects forbidden parameters, and withdrawal disables collection and resets local analytics data. | Release configuration and event allowlist test. |
| DAC-31 | screen | SCR-18 | Reset Keepr clears local sessions, preferences, consent, and analytics identifier/queue but does not delete any MediaStore photo. | Integration test with local and media fixtures. |
| DAC-32 | global | visual-system | User-facing screens use the Keepr custom system and contain no stock Material 3 visual component/theme in the release dependency and screenshot allowlist. | Dependency audit plus golden screenshot review. |
| DAC-33 | global | motion | Reduced-motion mode preserves every action and state change while removing nonessential spring/travel effects. | Accessibility setting UI test. |
| DAC-34 | global | accessibility | Keep, delete, undo, review, confirm, recovery, privacy, analytics, feedback, rating, reset, and permission actions are operable by TalkBack and Switch Access with non-color semantics. | Accessibility device test. |
| DAC-35 | screen | SCR-01 | Splash routes to a valid destination within the bounded startup budget or exposes Retry/Reset; it never hangs. | Macrobenchmark and timeout test. |
| DAC-36 | screen | SCR-02 | The selected language persists, current choice is announced, and unsupported device locales fall back to English. | Locale recreation test. |
| DAC-37 | screen | SCR-03 | Onboarding states the month ritual, review-before-delete safety, on-device photo handling, and that every month is free with no ads, purchases, or swipe limits. | UI text and navigation test. |
| DAC-38 | screen | SCR-16 | Feedback export excludes content URIs, media IDs, filenames, dates, thumbnails, and analytics identifiers. | Payload unit test. |
| DAC-39 | screen | SCR-17 | Rate-Us is user-initiated, dismissible, non-rewarded, and never appears during active cleanup or recovery. | Navigation/frequency policy test. |
| DAC-40 | global | retention | No missed-day punishment, broken-streak copy, notification nagging, or themed promotional interruption exists in the active product flow. | Content inventory and notification manifest audit. |
| DAC-41 | global | performance | Release build meets TTID and frame budgets on the Step 7 reference device and fixtures. | Macrobenchmark. |
| DAC-42 | global | resources | Database, media-cache, and battery budgets stay within Step 7 ceilings during the 500-photo reference session. | Performance and storage instrumentation. |
| DAC-43 | global | privacy | Network inspection finds no photo bytes, thumbnails, filenames, dates, content URIs, MediaStore IDs, exact counts, or exact byte totals leaving the device. | Signed-release proxy inspection. |
| DAC-44 | screen | SCR-14 | Settings exposes language, access scope, dark theme, motion, haptics, privacy, analytics, feedback, rate, diagnostics, and Reset Keepr with no commercial row. | UI inventory test. |
| DAC-45 | global | navigation | Every screen and listed state has a reachable action, automatic exit, or back destination; no dead end or orphan control exists. | Automated navigation graph traversal plus manual review. |

### 13.3 Definition of Done

A release candidate is done only when all of the following are true:

- Every `AC-Fn.m`, `AC-USn.m`, and `DAC-1..DAC-45` passes in the stated layer.
- Every SCR-01..SCR-22 state row is directly render-tested and reachable through automated graph traversal or a named system-fixture path.
- Every J1..J9 success path and failure boundary passes on the required API/device matrix.
- Room schema, migrations, transaction idempotency, process-death restoration, and deletion batching pass fault injection.
- Killing the process on every core route restores a valid state without losing committed decisions or duplicating a system launch.
- Commercial-absence audit confirms no billing/ad/purchase/entitlement/paywall implementation exists across code, resources, navigation, manifest, generated bundle, or transitive dependencies.
- Release dependency audit confirms no commercial SDK and no stock Material 3 user-facing component/theme.
- Signed-release network inspection finds no forbidden media fields and no analytics traffic before consent.
- Manifest, privacy policy, Data Safety form, broad-photo-access declaration, and shipped SDK behavior agree.
- TTID, frame, database, cache, and battery budgets pass on the reference fixtures.
- TalkBack, Switch Access, keyboard/D-pad, 200% font, high contrast, dark/light themes, Arabic RTL, adaptive layouts, and reduced motion pass.
- Full, Selected, Denied, and PermanentlyDenied photo access pass on every supported Android permission model.
- The prototype route registry, design tokens/components, current Design PRD, this Code PRD, generated bundle, and manifest agree.
- There are no TODOs, placeholders, dead controls, orphan routes, infinite spinners, duplicate system launches, or silent destructive fallbacks.

## 14. Build Sequence / Milestones, Dependencies, Risks

**Sequencing orders the work; it never reduces scope.** Every milestone retains all upstream requirements; later work may not defer or remove an earlier feature.

### 14.1 Risk register

| Risk ID | Risk | Likelihood | Impact | Owner | Required mitigation |
| --- | --- | --- | --- | --- | --- |
| RISK-1 | Google Play rejects broad READ_MEDIA_IMAGES access or requires a minimum-scope picker. | medium | high | 9 | Prepare the permission declaration, core-functionality video, manifest audit, and selected-photos fallback; do not ship MANAGE_EXTERNAL_STORAGE, READ_MEDIA_VIDEO, or ACCESS_MEDIA_LOCATION. |
| RISK-2 | Android 14 selected access can make a calendar month appear complete while unseen items exist. | high | high | 8 | Create a distinct selected-photo mode, show only granted-item progress, offer reselection/full access, and never call an unseen calendar month cleared. |
| RISK-3 | Large deletion sets require multiple system prompts because targetSdk 36 caps each MediaStore request at 2,000 URIs. | low | medium | 8 | Explain batch count before commit, persist each batch, allow cancel between prompts, and withhold completion until all batches are reconciled. |
| RISK-4 | A user, cloud provider, or another app changes or removes media between session snapshot and deletion commit. | medium | high | 9 | Treat MediaStore as source of truth, re-query before review and after every system result, and surface missing/permission-lost items as partial outcomes rather than success. |
| RISK-5 | API 29 per-item RecoverableSecurityException produces repeated consent and weaker bulk UX. | medium | medium | 8 | Show an Android 10-specific commit explanation, process items serially with resumable progress, and allow the user to cancel without losing reviewed decisions. |
| RISK-6 | Custom card physics, hard shadows, oversized typography, and bitmap churn cause jank or accessibility regressions. | medium | high | 9 | Use draw-phase transforms, bounded thumbnail prefetch, no blur, release Macrobenchmarks, semantics tests, 200% font tests, and reduced-motion behavior. |
| RISK-7 | Permanent deletion causes accidental loss if Keepr's review or directional mapping is ambiguous. | low | high | 8 | Keep right=keep and left=delete consistent, provide pre-commit undo/revision, list exact pending deletions, state permanence, and require both Keepr confirmation and the system confirmation. |
| RISK-8 | Legacy starter code or a future dependency reintroduces commercial SDKs, routes, strings, or gating. | medium | high | 9 | Fail CI on commercial artifacts across source, dependencies, manifest, resources, navigation, and generated bundles; remove the artifact rather than hide it behind a flag. |
| RISK-9 | Optional analytics or an SDK update transmits undeclared identifiers or media-derived parameters. | low | high | 9 | Ship analytics disabled, enforce a compile-time event/parameter allowlist, disable Ad ID and personalization, run release traffic inspection, and block unknown SDK network destinations. |
| RISK-10 | MediaStore SIZE may be null or stale, making reclaimed-space numbers misleading. | medium | medium | 8 | Label the value “estimated space removed,” omit it when unknown, calculate only from confirmed deleted rows with known size, and never claim exact device free-space increase. |

### 14.2 Milestones


| Milestone | Deliverables | Dependencies | Exit criteria | Primary risks |
| --- | --- | --- | --- | --- |
| M0 | Repo Adoption Note; dependency graph; package/SDK/architecture decisions; Material3 and commercial-absence audit | Repository available | All §2 dimensions mapped; OD-E defaults confirmed or equivalently replaced. | Parallel stack, hidden migration obligations. |
| M1 | Core model, Room schema, DataStore, clocks, dispatchers, migrations, reset primitives | M0 | Entity/index/migration tests pass; no KAPT. | Ledger corruption, unsafe reset. |
| M2 | Media permission coordinator, MediaStore month query, thumbnail repository, synthetic fixtures | M1 | API 29/33/34+ access matrix and month-boundary tests pass. | Play broad-access risk; partial-access confusion. |
| M3 | Keepr design system, semantics, reduced motion, golden baselines | M0 | No Material 3 visual dependency; components pass accessibility and screenshot tests. | Jank and custom-control accessibility. |
| M4 | Month picker, cleanup swipe/button/undo, session resume | M1-M3 | F1/F2/F3/F6/F8/F9 free-access tests pass across first and repeated 501+ item sessions. | Duplicate decisions, inaccessible month, stale recovery. |
| M5 | Review, confirmation, API 29/30+/36 deletion batching and reconciliation | M4 | F4/F5, process-death, 2,101-item, cancel/partial/failure tests pass. | Accidental deletion, duplicate prompts, false success. |
| M6 | Completion, truthful metrics, settings/feedback/rate/reset | M5 | F7/F10 and SCR-12/14-18 tests pass; zero-delete and unknown-size cases pass. | Misleading reclaimed-space copy, unsafe diagnostics. |
| M7 | SCR-19 permission recovery, SCR-20 empty, SCR-21 resume, SCR-22 partial deletion, and commercial-absence enforcement | M1,M4-M6 | F6/F9 recovery and free-access matrices pass; prototype and route registry agree. | Dead ends, denial/empty confusion, legacy commercial artifact. |
| M8 | Consent-gated analytics and KPI instrumentation | M4-M7 | Compile-time event allowlist and no-pre-consent/no-media-egress traffic tests pass. | SDK egress or policy mismatch. |
| M9 | Full E2E, performance, accessibility, Play release evidence, signed AAB | M1-M8 | All AC/DAC/TEST items pass; release checklist and declarations approved. | OEM behavior, target/API policy, performance regression. |

## 15. Requirements & User-Story Coverage Map

### 15.1 Feature coverage


| Feature | Requirement IDs | User stories | Module | Screens | Journeys | Code AC IDs | Design AC IDs |
| --- | --- | --- | --- | --- | --- | --- | --- |
| F1 | R2, R7 | US-1 | :feature:months | SCR-05, SCR-07 | J1, J3 | AC-F1.1, AC-F1.2, AC-F1.3 | DAC-2, DAC-3, DAC-4 |
| F2 | R3, R8 | US-2 | :feature:cleanup | SCR-07 | J3 | AC-F2.1, AC-F2.2, AC-F2.3, AC-F2.4 | DAC-5, DAC-6, DAC-7 |
| F3 | R4 | US-3 | :feature:cleanup | SCR-07, SCR-09 | J3, J4 | AC-F3.1, AC-F3.2, AC-F3.3 | DAC-8, DAC-10 |
| F4 | R4, R5 | US-4 | :feature:review | SCR-09, SCR-10 | J4 | AC-F4.1, AC-F4.2, AC-F4.3, AC-F4.4 | DAC-9, DAC-10, DAC-11, DAC-12 |
| F5 | R5, R6 | US-5 | :feature:deletion | SCR-10, SCR-11 | J5 | AC-F5.1, AC-F5.2, AC-F5.3, AC-F5.4, AC-F5.5, AC-F5.6 | DAC-12, DAC-13, DAC-14, DAC-15, DAC-16 |
| F6 | R2, R6 | US-6 | :core:recovery | SCR-07, SCR-08, SCR-11, SCR-19, SCR-21, SCR-22 | J2, J3, J5, J9 | AC-F6.1, AC-F6.2, AC-F6.3, AC-F6.4 | DAC-4, DAC-21, DAC-23, DAC-24 |
| F7 | R7 | US-7 | :feature:completion | SCR-12 | J6 | AC-F7.1, AC-F7.2, AC-F7.3, AC-F7.4 | DAC-17, DAC-18, DAC-19 |
| F8 | R8 | US-2, US-10 | :core:designsystem | SCR-03, SCR-07 | J1, J3 | AC-F8.1, AC-F8.2, AC-F8.3, AC-F8.4, AC-F8.5 | DAC-32, DAC-33, DAC-34 |
| F9 | R1, R10 | US-8 | :core:policy | SCR-05, SCR-06, SCR-07, SCR-12, SCR-14 | J3, J6, J7 | AC-F9.1, AC-F9.2, AC-F9.3, AC-F9.4, AC-F9.5, AC-F9.6, AC-F9.7 | DAC-1, DAC-2, DAC-26, DAC-27, DAC-28, DAC-29 |
| F10 | R9 | US-9 | :feature:permissions | SCR-03, SCR-04, SCR-06, SCR-13, SCR-14, SCR-15, SCR-19, SCR-20 | J1, J2, J8 | AC-F10.1, AC-F10.2, AC-F10.3, AC-F10.4, AC-F10.5 | DAC-20, DAC-21, DAC-22, DAC-25, DAC-30, DAC-43 |

### 15.2 Requirement coverage


| Requirement | Label | Tier | Features | Journeys | Screens | Code AC IDs | Design AC IDs |
| --- | --- | --- | --- | --- | --- | --- | --- |
| R1 | Permanent free access to every cleanup scope | P0 | F9 | J3, J6, J7 | SCR-05, SCR-06, SCR-07, SCR-12 | AC-F9.1, AC-F9.2, AC-F9.3, AC-F9.4, AC-F9.5, AC-F9.6, AC-F9.7 | DAC-1, DAC-2, DAC-26 |
| R2 | Bounded month selection and resumable progress | P0 | F1, F6 | J3, J9 | SCR-05, SCR-07 | AC-F1.1, AC-F1.2, AC-F1.3, AC-F6.1, AC-F6.2, AC-F6.3, AC-F6.4 | DAC-3, DAC-4, DAC-23 |
| R3 | Fast, legible keep-or-delete decisions | P0 | F2 | J3 | SCR-07 | AC-F2.1, AC-F2.2, AC-F2.3, AC-F2.4 | DAC-5, DAC-6, DAC-7 |
| R4 | Reversible decisions before commit | P0 | F3, F4 | J3, J4 | SCR-07, SCR-09 | AC-F3.1, AC-F3.2, AC-F3.3, AC-F4.1, AC-F4.2, AC-F4.3, AC-F4.4 | DAC-8, DAC-10, DAC-11 |
| R5 | Trustworthy final review and deletion outcome | P0 | F4, F5 | J4, J5 | SCR-09, SCR-10, SCR-11 | AC-F4.1, AC-F4.2, AC-F4.3, AC-F4.4, AC-F5.1, AC-F5.2, AC-F5.3, AC-F5.4, AC-F5.5, AC-F5.6 | DAC-9, DAC-12, DAC-13, DAC-14, DAC-15 |
| R6 | Reliable media loading and session recovery | P0 | F5, F6 | J5, J9 | SCR-08, SCR-11 | AC-F5.1, AC-F5.2, AC-F5.3, AC-F5.4, AC-F5.5, AC-F5.6, AC-F6.1, AC-F6.2, AC-F6.3, AC-F6.4 | DAC-14, DAC-23, DAC-24 |
| R7 | Visible momentum and month closure | P0 | F1, F7 | J3, J6 | SCR-05, SCR-12 | AC-F1.1, AC-F1.2, AC-F1.3, AC-F7.1, AC-F7.2, AC-F7.3, AC-F7.4 | DAC-3, DAC-17, DAC-18, DAC-19 |
| R8 | Custom tactile visual and motion language | P0 | F2, F8 | J3 | SCR-03, SCR-07 | AC-F2.1, AC-F2.2, AC-F2.3, AC-F2.4, AC-F8.1, AC-F8.2, AC-F8.3, AC-F8.4, AC-F8.5 | DAC-32, DAC-33, DAC-34 |
| R9 | On-device, narrowly scoped photo handling | P1 | F10 | J1, J2, J8 | SCR-04, SCR-06, SCR-13, SCR-14, SCR-15, SCR-19, SCR-20 | AC-F10.1, AC-F10.2, AC-F10.3, AC-F10.4, AC-F10.5 | DAC-20, DAC-21, DAC-22, DAC-25, DAC-43 |
| R10 | Auditable permanent non-commercial boundary | P0 | F9 | J3, J6, J7 | SCR-05, SCR-12, SCR-14 | AC-F9.1, AC-F9.2, AC-F9.3, AC-F9.4, AC-F9.5, AC-F9.6, AC-F9.7 | DAC-1, DAC-26, DAC-27 |

### 15.3 User-story coverage and preserved acceptance


| Story | Persona | Features | Journeys | Screens | Upstream AC IDs | Code AC IDs | Design AC IDs |
| --- | --- | --- | --- | --- | --- | --- | --- |
| US-1 | A | F1 | J3 | SCR-05, SCR-07 | AC-US1.1, AC-US1.2, AC-US1.3 | AC-F1.1, AC-F1.2, AC-F1.3 | DAC-2, DAC-3, DAC-4 |
| US-2 | C | F2, F8 | J3 | SCR-07 | AC-US2.1, AC-US2.2, AC-US2.3, AC-US2.4 | AC-F2.1, AC-F2.2, AC-F2.3, AC-F2.4, AC-F8.1, AC-F8.2, AC-F8.3, AC-F8.4, AC-F8.5 | DAC-5, DAC-6, DAC-7, DAC-32, DAC-33 |
| US-3 | B | F3 | J3 | SCR-07 | AC-US3.1, AC-US3.2, AC-US3.3 | AC-F3.1, AC-F3.2, AC-F3.3 | DAC-8 |
| US-4 | B | F4 | J4 | SCR-09 | AC-US4.1, AC-US4.2, AC-US4.3 | AC-F4.1, AC-F4.2, AC-F4.3, AC-F4.4 | DAC-9, DAC-10, DAC-11 |
| US-5 | B | F5 | J5 | SCR-10, SCR-11 | AC-US5.1, AC-US5.2, AC-US5.3, AC-US5.4 | AC-F5.1, AC-F5.2, AC-F5.3, AC-F5.4, AC-F5.5, AC-F5.6 | DAC-12, DAC-13, DAC-14, DAC-15 |
| US-6 | B | F6 | J3, J9 | SCR-07, SCR-08, SCR-11 | AC-US6.1, AC-US6.2, AC-US6.3 | AC-F6.1, AC-F6.2, AC-F6.3, AC-F6.4 | DAC-4, DAC-23, DAC-24 |
| US-7 | C | F7 | J6 | SCR-12 | AC-US7.1, AC-US7.2, AC-US7.3 | AC-F7.1, AC-F7.2, AC-F7.3, AC-F7.4 | DAC-17, DAC-18, DAC-19, DAC-40 |
| US-8 | A | F9 | J3, J6, J7 | SCR-05, SCR-06, SCR-07, SCR-12, SCR-14 | AC-US8.1, AC-US8.2, AC-US8.3, AC-US8.4 | AC-F9.1, AC-F9.2, AC-F9.3, AC-F9.4, AC-F9.5, AC-F9.6, AC-F9.7 | DAC-1, DAC-2, DAC-26 |
| US-9 | B | F10 | J1, J2 | SCR-03, SCR-04, SCR-06 | AC-US9.1, AC-US9.2, AC-US9.3 | AC-F10.1, AC-F10.2, AC-F10.3, AC-F10.4, AC-F10.5 | DAC-20, DAC-21, DAC-22, DAC-25 |
| US-10 | C | F8 | J3 | SCR-07 | AC-US10.1, AC-US10.2, AC-US10.3 | AC-F8.1, AC-F8.2, AC-F8.3, AC-F8.4, AC-F8.5 | DAC-32, DAC-33, DAC-34 |

### 15.4 Upstream acceptance text


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

- `AC-US8.1` — Across onboarding, permission recovery, every cleanup session, review, deletion, completion, settings, and recovery, no ad, swipe cap, paywall, trial, purchase, entitlement, or cross-promotion is shown.

- `AC-US8.2` — Every accessible month or selected-photo group remains fully usable regardless of item count, prior sessions, elapsed time, connectivity, or reclaimed space.

- `AC-US8.3` — No commercial surface, route, dependency, state, string, or remote gate exists anywhere in the product or release graph.

- `AC-US8.4` — Product-facing copy states that every month is free with no ads, purchases, or swipe limits, with no hidden qualification.



**US-9 — As a cautious curator, I want to understand and control Keepr's photo access so that cleanup does not imply cloud backup or broad account access.**

- `AC-US9.1` — Before requesting photo access, the app explains that access is used to review and delete local camera-roll media.

- `AC-US9.2` — The core cleanup flow requires no Keepr account and presents no cloud-backup claim.

- `AC-US9.3` — When the platform supports limited photo access, the app explains the resulting scope and offers a path to adjust the selected media.



**US-10 — As a ritual seeker, I want the interface to feel uniquely Keepr without sacrificing clarity so that the chore feels playful but safe.**

- `AC-US10.1` — Primary tactile surfaces use the specified chunky borders, hard offset shadows, oversized rounded forms, and visible pressed-depth states rather than stock Material 3 component styling.

- `AC-US10.2` — Reduced-motion mode preserves every action and state change while replacing nonessential spring or travel effects with immediate transitions.

- `AC-US10.3` — Keep, delete, undo, review, confirm, and recovery actions remain distinguishable by text or iconography in addition to motion and color.



### 15.5 Stable-ID completeness

- Requirements preserved: `R1..R10`.
- Features preserved: `F1..F10`.
- User stories preserved: `US-1..US-10` and every `AC-USn.m`.
- Journeys preserved: `J1..J9`.
- Screens reconciled to the production prototype: `SCR-01..SCR-22` and every listed state.
- Design acceptance preserved: `DAC-1..DAC-45`.
- Platform constraints preserved: `PC-1..PC-8`.
- Technical tests preserved: `TEST-1..TEST-14`.
- Technical risks preserved and owned: `RISK-1..RISK-10`.
- Step 7b remains skipped because `spike_required` is empty.
