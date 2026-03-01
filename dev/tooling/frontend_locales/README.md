# Frontend Localization Validation Tool

This tool validates frontend locale files and provides automated fixes for common localization issues.

## Overview

The frontend localization validation tool is organized as a Python package in `tooling/frontend_locales/`. It performs comprehensive validation of frontend localization files using `en-US` as the reference locale. It checks for:

1. **Missing Keys**: All translation keys used in code (`t('key')`) exist in all locale files
2. **Unused Keys**: No unused keys exist in the `en-US` file
3. **Variable Consistency**: Variables like `{limit}` are consistent across all languages
4. **Extra Keys**: Other languages don't contain keys not present in `en-US`

## Project Structure

The tool is organized into separate modules for better maintainability:

```
tooling/frontend_locales/
├── __init__.py          # Package initialization
├── __main__.py          # CLI entry point
├── common.py            # Shared utilities (file I/O, key extraction)
├── validator.py         # Validation checks
└── fixer.py             # Fix routines
```

## Requirements

- Python 3.6+
- No external dependencies (uses only standard library)

## Usage

### Run All Validation Checks

```bash
# From the tooling directory
cd tooling
python3 -m frontend_locales

# Or from the project root
python3 -m tooling.frontend_locales
```

This runs all four validation checks and reports any issues found. Exit code is 0 if all checks pass, 1 otherwise.

### Fix Issues (Dry Run)

Preview what would be fixed without making changes:

```bash
# From the tooling directory
cd tooling

# Preview fixing missing keys in en-US
python3 -m frontend_locales --fix-missing

# Preview removing unused keys from all locales
python3 -m frontend_locales --fix-unused

# Preview removing extra keys from non-en-US locales
python3 -m frontend_locales --fix-extra
```

### Apply Fixes

Use the `--fix` flag to actually apply changes:

```bash
# From the tooling directory
cd tooling

# Apply all fixes
python3 -m frontend_locales --fix-missing --fix-unused --fix-extra --fix

# Or use --fix alone to apply all available fixes
python3 -m frontend_locales --fix
```

## Validation Checks

### Check 1: Missing Keys in Locale Files

**What it checks:** All translation keys used in the code (via `t('key')` calls) exist in all locale files.

**Example issue:**
```
Locale 'de' is missing keys: ['settings.newFeature', 'profile.customField']
```

**Fix:** Use `--fix-missing` to add missing keys to `en-US` only with placeholder values like `"TODO: key.name"`. Other locales should be updated through the translation process.

### Check 2: Unused Keys

**What it checks:** No keys exist in `en-US` that are not used anywhere in the code.

**Example issue:**
```
en-US has 5 unused keys: ['old.feature', 'deprecated.setting', ...]
```

**Fix:** Use `--fix-unused` to remove unused keys from all locale files.

### Check 3: Variable Consistency

**What it checks:** Variables like `{username}`, `{count}`, `{limit}` in translation strings are consistent across all languages.

**Example issue:**
```
Locale 'fr', key 'error.limit': missing variables {'limit'}
Locale 'de', key 'welcome.message': extra variables {'extraVar'}
```

**Fix:** This requires manual correction in the affected locale files. The tool reports the issues but doesn't auto-fix them to avoid breaking translations.

### Check 4: Extra Keys in Other Locales

**What it checks:** Other language files don't contain keys that aren't in `en-US`.

**Example issue:**
```
Locale 'de' has 3 extra keys not in en-US: ['custom.key1', 'custom.key2', ...]
```

**Fix:** Use `--fix-extra` to remove extra keys from non-en-US locale files.

## Command Line Options

```
--fix                 Apply fixes (without this, runs in dry-run mode)
--fix-missing         Fix missing keys (add to en-US only)
--fix-unused          Fix unused keys (delete from all locale files)
--fix-extra           Fix extra keys (remove from non-en-US locales)
--project-root PATH   Path to project root (default: parent of script directory)
```

## How It Works

### Key Extraction

The tool scans all `.vue`, `.ts`, and `.js` files in `frontend/src` for translation key usage patterns:

**Pattern 1: Translation function calls**
- `t('key.name')` - Single quotes
- `t("key.name")` - Double quotes
- Regex pattern: `\bt\(['"][\w.]+['"]\)`

**Pattern 2: Template string interpolations**
- `` t(`key.prefix.${variable}`) `` - Template strings with variable interpolation
- `` t(`key.prefix.${variable}.suffix`) `` - With both prefix and suffix
- The `${variable}` part is treated as a wildcard pattern `.+?` that matches any characters
- Examples:
  - `` t(`general.reputation.mode.modes.${mode}`) `` matches keys like:
    - `general.reputation.mode.modes.TOTAL`
    - `general.reputation.mode.modes.ROLLING_MONTH`
    - `general.reputation.mode.modes.WEEK`
  - `` t(`autopost.refreshInterval.${interval}`) `` matches:
    - `autopost.refreshInterval.HOURLY`
    - `autopost.refreshInterval.DAILY`
    - `autopost.refreshInterval.WEEKLY`
