#!/bin/bash
set -e

VERSION="1.0.0"
GROUP_PATH="io/github/mohmk10"
BUNDLES_DIR="release-bundles"
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

cd "$PROJECT_ROOT"

rm -rf "$BUNDLES_DIR"
mkdir -p "$BUNDLES_DIR"

MODULES=(
    "core:changelog-hub-core"
    "openapi-parser:changelog-hub-openapi-parser"
    "graphql-parser:changelog-hub-graphql-parser"
    "grpc-parser:changelog-hub-grpc-parser"
    "asyncapi-parser:changelog-hub-asyncapi-parser"
    "spring-parser:changelog-hub-spring-parser"
    "git-integration:changelog-hub-git-integration"
    "cli:changelog-hub-cli"
    "maven-plugin:changelog-hub-maven-plugin"
    "notification-service:changelog-hub-notification-service"
    "analytics-engine:changelog-hub-analytics-engine"
)

echo "=== Changelog Hub Maven Central Bundle Generator v2 ==="
echo "Version: $VERSION"
echo ""

for MODULE_ENTRY in "${MODULES[@]}"; do
    IFS=':' read -r MODULE_DIR ARTIFACT_ID <<< "$MODULE_ENTRY"

    echo "Processing $ARTIFACT_ID..."

    TARGET_DIR="$MODULE_DIR/target"
    BUNDLE_WORK_DIR="$BUNDLES_DIR/work/$GROUP_PATH/$ARTIFACT_ID/$VERSION"

    mkdir -p "$BUNDLE_WORK_DIR"

    # Copy main artifacts
    cp "$TARGET_DIR/$ARTIFACT_ID-$VERSION.jar" "$BUNDLE_WORK_DIR/"
    cp "$TARGET_DIR/$ARTIFACT_ID-$VERSION-sources.jar" "$BUNDLE_WORK_DIR/"
    cp "$TARGET_DIR/$ARTIFACT_ID-$VERSION-javadoc.jar" "$BUNDLE_WORK_DIR/"

    # Copy POM
    if [ -f "$TARGET_DIR/$ARTIFACT_ID-$VERSION.pom" ]; then
        cp "$TARGET_DIR/$ARTIFACT_ID-$VERSION.pom" "$BUNDLE_WORK_DIR/"
    else
        cp "$MODULE_DIR/pom.xml" "$BUNDLE_WORK_DIR/$ARTIFACT_ID-$VERSION.pom"
    fi

    # Copy GPG signatures
    cp "$TARGET_DIR/$ARTIFACT_ID-$VERSION.jar.asc" "$BUNDLE_WORK_DIR/"
    cp "$TARGET_DIR/$ARTIFACT_ID-$VERSION-sources.jar.asc" "$BUNDLE_WORK_DIR/"
    cp "$TARGET_DIR/$ARTIFACT_ID-$VERSION-javadoc.jar.asc" "$BUNDLE_WORK_DIR/"

    if [ -f "$TARGET_DIR/$ARTIFACT_ID-$VERSION.pom.asc" ]; then
        cp "$TARGET_DIR/$ARTIFACT_ID-$VERSION.pom.asc" "$BUNDLE_WORK_DIR/"
    else
        gpg --batch --yes --armor --detach-sign "$BUNDLE_WORK_DIR/$ARTIFACT_ID-$VERSION.pom"
    fi

    # Generate checksums
    cd "$BUNDLE_WORK_DIR"
    for file in *.jar *.pom *.asc; do
        if [ -f "$file" ]; then
            md5 -q "$file" > "$file.md5"
            shasum -a 1 "$file" | cut -d' ' -f1 > "$file.sha1"
        fi
    done
    cd "$PROJECT_ROOT"

    # Create ZIP bundle with correct structure
    cd "$BUNDLES_DIR/work"
    zip -r "../$ARTIFACT_ID-$VERSION-bundle.zip" "$GROUP_PATH/$ARTIFACT_ID/$VERSION"
    cd "$PROJECT_ROOT"

    echo "  Created $ARTIFACT_ID-$VERSION-bundle.zip"
done

# Cleanup work directory
rm -rf "$BUNDLES_DIR/work"

echo ""
echo "=== All bundles created in $BUNDLES_DIR/ ==="
ls -lh "$BUNDLES_DIR/"*.zip
