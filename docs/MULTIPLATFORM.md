# Roadmap: iPhone, Pixel Watch, Apple Watch

Plan for taking 837 Dice beyond Android. Nothing here is implemented yet -
this is the route, the cost, and the decisions that need making first.

Target order, as agreed: **iPhone first**, then the Pixel Watch companion
(details in [WEAR.md](WEAR.md)), then Apple Watch some day.

## Verdict

Very doable, and cheaper than it looks - because the game logic was kept
free of Android from day one. The real obstacles are not code, they are
Apple's rules: you need a Mac to build, and €99/year to get the app onto
your friends' phones.

## What we already have going for us

Measured against the current code, not guessed:

| | Lines | State |
|---|---|---|
| `:core` (rules, menu, stats) | 560 | Pure Kotlin, **2 blockers** |
| `:app` (Android UI + data) | 3.937 | 100 % Compose, **3 platform-specific files** |
| `:wear` (watch) | 244 | Wear OS only |

The entire platform-specific surface of the app is:

- `ui/roll/DiceFeedback.kt` - SoundPool (roll sound), SensorManager
  (shake), SharedPreferences (mute setting)
- `ui/history/HistoryViewModel.kt` - FileProvider, ContentResolver (export
  and import files)
- `ui/history/HistoryScreen.kt` - Android file picker / share sheet
- Plus the Room database setup and `java.util.UUID` / `SimpleDateFormat`
  in four files

That is a remarkably small list for a 4.000-line app.

## The three hard constraints

These are Apple's, not ours, and they cannot be engineered away:

1. **iOS apps can only be built on macOS.** Your machine is Windows.
   GitHub Actions solves the *release* build (macOS runners, free for
   this repo because it is public), but developing and debugging the iOS
   app with a CI-only loop is painful - every trial is a push, a tag and
   a wait. Realistic options: a used Mac mini (from ~400 €), a rented Mac
   (MacinCloud etc., ~25 €/month), or accepting the slow loop.
2. **Apple Developer Program: €99 per year.** There is no free
   equivalent to "APK herunterladen und installieren". Without a paid
   account an app expires after 7 days and each person would need their
   own Mac. For a group of friends, the €99 is effectively mandatory.
3. **Distribution works differently.** No IPA to download from a GitHub
   release. The path is **TestFlight**: you upload a build, your friends
   install Apple's TestFlight app once and get every new version through
   it. Up to 100 internal testers. Builds expire after 90 days, so a
   festival-only app needs a fresh build each summer. (Ad-hoc
   distribution exists but requires registering every single device's
   UDID - worse for a group.)

**Recommendation:** decide on the €99 *before* any code is written. If
that is a no, the whole iOS branch is pointless and the effort is better
spent on the Pixel Watch.

## Stage 0 - make `:core` multiplatform

This is the one investment that pays for *all* later stages, including
the Apple Watch. Small, self-contained, and it can happen right now
without touching the Android app's behaviour.

`:core` becomes a Kotlin Multiplatform module (targets: JVM/Android +
iosArm64, iosSimulatorArm64, iosX64) with the code moving to
`commonMain`. Concretely, only these need fixing:

- **`String.format` (5 places)** - `Model.kt`, `OrderSummary.kt` (×2),
  `PriceInput.kt`, `StatsCalculator.kt`. `"%d,%02d €".format(...)` is
  JVM-only. Replace with a hand-rolled formatter, e.g.
  `"${cents / 100},${(cents % 100).toString().padStart(2, '0')} €"`.
  The existing unit tests already pin this behaviour, so the change is
  safe.
- **`java.util.Calendar` (1 place)** - `History.kt` uses it for
  `HistoryRound.year` (the festival-year filter). Replace with
  `kotlinx-datetime`.

Everything else - `DiceRules`, `GameFlow`, `MenuSeed`, `RoundSession`,
`OrderSummary`, `StatsCalculator`, `PlayerName`, `Player`, `Model` -
compiles as-is. The 73 unit tests keep running and gain iOS as a second
target, so the dice rules are verified on both platforms.

**Effort: small.** A focused weekend.

## Stage 1 - the iPhone app

### The one big decision: shared UI or native UI?

**Option A - Compose Multiplatform (recommended).**
The app is already 100 % Jetpack Compose. Compose Multiplatform renders
that same code on iOS (stable since 1.8). Roughly 90 % of the existing
~3.900 UI lines get reused; one codebase stays the source of truth, so a
new feature lands on both platforms at once - which matters a lot for a
one-person hobby project.
*Downside:* the app will feel Compose-ish rather than 100 % native iOS
(scroll physics, back-swipe, text fields). For a private dice app at a
festival table: irrelevant.

**Option B - shared `:core` + a SwiftUI app.**
Perfectly native feel, but the entire UI gets written a second time in a
language you would be learning as you go, and every future feature has to
be built twice. Only worth it if the app were going to the App Store for
strangers.