- Regex pattern: `\bt\(\`([a-zA-Z.]*)\$\{[^}]+\}([a-zA-Z.]*)\`\)`

**Pattern 3: Simple string locale codes**
- `'key.name'` or `"key.name"` - Quoted strings with dots (e.g., in object properties like `titleKey: 'setup.steps.language.title'`)
- Regex pattern: `['"]([a-zA-Z]+\.[a-zA-Z.]+)['"]`
- Automatically filters out common false positives:
  - Object property paths ending with: `.id`, `.name`, `.code`, `.key`, `.path`, `.roleId`, `.roleIds`, `.internalName`
  - Session/settings paths: `session.settings.*`
  - Enum-like patterns with all caps: `*.CONSTANT_NAME`

### Variable Detection

Variables in translation strings are detected using the pattern: `\{(\w+)\}`

Examples:
- `"Welcome {username}!"` → extracts `username`
- `"Limit: {limit} items"` → extracts `limit`

### Locale File Structure

The tool handles nested JSON structures and converts them to dot-notation internally:

```json
{
  "settings": {
    "profile": {
      "title": "Profile Settings"
    }
  }
}
```

Is treated as: `settings.profile.title`

## Testing

Run the test suite to verify the tool is working correctly:

```bash
# From the tooling directory
cd tooling
python3 test_frontend_locales.py

# Or from the project root
python3 tooling/test_frontend_locales.py
```

This runs all validation checks and demonstrates the fix tools in dry-run mode.

## Integration with CI/CD

Add to your CI pipeline to ensure localization quality:

```yaml
# Example GitHub Actions workflow
- name: Validate Frontend Localization
  run: |
    cd tooling
    python3 -m frontend_locales
```

The script exits with code 1 if any validation fails, making it suitable for CI/CD gates.

## Examples

### Example 1: Check for Issues

```bash
$ cd tooling
$ python3 -m frontend_locales
================================================================================
Frontend Localization Validation
================================================================================

Check 1: All translation keys used in code exist in all locale files
--------------------------------------------------------------------------------
✗ FAIL: Some keys are missing
  - Locale 'de' is missing keys: ['new.feature']

Check 2: No unused keys in en-US
--------------------------------------------------------------------------------
✓ PASS: No unused keys in en-US

Check 3: Variables consistency across all languages
--------------------------------------------------------------------------------
✓ PASS: All variables are consistent

Check 4: No extra keys in other languages (not in en-US)
--------------------------------------------------------------------------------
✓ PASS: No extra keys in other languages

================================================================================
✗ Some checks failed. Use --fix to automatically fix issues.
================================================================================
```

### Example 2: Fix Missing Keys (Dry Run)

```bash
$ cd tooling
$ python3 -m frontend_locales --fix-missing
================================================================================
DRY RUN MODE - No changes will be made
Use --fix to apply changes
================================================================================

Fix: Adding missing keys to en-US
--------------------------------------------------------------------------------
  Added 2 missing keys to en-US: ['new.feature', 'another.key']
```

### Example 3: Apply All Fixes

```bash
$ cd tooling
$ python3 -m frontend_locales --fix
Fix: Removing unused keys from all locale files
--------------------------------------------------------------------------------
  Removed 5 unused keys from 'en-US'
  Removed 5 unused keys from 'de'
  ...

Fix: Removing extra keys from non-en-US locales
--------------------------------------------------------------------------------
  Removed 2 extra keys from 'fr'

Fix: Adding missing keys to en-US
--------------------------------------------------------------------------------
  Added 1 missing keys to en-US: ['new.feature']

================================================================================
✓ Fixes applied successfully!
================================================================================
```

## Best Practices

1. **Run validation regularly** - Add to your CI/CD pipeline
2. **Fix missing keys first** - Use `--fix-missing` to add placeholders to en-US
3. **Clean up unused keys** - Use `--fix-unused` to remove dead code
4. **Keep en-US as source of truth** - Use `--fix-extra` to remove keys not in en-US
5. **Manual review for variables** - Variable inconsistencies require manual fixes
6. **Test after fixes** - Always run validation again after applying fixes

## Troubleshooting

### Issue: Script reports false positives

**Solution:** Check if the translation key pattern in your code matches the expected format `t('key')` or `t("key")`.

### Issue: Variables not detected

**Solution:** Ensure variables use curly braces: `{variable}` not `%variable%` or other formats.

### Issue: Nested keys not working

**Solution:** The tool automatically handles nested JSON. Use dot notation in code: `t('parent.child.key')`.

## Contributing

When adding new translations:

1. Add the key to `en-US.json` first
2. Use the key in your code: `t('your.new.key')`
3. Run validation to ensure consistency
4. Submit for translation to other languages

## License

This tool is part of the reputation-bot project and follows the same license.
