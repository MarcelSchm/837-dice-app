# 837 Würfel – Trinkspiel-App der Gyrosbande

## Überblick

Private Android-Spaß-App für eine Festival-Freundesgruppe („837 Gyrosbande").
Die Gruppe fährt jedes Jahr zum **Open Flair Festival** (Eschwege) und isst dort
fast täglich beim Griechen **San Remo** – meist das Mittagsmenü Nr. 837, daher
der Name. Dort wird traditionell **Schnapsgewürfelt**: Zwei Würfelwürfe
bestimmen, welchen Drink von der Karte man bestellen muss.

Die App digitalisiert dieses Ritual: virtuell würfeln **oder** echte
Würfelergebnisse eintragen, und am Ende wissen alle, was sie bestellen müssen.

- **Zielgruppe:** Nur die eigene Freundesgruppe, kein Play-Store-Release nötig
  (APK wird direkt geteilt).
- **Sprache der App:** Deutsch.
- **Offline-first:** Keine Server, keine Accounts, alles lokal auf dem Gerät.
- **Logo:** `Gyrosbande_5000x5000 transparent.png` (schwarz/weiß, „837" über
  einer dampfenden Gyros-Schale mit Schriftzug „GYROSBANDE"). Dient als
  App-Icon und Splash/Home-Branding. Farbwelt der App: Schwarz/Weiß mit
  Akzentfarbe (Vorschlag: Rot oder Gold – am Logo orientieren).

## Spielregeln (Kern der App)

1. **Wurf 1 – Kategorie:** Ein Würfel (1–6) bestimmt die Getränkekategorie:

   | Augenzahl | Kategorie |
   |---|---|
   | 1 | Schnäpse & Brände |
   | 2 | Rum & Spezial |
   | 3 | Bitter |
   | 4 | Likör |
   | 5 | Whisky Longdrink |
   | 6 | Weinbrand & Cognac |

   Die Zuordnung Augenzahl→Kategorie muss in der App **konfigurierbar** sein
   (siehe offene Fragen).

2. **Wurf 2 – Drink:** Innerhalb der Kategorie wird der konkrete Drink
   erwürfelt. Die Drinks sind in Kartenreihenfolge von oben nach unten
   nummeriert.
   - Hat die Kategorie **bis zu 6 Drinks**: ein Würfel.
   - Hat sie **mehr als 6 Drinks** (z. B. Schnäpse & Brände mit 7): **zwei
     Würfel, die Summe zählt** (2–12).
   - **Wrap-Regel („unten durch → oben weiter"):** Ist die gewürfelte Zahl
     größer als die Anzahl der Drinks, wird oben in der Liste weitergezählt
     (effektiv: `((wurf - 1) mod anzahl) + 1`). Beispiel: 7 Schnäpse, Summe 9
     → 9 − 7 = 2 → Drink Nr. 2 (Grappa).

3. Das Ergebnis (Spieler + Drink + Preis) wird in die aktuelle **Runde**
   eingetragen. Es kann auch eine ganze Flasche Prosecco oder ein Grog dabei
   herauskommen – Pech gehabt, wird bestellt und getrunken.

## Getränkekarte San Remo (Stand: Fotos von 2026, im Projektordner)

Quelle: `WhatsApp Image 2026-07-19 at 16.28.53.jpeg` (Wein & Spirituosen) und
`...16.28.59.jpeg` (Getränkekarte). Diese Daten sind die **Seed-Daten** der
App und müssen in der App editierbar sein (Preise/Sortiment ändern sich).

### Würfel-Kategorien

**1 – Schnäpse & Brände** (2 cl, je 2,50 €) – 7 Drinks → zwei Würfel:
1. Ouzo 38 %
2. Grappa 40 %
3. Obstler 38 %
4. Linie Aquavit 41,5 %
5. Malteser 40 %
6. Fürst Bismark 38 %
7. Wodka 38 %

**2 – Rum & Spezial** – Rum allein hat nur Bacardi, deshalb werden die
Spezialitäten der Karte (Sekt, Grog & Co.) dieser Kategorie zugeschlagen
(Standard-Vorschlag, in der App anpassbar – siehe offene Fragen):
1. Bacardi 37,5 % (2 cl) – 2,50 €
2. Flasche Prosecco (0,7 l) – 13,50 €
3. Glas Prosecco (0,1 l) – 2,90 €
4. Grog – 3,00 €
5. Glühwein – 3,00 €

**3 – Bitter** (2 cl, je 2,50 €):
1. Ramazotti 30 %
2. Averna 32 %
3. Jägermeister 35 %
4. Fernet Branca 42 %

**4 – Likör** (2 cl, je 2,50 €):
1. Sambuca 40 %
2. Amaretto 21,5 %
3. Marsala 15 %

**5 – Whisky Longdrink**:
1. Johnnie Walker 40 % – 3,50 €
2. Jim Beam 40 % – 3,50 €
3. Jack Daniels 43 % – 4,00 €
4. Chevas Regal 40 % – 4,00 €

**6 – Weinbrand & Cognac** (2 cl, je 2,50 €):
1. Mariacron 36 %
2. Asbach Uralt 38 %
3. Veccia Romagna 38 %
4. Metaxa 5 Sterne 38 %

### Restliche Karte (für die erweiterte Bestellliste, nicht Teil des Würfelns)

- **Aperitif:** Campari Orange 4,50 € · Campari Soda 4,50 € · Aperol Spritz 6,00 €
- **Wein** (0,25 l / 0,5 l): Rotwein – Chianti (trocken) 5,50/11,00 ·
  Valpolichella (halbtrocken) 5,90/11,80 · Lambrusco (lieblich) 5,50/11,00 ·
  Rose (halbtrocken) 5,50/11,00; Weißwein – Frascati (trocken), Soave
  (halbtrocken), Frizzantino (lieblich) je 5,50/11,00
- **Apfelwein** (0,25 l / 0,5 l): Pur, Süß gespritzt, Sauer gespritzt je 3,00/4,50
- **Fassbier** (0,3/0,4/0,5 l): Eschweger Pils 3,00/3,70/4,50 · Radler/Diesel 3,00/3,70/4,50
- **Flaschenbier:** Erdinger Hefeweizen/Dunkel 4,50 (0,5 l) · Erdinger
  alkoholfrei 4,50 · Alkoholfreies Bier 3,00 (0,33 l) · Malzbier 3,00 ·
  Jacobinus Schwarzbier 3,00
- **Alkoholfrei** (0,3/0,5 l): Cola, Cola Zero, Sprite, Fanta, Spezi,
  Apfelsaftschorle, Säfte (Apfel, Orange, Traube, Banane, Kirsche, KiBa) je
  3,00/4,50 · Bitter Lemon, Ginger Ale, Tonic 3,00/4,50 · Lassi/Mango-Lassi
  3,90/4,90 · Germeta Wasser 3,00/4,50/6,00 (0,7 l)
- **Heiße Getränke:** Kaffee 2,50 · Pott Kaffee 3,00 · Espresso 2,50 ·
  Doppelter Espresso 3,00 · Cappuccino 3,00 · Milchkaffee 3,00 · Latte
  Macchiato 3,00 · Heiße Schokolade mit Sahne 3,00 · Tee 2,50 · Chai-Tea 3,00
- Und natürlich das Essen, allen voran **Menü 837** (Preis in der App pflegbar).

## Features

### MVP (Version 1)

1. **Würfeln – zwei Modi pro Wurf:**
   - *Virtuell:* Animierter Würfelwurf in der App (Tippen **oder Handy
     schütteln**, Sensor: Accelerometer), inkl. Sound-Effekt (abschaltbar).
   - *Manuell:* Echte Würfel am Tisch, Ergebnis wird per Zahlen-Buttons
     eingetragen.
   - Die App führt durch den Ablauf: Wurf 1 → Kategorie anzeigen → Wurf 2
     (automatisch ein oder zwei Würfel je nach Kategoriegröße) → Drink-Ergebnis
     groß und feierlich anzeigen (mit Preis).
2. **Spieler & Runden:**
   - Spieler einmalig anlegen (Name, optional Emoji/Farbe), bleiben gespeichert.
   - Eine „Runde" = ein Durchgang, in dem jeder Spieler würfelt. Die App zeigt,
     wer dran ist, und sammelt alle Ergebnisse.
   - **Bestellübersicht** am Rundenende: gruppierte Liste („2× Ouzo, 1× Flasche
     Prosecco …") mit Einzel- und Gesamtpreis – zum Vorzeigen beim Bestellen.
   - Optional erweiterbar um manuell hinzugefügte Positionen (Essen, Bier,
     Cola …) aus der restlichen Karte, damit der Gesamtpreis des Tisches stimmt.
3. **Karte pflegbar:** Kategorien, Drinks, Preise und die
   Augenzahl-Zuordnung in der App bearbeiten (CRUD). Seed-Daten wie oben.

### Version 2 (nach dem MVP)

4. **Historie & Statistik:** Alle Runden werden gespeichert (Datum, Spieler,
   Ergebnisse). Auswertungen: Wer musste am häufigsten die Prosecco-Flasche
   trinken? Wer hat am meisten ausgegeben? Rangliste pro Festival-Jahr,
   „Hall of Fame".
5. Nice-to-have-Ideen (nur wenn Lust): Trinksprüche/Sonderregeln bei
   Pasch-Würfen, „Festival-Modus" mit Countdown zum nächsten Open Flair,
   Export der Statistik als Bild zum Teilen in die WhatsApp-Gruppe.

## Technik

- **Stack:** Kotlin + **Jetpack Compose** (Material 3), Single-Activity.
- **Min SDK:** 26 (Android 8.0) · Target SDK: aktuell (35/36).
- **Persistenz:** **Room** (SQLite) für Spieler, Karte, Runden, Historie.
  Seed-Daten beim ersten Start aus Code/JSON einspielen.
- **Architektur:** MVVM – Compose-UI → ViewModel (`StateFlow`) → Repository →
  Room. Navigation über Navigation-Compose. Kein DI-Framework nötig
  (manuelle Injection oder Hilt, wenn es sich anbietet – klein halten).
- **Sensorik:** `SensorManager`/Accelerometer für Shake-to-roll.
- **Kein Internet-Permission nötig.** Keine Analytics, keine Werbung.
- **Verteilung:** Signierte Release-APK, Weitergabe direkt an die Gruppe.
- Projekt wird in diesem Ordner (`C:\Repo\837 Würfel App`) als
  Android-Studio-Projekt angelegt (Gradle, Kotlin DSL).

### Datenmodell (Entwurf)

- `Category(id, name, diceNumber 1–6, sortOrder)`
- `Drink(id, categoryId, name, priceCents, sizeLabel?, abv?, sortOrder)`
- `Player(id, name, emoji?, isActive)`
- `Round(id, startedAt, finishedAt?)`
- `RollResult(id, roundId, playerId, drinkId, categoryRoll, drinkRoll1,
  drinkRoll2?, wasVirtual, timestamp)`
- `ExtraOrderItem(id, roundId, label, priceCents, quantity)` – für Essen &
  sonstige Getränke in der Bestellliste

### Screens (Entwurf)

1. **Home:** Logo, Buttons „Neue Runde", „Schnell würfeln" (ohne
   Spieler/Runde), „Historie", „Karte", „Spieler".
2. **Würfel-Screen:** Herzstück. Zeigt aktuellen Spieler, Wurfphase
   (Kategorie/Drink), Würfel-Animation bzw. Eingabe-Buttons 1–6, Ergebnis.
3. **Runden-/Bestellübersicht:** Ergebnisliste pro Spieler, gruppierte
   Bestellung, Gesamtpreis, Button „Runde abschließen".
4. **Karte verwalten:** Kategorien + Drinks bearbeiten.
5. **Spieler verwalten.**
6. **Historie/Statistik** (V2).

## Meilensteine

1. Android-Studio-Projekt aufsetzen, Logo als App-Icon, Theme, Navigation.
2. Datenmodell + Room + Seed der Getränkekarte.
3. Würfel-Logik (Kategorie-Wurf, 1-vs-2-Würfel-Entscheidung, Wrap-Regel) als
   reine Kotlin-Klasse **mit Unit-Tests** – die Regeln sind das Herz der App.
4. Würfel-Screen mit manueller Eingabe (damit ist die App am Tisch schon
   nutzbar), dann Animation + Schütteln + Sound.
5. Spieler & Runden + Bestellübersicht.
6. Kartenpflege-UI.
7. Historie & Statistik.
8. Release-APK bauen und in der Gruppe testen (Feldtest beim Open Flair 😄).

## Offene Fragen / zu klären mit der Gruppe

- **Zusammensetzung „Rum & Spezial":** Marcel meint, Sekt/Grog & Co. kommen
  zu Rum („weil da sonst wenig ist") – ist aber nicht 100 % sicher. Vor dem
  Festival mit der Gruppe abklären, welche Spezialitäten genau dazugehören
  (auch Glas vs. nur Flasche Prosecco? Aperitif dabei?). Dank Kartenpflege-UI
  im Zweifel einfach in der App anpassbar.
- Gibt es Sonderregeln (Pasch, Weiterschenken, Nachwürfeln), die die App
  kennen sollte?
- Preis/Inhalt von Menü 837 für die erweiterte Bestellliste.

## Build & Entwicklung

- Bauen ohne Android Studio (PowerShell):
  `$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'`
  dann `.\gradlew.bat test assembleDebug`
- Debug-APK liegt danach unter `app\build\outputs\apk\debug\app-debug.apk`.
- Unit-Tests der Spielregeln: `app\src\test\java\de\gyrosbande\wuerfel\domain\`
  (`DiceRulesTest`, `GameFlowTest`) – bei Regeländerungen zuerst Tests anpassen.
- Kernlogik liegt Android-frei in `domain\` (`DiceRules`, `GameFlow`,
  `MenuSeed`); UI in `ui\` (Compose, `RollViewModel`).
- Achtung Windows/PowerShell 5.1: Der Projektpfad enthält ein „ü" –
  PowerShell-Skripte, die den Pfad hart codieren, als ASCII mit
  `[char]0x00FC` bauen oder Pfade als Parameter übergeben.
- **Wichtig – nicht entfernen:** In `gradle.properties` steht
  `-Dfile.encoding=windows-1252` (org.gradle.jvmargs) und
  `android.overridePathCheck=true`. Ohne beides schlägt der Build wegen des
  „ü" im Pfad fehl (Unit-Tests: ClassNotFoundException, weil der Java-Launcher
  Gradles Worker-Argfile mit nativer Windows-Kodierung liest).
- Emulator: AVD „Wuerfel837" (Pixel 7, Android 37.1, Anzeigename
  „837 Wuerfel Testgeraet") wurde manuell unter `%USERPROFILE%\.android\avd\`
  angelegt und ist in Android Studios Device Manager sichtbar.

### CI/CD (GitHub Actions)

- Workflow `.github/workflows/android.yml`: bei jedem Push auf `main`
  Tests + Release-APK als Artifact (90 Tage); bei Tags `v*` zusätzlich ein
  GitHub-Release mit angehängter APK (öffentlich herunterladbar).
- Signierung: Secrets `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`,
  `KEY_PASSWORD` → Release-Keystore; ohne Secrets Debug-Signatur-Fallback
  (Logik in `app/build.gradle.kts`).
- Keystore + Passwort + Base64 liegen lokal in `C:\Repo\837-wuerfel-signing\`
  (bewusst außerhalb des Repos, `*.jks` ist gitignored).
- Neue Version: `versionCode`/`versionName` in `app/build.gradle.kts` erhöhen,
  committen, `git tag vX.Y && git push origin main vX.Y`.

### Umsetzungsstand

- ✅ Meilenstein 1–4 (teilweise): Projektgerüst, Würfellogik + Tests,
  App-Icon, Würfel-Screen mit virtuellem Würfeln und manueller Eingabe
  („Schnell würfeln" ohne Spielerverwaltung).
- ⏳ Offen: Room-Persistenz, Spieler & Runden + Bestellübersicht,
  Kartenpflege-UI, Historie/Statistik, Schütteln + Sound.

## Hinweise für Claude

- Ton der App: humorvoll, Insider-Sprache der Gruppe ist erwünscht
  („Gyrosbande", „837"), aber UI klar und am Tisch mit einer Hand bedienbar
  (große Buttons – die Nutzer haben ggf. schon getrunken 🍻).
- Verantwortungsvoller Rahmen: private Spaß-App für Erwachsene; keine
  Belehrungen einbauen, aber auch keine Trink-Zwang-Mechaniken erfinden, die
  über das bestehende Spiel der Gruppe hinausgehen.
- Die Regeln aus „Spielregeln" sind verbindlich; bei Unklarheiten nachfragen
  statt raten.
- Getränkedaten immer aus der Datenbank lesen, nie hart im UI codieren.
