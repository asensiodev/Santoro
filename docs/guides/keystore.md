# Guide — Release Keystore Setup (Multi-Machine)

| Field       | Value                          |
|-------------|--------------------------------|
| **Type**    | One-time environment setup     |
| **Scope**   | Local build / release signing  |
| **Date**    | 2026-04-29                     |
| **Author**  | @asensiodev                    |

---

> This guide solves the `KEYSTORE_STORE_FILE must not be null` error that appears when moving to a new machine or when `local.properties` is missing.
>
> The keystore credentials are **never committed** to version control. They live in `~/.gradle/gradle.properties` on each machine.
>
> **Important:** Santoro uses **Google Play App Signing** (verified in Play Console → App Integrity → App Signing). This means Google holds the final signing key that users see. Your local `.jks` is only the **upload key** — used to sign the AAB before uploading it to Play Console. If you lose the upload key, it is recoverable by contacting Google Play support. If you did **not** have App Signing enabled, losing the keystore would be catastrophic.

---

## Where to start?

- **Mac Studio M1 Max** (the one that already has the keystore and the old `local.properties`) → start at **Step 1**.
- **Mac M5 Pro** (this machine, currently failing) → start at **Step 2** after the keystore file has been copied from the M1 Max.

---

## Step 1 — Migrate credentials from `local.properties` to `~/.gradle/gradle.properties` (M1 Max)

On the machine that currently builds releases:

- [ ] Open Terminal
- [ ] Open (or create) `~/.gradle/gradle.properties`:

```bash
nano ~/.gradle/gradle.properties
```

- [ ] Copy the four keystore values from the old `local.properties` (or from wherever you stored them) and paste them exactly like this:

```properties
KEYSTORE_STORE_FILE=/Users/YOUR_USERNAME/keystores/santoro-release.jks
KEYSTORE_KEY_ALIAS=santoro
KEYSTORE_STORE_PASSWORD=YOUR_STORE_PASSWORD
KEYSTORE_KEY_PASSWORD=YOUR_KEY_PASSWORD
```

- [ ] Replace the path with the **actual** location of your `.jks` file on this machine.
- [ ] Save (`Ctrl+O`, `Enter`, `Ctrl+X`).

> **Why `~/.gradle/gradle.properties`?**
> It is global to your user — every Android project on this Mac can read these values via `findProperty()`. It is also outside the project folder, so it will never be committed.

---

## Step 2 — Copy the keystore file to the new machine (M5 Pro)

- [ ] On the **M1 Max**, locate the `.jks` (or `.keystore`) file referenced by `KEYSTORE_STORE_FILE`.
- [ ] Copy it to the **M5 Pro** using AirDrop, iCloud Drive, USB, or `scp`:

```bash
# Example using scp
scp /Users/YOUR_M1_USER/keystores/santoro-release.jks \
  YOUR_M5_USER@YOUR_M5_IP:/Users/YOUR_M5_USER/keystores/
```

- [ ] On the **M5 Pro**, verify the file exists at the path you will reference.

> **Security note:** the keystore is a sensitive file. Avoid public cloud syncs (Dropbox, Google Drive) if possible. Prefer AirDrop, encrypted USB, or `scp`.

---

## Step 3 — Create `~/.gradle/gradle.properties` on the new machine (M5 Pro)

- [ ] Open Terminal on the **M5 Pro**
- [ ] Open (or create) `~/.gradle/gradle.properties`:

```bash
nano ~/.gradle/gradle.properties
```

- [ ] Paste the same four properties, adjusting the path to match the M5 Pro user:

```properties
KEYSTORE_STORE_FILE=/Users/angelasensio/keystores/santoro-release.jks
KEYSTORE_KEY_ALIAS=santoro
KEYSTORE_STORE_PASSWORD=YOUR_STORE_PASSWORD
KEYSTORE_KEY_PASSWORD=YOUR_KEY_PASSWORD
```

- [ ] Save and exit.

---

