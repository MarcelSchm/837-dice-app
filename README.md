# 🎲 837 Dice - the Gyrosbande's drinking game app

The legendary schnapps dice game from the San Remo restaurant (Open Flair
festival, Eschwege) as an Android app. Roll 1 picks the category, roll 2
picks the drink - categories with more than 6 drinks use two dice, and if
you run off the bottom of the list you keep counting from the top. Can be a
whole bottle of Prosecco. Rules are rules. 🍾

![Android CI](../../actions/workflows/android.yml/badge.svg)

## 📲 Download the app

- **Recommended:** grab the latest version from
  [Releases](../../releases/latest) - download the `.apk`, open it on your
  phone, allow installation from unknown sources, done. No GitHub account
  needed.
- **Every commit:** each build under [Actions](../../actions) has an
  artifact `837-dice-apk` (requires GitHub login, kept for 90 days).

## 🚀 Publish a new version

```bash
git tag v1.0
git push origin v1.0
```

The pipeline builds, tests and attaches the APK to a GitHub release
automatically. Bump `versionName`/`versionCode` in `app/build.gradle.kts`
beforehand.

## 🔐 Signing (one-time setup)

Without configuration the CI signs with a debug key (works, but changing
keys means uninstalling the app before an update). For stable updates:
repo → **Settings → Secrets and variables → Actions** and create four
secrets:

| Secret | Value |
|---|---|
| `KEYSTORE_BASE64` | the keystore file as Base64 |
| `KEYSTORE_PASSWORD` | keystore password |
| `KEY_ALIAS` | `wuerfel837` |
| `KEY_PASSWORD` | key password (same as the keystore password) |

## 🛠️ Build locally

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat test assembleDebug
```

Details, game rules and architecture: see [CLAUDE.md](CLAUDE.md).
