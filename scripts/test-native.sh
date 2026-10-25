#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
test_dir="$(mktemp -d)"
trap 'rm -rf "$test_dir"' EXIT
java com.sun.tools.javac.Main -encoding UTF-8 -d "$test_dir" \
 app/src/main/java/by/local/diaryplus/ProfileStore.java \
 app/src/main/java/by/local/diaryplus/PasswordCipher.java \
 app/src/main/java/by/local/diaryplus/ReleaseVersion.java \
 tests/ProfileStoreTest.java tests/PasswordCipherTest.java tests/ReleaseVersionTest.java
java -cp "$test_dir" by.local.diaryplus.ProfileStoreTest
java -cp "$test_dir" by.local.diaryplus.PasswordCipherTest
java -cp "$test_dir" by.local.diaryplus.ReleaseVersionTest
