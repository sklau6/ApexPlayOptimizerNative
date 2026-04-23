# ApexPlay Play Store Deployment Guide

## Current Status
- AdMob App ID is configured: `ca-app-pub-5428983411611852~2597826529`
- All 6 ad unit IDs are configured in `app/src/main/java/com/apexplayoptimizer/app/data/AdManager.kt`
- Release signing is wired in `app/build.gradle` via `keystore.properties`

## Blocker Found
`gradle/wrapper/gradle-wrapper.jar` is missing, so `gradlew.bat` cannot run.

## Step 1: Restore Gradle Wrapper
You have 2 options:

### Option A (Recommended)
Copy `gradle-wrapper.jar` from a working Android project using a similar Gradle version into:
- `gradle/wrapper/gradle-wrapper.jar`

### Option B
Install Gradle globally, then run from repo root:
```bat
gradle wrapper
```
This regenerates wrapper files.

## Step 2: Configure Release Signing
1. Copy:
- `keystore.properties.example` -> `keystore.properties`

2. Fill `keystore.properties`:
```properties
storeFile=C:/path/to/your-release-key.jks
storePassword=YOUR_STORE_PASSWORD
keyAlias=YOUR_KEY_ALIAS
keyPassword=YOUR_KEY_PASSWORD
```

3. If you don't have a keystore yet, create one:
```bat
keytool -genkeypair -v -keystore C:\path\to\apexplay-release.jks -alias apexplay -keyalg RSA -keysize 2048 -validity 10000
```

## Step 3: Build Production Bundle (.aab)
From repo root:
```bat
gradlew.bat :app:bundleRelease --no-daemon
```
Output:
- `app/build/outputs/bundle/release/app-release.aab`

## Step 4: Upload to Play Console
1. Play Console -> Your app -> `Release` -> `Production`
2. Create new release
3. Upload `app-release.aab`
4. Add release notes
5. Save -> Review release -> Start rollout to production

## Step 5: Production Checks
- Verify `versionCode` increment for every new release (`app/build.gradle`)
- Keep signing key backed up securely
- In AdMob, make sure app-ads.txt is configured on your domain if needed
- Ensure privacy policy URL and Data Safety form are complete in Play Console

## Optional Recommended Improvements
- Enable `minifyEnabled true` for release + proper ProGuard rules
- Build App Bundle only for Play (`.aab`) and avoid distributing release APK directly
- Add Crashlytics / ANR monitoring before full rollout
