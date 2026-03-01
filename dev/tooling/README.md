# Translation Tool

This tool uses DeepL API to automatically translate missing keys in language files.

## Prerequisites

1. Install dependencies using pipenv (from the project root):
   ```bash
   pipenv install
   ```

2. Set your DeepL API key as an environment variable:
   ```bash
   export DEEPL_API_KEY="your-api-key-here"
   ```
   Or create a `.env` file in the project root with:
   ```
   DEEPL_API_KEY=your-api-key-here
   ```

## Quick Start

The easiest way to run translations is using the provided bash scripts:

### Translate Backend (Property Files)

```bash
./tooling/translate-backend.sh [reference_lang]
```

Default reference language is `en_US`. This translates all `locale_*.properties` files in `src/main/resources/`.

**Example:**
```bash
./tooling/translate-backend.sh en_US
```

### Translate Frontend (JSON Files)

```bash
./tooling/translate-frontend.sh [reference_lang]
```

Default reference language is `en-US`. This translates all `*.json` files in `frontend/src/locales/`.

**Example:**
```bash
./tooling/translate-frontend.sh en-US
```

## Advanced Usage

The script supports two modes: **Properties mode** for backend Java properties files and **JSON mode** for frontend locale files.

### Properties Mode (Backend)

Translates Java `.properties` files with a specific prefix pattern.

```bash
pipenv run python tooling/translate.py <prefix> <reference_lang> <path>
```

**Arguments:**
- `<prefix>`: The prefix of the properties files (e.g., `locale`, `messages`)
- `<reference_lang>`: The reference language code (e.g., `en_US`, `de_DE`)
- `<path>`: Path to the directory containing the properties files

**Example:**
```bash
pipenv run python tooling/translate.py locale en_US src/main/resources/
```

This will:
1. Load all `locale_*.properties` files from the specified directory
2. Use `locale_en_US.properties` as the reference
3. Translate missing keys in all other language files
4. Write the updated files back

### JSON Mode (Frontend)

Translates JSON locale files with nested structure.

```bash
pipenv run python tooling/translate.py <reference_lang> <path>
```

**Arguments:**
- `<reference_lang>`: The reference language code (e.g., `en-US`, `de`, `es-ES`)
- `<path>`: Path to the directory containing the JSON locale files

**Example:**
```bash
pipenv run python tooling/translate.py en-US frontend/src/locales/
```

This will:
1. Load all `*.json` files from the specified directory
2. Use `en-US.json` as the reference
3. Flatten nested JSON structures for translation
4. Translate missing keys in all other language files
5. Unflatten and write the updated JSON files back with proper structure

## Language Code Mapping

The script uses `lang_mapper.json` to map certain language codes to DeepL-compatible formats:

```json
{
  "es_ES": "ES",
  "NO": "NB",
  "sv_SE": "SV",
  "zh_CN": "ZH-HANS"
}
```

## Translation Features

- **Informal tone**: Uses `PREFER_LESS` formality for casual, friendly translations
- **Placeholder preservation**: Protects placeholders like `{variable}` and `%placeholder%` from translation
- **Reference preservation**: Skips translation for reference patterns like `$reference$`
- **Quality optimized**: Uses DeepL's quality-optimized model for best results
- **Nested JSON support**: Automatically handles nested JSON structures in frontend files

## Notes

- The script only translates **missing** keys. Existing translations are preserved.
- Empty values in target languages are considered missing and will be translated.
- The script uses UTF-8 encoding for all files.
- JSON files are formatted with 2-space indentation and include a trailing newline.
