# Pixel Watch companion app - concept

Feasibility check and design sketch for bringing 837 Dice to Wear OS
(Pixel Watch and other Wear OS 3+ watches). Not implemented yet - this is
the plan.

## Verdict: very doable

The project is unusually well prepared for a watch app: the entire game
logic (`DiceRules`, `GameFlow`, `MenuSeed`, `OrderSummary`, ...) is pure
Kotlin with zero Android dependencies and full unit-test coverage. It can
be shared with a watch module as-is. Wear OS apps are written with
Compose for Wear OS, so even the UI concepts (state, ViewModels) carry
over - only the widgets differ (`ScalingLazyColumn`, `Chip`, rotary input
instead of large buttons).

## Why a watch app at all?

At the festival the phone is in your pocket, hands are full, the table is
sticky. Raising your wrist, shaking it (the watch has an accelerometer -
the dice-cup gesture works even better on a wrist) and reading "Ouzo,
2,50 €" off the watch face is the perfect table experience.

## Phase 1 - standalone quick roll (MVP)

A self-contained watch app, no phone connection needed:

- **Screen 1:** big "Würfeln 🎲" button; alternatively shake the wrist.
- **Screen 2:** category result ("Wurf 1: Bitter"), auto-advances.
- **Screen 3:** drink result - name, price, category and rolls, in the
  same black/gold look. Tap to roll again.
- Menu data: bundled seed (`MenuSeed`). No history, no players - the
  watch mirrors the phone's "Schnell würfeln" (which by design doesn't
  count towards statistics), so nothing needs syncing.
- Haptics instead of sound (vibration pattern on landing), tile support
  so the app is one swipe away from the watch face.

Effort: small. New Gradle module `:wear` plus extracting the domain
package into a shared `:core` module that both `:app` and `:wear` depend
on (a mechanical refactor - the code already has no Android imports).

## Phase 2 - connected rounds (the fun one)

The watch becomes the dice cup for a real round running on the phone:

- Phone starts the round as usual and stays the source of truth
  (database, players, order summary).
- The watch shows "Marcel ist dran" and lets the current player roll on
  the wrist; the result travels to the phone, which records it exactly
  like a local roll and advances to the next player.
- Transport: Wearable Data Layer API (`MessageClient` for roll events,
  `DataClient` for the current round state). Both devices must be
  paired; the phone app keeps working standalone when no watch is
  around.
- The current menu (with edits) syncs to the watch via `DataClient`, so
  both devices always roll on the same card.

Effort: medium - the protocol is simple (two message types), but pairing
states, reconnects and "phone app not running" cases need care.

## Phase 3 - nice-to-haves

- Watch face complication showing the days until the next Open Flair.
- "Wer ist dran?"-Tile during a round.
- The hall-of-fame Prosecco king as a tiny trophy screen.

## Suggested module layout

```
837-dice/
├── app/    (existing phone app)
├── wear/   (new: Compose for Wear OS app, minSdk 30, Wear OS 3+)
└── core/   (new: the pure domain package, shared by both)
```

CI: the existing workflow builds `:app`; a `:wear` build job would attach
`837-dice-wear-vX.Y.apk` to the same release. Wear APKs install via
`adb` or the Play Store's internal track - for a private group, adb
sideloading onto the watch (developer mode) is the realistic path, which
is a bit more fiddly than the phone APK.

## Recommendation

Start with Phase 1: it delivers the core fun (wrist-shake dice at the
table) with low effort and no sync complexity, and it forces the healthy
`:core` module split that Phase 2 builds on.
