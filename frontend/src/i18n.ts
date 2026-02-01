import { createI18n } from 'vue-i18n'

// Supported locales matching the bot's supported languages
export const SUPPORTED_LOCALES = [
  'cs',
  'de',
  'el',
  'en-US',
  'es-ES',
  'fi',
  'fr',
  'it',
  'ja',
  'ko',
  'nl',
  'no',
  'pl',
  'pt-BR',
  'ru',
  'sv-SE',
  'tr',
  'uk',
  'zh-CN'
] as const

export type SupportedLocale = typeof SUPPORTED_LOCALES[number]

// Load locale messages from JSON files
async function loadLocaleMessages(locale: string) {
  const messages = await import(`./locales/${locale}.json`)
  return messages.default
}

// Get browser locale or default to en-US
function getBrowserLocale(): SupportedLocale {
  const browserLocale = navigator.language
  
  // Try exact match first
  if (SUPPORTED_LOCALES.includes(browserLocale as SupportedLocale)) {
    return browserLocale as SupportedLocale
  }
  
  // Try language code only (e.g., 'en' from 'en-GB')
  const languageCode = browserLocale.split('-')[0]
  const match = SUPPORTED_LOCALES.find(locale => 
    locale.split('-')[0] === languageCode
  )
  
  return (match || 'en-US') as SupportedLocale
}

// Get initial locale from query parameter or browser
function getInitialLocale(): SupportedLocale {
  // Check for lang query parameter
  const urlParams = new URLSearchParams(window.location.search)
  const langParam = urlParams.get('lang')
  
  if (langParam && SUPPORTED_LOCALES.includes(langParam as SupportedLocale)) {
    return langParam as SupportedLocale
  }
  
  // Fall back to browser locale
  return getBrowserLocale()
}

// Create i18n instance
export const i18n = createI18n({
  legacy: false,
  locale: getInitialLocale(),
  fallbackLocale: 'en-US',
  messages: {}
})

// Load initial locale
export async function setupI18n() {
  const locale = i18n.global.locale.value as string
  const messages = await loadLocaleMessages(locale)
  i18n.global.setLocaleMessage(locale, messages)
  
  // Remove lang parameter from URL if present
  const urlParams = new URLSearchParams(window.location.search)
  if (urlParams.has('lang')) {
    urlParams.delete('lang')
    const newRelativePathQuery = window.location.pathname + (urlParams.toString() ? '?' + urlParams.toString() : '')
    window.history.replaceState(null, '', newRelativePathQuery)
  }
  
  return i18n
}

// Function to change locale dynamically
export async function setLocale(locale: SupportedLocale) {
  // Load locale if not already loaded
  if (!i18n.global.availableLocales.includes(locale)) {
    const messages = await loadLocaleMessages(locale)
    i18n.global.setLocaleMessage(locale, messages)
  }
  
  i18n.global.locale.value = locale
}
