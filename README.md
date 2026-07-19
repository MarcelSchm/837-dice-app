# 🎲 837 Würfel – die Trinkspiel-App der Gyrosbande

Das legendäre Schnapswürfeln vom San Remo (Open Flair, Eschwege) als
Android-App. Wurf 1 bestimmt die Kategorie, Wurf 2 den Drink – bei mehr als
6 Drinks mit zwei Würfeln, und wer „unten durch" ist, zählt oben weiter.
Kann auch eine ganze Flasche Prosecco werden. Regeln sind Regeln. 🍾

![Android CI](../../actions/workflows/android.yml/badge.svg)

## 📲 App herunterladen

- **Empfohlen:** Neueste Version unter
  [Releases](../../releases/latest) – die `.apk` herunterladen, auf dem
  Handy öffnen, Installation aus unbekannten Quellen erlauben, fertig.
  Funktioniert ohne GitHub-Konto.
- **Jeder Commit:** Unter [Actions](../../actions) hat jeder Build ein
  Artifact `837-wuerfel-apk` (GitHub-Login nötig, 90 Tage verfügbar).

## 🚀 Neue Version veröffentlichen

```bash
git tag v1.0
git push origin v1.0
```

Die Pipeline baut, testet und hängt die APK automatisch an ein
GitHub-Release. Versionsnummer vorher in `app/build.gradle.kts`
(`versionName`/`versionCode`) hochzählen.

## 🔐 Signierung (einmalig einrichten)

Ohne Konfiguration signiert die CI mit einem Debug-Key (funktioniert, aber
bei wechselnden Keys muss man die App vor einem Update deinstallieren).
Für stabile Updates: Repo → **Settings → Secrets and variables → Actions**
und vier Secrets anlegen:

| Secret | Inhalt |
|---|---|
| `KEYSTORE_BASE64` | Keystore-Datei als Base64 (liegt vorbereitet in `C:\Repo\837-wuerfel-signing\keystore-base64.txt`) |
| `KEYSTORE_PASSWORD` | Keystore-Passwort |
| `KEY_ALIAS` | `wuerfel837` |
| `KEY_PASSWORD` | Key-Passwort (identisch zum Keystore-Passwort) |

## 🛠️ Lokal bauen

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat test assembleDebug
```

Details, Spielregeln und Architektur: siehe [CLAUDE.md](CLAUDE.md).
