# Signing & release secrets

Android requires every APK to be cryptographically signed. Two options are
wired up in `app/build.gradle.kts`:

- **No secrets configured:** the release build falls back to the built-in
  **debug key**. This works out of the box (forks, first-time clones,
  local builds) but the debug key is not secret and not meant for
  distribution - and if the signing key ever changes, everyone has to
  uninstall the app before an update.
- **Secrets configured (recommended for real releases):** the CI signs
  with a dedicated **release keystore**, so the same key is used for every
  release and updates install cleanly on top of each other.

## One-time setup: add the release secrets

In the GitHub repo: **Settings → Secrets and variables → Actions → New
repository secret**, then create these four:

| Secret | Value |
|---|---|
| `KEYSTORE_BASE64` | the keystore file (`.jks`), Base64-encoded |
| `KEYSTORE_PASSWORD` | the keystore password |
| `KEY_ALIAS` | the key alias inside the keystore |
| `KEY_PASSWORD` | the key password (often the same as the keystore password) |

The workflow (`.github/workflows/android.yml`) decodes `KEYSTORE_BASE64`
back into a `.jks` file at build time and passes all four values as
environment variables to Gradle; `app/build.gradle.kts` picks them up and
configures the `release` signing config automatically.

## Generating a release keystore

If there's no keystore yet, create one with `keytool` (ships with the JDK,
e.g. inside Android Studio's bundled JDK at
`...\Android Studio\jbr\bin\keytool.exe`):

```
keytool -genkeypair -v -keystore release.jks -alias <your-alias> \
  -keyalg RSA -keysize 2048 -validity 10000
```

To turn the resulting `release.jks` into the value for `KEYSTORE_BASE64`
(PowerShell):

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("release.jks")) |
  Out-File keystore-base64.txt -Encoding ascii
```

Paste the contents of `keystore-base64.txt` into the `KEYSTORE_BASE64`
secret.

## Keep it safe

- **Never commit the keystore or its passwords** to the repo (`*.jks` /
  `*.keystore` are gitignored on purpose).
- **Back it up.** If the keystore or its password is lost, there is no way
  to publish an update under the same signature ever again - everyone
  would need to uninstall and reinstall a differently-signed APK.
- Store the keystore file and passwords together somewhere private (a
  password manager, an encrypted drive) outside of any git repository.