→ **Take A.** But note the asterisk: Compose Multiplatform does **not**
target watchOS. The Apple Watch app (stage 3) will need SwiftUI no matter
which option is picked here - which is precisely why stage 0 matters.

### What actually has to be built

1. **Data layer.** Room 2.7 supports KMP, and the project already uses
   2.7.2 - so no version jump, but the setup changes from
   `Room.databaseBuilder(context, ...)` to the driver-based KMP builder
   (`BundledSQLiteDriver`). Entities, DAOs and the four existing
   migrations carry over. `kotlinx-serialization` (export/import format)
   is already multiplatform.
2. **`expect`/`actual` for the three platform files:**
   | Android | iOS |
   |---|---|
   | SoundPool | `AVAudioPlayer` |
   | SensorManager (shake) | `CoreMotion` / `CMMotionManager` |
   | SharedPreferences | `NSUserDefaults` |
   | FileProvider + share sheet | `UIActivityViewController` |
   | `ActivityResultContracts` (file pick) | `UIDocumentPickerViewController` |
3. **Replace the remaining JVM APIs** in the shared UI: `java.util.UUID`
   → `kotlin.uuid.Uuid`, `SimpleDateFormat` → `kotlinx-datetime`
   formatting (4 files).
4. **An Xcode project** (`iosApp/`) as the host - a thin Swift entry
   point that presents the Compose view. Plus app icon, launch screen,
   bundle id, `Info.plist`.
5. **Signing and TestFlight** - see CI below.

**Effort: the big one.** Several weekends, and the Mac question decides
how painful it gets.

### Suggested module layout afterwards

```
837-dice/
├── core/        KMP: commonMain + android + ios targets (rules, stats)
├── shared/      Compose Multiplatform UI + data, used by app and iosApp
├── app/         Android host
├── iosApp/      Xcode project, iOS host
├── wear/        Wear OS (stays Android-only)
└── watchApp/    (stage 3) watchOS, SwiftUI
```

## Stage 2 - Pixel Watch, phase 2

Unchanged from [WEAR.md](WEAR.md): the watch stops being standalone and
becomes a dice cup for the round running on the phone (Wearable Data
Layer). Independent of the iOS work, and it profits from stage 0 as well.

## Stage 3 - Apple Watch

Last on purpose. Needs SwiftUI (Compose Multiplatform does not render on
watchOS), so it is a genuine second UI - but only for two or three
screens, and on top of the already-shared `:core`. The counterpart to the
Pixel Watch's phase 2 would be `WatchConnectivity` instead of the
Wearable Data Layer.

**Effort: medium**, and only sensible once stages 0 and 1 stand.

## CI: getting an IPA from the same tag

Today `git tag v2.2` produces two APKs. Adding iOS means a second job in
`.github/workflows/android.yml` (or a new workflow) on a `macos-latest`
runner:

```yaml
  ios:
    runs-on: macos-latest      # free for this public repo
    needs: build
    if: startsWith(github.ref, 'refs/tags/v')
```

What that job needs:

- **The same tag-derived version.** `RELEASE_TAG` already exists; it maps
  to `CFBundleShortVersionString` (`2.2`) and `CFBundleVersion`
  (`20200`) exactly like `versionName`/`versionCode` today. No second
  place to bump.
- **Signing secrets**, the same pattern as the Android keystore but with
  more parts: distribution certificate (`.p12`, base64), provisioning
  profile (base64), and their passwords. See
  [SIGNING.md](SIGNING.md) for how the Android side does it.
- **App Store Connect API key** (issuer ID, key ID, `.p8`) to upload the
  build to TestFlight, e.g. via `xcrun altool` or fastlane `pilot`.
- **Note:** the release will *not* carry a downloadable IPA. The iOS
  build ends in TestFlight; the release notes should link there instead.
  Worth mentioning in the release template so nobody hunts for a file
  that is not there.

Cost: none for this repo, since public repositories get GitHub-hosted
runners for free - macOS included. (If the repo ever goes private,
macOS minutes bill at ten times the Linux rate.)

## Recommended order

1. **Decide on the €99/year** - everything else depends on it.
2. **Stage 0 now** (`:core` → KMP). Small, useful even if iOS never
   happens, and it stops the multiplatform debt from growing with every
   new feature.
3. **Clarify the Mac question** (buy / rent / CI-only).
4. **Stage 1** - the iPhone app via Compose Multiplatform.
5. **Stage 2** - Pixel Watch phase 2.
6. **Stage 3** - Apple Watch, when the itch comes.

## Open questions

- Is the €99/year worth it - i.e. how many in the Gyrosbande actually
  have iPhones? If it is one person, a shared phone at the table may be
  the cheaper answer than a whole port.
- Mac: buy, rent, or live with the CI-only loop?
- Does the history export/import need to work *between* Android and
  iPhone? (It would - the JSON format is platform-neutral - but it is
  worth testing explicitly, because that is the feature that keeps the
  group's statistics together.)
