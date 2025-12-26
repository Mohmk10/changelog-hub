#!/bin/bash
set -e

VERSION="1.0.0"
GROUP_PATH="io/github/mohmk10"
BUNDLES_DIR="release-bundles"
GPG_PASSPHRASE='@El Hadj M10'

CENTRAL_TOKEN="YVZoS1hTOjNLTWdGanV4RTc2enlzOFpDV1dpV0szSWpMWU52WEYxRw=="

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

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

rm -rf "$BUNDLES_DIR"
mkdir -p "$BUNDLES_DIR"

declare -a DEPLOY_RESULTS

echo "=============================================="
echo "MAVEN CENTRAL AUTOMATED DEPLOYMENT"
echo "=============================================="
echo "Version: $VERSION"
echo ""

for MODULE_ENTRY in "${MODULES[@]}"; do
    IFS=':' read -r MODULE_DIR ARTIFACT_ID <<< "$MODULE_ENTRY"

    echo ""
    echo "=============================================="
    echo "Processing: $ARTIFACT_ID"
    echo "=============================================="

    TARGET_DIR="$MODULE_DIR/target"
    BUNDLE_WORK_DIR="$BUNDLES_DIR/work/$GROUP_PATH/$ARTIFACT_ID/$VERSION"
    BUNDLE_ZIP="$BUNDLES_DIR/$ARTIFACT_ID-$VERSION-bundle.zip"

    mkdir -p "$BUNDLE_WORK_DIR"

    echo "[1/6] Copying artifacts..."
    if [ ! -f "$TARGET_DIR/$ARTIFACT_ID-$VERSION.jar" ]; then
        echo "ERROR: Main JAR not found: $TARGET_DIR/$ARTIFACT_ID-$VERSION.jar"
        DEPLOY_RESULTS+=("$ARTIFACT_ID:FAILED - Main JAR not found")
        continue
    fi

    cp "$TARGET_DIR/$ARTIFACT_ID-$VERSION.jar" "$BUNDLE_WORK_DIR/"
    cp "$TARGET_DIR/$ARTIFACT_ID-$VERSION-sources.jar" "$BUNDLE_WORK_DIR/"
    cp "$TARGET_DIR/$ARTIFACT_ID-$VERSION-javadoc.jar" "$BUNDLE_WORK_DIR/"

    echo "[2/6] Processing POM..."
    if [ -f "$MODULE_DIR/.flattened-pom.xml" ]; then
        cp "$MODULE_DIR/.flattened-pom.xml" "$BUNDLE_WORK_DIR/$ARTIFACT_ID-$VERSION.pom"
        echo "Using flattened POM"
    else
        echo "WARNING: No flattened POM, using original"
        cp "$MODULE_DIR/pom.xml" "$BUNDLE_WORK_DIR/$ARTIFACT_ID-$VERSION.pom"
    fi

    echo "[3/6] Validating POM..."
    POM_FILE="$BUNDLE_WORK_DIR/$ARTIFACT_ID-$VERSION.pom"
    if ! grep -q "<groupId>io.github.mohmk10</groupId>" "$POM_FILE"; then
        echo "ERROR: POM missing groupId"
        DEPLOY_RESULTS+=("$ARTIFACT_ID:FAILED - Missing groupId in POM")
        continue
    fi
    if ! grep -q "<version>$VERSION</version>" "$POM_FILE"; then
        echo "ERROR: POM missing version"
        DEPLOY_RESULTS+=("$ARTIFACT_ID:FAILED - Missing version in POM")
        continue
    fi
    echo "POM validation OK"

    echo "[4/6] Copying/generating signatures..."
    cd "$BUNDLE_WORK_DIR"

    for file in *.jar *.pom; do
        if [ -f "$file" ]; then
            ASC_SOURCE="$PROJECT_ROOT/$TARGET_DIR/$(basename $file).asc"
            if [ -f "$ASC_SOURCE" ]; then
                cp "$ASC_SOURCE" "$file.asc"
            else
                echo "Generating signature for $file..."
                gpg --batch --yes --passphrase "$GPG_PASSPHRASE" --pinentry-mode loopback --armor --detach-sign "$file" 2>/dev/null || \
                gpg --batch --yes --passphrase "$GPG_PASSPHRASE" --armor --detach-sign "$file"
            fi
        fi
    done

    echo "[5/6] Generating checksums..."
    for file in *.jar *.pom *.asc; do
        if [ -f "$file" ]; then
            md5 -q "$file" > "$file.md5"
            shasum -a 1 "$file" | cut -d' ' -f1 > "$file.sha1"
        fi
    done
    cd "$PROJECT_ROOT"

    echo "[6/6] Creating bundle..."
    cd "$BUNDLES_DIR/work"
    zip -r "../$(basename $BUNDLE_ZIP)" "$GROUP_PATH/$ARTIFACT_ID/$VERSION" > /dev/null
    cd "$PROJECT_ROOT"

    FILE_COUNT=$(unzip -l "$BUNDLE_ZIP" 2>/dev/null | grep -c "$ARTIFACT_ID" || echo "0")
    echo "Bundle created with $FILE_COUNT files"

    echo ""
    echo "Uploading to Maven Central..."

    UPLOAD_RESPONSE=$(curl -s -w "\n%{http_code}" \
        -X POST \
        -H "Authorization: Bearer $CENTRAL_TOKEN" \
        -F "bundle=@$BUNDLE_ZIP" \
        "https://central.sonatype.com/api/v1/publisher/upload?name=$ARTIFACT_ID-$VERSION&publishingType=AUTOMATIC")

    HTTP_CODE=$(echo "$UPLOAD_RESPONSE" | tail -n1)
    RESPONSE_BODY=$(echo "$UPLOAD_RESPONSE" | sed '$d')

    if [ "$HTTP_CODE" = "201" ] || [ "$HTTP_CODE" = "200" ]; then
        echo "SUCCESS - Upload completed for $ARTIFACT_ID (HTTP $HTTP_CODE)"
        DEPLOYMENT_ID=$(echo "$RESPONSE_BODY" | grep -o '"deploymentId":"[^"]*"' | cut -d'"' -f4 || echo "$RESPONSE_BODY")
        DEPLOY_RESULTS+=("$ARTIFACT_ID:SUCCESS - ID: $DEPLOYMENT_ID")
    else
        echo "FAILED - Upload failed for $ARTIFACT_ID (HTTP $HTTP_CODE)"
        echo "Response: $RESPONSE_BODY"

        echo "Retrying in 5 seconds..."
        sleep 5

        RETRY_RESPONSE=$(curl -s -w "\n%{http_code}" \
            -X POST \
            -H "Authorization: Bearer $CENTRAL_TOKEN" \
            -F "bundle=@$BUNDLE_ZIP" \
            "https://central.sonatype.com/api/v1/publisher/upload?name=$ARTIFACT_ID-$VERSION&publishingType=AUTOMATIC")

        RETRY_CODE=$(echo "$RETRY_RESPONSE" | tail -n1)
        RETRY_BODY=$(echo "$RETRY_RESPONSE" | sed '$d')

        if [ "$RETRY_CODE" = "201" ] || [ "$RETRY_CODE" = "200" ]; then
            echo "SUCCESS - Retry completed for $ARTIFACT_ID"
            DEPLOY_RESULTS+=("$ARTIFACT_ID:SUCCESS (retry)")
        else
            echo "FAILED - Retry failed for $ARTIFACT_ID"
            DEPLOY_RESULTS+=("$ARTIFACT_ID:FAILED - HTTP $RETRY_CODE: $RETRY_BODY")
        fi
    fi
done

rm -rf "$BUNDLES_DIR/work"

echo ""
echo "=============================================="
echo "DEPLOYMENT SUMMARY"
echo "=============================================="

SUCCESS_COUNT=0
FAILED_COUNT=0

for RESULT in "${DEPLOY_RESULTS[@]}"; do
    ARTIFACT=$(echo "$RESULT" | cut -d':' -f1)
    STATUS=$(echo "$RESULT" | cut -d':' -f2-)

    if [[ "$STATUS" == SUCCESS* ]]; then
        echo "SUCCESS: $ARTIFACT - $STATUS"
        ((SUCCESS_COUNT++))
    else
        echo "FAILED: $ARTIFACT - $STATUS"
        ((FAILED_COUNT++))
    fi
done

echo ""
echo "Total: $SUCCESS_COUNT succeeded, $FAILED_COUNT failed"
echo ""

if [ "$FAILED_COUNT" -gt 0 ]; then
    echo "Some deployments failed. Check the errors above."
    exit 1
else
    echo "All deployments successful!"
    echo "Visit https://central.sonatype.com/publishing/deployments to monitor status."
fi
