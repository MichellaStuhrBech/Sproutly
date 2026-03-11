#!/usr/bin/env bash
# Build the Sproutly backend JAR for deployment (e.g. Digital Ocean).
# Requires: Maven (mvn) and JDK 17 on PATH.
# Output: backend/target/sproutly-backend.jar

set -e
cd "$(dirname "$0")"

if ! command -v mvn &>/dev/null; then
  echo "Maven (mvn) not found. Install Maven and ensure it is on your PATH." >&2
  echo "Then run: mvn clean package -DskipTests" >&2
  exit 1
fi

echo "Building backend JAR (tests skipped for deploy build)..."
mvn clean package -DskipTests -q

JAR="target/sproutly-backend.jar"
if [ -f "$JAR" ]; then
  echo "Done. JAR: $JAR"
  echo "Run with: java -jar $JAR"
else
  echo "JAR was not produced at $JAR" >&2
  exit 1
fi
