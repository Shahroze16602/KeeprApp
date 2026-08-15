# Build Keepr — Plan-Mode Brief for the Coding Agent

You are a senior software engineer building a **COMPLETE, production-ready** application (**Keepr**). You are running inside an existing repository (usually a starter project). You are in **PLAN MODE — do NOT write any code yet.** First read the repo and the three attached documents, then produce a plan and a list of questions.

## Inputs (attached, paths at the end)

1. **Code PRD (PRD 2)** — the implementation contract: WHAT to build and the exact behavior it must have (data model, API contracts, screen state matrix, flow implementation, permission handling, platform behavior, acceptance criteria, coverage map).
2. **Design PRD (PRD 1)** — the product/UX intent and scope (background/context).
3. **Exported design** — the actual produced design from the design tool (screens, components, states, interactions).

## Source-of-truth precedence

There are TWO design sources of truth, in this order:

1. **Exported design files (`./design/`)** — the FIRST source of truth for visuals and interaction (screens, layout, components, navigation, look-and-feel).
2. **Design PRD (PRD 1)** — the SECOND source of truth. Use it to fill anything the design files don't cover (missing screens, states, details) and to understand intent.

Rules for combining them:

* When the design files and the Design PRD **conflict**, the **design files win**.
* When something is **missing** from the design files, fall back to the **Design PRD**; if still unspecified, make a sensible, consistent decision (and note it).
* The design is **NOT frozen** — you MAY make small, deliberate UX refinements to the design where they clearly improve usability and stay consistent with the rest of the app. But a refinement is never an excuse to drop, skip, or hollow out a screen/feature that exists in the design or the Design PRD.
* The **Code PRD (PRD 2)** is authoritative for BEHAVIOR, data, contracts, and acceptance criteria. If a visual/UX choice would drop or contradict a behavior the Code PRD requires, reconcile toward satisfying the requirement and raise it as an Open Question if material.

## Repo-first (critical)

* Read the repository you are in and discover its existing architecture, language, framework, state management, navigation, persistence/data layer, build tooling, key libraries, folder structure, and lint/format/naming conventions.
* **ADOPT and EXTEND** those — do not impose, replace, or re-architect a different stack, and do not add a parallel stack. Build within the repo's patterns.
* If the repo is greenfield/empty, treat architecture & stack as Open Questions (with options + a recommended default) before building.

## Design coverage — build 100% of the design (you may refine UX, never skip it) (critical)

* The `./design/` folder is the primary visual/interaction source of truth. **Read EVERY file in it** (every screen, variant, state, component, asset, and any spec/notes) before and during the build — do not sample a few and infer the rest.
* Implement **every** screen, component, and state present in the design files (and any covered only by the Design PRD). COVERAGE must be complete — "followed ~half the design" is a FAILURE.
* Match the design closely by default: layout, hierarchy, spacing/sizing, components, typography roles, iconography, states, and interactions. You **MAY** make small, deliberate UX improvements where they clearly help and stay visually consistent — but never use a refinement as an excuse to drop, simplify away, or hollow out required content/screens/states.
* Maintain a written **design coverage checklist** (every design screen/component → implemented? all states done? consistent with the design?) and drive it to 100% before declaring done.
* If something is missing or ambiguous in the design files, fill it from the Design PRD; if still unclear, decide sensibly and note it. Raise material conflicts (or anything that would contradict the Code PRD) as an Open Question rather than silently dropping it.

## Scope is the UNION of the PRD and the design (critical)

* The design tool often ADDS scope the PRDs did not list — extra screens, flows, states, edge cases, and supporting features it judged necessary for a complete UX. This is expected and intended, not noise to ignore.
* Your build scope = EVERYTHING in the Code/Design PRD **PLUS** everything additional the design introduces. Example: if the PRD described 10 flows and the design has 13, build all 13 (the PRD's 10 + the design's 3 extra) — never cap scope at the PRD's list.
* Actively MINE the design files (`./design/`) for additions beyond the PRD: extra flows, screens, states, components, interactions, and edge cases. Treat each design-only addition as IN-SCOPE and implement it fully — with its own states, flows, navigation, and edge cases — not as optional.
* Do this in BOTH phases: in PLAN mode, diff the design against the PRD and enumerate the design-only additions; in EXECUTION, implement them alongside the PRD scope and track them on the coverage checklist.
* Exceeding/extending the PRD is fine and expected — only pause and raise an Open Question when a design addition would CONTRADICT a Code PRD requirement (not merely go beyond it).

## What "done" means — production-final, nothing broken

