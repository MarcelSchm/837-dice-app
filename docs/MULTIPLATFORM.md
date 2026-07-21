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

## The Apple-hurdle bypass: a PWA on GitHub Pages

Before committing to a native iOS app, seriously consider a **Progressive
Web App** served from GitHub Pages in this very repo. It sidesteps all
three constraints above at once: no Mac, no €99/year, no TestFlight
expiry. Your iPhone friends open a link, "Add to Home Screen" in Safari,
and it behaves like an app - full-screen, own icon, offline-capable. One
link works on iPhone, Android, and desktop alike, and a new version is
just a redeploy (no store review). GitHub Pages hosts it for free.

For a five-person festival group, this may simply be the better iOS
answer than a native port. But two honest catches decide whether it fits:

### Catch 1 - iOS wipes PWA storage between festivals (the big one)

iOS clears an installed PWA's IndexedDB/local storage when the app hasn't
been used for a few weeks; there is no persistent-storage guarantee on
iOS and the site cannot override it. For a *once-a-year* festival app
this is not an edge case - it is basically certain the local data is gone
by next summer.

That is survivable **because the app already has JSON export/import** -
but only if it stops being optional on the web. The web version must
treat "share your history into the WhatsApp group" as the real save, and
offer to auto-export at the end of a festival. `navigator.storage.
persist()` lowers the eviction odds but does not remove them. Net: the
history feature keeps working, the export just becomes the backbone
instead of a bonus. Worth an explicit test: export from the Android app,
import into the PWA, and back - the JSON format is platform-neutral, so
it should round-trip.

### Catch 2 - the existing Compose UI does *not* carry over to the web

Tempting assumption: "it's all Compose, run Compose Multiplatform for
Web." It doesn't work well for this target. Compose for Web is Canvas/
Skia-based and, as of 2026, still Beta - and crucially **Kotlin/Wasm does
not run in iOS Safari at all**; you'd fall back to the slower Kotlin/JS,
rendering the whole UI onto a canvas with non-native text fields and
scrolling. For an app whose entire audience reaches it *through Safari*,
that is the worst-case combination.

So the web UI is a fresh build either way. Two sane routes:

- **Share `:core` via Kotlin/JS, hand-write a DOM UI.** After the Stage 0
  KMP work, the 560 lines of tested rules compile to JavaScript; the web
  app is a thin HTML/CSS layer on top. One language, one source of truth
  for the rules - consistent with the rest of the project.
- **A standalone TypeScript PWA.** The rules are small (category roll,
  drink roll with the wrap rule, grouping, totals) - a few hundred lines
  of TS, with the existing `:core` unit tests as the exact spec to port
  against. Fastest to stand up, at the cost of a second implementation of
  logic that must not drift (it has been stable since v1, so the risk is
  low).

### Other iOS-PWA rough edges (all minor here)

- **Install is Safari-only.** "Share → Add to Home Screen." Chrome on iOS
  can't install PWAs. A one-time explain-once step for non-techies.
- **Shake-to-roll** needs `DeviceMotionEvent.requestPermission()` behind
  a user tap (iOS 13+). Doable with a "Schütteln aktivieren" button.
- **Sound** needs a user gesture to start - and rolling *is* a tap, so
  the roll sound is fine.
- No push, no background - the app needs neither.

### What it would take

`web/` folder, deployed by an `actions/deploy-pages` job on push/tag (see
CI below). A web manifest + icons (the Gyrosbande logo is already there),
a service worker for offline use, IndexedDB for storage, `navigator.
share()` / a file input for export/import. Effort: **small-to-medium**,
and every hour spent here also serves Android-browser and desktop users.

**Recommendation:** if the honest answer to "how many of us have
iPhones?" is "a couple", build the PWA and skip the native iOS port
entirely - it removes the Mac, the €99, and the TestFlight refresh from
the picture. Keep the native port on the table only if the iOS feel has
to be pixel-perfect, which for a dice app at a sticky festival table it
does not.

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

## CI: deploying the PWA from the same tag

If the PWA route wins, the CI side is trivial compared to iOS signing -
no certificates, no macOS runner. A job builds the `web/` output and
publishes it with the official Pages actions:

```yaml
  pages:
    runs-on: ubuntu-latest
    needs: build
    permissions:
      pages: write
      id-token: write
    # every push to main, or every v* tag - your call
    steps:
      - uses: actions/checkout@v4
      - # build web/ (Kotlin/JS or the TS app)
      - uses: actions/upload-pages-artifact@v3
        with: { path: web/dist }
      - uses: actions/deploy-pages@v4
```

The app then lives at `https://marcelschm.github.io/837-dice-app/`. Same
`RELEASE_TAG` scheme can stamp the version into the page footer. One-time
setup: repo **Settings → Pages → Source: GitHub Actions**.

## CI: getting an IPA from the same tag (native route only)

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

The fork is early: **PWA or native for iOS?**

- **If iPhones are few / you want it cheap and soon:** the PWA is the
  answer. Stage 0 (`:core` → KMP, or even just port the rules to TS) →
  the PWA on GitHub Pages → done for iOS, and Android/desktop get a web
  version for free. No Mac, no €99, no TestFlight.
- **If a native iOS feel is worth the cost:** €99 decision → Stage 0 →
  Mac question → Stage 1 (Compose Multiplatform).

Either way:

1. **Stage 0 now** (`:core` → multiplatform). Small, useful even if iOS
   never happens, and it stops the multiplatform debt from growing with
   every new feature. It underpins both the PWA and the native routes.
2. **iOS**: the PWA (recommended for this group) *or* the native port.
3. **Stage 2** - Pixel Watch phase 2.
4. **Stage 3** - Apple Watch, when the itch comes.

## Open questions

- Is the €99/year worth it - i.e. how many in the Gyrosbande actually
  have iPhones? If it is one person, a shared phone at the table may be
  the cheaper answer than a whole port.
- Mac: buy, rent, or live with the CI-only loop?
- Does the history export/import need to work *between* Android and
  iPhone? (It would - the JSON format is platform-neutral - but it is
  worth testing explicitly, because that is the feature that keeps the
  group's statistics together.)
