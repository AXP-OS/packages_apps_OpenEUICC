#!/bin/bash
######################################################################
#
# update version
# Copyright (C) 2026 - steadfasterX <steadfasterX |at| binbash #DOT# rocks>
#
######################################################################

set -euo pipefail

# --- Config ---
API_URL="https://gitea.angry.im/api/v1/repos/PeterCxy/OpenEUICC/releases/latest"
TAG_PREFIX="unpriv-v"
GRADLE_PROPS="gradle.properties"

# --- 1. Get latest release tag ---
echo "🔍 Fetching latest release tag..."
LATEST_TAG=$(curl -s "$API_URL" | jq -r '.tag_name // empty' || echo "")
if [[ -z "$LATEST_TAG" ]]; then
  echo "❌ Failed to fetch latest tag from API. Check network or API access."
  exit 1
fi

echo "Latest tag: $LATEST_TAG"

# Verify tag starts with expected prefix
if [[ ! "$LATEST_TAG" =~ ^${TAG_PREFIX} ]]; then
  echo "⚠️  Tag '$LATEST_TAG' does not match expected prefix '$TAG_PREFIX'. Using raw value."
fi

# Extract version: remove "unpriv-v" prefix → get "X.X.X"
VERSION_NAME="${LATEST_TAG#unpriv-v}"
if [[ "$VERSION_NAME" == "$LATEST_TAG" ]]; then
  echo "❌ No version found in tag '$LATEST_TAG' (missing 'unpriv-v'?)"
  exit 1
fi

echo "Extracted versionName: $VERSION_NAME"

# --- 2. Read current versionCode from gradle.properties ---
if [[ -f "$GRADLE_PROPS" ]]; then
  # Extract existing versionCode (handles "versionCode=123" or "versionCode = 123")
  CURRENT_CODE=$(grep -E "^\s*versionCode\s*=\s*[0-9]+" "$GRADLE_PROPS" | sed -E 's/.*=\s*([0-9]+).*/\1/' | tail -1)
else
  CURRENT_CODE=""
fi

if [[ -z "$CURRENT_CODE" ]]; then
  echo "⚠️  No valid versionCode found in $GRADLE_PROPS — defaulting to 1"
  CURRENT_CODE=1
fi

# Increment versionCode
VERSION_CODE=$((CURRENT_CODE + 1))
echo "versionName: $VERSION_NAME"
echo "versionCode: $VERSION_CODE (was $CURRENT_CODE)"

# --- 3. Update gradle.properties (idempotently) ---
# Prepare new lines
NEW_VERSION_NAME_LINE="versionName=$VERSION_NAME"
NEW_VERSION_CODE_LINE="versionCode=$VERSION_CODE"

# If file doesn't exist, create with defaults
touch "$GRADLE_PROPS"

# Use awk to safely update or insert lines
awk -v vname="$NEW_VERSION_NAME_LINE" -v vcode="$NEW_VERSION_CODE_LINE" '
BEGIN { updated_name = 0; updated_code = 0 }
/^[[:space:]]*versionName[[:space:]]*=/ { sub(/=.*/, "=" vname); updated_name = 1; print; next }
/^[[:space:]]*versionCode[[:space:]]*=/ { sub(/=.*/, "=" vcode); updated_code = 1; print; next }
{ print }
END {
  if (!updated_name) print vname
  if (!updated_code) print vcode
}
' "$GRADLE_PROPS" > "${GRADLE_PROPS}.tmp" && mv "${GRADLE_PROPS}.tmp" "$GRADLE_PROPS"

echo "✅ Updated $GRADLE_PROPS"
echo "---"
cat "$GRADLE_PROPS" | grep -E "^(versionName|versionCode)" || true