#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
VERSION="1.0.0"
BUNDLE_DIR="$PROJECT_ROOT/release-bundles"

MODULES=(
    "core"
    "openapi-parser"
    "graphql-parser"
    "grpc-parser"
    "asyncapi-parser"
    "spring-parser"
    "git-integration"
    "cli"
    "maven-plugin"
    "notification-service"
    "analytics-engine"
)

echo "=== Changelog Hub Maven Central Bundle Generator ==="
echo "Version: $VERSION"
echo ""

rm -rf "$BUNDLE_DIR"
mkdir -p "$BUNDLE_DIR"

echo "[1/3] Building project with release profile..."
cd "$PROJECT_ROOT"
mvn clean install -Prelease -DskipTests -q

echo "[2/3] Creating bundles for each module..."

for MODULE in "${MODULES[@]}"; do
    echo "  Processing: $MODULE"

    MODULE_DIR="$PROJECT_ROOT/$MODULE"
    TARGET_DIR="$MODULE_DIR/target"

    if [ ! -d "$TARGET_DIR" ]; then
        echo "    SKIP: No target directory"
        continue
    fi

    ARTIFACT_ID="$MODULE"
    JAR_FILE="$TARGET_DIR/${ARTIFACT_ID}-${VERSION}.jar"
    SOURCES_JAR="$TARGET_DIR/${ARTIFACT_ID}-${VERSION}-sources.jar"
    JAVADOC_JAR="$TARGET_DIR/${ARTIFACT_ID}-${VERSION}-javadoc.jar"
    POM_FILE="$MODULE_DIR/pom.xml"

    if [ ! -f "$JAR_FILE" ]; then
        echo "    SKIP: JAR not found"
        continue
    fi

    TEMP_DIR=$(mktemp -d)
    BUNDLE_NAME="${ARTIFACT_ID}-${VERSION}-bundle"

    cp "$JAR_FILE" "$TEMP_DIR/${ARTIFACT_ID}-${VERSION}.jar"

    if [ -f "$SOURCES_JAR" ]; then
        cp "$SOURCES_JAR" "$TEMP_DIR/"
    else
        echo "    WARN: sources.jar not found"
    fi

    if [ -f "$JAVADOC_JAR" ]; then
        cp "$JAVADOC_JAR" "$TEMP_DIR/"
    else
        echo "    WARN: javadoc.jar not found"
    fi

    cp "$POM_FILE" "$TEMP_DIR/${ARTIFACT_ID}-${VERSION}.pom"

    echo "    Signing artifacts with GPG..."
    for FILE in "$TEMP_DIR"/*; do
        if [ -f "$FILE" ]; then
            gpg --batch --yes -ab "$FILE" 2>/dev/null || {
                echo "    WARN: GPG signing failed for $(basename "$FILE")"
            }
        fi
    done

    echo "    Creating ZIP bundle..."
    cd "$TEMP_DIR"
    zip -q "$BUNDLE_DIR/${BUNDLE_NAME}.zip" *
    cd "$PROJECT_ROOT"

    rm -rf "$TEMP_DIR"

    echo "    OK: ${BUNDLE_NAME}.zip"
done

echo ""
echo "[3/3] Bundle generation complete!"
echo ""
echo "=== Generated Bundles ==="
ls -la "$BUNDLE_DIR"/*.zip 2>/dev/null || echo "No bundles generated"

echo ""
echo "=== Next Steps ==="
echo "1. Go to https://central.sonatype.com"
echo "2. Click 'Publish' -> 'Upload a Deployment Bundle'"
echo "3. Upload each ZIP file from: $BUNDLE_DIR"
echo "4. Click 'Publish' for each bundle"
echo ""
