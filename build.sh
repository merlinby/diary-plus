#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
SDK_DIR="${DIARY_SDK_DIR:?Set DIARY_SDK_DIR to Android SDK directory}"
TOOLS_DIR="${DIARY_BUILD_TOOLS_DIR:-$SDK_DIR/build-tools/35.0.0}"
PLATFORM_JAR="${DIARY_PLATFORM_JAR:-$SDK_DIR/platforms/android-35/android.jar}"
SIGN_DIR="${DIARY_SIGNING_DIR:-$PWD/signing}"
if [ ! -f "$SIGN_DIR/release.jks" ] || [ ! -f "$SIGN_DIR/password.txt" ]; then
 echo "Signing key missing. Restore the separate private signing backup into signing/ (not Git)." >&2
 exit 1
fi
APP_VERSION="$(python3 -c 'import xml.etree.ElementTree as E; print(E.parse("app/src/main/AndroidManifest.xml").getroot().get("{http://schemas.android.com/apk/res/android}versionName"))')"
OUTPUT_APK="release/DiaryPlus-$APP_VERSION.apk"
rm -rf build/classes build/dex build/generated
mkdir -p build/classes build/dex build/generated release
"$TOOLS_DIR/aapt2" compile --dir app/src/main/res -o build/resources.zip
"$TOOLS_DIR/aapt2" link -o build/base.apk -I "$PLATFORM_JAR" --manifest app/src/main/AndroidManifest.xml --java build/generated -A app/src/main/assets build/resources.zip
java com.sun.tools.javac.Main -source 8 -target 8 -encoding UTF-8 -bootclasspath "$PLATFORM_JAR:$TOOLS_DIR/core-lambda-stubs.jar" -d build/classes $(find app/src/main/java build/generated -name '*.java')
"$TOOLS_DIR/d8" --lib "$PLATFORM_JAR" --min-api 29 --output build/dex $(find build/classes -name '*.class')
python3 - <<'PY'
import zipfile,shutil
shutil.copy('build/base.apk','build/unsigned.apk')
with zipfile.ZipFile('build/unsigned.apk','a',compression=zipfile.ZIP_DEFLATED) as z:z.write('build/dex/classes.dex','classes.dex')
PY
"$TOOLS_DIR/zipalign" -f -P 16 4 build/unsigned.apk build/aligned.apk
"$TOOLS_DIR/apksigner" sign --ks "$SIGN_DIR/release.jks" --ks-key-alias diary --ks-pass "file:$SIGN_DIR/password.txt" --out "$OUTPUT_APK" build/aligned.apk
"$TOOLS_DIR/apksigner" verify --verbose "$OUTPUT_APK"
"$TOOLS_DIR/zipalign" -c -P 16 4 "$OUTPUT_APK"
sha256sum "$OUTPUT_APK" > release/SHA256SUMS.txt