* This is the final production release. No "v1/MVP/phase 1", no TODO stubs, no placeholder or dead/decorative controls.
* Implement **EVERY screen** from the produced design, and **EVERY user-facing state** (loading/skeleton, empty, partial, populated, refreshing, paginating, validation errors, error/retry, timeout, offline, permission-required/denied, disabled, success, destructive-confirm).
* **Every interactive element actually works**: every button, link, tab, toggle, input, list row, gesture — each with its action, destination, and full set of states. If there are 100 controls, all 100 work. Cover each component's OWN intrinsic controls (e.g. a date picker's prev/next month, selectable vs disabled dates, range selection).
* **Every flow is scenario-complete**: entry, branches, loops/retries, cancel/back/early-exit, background→resume, process-death→restore, success, failure/error recovery, empty/offline — with state persisted/restored across transitions.
* **Navigation is perfect and never hangs**: every screen reachable, back/up/cancel correct, no dead ends. Transient screens (splash/loading/processing) auto-advance on completion or a max timeout with a fallback — a splash must NEVER be stuck forever. No infinite spinners.
* **All permissions handled end-to-end**: rationale → request → granted / denied / permanently-denied (with settings deep-link) and graceful degradation.
* **Platform behavior implemented** (back/up & back-stack, lifecycle/background/resume, process-death restoration, config changes, deep links, notifications, connectivity/offline, storage, accessibility).
* **Complete backend + frontend, wired together.** Every requirement, user story, and acceptance criterion in the Code PRD is satisfied.

## Questions policy

* Ask me about anything the repo and these inputs do NOT already settle (genuine product/scope decisions, account/auth model, data storage/sync strategy, third-party services, min-version targets — and architecture/stack only if the repo is greenfield).
* Do NOT ask about choices the repo already establishes — adopt them silently.
* For anything clearly determined, proceed without asking.

## Plan mode — work through these STEPS now (no code yet)

Do these as explicit, ordered steps and show the output of each:

1. **Repo discovery** — summarize the repo's architecture/stack/navigation/state/persistence/conventions you will build on (or note it's greenfield).

2. **Exhaustive design inventory** — open and list EVERY file in `./design/`. Produce a complete table of every screen, every flow, and every reusable component, with its states/variants, edge cases, and key interactions. Count them so coverage is measurable (e.g. "37 screens, 13 flows, 12 components"). Do not summarize away or skip any.

3. **Diff, union & mapping** — map every design screen/flow/component → the target route/module + the Code PRD requirements/user stories it satisfies. Then explicitly DIFF the design against the PRD and list:

    * **Design-only additions** — extra flows/screens/states/edge cases the design has beyond the PRD. These are IN SCOPE.
    * **PRD items missing from the design** — fill these from the PRD.

   State the final build scope as the UNION (PRD ∪ design) with totals.

4. **Phased execution plan** — break the build into ordered PHASES, listing exactly which screens/components/features land in each phase, with a per-phase exit check.

5. **Open Questions & Decisions Required** — numbered, each with 2–3 options and a recommended default.

Wait for my answers to the open questions before writing code.

## Execution in phases (after I approve the plan)

Build in phases over the UNION scope (PRD ∪ design) — do NOT attempt the whole app in one pass, and do NOT cap the build at the PRD's list.

Suggested phases (adapt to the app):

* **Phase 0 — Foundation:** repo wiring, navigation graph/routes for ALL screens AND all flows (PRD + design-only), shared theme/layout primitives, and the backend data model + API contracts.
* **Phase 1..N — Screens & flows in batches:** implement screen-by-screen / flow-by-flow in small batches — including the design-only additions. For EACH, build the full layout to match `./design/`, all its states and edge cases, and wire every control/interaction and its data.
* **Final phase — Wiring & polish:** end-to-end flows, permissions, platform behavior, edge cases, accessibility.

Rules for every phase:

* After each phase, verify the implemented screens/flows against the design files and update the coverage checklist.
* Report coverage against the UNION total.
* Do NOT move past a phase while any screen or flow in it is missing or only partially built.
* Before declaring the whole app done, do a final design-parity pass by re-opening every file in `./design/`.
* Coverage must reach **100%** of the UNION scope.
* Then verify the app builds/runs and meets every Code PRD acceptance criterion.

## Attachments (all in this folder)

* **Code prompt (this file):** `./agent-prompt.md`
* **Design PRD (PRD 1):** `./design-prd.md`
* **Code PRD (PRD 2):** `./code-prd.md`
* **Exported design (from design tool):** `./design/`
