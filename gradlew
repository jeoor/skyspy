#!/usr/bin/env sh
DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"

if command -v pwsh >/dev/null 2>&1; then
  pwsh -NoProfile -ExecutionPolicy Bypass -File "$DIR/gradle/wrapper/bootstrap-gradle.ps1" -GradleVersion "9.1.0" "$@"
elif command -v powershell.exe >/dev/null 2>&1; then
  powershell.exe -NoProfile -ExecutionPolicy Bypass -File "$DIR/gradle/wrapper/bootstrap-gradle.ps1" -GradleVersion "9.1.0" "$@"
else
  echo "PowerShell is required to bootstrap Gradle for this project." >&2
  exit 1
fi
