# 837 Dice - the Gyrosbande's drinking game app

## Overview

Private fun app for a festival friend group ("837 Gyrosbande").
The group travels to the **Open Flair festival** (Eschwege, Germany) every
year and eats almost daily at the Greek restaurant **San Remo** - usually
lunch menu no. 837, hence the name. A tradition there is **schnapps dicing**:
two dice rolls determine which drink from the menu you have to order.

The app digitizes this ritual: roll virtually **or** enter the results of
real dice, and in the end everyone knows what to order.

- **Audience:** only the friend group itself, no Play Store release
  (the APK is shared directly / via GitHub releases).
- **App language:** German UI (that's the audience); code, comments and
  docs are English.
- **Offline-first:** no servers, no accounts, everything stays on the device.
- **Logo:** `images/Gyrosbande_5000x5000 transparent.png` (black/white,
  "837" above a steaming gyros bowl labeled "GYROSBANDE"). Used as the app
  icon and on the home screen. App color world: black/white with a gold
  accent.

## Game rules (the heart of the app)

1. **Roll 1 - category:** one die (1-6) picks the drink category:

   | Pips | Category |
   |---|---|
   | 1 | Schnäpse & Brände |
   | 2 | Rum & Spezial |
   | 3 | Bitter |
   | 4 | Likör |
   | 5 | Whisky Longdrink |
   | 6 | Weinbrand & Cognac |

   The pips-to-category mapping must be **configurable** in the app
   (see open questions).

2. **Roll 2 - drink:** the concrete drink is rolled within the category.
   Drinks are numbered top to bottom in menu order.
   - Category with **up to 6 drinks**: one die.
   - Category with **more than 6 drinks** (e.g. Schnäpse & Brände with 7):
     **two dice, the sum counts** (2-12).
   - **Wrap rule ("off the bottom -> back to the top"):** if the rolled
     number is greater than the number of drinks, counting continues at the
     top of the list (effectively `((roll - 1) mod count) + 1`). Example:
     7 schnapps, sum 9 -> 9 - 7 = 2 -> drink no. 2 (Grappa).

3. The result (player + drink + price) is recorded in the current **round**.
   It can turn out to be a whole bottle of Prosecco or a Grog - tough luck,
   it gets ordered and drunk.

## San Remo drinks menu (from the 2026 photos in the project folder)

Source: `images/WhatsApp Image 2026-07-19 at 16.28.53.jpeg` (wine & spirits)
and `images/...16.28.59.jpeg` (drinks menu). This data is the app's **seed
data** and must be editable in the app (prices/lineup change). Category and
drink names stay German - they are what's printed on the real menu.

### Dice categories

**1 - Schnäpse & Brände** (2 cl, 2,50 € each) - 7 drinks -> two dice:
1. Ouzo 38 %
2. Grappa 40 %
3. Obstler 38 %
4. Linie Aquavit 41,5 %
5. Malteser 40 %
6. Fürst Bismark 38 %
7. Wodka 38 %

**2 - Rum & Spezial** - rum alone only has Bacardi, so the menu's specials
(sparkling wine, Grog etc.) are folded into this category (default
suggestion, adjustable in the app - see open questions):
1. Bacardi 37,5 % (2 cl) - 2,50 €
2. Flasche Prosecco (0,7 l) - 13,50 €
3. Glas Prosecco (0,1 l) - 2,90 €
4. Grog - 3,00 €
5. Glühwein - 3,00 €

**3 - Bitter** (2 cl, 2,50 € each):
1. Ramazotti 30 %
2. Averna 32 %
3. Jägermeister 35 %
4. Fernet Branca 42 %

**4 - Likör** (2 cl, 2,50 € each):
1. Sambuca 40 %
2. Amaretto 21,5 %
3. Marsala 15 %

**5 - Whisky Longdrink**:
1. Johnnie Walker 40 % - 3,50 €
2. Jim Beam 40 % - 3,50 €
3. Jack Daniels 43 % - 4,00 €
4. Chevas Regal 40 % - 4,00 €

**6 - Weinbrand & Cognac** (2 cl, 2,50 € each):
1. Mariacron 36 %
2. Asbach Uralt 38 %
3. Veccia Romagna 38 %
4. Metaxa 5 Sterne 38 %

### Rest of the menu (for the extended order list, not part of the dicing)

- **Aperitif:** Campari Orange 4,50 € · Campari Soda 4,50 € · Aperol Spritz 6,00 €
- **Wein** (0,25 l / 0,5 l): red - Chianti (dry) 5,50/11,00 ·
  Valpolichella (semi-dry) 5,90/11,80 · Lambrusco (sweet) 5,50/11,00 ·
  Rose (semi-dry) 5,50/11,00; white - Frascati (dry), Soave (semi-dry),
  Frizzantino (sweet) 5,50/11,00 each
- **Apfelwein** (0,25 l / 0,5 l): Pur, Süß gespritzt, Sauer gespritzt 3,00/4,50 each
- **Fassbier** (0,3/0,4/0,5 l): Eschweger Pils 3,00/3,70/4,50 · Radler/Diesel 3,00/3,70/4,50
- **Flaschenbier:** Erdinger Hefeweizen/Dunkel 4,50 (0,5 l) · Erdinger
  alkoholfrei 4,50 · non-alcoholic beer 3,00 (0,33 l) · Malzbier 3,00 ·
  Jacobinus Schwarzbier 3,00
- **Non-alcoholic** (0,3/0,5 l): Cola, Cola Zero, Sprite, Fanta, Spezi,
  Apfelsaftschorle, juices (apple, orange, grape, banana, cherry, KiBa)
  3,00/4,50 each · Bitter Lemon, Ginger Ale, Tonic 3,00/4,50 ·
  Lassi/Mango-Lassi 3,90/4,90 · Germeta water 3,00/4,50/6,00 (0,7 l)
- **Hot drinks:** coffee 2,50 · pot of coffee 3,00 · espresso 2,50 · double
  espresso 3,00 · cappuccino 3,00 · milk coffee 3,00 · latte macchiato 3,00 ·
  hot chocolate with cream 3,00 · tea 2,50 · chai tea 3,00
- And of course the food, first and foremost **menu 837** (price editable
  in the app).

## Features

### MVP (version 1)

1. **Rolling - two modes per roll:**
   - *Virtual:* animated dice roll in the app (tap **or shake the phone**,
     accelerometer), plus a sound effect (can be muted).
   - *Manual:* real dice at the table, results entered via number buttons.
   - The app guides the flow: roll 1 -> show category -> roll 2
     (automatically one or two dice depending on category size) -> show the
     drink result big and celebratory (with price).
2. **Players & rounds:**
   - Create players once (name, optional emoji/color), they stay saved.
   - A "round" = one pass in which every player rolls. The app shows whose
     turn it is and collects all results.
   - **Order summary** at the end of a round: grouped list ("2× Ouzo,
     1× Flasche Prosecco ...") with item and total prices - to show the
     waiter when ordering.
   - Optionally extendable with manually added items (food, beer, cola ...)
     from the rest of the menu so the table's total is right.
3. **Editable menu:** categories, drinks, prices and the pips mapping can be
   edited in the app (CRUD). Seed data as above.

### Version 2 (after the MVP)

4. **History & statistics:** all rounds are stored (date, players, results).
   Insights: who had to drink the Prosecco bottle most often? Who spent the
   most? Per-festival-year leaderboard, "hall of fame".
5. Nice-to-have ideas (only if in the mood): toasts/special rules on
   doubles, a "festival mode" with countdown to the next Open Flair,
   exporting statistics as an image to share in the WhatsApp group.

## Tech

- **Stack:** Kotlin + **Jetpack Compose** (Material 3), single activity.
- **Min SDK:** 26 (Android 8.0) · target SDK: current (36).
- **Persistence:** **Room** (SQLite) for players, menu, rounds, history.
  Seed data injected from code on first start.
- **Architecture:** MVVM - Compose UI -> ViewModel (`StateFlow`) ->
  repository -> Room. Navigation via Navigation-Compose. No DI framework
  (manual injection, or Hilt only if it clearly pays off - keep it small).
- **Sensors:** `SensorManager`/accelerometer for shake-to-roll.
- **No internet permission.** No analytics, no ads.
- **Distribution:** signed release APK via GitHub releases.

### Data model (draft)

- `Category(id, name, diceNumber 1-6, sortOrder)`
- `Drink(id, categoryId, name, priceCents, sizeLabel?, abv?, sortOrder)`
- `Player(id, name, emoji?, isActive)`
- `Round(id, startedAt, finishedAt?)`
- `RollResult(id, roundId, playerId, drinkId, categoryRoll, drinkRoll1,
  drinkRoll2?, wasVirtual, timestamp)`
- `ExtraOrderItem(id, roundId, label, priceCents, quantity)` - for food and
  other drinks on the order list

### Screens (draft)

1. **Home:** logo, buttons "Neue Runde", "Schnell würfeln" (no
   players/round), "Historie", "Karte", "Spieler".
2. **Dice screen:** the centerpiece. Shows current player, roll phase
   (category/drink), dice animation or 1-6 input buttons, result.
3. **Round/order summary:** results per player, grouped order, total price,
   "Runde abschließen" button.
4. **Menu management:** edit categories + drinks.
5. **Player management.**
6. **History/statistics** (V2).

## Build & development

- Local project path: `C:\Repo\837-dice` (renamed from the original
  `C:\Repo\837 Würfel App` once the app itself was renamed to "837 Dice";
  the new path is plain ASCII, no spaces).
- Build without Android Studio (PowerShell):
  `$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'`
  then `.\gradlew.bat test assembleDebug`
- Debug APK lands in `app\build\outputs\apk\debug\app-debug.apk`.
- Unit tests for the game rules: `app\src\test\java\de\gyrosbande\dice\domain\`
  (`DiceRulesTest`, `GameFlowTest`) - when rules change, adapt tests first.
- Core logic is Android-free in `domain\` (`DiceRules`, `GameFlow`,
  `MenuSeed`); UI in `ui\` (Compose, `RollViewModel`).
- Emulator: AVD "Wuerfel837" (Pixel 7, Android 37.1, display name
  "837 Wuerfel Testgeraet") was created manually under
  `%USERPROFILE%\.android\avd\` and shows up in Android Studio's device
  manager.

### CI/CD (GitHub Actions)

- Workflow `.github/workflows/android.yml`: every push to `main` runs the
  tests and uploads the release APK as an artifact (90 days); tags `v*`
  additionally create a GitHub release with the APK attached (publicly
  downloadable).
- Signing: secrets `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`,
  `KEY_PASSWORD` -> release keystore; without secrets the build falls back
  to the debug signature (logic in `app/build.gradle.kts`).
- Keystore + password + Base64 live locally in `C:\Repo\837-wuerfel-signing\`
  (deliberately outside the repo, `*.jks` is gitignored).
- New version: just `git tag vX.Y && git push origin main vX.Y` - no file
  edit, no version-bump commit. The version is derived from the tag at CI
  build time: the "Build APK" step passes `RELEASE_TAG: ${{ github.ref_name }}`
  to Gradle, and `app/build.gradle.kts` parses it into `versionCode`
  (`major*10_000 + minor*100 + patch`, always increasing) and `versionName`.
  Non-tag builds (main, PRs, local `gradlew assembleRelease`) fall back to
  `versionName = "0.0-dev"`, `versionCode = 1`; set the `RELEASE_TAG` env
  var locally to override. CI generates the release notes AND pushes a
  regenerated `CHANGELOG.md` to main as a `chore(release)` bot commit
  (skipped by cliff.toml), so pull main after releasing.
- Release notes are generated by git-cliff from Conventional Commits in CI
  (orhun/git-cliff-action, config `cliff.toml`) - another reason the commit
  convention is mandatory. Checkout uses fetch-depth 0 for that.

### Status

- ✅ Done: project scaffold, dice logic + tests, app icon, roll UI with
  virtual rolling and manual entry, CI/CD pipeline (v1.0 released), Room
  persistence with seeded menu, player management, round flow with the
  grouped order summary (v1.1), history with per-day round list, detail
  view and year filter, hall-of-fame fun facts (StatsCalculator), JSON
  export/import with idempotent cross-device merge by round uuid (v1.2),
  unavailable-drink handling (reroll within the same category or manual
  substitute, marked in history; schema v3) and git-cliff release notes
  (v1.3), round deletion + auto-changelog (v1.4), menu editing (rename,
  pip swap, drink CRUD + reorder, reset to seed), extra order items on
  the summary (schema v4, in export format), shake-to-roll with a
  synthesized dice sound (mutable via 🔊 toggle) (v1.5).
- ✅ Wear OS phase 1 (v2.1): `:core` module split (the pure domain package
  is now shared), `:wear` = standalone Compose-for-Wear quick-roll app
  (tap or wrist-shake, haptics instead of sound, bundled menu seed, same
  applicationId as the phone app, attached to releases as
  `837-dice-wear-vX.Y.apk`). Deliberately unsynced - mirrors the phone's
  quick roll, which never counts towards history.
- ✅ Also done: unavailable drinks are reported per drink in the summary
  (everyone who rolled it re-rolls in turn; the drink stays flagged for
  the round and rolling it again is refused), player names are
  capitalized and must be unique (`PlayerName` in :core), and the roll
  sound is the group's own recording (`res/raw/dice_roll.mp3`, ~2 s with
  a build-up) with the roll animation synced to its length via
  `RollController.ROLL_ANIMATION_MS`.
- ⏳ Open: Wear OS phase 2 - Data Layer sync (menu + watch rolls joining
  phone rounds), see docs/WEAR.md.
- ⏳ Planned iOS: **decided on a PWA served from GitHub Pages**, not a
  native port (avoids Mac/€99/TestFlight); roadmap, the iOS-storage
  caveat and the deploy-pages CI job are in docs/MULTIPLATFORM.md. Still
  keep `:core` free of JVM-only APIs (`String.format`, `java.util.*`) so
  sharing the rules to Kotlin/JS stays cheap; the two remaining offenders
  are listed there.
- Decisions: quick rolls ("Schnell würfeln") deliberately do NOT count
  towards history/statistics - only finished rounds do. Players are
  matched across devices by trimmed, case-insensitive name. The db is at
  schema v2 (rounds.uuid unique + roll_results.categorySize); migrations
  live in AppDatabase and were verified against real v1.1 data.

## Open questions / to clarify with the group

- **Composition of "Rum & Spezial":** Marcel believes the specials
  (Prosecco, Grog etc.) join the rum category ("because it's thin
  otherwise") - not 100% certain. Clarify with the group before the
  festival which specials exactly belong there (glass vs. only bottle of
  Prosecco? aperitifs included?). Thanks to the menu editing UI it stays
  adjustable in the app anyway.
- Are there special rules (doubles, passing drinks on, re-rolls) the app
  should know about?
- Price/content of menu 837 for the extended order list.

## Notes for Claude

- App tone: humorous, the group's insider language is welcome
  ("Gyrosbande", "837"), but the UI must stay clear and one-hand-operable
  at the table (big buttons - users may already have had a few 🍻).
- UI copy is German (target audience), code/comments/docs are English.
- Responsible framing: private fun app for adults; don't add lectures, but
  also don't invent drinking-pressure mechanics beyond the group's existing
  game.
- The rules under "Game rules" are binding; when in doubt, ask instead of
  guessing.
- Always read drink data from the database, never hard-code it in the UI.
- **All commit messages must follow [Conventional Commits](https://www.conventionalcommits.org/)**
  (`type: description`, e.g. `feat:`, `fix:`, `docs:`, `refactor:`, `chore:`,
  `test:`, `ci:`) and be written in English, regardless of the German UI
  copy. This applies to every commit on this project, not just past ones.
