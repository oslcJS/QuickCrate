#!/usr/bin/env bash
# build-all.sh — builds QuickCrates against every supported Paper API version.
# Versions: 1.20.4, 1.20.5, 1.20.6, 1.21, 1.21.1, 1.21.3, 1.21.4, 1.21.5,
#           1.21.6, 1.21.7, 1.21.8, 26.1, 26.1.1, 26.1.2
# Note: Paper 26.1+ uses a new versioning scheme (26.x.y.build.N) instead of
#       the old R0.1-SNAPSHOT format. Profiles use Maven version ranges accordingly.
# The default jar (built without a profile) is compiled against 1.20.4 and
# is runtime-compatible with ALL listed versions because we use only stable
# Bukkit/Paper APIs. The per-profile builds are sanity-check compiles.
set -e
PROFILES=(paper-1.20.4 paper-1.21.4 paper-1.21.5 paper-1.21.6 paper-1.21.7 \
          paper-1.21.8 paper-26.1.1 paper-26.1.2)
mkdir -p dist
echo "==> Building default (1.20.4 baseline, runtime-compatible everywhere)"
mvn -q clean package
cp target/QuickCrates-*.jar dist/QuickCrates-universal.jar
for p in "${PROFILES[@]}"; do
  echo "==> Verifying compile against $p"
  mvn -q -P "$p" clean package
  cp target/QuickCrates-*.jar "dist/QuickCrates-${p#paper-}.jar"
done
echo "==> Done. Artifacts in dist/"
ls -la dist/
