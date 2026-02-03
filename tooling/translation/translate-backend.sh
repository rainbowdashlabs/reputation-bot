#!/usr/bin/env bash

# Script to translate backend property files
# Usage: ./translate-backend.sh [reference_lang]
# Default reference language is en_US

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$(dirname "$SCRIPT_DIR")")"

REFERENCE_LANG="${1:-en_US}"
PROPERTIES_PATH="$PROJECT_ROOT/src/main/resources"
PREFIX="locale"

echo "Translating backend property files..."
echo "Reference language: $REFERENCE_LANG"
echo "Properties path: $PROPERTIES_PATH"
echo "Prefix: $PREFIX"
echo ""

cd "$PROJECT_ROOT" && pipenv run python "$SCRIPT_DIR/translate.py" "$PREFIX" "$REFERENCE_LANG" "$PROPERTIES_PATH"

echo ""
echo "Backend translation complete!"
