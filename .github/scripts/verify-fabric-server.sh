#!/usr/bin/env bash
# Starts Loom's real Fabric dedicated-server runtime. A successful Gradle build
# alone does not prove that Fabric can discover and initialize the mod.
set -Eeuo pipefail

project="$1"
minecraft_version="$2"
run_dir="$project/run/server-$minecraft_version"
log_file="$project/build/ci-server.log"

mkdir -p "$run_dir" "$(dirname "$log_file")"
printf 'eula=true\n' > "$run_dir/eula.txt"

set +e
timeout --signal=INT --kill-after=20s 180s \
  ./gradlew ":$project:runServer" --no-daemon --stacktrace 2>&1 | tee "$log_file"
pipeline_status=("${PIPESTATUS[@]}")
set -e

gradle_status="${pipeline_status[0]}"
if ! grep -Eq 'Done \([0-9.]+s\)! For help, type "help"|Done \([0-9.]+s\)!' "$log_file"; then
  echo "Fabric server for Minecraft $minecraft_version did not finish starting."
  exit 1
fi

if ! grep -Fq 'WorldSoul mod ready (harness=' "$log_file"; then
  echo "The Fabric server started, but WorldSoul did not report initialization."
  exit 1
fi

# The smoke test intentionally stops the persistent server after it has started.
if [[ "$gradle_status" != 124 && "$gradle_status" != 130 && "$gradle_status" != 0 ]]; then
  echo "Server process exited unexpectedly with status $gradle_status."
  exit "$gradle_status"
fi

echo "Fabric $minecraft_version started and loaded WorldSoul successfully."