## Step 4 — Verify `app/build.gradle.kts` reads from the global properties

- [ ] Open `app/build.gradle.kts`
- [ ] Confirm the `signingConfigs` block looks like this (it should have been updated already):

```kotlin
signingConfigs {
    create("release") {
        val keystoreStoreFile = findProperty("KEYSTORE_STORE_FILE") as String?
        if (keystoreStoreFile != null) {
            keyAlias = findProperty("KEYSTORE_KEY_ALIAS") as String?
            storeFile = file(keystoreStoreFile)
            storePassword = findProperty("KEYSTORE_STORE_PASSWORD") as String?
            keyPassword = findProperty("KEYSTORE_KEY_PASSWORD") as String?
        }
    }
}

buildTypes {
    release {
        val releaseSigning = signingConfigs.findByName("release")
        if (releaseSigning?.storeFile != null) {
            signingConfig = releaseSigning
        }
    }
}
```

> This makes the release signing **optional**. Debug builds work immediately without any keystore. Release builds use the keystore only when the properties are present.

---

## Step 5 — Test the build

- [ ] On the **M5 Pro**, run a debug build:

```bash
./gradlew :app:assembleDebug
```

- [ ] It should succeed without keystore errors.
- [ ] (Optional) If you copied the keystore and filled the passwords, test a release build:

```bash
./gradlew :app:assembleRelease
```

- [ ] The release build should sign the APK automatically.

---

## Step 6 — Clean up old `local.properties` keystore entries (optional)

- [ ] On the **M1 Max**, open the project root `local.properties`
- [ ] Remove any keystore lines (`KEYSTORE_KEY_ALIAS`, `KEYSTORE_STORE_FILE`, etc.) if they were ever added there. Keep only `sdk.dir`.

---

## Troubleshooting

| Problem | Solution |
|---|---|
| `KEYSTORE_STORE_FILE must not be null` | `~/.gradle/gradle.properties` is missing or the properties are misspelled. Check spelling and case. |
| `File not found` for the `.jks` | The path in `KEYSTORE_STORE_FILE` is wrong. Verify with `ls` in Terminal. |
| Wrong keystore password | Double-check `KEYSTORE_STORE_PASSWORD` and `KEYSTORE_KEY_PASSWORD` in `~/.gradle/gradle.properties`. |
| `assembleDebug` fails | This should not happen after the change — the signing config is optional for debug. If it fails, the error is unrelated to the keystore. |
| New machine, lost keystore file (no backup) | Because **Google Play App Signing** is enabled, this is **recoverable**. Contact Google Play support to register a new upload key. The final app signing key (seen by users) stays with Google and never changes. Users are unaffected. |
| New machine, lost keystore file **and no App Signing** | **Catastrophic.** You cannot update the app ever. You would need to publish a new app with a different package name. |

---

## How to Verify Your App Signing Setup

If you are unsure whether your app uses Google Play App Signing:

- [ ] Open [Google Play Console](https://play.google.com/console)
- [ ] Select your app (Santoro)
- [ ] Go to **Setup** → **App integrity** → **App signing**
- [ ] Look for **"Google Play app signing"**
  - If it says **"Enabled"** or **"Firma de Google Play"** → you are protected. Your local `.jks` is only the upload key.
  - If it says **"Managed by you"** or **"Tú gestionas la clave"** → your local `.jks` is the **only** key. Losing it is catastrophic.

---

## Backup Reminder

- [ ] Make an offline backup of the `.jks` file (encrypted USB, external drive, password-protected ZIP).
- [ ] Write the passwords in a password manager (1Password, Bitwarden, Apple Passwords, etc.).
- [ ] Never store the keystore or passwords in the git repository.
- [ ] Store the backup in at least **two physically separate locations** (e.g., USB stick + cloud storage with encryption).

> **Risk level with App Signing:** Losing the upload key is an inconvenience (contact Google support, wait a few days). Without App Signing, it is a permanent lockout. Back up anyway — why take the risk?
