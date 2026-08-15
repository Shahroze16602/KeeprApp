---
name: keepr-design
description: Design and implement Keepr, a completely free local-first Android photo cleaner, using its production UI kit, tokens, components, UX contract, and Android handoff.
user-invocable: true
---

Read `readme.md`, `uploads/design-prd.md`, and `uploads/code-prd.md` before production work. Use `styles.css` and the components in `_ds_bundle.js` for web artifacts. Use the equivalent local tokens, vector icons, fonts, and Compose components described in the code PRD for Android.

Keepr has no ads, subscriptions, purchases, entitlements, locked months, or usage limits. Do not add commercial surfaces or access gating.

Core behavior:

- Swipe right to keep; swipe left to delete.
- Review all decisions before asking Android to delete anything.
- Reconcile actual MediaStore results before reporting completion.
- Persist sessions and recover from denial, process death, unavailable media, empty results, and partial deletion.
- Keep photo data on-device; analytics are anonymous, optional, and off by default.

Visual language:

- Custom, chunky, arcade-adjacent UI; not stock Material 3.
- Warm tangerine for Keep, cyan-blue for Delete, gold for rewards, green for success.
- Hard offset shadows, visible ink borders, large radii, 48 dp minimum targets.
- Archivo typography, Phosphor Fill-equivalent local vectors, authored Keepr wordmark/mark.
- Provide dark/light themes, visible focus, TalkBack semantics, reduced motion, and RTL.
