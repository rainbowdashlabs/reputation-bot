#!/usr/bin/env bash

# Script to translate frontend JSON files
# Usage: ./translate-frontend.sh [reference_lang]
# Default reference language is en-US

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$(dirname "$SCRIPT_DIR")")"

REFERENCE_LANG="${1:-en-US}"
LOCALES_PATH="$PROJECT_ROOT/frontend/src/locales"

echo "Translating frontend JSON files..."
echo "Reference language: $REFERENCE_LANG"
echo "Locales path: $LOCALES_PATH"
echo ""

cd "$PROJECT_ROOT" && pipenv run python "$SCRIPT_DIR/translate.py" "$REFERENCE_LANG" "$LOCALES_PATH"

echo ""
echo "Frontend translation complete!"
