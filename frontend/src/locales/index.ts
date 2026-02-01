// Locale metadata for display purposes
export interface LocaleMetadata {
  code: string
  name: string
  nativeName: string
}

// Locale information matching the bot's supported languages
export const LOCALE_METADATA: Record<string, LocaleMetadata> = {
  'cs': { code: 'cs', name: 'Czech', nativeName: 'Čeština' },
  'de': { code: 'de', name: 'German', nativeName: 'Deutsch' },
  'el': { code: 'el', name: 'Greek', nativeName: 'Ελληνικά' },
  'en-US': { code: 'en-US', name: 'English (US)', nativeName: 'English (US)' },
  'es-ES': { code: 'es-ES', name: 'Spanish (Spain)', nativeName: 'Español (España)' },
  'fi': { code: 'fi', name: 'Finnish', nativeName: 'Suomi' },
  'fr': { code: 'fr', name: 'French', nativeName: 'Français' },
  'it': { code: 'it', name: 'Italian', nativeName: 'Italiano' },
  'ja': { code: 'ja', name: 'Japanese', nativeName: '日本語' },
  'ko': { code: 'ko', name: 'Korean', nativeName: '한국어' },
  'nl': { code: 'nl', name: 'Dutch', nativeName: 'Nederlands' },
  'no': { code: 'no', name: 'Norwegian', nativeName: 'Norsk' },
  'pl': { code: 'pl', name: 'Polish', nativeName: 'Polski' },
  'pt-BR': { code: 'pt-BR', name: 'Portuguese (Brazil)', nativeName: 'Português (Brasil)' },
  'ru': { code: 'ru', name: 'Russian', nativeName: 'Русский' },
  'sv-SE': { code: 'sv-SE', name: 'Swedish (Sweden)', nativeName: 'Svenska (Sverige)' },
  'tr': { code: 'tr', name: 'Turkish', nativeName: 'Türkçe' },
  'uk': { code: 'uk', name: 'Ukrainian', nativeName: 'Українська' },
  'zh-CN': { code: 'zh-CN', name: 'Chinese (Simplified)', nativeName: '简体中文' }
}

// Get all available locales
export function getAvailableLocales(): LocaleMetadata[] {
  return Object.values(LOCALE_METADATA)
}

// Get locale metadata by code
export function getLocaleMetadata(code: string): LocaleMetadata | undefined {
  return LOCALE_METADATA[code]
}
