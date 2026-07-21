# Game rules, menu & architecture

Background: this app digitizes the schnapps dice game the "837 Gyrosbande"
plays at the San Remo restaurant during the Open Flair festival (Eschwege,
Germany). Two dice rolls decide which drink you have to order - roll
virtually or enter the result of real dice.

## Game rules

1. **Roll 1 - category:** one die (1-6) picks the drink category:

   | Pips | Category |
   |---|---|
   | 1 | Schnäpse & Brände |
   | 2 | Rum & Spezial |
   | 3 | Bitter |
   | 4 | Likör |
   | 5 | Whisky Longdrink |
   | 6 | Weinbrand & Cognac |

2. **Roll 2 - drink:** the concrete drink is rolled within the category.
   Drinks are numbered top to bottom in menu order.
   - Category with **up to 6 drinks**: one die.
   - Category with **more than 6 drinks** (e.g. Schnäpse & Brände with 7):
     **two dice, the sum counts** (2-12).
   - **Wrap rule ("off the bottom -> back to the top"):** if the rolled
     number is greater than the number of drinks, counting continues at the
     top of the list (effectively `((roll - 1) mod count) + 1`). Example:
     7 schnapps, sum 9 -> 9 - 7 = 2 -> drink no. 2 (Grappa).

3. The result (player + drink + price) is recorded in the current round. It
   can turn out to be a whole bottle of Prosecco or a Grog - tough luck, it
   gets ordered and drunk.

## San Remo drinks menu

Category and drink names stay German - they are what's printed on the real
menu. This data is the app's seed data (see [Architecture](#architecture))
and can change (prices/lineup) - the source photos live in `images/`.

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
(sparkling wine, Grog etc.) are folded into this category:
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

### Rest of the menu (not part of the dicing)

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
- And of course the food, first and foremost **menu 837**.

## Features

### Done

- Roll virtually or enter the result of real dice, with the automatic
  1-vs-2-dice decision and the wrap rule.
- Player management ("spielt mit" checkbox picks who joins the next round).
- Round flow: active players roll in turn; the round ends in a grouped
  order summary ("2× Ouzo, 1× Flasche Prosecco ...") with the total price,
  ready to read out to the waiter.
- Menu and results are persisted (Room); roll results are stored as
  snapshots (name/price at roll time) so later menu edits don't rewrite
  history.
- History: per-day round list with detail view and a festival-year filter.
  Quick rolls ("Schnell würfeln") deliberately don't count - only finished
  rounds enter history and statistics.
- Hall of fame fun facts: Prosecco king, top spender, the group's
  most-rolled drink, category magnet, doubles champion, wrap victim, plus
  the overall tally. "Als Bild teilen" renders the same standings into a
  black/gold PNG (logo, fun facts, tally) via plain Canvas/Paint and hands
  it to the share sheet, ready for the WhatsApp group. The wording lives in
  StatsPresentation (:core) so the on-screen cards and the image always
  read identically.
- Festival countdown: tap "Open Flair eintragen" on the home screen to pick
  the festival start date; the banner then counts down the days, flips to a
  gold "day X of Y" card while the festival runs, and steps aside once it is
  over. The date logic (FestivalCountdown) lives in :core for the future
  PWA; the date itself is stored in SettingsRepository.
- Export/import: share the history as a JSON file (e.g. into the WhatsApp
  group) or save it locally, and merge files from other phones. The merge
  is idempotent - rounds are deduplicated by uuid, players matched by
  name (case-insensitive), so everyone can import everyone's export in
  any order.
- Unavailable drinks: if San Remo is out of the rolled drink, either
  reroll the drink within the same category (the house rule) or pick a
  replacement from the menu by hand - the original rolls stay recorded
  and the result is marked as substituted. This also works late: while
  the order is read out, tap the *drink* in the summary. The app names
  everyone who rolled it (three people on one Bacardi is one tap, not
  three), then walks them through re-rolling one after another. The
  drink stays flagged for the round, so rolling it again is refused with
  "Das gibt's immer noch nicht" instead of being accepted. Corrected
  results are rewritten in place, keeping player and turn order.
- Player names are capitalized automatically and have to be unique - with
  two Marcels in the group, one becomes "Marcel S", so every name in the
  order summary points at exactly one person.
- Rounds can be deleted from the history (list and detail view, with a
  confirmation). Note: importing an older export file that still contains
  a deleted round brings it back - that's inherent to the merge design.

- Menu editing: categories can be renamed, their pip number reassigned
  (swaps with the holder), drinks can be added, edited, deleted and
  reordered (order = roll order!), and the whole menu can be reset to the
  original San Remo card.
- Extra order items: food and other drinks can be added to a round's
  summary by hand (label, price, quantity), counted into the total and
  the history, and shared via export/import.
- Shake-to-roll: shaking the phone rolls the virtual dice (like a dice
  cup), with a dice-rattle sound effect that can be muted.
- Wear OS companion app for the Pixel Watch: standalone quick-roll on the
  wrist, the phone's edited menu syncs to the watch, and a live phone round
  is mirrored to the watch as a passive second display (whose turn, the
  rolled drink) - see [WEAR.md](WEAR.md).

### Planned

- Watch complications and tiles (phase 3) - see [WEAR.md](WEAR.md).

## Architecture

- **Stack:** Kotlin + Jetpack Compose (Material 3), single activity,
  offline-first (no servers, no accounts, no internet permission).
- **Pattern:** MVVM - Compose UI -> ViewModel (Compose state / `StateFlow`)
  -> repository -> Room. No DI framework; `DiceApp` (the `Application`
  subclass) is a small manual container, wired to ViewModels via
  `AppViewModelProvider`.
- **Persistence:** Room (SQLite). `MenuRepository` seeds the database with
  the menu above on first access.
- **Package layout** (`app/src/main/java/de/gyrosbande/dice/`):
  - `domain/` - pure Kotlin, no Android dependencies, fully unit-tested:
    `DiceRules`, `GameFlow` (the roll state machine), `MenuSeed`,
    `RoundSession` (turn order + collected results), `OrderSummary`
    (grouping + totals), `Player`.
  - `data/` - Room entities/DAOs (`data/db/`) and repositories
    (`MenuRepository`, `PlayerRepository`, `RoundRepository`).
  - `ui/` - Compose screens and ViewModels, one subpackage per feature:
    `roll/` (the shared `RollPanel`/`RollController` used by both quick
    rolling and rounds), `players/`, `round/`, `theme/`.
- **Tests:** `app/src/test/java/de/gyrosbande/dice/domain/` covers the game
  rules and round/order logic - when rules change, adapt tests first.
