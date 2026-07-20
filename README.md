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

Bump `versionName`/`versionCode` in `app/build.gradle.kts`, commit, then
tag and push:

```bash
git tag v1.3
git push origin v1.3
```

The pipeline builds, tests and attaches the APK to a GitHub release.
Release notes are generated automatically from the
[Conventional Commit](https://www.conventionalcommits.org) messages with
[git-cliff](https://git-cliff.org) (config: `cliff.toml`), so users can
see what changed between versions - see [CHANGELOG.md](CHANGELOG.md) for
the full history. After the release, CI also regenerates `CHANGELOG.md`
and pushes it to `main` as a bot commit - run `git pull` afterwards to
get it locally.

Signing is covered in [docs/SIGNING.md](docs/SIGNING.md).

## 🛠️ Build locally

### In Android Studio

1. **File → Open**, select this folder. Android Studio detects the Gradle
   project and syncs it automatically (first sync downloads dependencies -
   takes a few minutes).
2. Pick a run target in the toolbar (an emulator or a USB-connected phone
   with developer mode / USB debugging enabled).
3. Press **Run ▶** (or Shift+F10) to build, install and launch the debug
   build in one step.
4. To build a version to hand out without running it: **Build → Build
   Bundle(s) / APK(s) → Build APK(s)**. Click **locate** in the
   notification that appears when it finishes, or find it under
   `app/build/outputs/apk/debug/app-debug.apk`.
5. To build a **release APK signed with your own keystore** (skip this for
   just trying the app - the debug build above is enough): **Build →
   Generate Signed Bundle / APK…** → APK → point it at your keystore (see
   [docs/SIGNING.md](docs/SIGNING.md)) → choose the `release` build
   variant.
6. Change the app version before building: open
   `app/build.gradle.kts` in the project tree and bump `versionCode` /
   `versionName` in `defaultConfig`.

### From the command line

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat test assembleDebug
```

The debug APK lands in `app/build/outputs/apk/debug/app-debug.apk`.

## 📚 More

- [Game rules, menu & architecture](docs/DETAILS.md)
- [Signing & release secrets](docs/SIGNING.md)
