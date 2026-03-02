#!/bin/bash
######################################################################
#
# update version
# Copyright (C) 2026 - steadfasterX <steadfasterX |at| binbash #DOT# rocks>
#
######################################################################
set -euo pipefail

API_URL="https://gitea.angry.im/api/v1/repos/PeterCxy/OpenEUICC/releases/latest"
TAG_PREFIX="unpriv-v"
GRADLE_PROPS="gradle.properties"

echo "🔍 Fetching latest release tag..."
LATEST_TAG=$(curl -s "$API_URL" | jq -r '.tag_name // empty' || echo "")

if [[ -z "$LATEST_TAG" ]] || [[ ! "$LATEST_TAG" =~ ^${TAG_PREFIX} ]]; then
  echo "❌ Failed to fetch or invalid tag: '$LATEST_TAG'"
  exit 1
fi

# Extract base version from tag (e.g., "unpriv-v1.5.1" → "1.5.1")
VERSION_BASE="${LATEST_TAG#unpriv-v}"
echo "Base version from tag: $VERSION_BASE"

# Read current versionCode (default 0 if not found)
CURRENT_CODE=$(grep -E '^\s*versionCode\s*=' "$GRADLE_PROPS" 2>/dev/null | \
  sed -E 's/^\s*versionCode\s*=\s*//' | tail -1) || CURRENT_CODE=0

# If nothing found or not numeric, default to 0
[[ "$CURRENT_CODE" =~ ^[0-9]+$ ]] || CURRENT_CODE=0

# Increment versionCode
VERSION_CODE=$((CURRENT_CODE + 1))

# Construct new versionName: base + "-" + versionCode
VERSION_NAME="${VERSION_BASE}-${VERSION_CODE}"
echo "✅ New versionName: $VERSION_NAME"
echo "✅ New versionCode: $VERSION_CODE"

# Update gradle.properties atomically and idempotently
{
  # Replace existing versionCode and versionName lines (any whitespace allowed around =)
  # Then append defaults if missing
  sed -E \
    -e "s/^[[:space:]]*versionCode[[:space:]]*=[[:space:]]*[0-9]+[[:space:]]*$/versionCode=$VERSION_CODE/" \
    -e "s/^[[:space:]]*versionName[[:space:]]*=[[:space:]]*[^[:space:]]+[[:space:]]*$/versionName=$VERSION_NAME/" \
    "$GRADLE_PROPS" | \
  awk -v name="$VERSION_NAME" -v code="$VERSION_CODE" '
    !/^[[:space:]]*versionCode/ && !/^[[:space:]]*versionName/ { print }
    /^versionCode/ { print "versionCode=" code; code_printed=1; next }
    /^versionName/ { print "versionName=" name; name_printed=1; next }
    END {
      if (!code_printed) print "versionCode=" code
      if (!name_printed) print "versionName=" name
    }
  '
} > "$GRADLE_PROPS.tmp" && mv "$GRADLE_PROPS.tmp" "$GRADLE_PROPS"

echo "🎉 Updated gradle.properties:"
grep -E '^version' "$GRADLE_PROPS" || echo "(none)"