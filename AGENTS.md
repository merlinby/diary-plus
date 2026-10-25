# Diary Plus Android

Read README.md and docs/CODEX_HANDOFF.md before changing the app.

## Product requirements
- Full official diary.e-schools.by in a WebView, plus local analytics.
- Two separate student accounts, independently persistent sessions and login settings.
- Keep the default WebView directory for profile 1 and process-specific suffix for profile 2.
- Biometric/device-credential prompts only when retrieving/saving/editing a saved password. NEVER require app unlock just for opening/resuming or switching an already signed-in profile.
- Saving a password is optional and unchecked by default. Keep Android Keystore, AES-GCM and profile/login AAD binding.
- Password autofill must be restricted to the exact official HTTPS origin; never auto-submit or fill a password for another username.

## Commands
- JS tests: npm ci && npm test (Node 22+).
- JVM tests: bash scripts/test-native.sh (JDK 17).
- APK: DIARY_SDK_DIR=/path/to/sdk bash build.sh (Platform 35, Build Tools 35.0.0, Python 3, JDK 17).
- Override nonstandard SDK paths with DIARY_BUILD_TOOLS_DIR and DIARY_PLATFORM_JAR.
- The build requires signing/release.jks and signing/password.txt, or DIARY_SIGNING_DIR. Obtain these from the separate private backup. Do not generate a replacement release key silently.

## Constraints
- Do not commit signing material, APKs, credentials, session tokens, real diary responses or logs containing them.
- Do not add servers, telemetry, bridges to native Java, or new permissions without a concrete requirement.
- Increment manifest versionCode/versionName for a new APK. Keep package by.local.diaryplus and original signing identity for installed-app updates.
- Never describe jsdom/JVM testing as Android device testing. Actual WebView, Keystore, biometrics and real sessions need device/emulator testing.
- Keep changes scoped to the requested task; test the affected behavior.
