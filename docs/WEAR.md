# Pixel Watch companion app

Design sketch and status for 837 Dice on Wear OS (Pixel Watch and other
Wear OS 3+ watches).

**Status: Phase 1 and the whole of phase 2 (menu sync + connected rounds)
are implemented.** The `:wear` module is the standalone quick-roll app
described below, built on the shared `:core` module and attached to every
release as `837-dice-wear-vX.Y.apk`. On top of phase 1, the phone syncs its
(edited) menu to the watch (2a) and mirrors a live round to the watch as a
passive second display (2b). Phase 3 (complications/tiles) is still a concept.

> Part of the wider platform plan - see
> [MULTIPLATFORM.md](MULTIPLATFORM.md) for how this fits together with
> the planned iPhone and Apple Watch versions.

**What syncs (phase 2a):** whenever the menu changes on the phone, it is
serialized (`MenuSync` in `:core`) and written to the Data Layer at path
`/menu`; the watch listens and rolls on that card instead of its bundled
seed. The watch stays fully standalone - with no paired phone (or before
the first sync) it falls back to the seed, and the start screen shows
whether it is on the phone's card ("🔗 Karte vom Handy") or the default.
The phone stays the source of truth; sync is one-way and best-effort, so a
missing watch or Play Services never affects the phone app.

**Still not synced, by design:** the watch mirrors the phone's "Schnell
würfeln", which never counts towards history or statistics - so watch
rolls still do not join a phone round or reach history. That is phase 2b
below.

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

## Phase 1 - standalone quick roll (implemented)

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

## Phase 2a - menu sync (implemented)

The current menu (with edits) syncs to the watch via `DataClient`, so both
devices always roll on the same card:

- The phone writes the menu to the Data Layer at path `/menu` whenever it
  changes (`MenuSyncPublisher`, driven from `DiceApp`'s application scope).
  Identical menus produce an identical data item, which the Data Layer
  drops, so this does not spam updates.
- The watch listens (`WatchMenu`) and rebuilds its `GameFlow` on the synced
  card, but only while idle at the start screen so a phone edit never
  rewrites the card mid-round. No paired phone → bundled seed.
- The wire format lives in `:core` (`MenuSync`, kotlinx-serialization) with
  round-trip unit tests, ready to be reused by the future PWA.

## Phase 2b - connected rounds (implemented, watch is display-only)

The watch is a live second display for a round running on the phone - it
shows what's happening but never drives it, so passing the phone around and
jostling the watch can't trigger an accidental roll. All rolling stays in
the phone app.

- The phone starts the round as usual and stays the source of truth
  (database, players, order summary). While the round screen is open it
  mirrors the live state (one-way) to the watch at Data Layer path `/round`
  (`WatchRoundLink`, driven from `RoundViewModel`).
- The watch shows "Marcel ist dran", the tumble while the phone rolls, and
  the rolled drink and price on the wrist - plus a short buzz when a result
  lands. No taps, no shake: it is purely passive.
- The state type lives in `:core` (`RoundSync`, `WatchRoundState`) with unit
  tests. When the round screen closes the link publishes "inactive" and the
  watch falls back to standalone quick-roll.

Known limits (v1): needs the round screen open on the phone (the state is
published from it); if the phone process is killed mid-round a stale
"active" state is only cleared when a round screen is next opened.

## Phase 3 - nice-to-haves

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
