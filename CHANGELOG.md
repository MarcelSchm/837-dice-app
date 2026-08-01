# Changelog

All notable changes to 837 Dice. Generated from
[Conventional Commits](https://www.conventionalcommits.org) with
[git-cliff](https://git-cliff.org).
## [2.9] - 2026-08-01

### 🩹 Fixed
- Make the SBOM describe the app, not the build machine

### 📚 Documentation
- Show the app in the README
- Redo the screenshots with the real group
- Say plainly why the security tooling is here

### ⚙️ CI
- Dependabot, SBOM, CVE scan and a tighter workflow
- Let a workflow propose Gradle wrapper updates, and group the toolchain
- Drop the redundant wrapper workflow and cap Gradle at 9.5.x
- Surface the SBOM and CVE findings instead of burying them

### 🧹 Maintenance
- Bump the actions group with 8 updates
- Bump actions/checkout in the actions group
- Bump gradle-wrapper from 8.14.3 to 9.5.1
- Bump the kotlin group across 1 directory with 6 updates
- Bump the toolchain group across 1 directory with 7 updates
- Renormalize gradlew.bat and mark the audio/keystore files binary

### Other
- Merge pull request #2 from MarcelSchm/dependabot/github_actions/actions-626cb31b68

chore(deps): Bump the actions group with 8 updates
- Merge pull request #7 from MarcelSchm/dependabot/github_actions/actions-7a5a078ad4

chore(deps): Bump actions/checkout from 4.4.0 to 7.0.1 in the actions group
- Migrate jvmTarget to the compilerOptions DSL
- Merge pull request #8 from MarcelSchm/dependabot/gradle/gradle-wrapper-9.5.1

chore(deps): Bump gradle-wrapper from 8.14.3 to 9.5.1
- Merge pull request #3 from MarcelSchm/dependabot/gradle/kotlin-4760a49799

chore(deps): Bump the kotlin group across 1 directory with 6 updates
- Declare the Material Icons dependency explicitly
- Merge pull request #5 from MarcelSchm/dependabot/gradle/toolchain-c49a1a5441

chore(deps): Bump the toolchain group across 1 directory with 7 updates
## [2.8] - 2026-07-30

### 🎲 New
- Open Flair countdown complication for the watch face (phase 3)
- "Dauergast" and "Seltener Gast" in the hall of fame
- Pick the line-up and seating order before a round
- Correct a round afterwards in the history
- Corrections reach the other phones too (schema v5)

### 📚 Documentation
- Record the Open Flair countdown complication (phase 3)
## [2.7] - 2026-07-21

### 🎲 New
- Applause on the Prosecco celebration
## [2.6] - 2026-07-21

### 🎲 New
- Confetti celebration when the Prosecco bottle is rolled
## [2.5] - 2026-07-21

### 🎲 New
- Mirror a live round to the watch as a second display (phase 2b)

### 📚 Documentation
- Record the watch round mirror (phase 2b, display-only)

### Other
- Merge branch 'main' of https://github.com/MarcelSchm/837-dice-app
## [2.4] - 2026-07-21

### 🎲 New
- Rename the home title to "Würfelspiel"
- Sync the phone's menu to the watch (Data Layer, phase 2a)

### 📚 Documentation
- Record watch menu sync (phase 2a) and split out phase 2b

### Other
- Merge branch 'main' of https://github.com/MarcelSchm/837-dice-app
## [2.3] - 2026-07-21

### 🎲 New
- Count down to the next Open Flair on the home screen
- Share the hall of fame as an image

### 📚 Documentation
- Add the iPhone and Apple Watch roadmap
- Add the GitHub Pages PWA as the Apple-hurdle bypass
- Record the decision to go PWA for iOS
- Describe the festival countdown and hall-of-fame image
## [2.2] - 2026-07-20

### 🎲 New
- Use the group's own dice sound and sync the roll animation
- Report unavailable drinks per drink, not per player
- Capitalize player names and reject duplicates

### 📚 Documentation
- Describe the new drink-level flow and name rules
## [2.1] - 2026-07-20

### 🎲 New
- Add standalone Wear OS quick-roll app (phase 1)
- Fix a rolled drink from the order summary

### 🔧 Changed
- Extract the pure game logic into a shared :core module

### ⚙️ CI
- Attach the wear APK to releases and document the watch app

### Other
- Merge branch 'main' of https://github.com/MarcelSchm/837-dice-app
## [2.0] - 2026-07-20

### 🎲 New
- Add manual extra order items to the round summary
- Make the drinks menu editable in the app
- Add shake-to-roll and a dice rattle sound
- Show extra order items in the history detail

### 📚 Documentation
- Add Pixel Watch companion concept and update feature status
## [1.5] - 2026-07-20

### ⚙️ CI
- Derive app version from the release tag instead of a manual bump

### 🧪 Tests
- Cover import/export edge cases (invalid files, merge corners)
## [1.4] - 2026-07-20

### 🎲 New
- Allow deleting rounds from the history

### ⚙️ CI
- Auto-update CHANGELOG.md after release tags
## [1.3] - 2026-07-20

### 🎲 New
- Handle unavailable drinks with reroll or manual substitute

### ⚙️ CI
- Generate release notes with git-cliff
## [1.2] - 2026-07-20

### 🎲 New
- Add history data layer with stats and cross-device merge
- Add history screens with hall of fame, export and import

### 📚 Documentation
- Split README into signing and game-details guides

### 🧹 Maintenance
- Bump Android Gradle Plugin to 8.13.2
## [1.1] - 2026-07-19

### 🎲 New
- Add Room persistence with seeded San Remo menu
- Add players and rounds with a grouped order summary
## [1.0] - 2026-07-19

### 🎲 New
- Scaffold Android app for the 837 schnapps dice game

### 🔧 Changed
- Rename app to 837 Dice and translate project to English

### 📚 Documentation
- Add CI/CD documentation to CLAUDE.md

### 🧹 Maintenance
- Drop Windows path workarounds after moving to an ASCII path
- Move source images into an images/ folder
