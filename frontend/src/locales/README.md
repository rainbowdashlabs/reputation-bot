# Frontend Internationalization (i18n)

This directory contains the internationalization setup for the frontend, supporting all languages that the bot itself supports.

## Supported Languages

The frontend supports the following 19 languages:

- Czech (cs)
- German (de)
- Greek (el)
- English US (en-US) - Default
- Spanish Spain (es-ES)
- Finnish (fi)
- French (fr)
- Italian (it)
- Japanese (ja)
- Korean (ko)
- Dutch (nl)
- Norwegian (no)
- Polish (pl)
- Portuguese Brazil (pt-BR)
- Russian (ru)
- Swedish Sweden (sv-SE)
- Turkish (tr)
- Ukrainian (uk)
- Chinese Simplified (zh-CN)

## File Structure

```
locales/
├── README.md           # This file
├── index.ts            # Locale metadata and helper functions
├── en-US.json          # English translations (with sample content)
├── cs.json             # Czech translations (empty structure)
├── de.json             # German translations (empty structure)
└── ...                 # Other language files
```

## Usage in Components

### Using translations in templates

```vue
<template>
  <div>
    <h1>{{ $t('common.save') }}</h1>
    <button>{{ $t('common.cancel') }}</button>
  </div>
</template>
```

### Using translations in script

```vue
<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t, locale } = useI18n()

// Use translation
const saveText = t('common.save')

// Get current locale
console.log(locale.value) // e.g., 'en-US'
</script>
```

### Changing locale dynamically

```typescript
import { setLocale } from '@/i18n'

// Change to German
await setLocale('de')
```

### Getting available locales

```typescript
import { getAvailableLocales, getLocaleMetadata } from '@/locales'

// Get all locales with metadata
const locales = getAvailableLocales()
// Returns: [{ code: 'en-US', name: 'English (US)', nativeName: 'English (US)' }, ...]

// Get specific locale metadata
const metadata = getLocaleMetadata('de')
// Returns: { code: 'de', name: 'German', nativeName: 'Deutsch' }
```

## Translation File Structure

Each locale file follows this structure:

```json
{
  "common": {
    "save": "Save",
    "cancel": "Cancel",
    "delete": "Delete",
    "edit": "Edit",
    "close": "Close",
    "confirm": "Confirm",
    "loading": "Loading...",
    "error": "Error",
    "success": "Success"
  },
  "navigation": {
    "home": "Home",
    "settings": "Settings",
    "profile": "Profile",
    "logout": "Logout"
  },
  "settings": {
    "title": "Settings",
    "general": "General",
    "reputation": "Reputation",
    "messages": "Messages"
  }
}
```

## Adding New Translations

1. Open the appropriate locale file (e.g., `de.json` for German)
2. Add the translated text for each key
3. Maintain the same structure across all locale files
4. Use empty strings for untranslated keys (will fall back to en-US)

## Adding New Translation Keys

1. Add the new key to `en-US.json` first with the English text
2. Add the same key structure to all other locale files
3. Fill in translations for each language (or leave empty for fallback)

## Browser Locale Detection

The i18n system automatically detects the user's browser locale on first load:
- Exact match: If browser locale matches a supported locale (e.g., `en-US`)
- Language match: If only language code matches (e.g., `en` → `en-US`)
- Fallback: Defaults to `en-US` if no match found

## Notes

- All locale files are loaded dynamically (lazy loading)
- The fallback locale is always `en-US`
- Locale files are in JSON format for easy editing
- The locale codes match the bot's backend locale codes where possible
