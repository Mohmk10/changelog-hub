#!/bin/bash

# Changelog Hub CLI Launcher
# Usage: ./run.sh [command] [options]

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAR_FILE="$SCRIPT_DIR/target/cli-1.0.0-SNAPSHOT.jar"

if [ ! -f "$JAR_FILE" ]; then
    echo "JAR file not found: $JAR_FILE"
    echo "Please build the project first: mvn clean package -pl cli -am"
    exit 1
fi

java -jar "$JAR_FILE" "$@"
