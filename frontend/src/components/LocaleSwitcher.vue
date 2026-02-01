<script setup lang="ts">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { setLocale } from '@/i18n'
import { getAvailableLocales, getLocaleMetadata } from '@/locales'

const { locale, t } = useI18n()
const isOpen = ref(false)

const availableLocales = getAvailableLocales()

const currentLocaleMetadata = computed(() => {
  const metadata = getLocaleMetadata(locale.value)
  return metadata || { code: 'en-US', name: 'English (US)', nativeName: 'English (US)' }
})

const toggleDropdown = () => {
  isOpen.value = !isOpen.value
}

const selectLocale = async (localeCode: string) => {
  await setLocale(localeCode as any)
  isOpen.value = false
}

// Close dropdown when clicking outside
const closeDropdown = () => {
  isOpen.value = false
}
</script>

<template>
  <div class="relative inline-block text-left" @click.stop>
    <button
      @click="toggleDropdown"
      class="inline-flex items-center justify-between w-full px-4 py-1 text-sm font-medium text-gray-500 bg-transparent border border-gray-300 shadow-sm hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
      :aria-label="t('locale.selectLanguage')"
    >
      <span class="flex items-center gap-2">
        {{ currentLocaleMetadata.nativeName }}
      </span>
    </button>

    <Teleport to="body">
      <div
        v-if="isOpen"
        class="fixed inset-0 z-40"
        @click="closeDropdown"
      ></div>
    </Teleport>

    <Transition
      enter-active-class="transition ease-out duration-100"
      enter-from-class="transform opacity-0 scale-95"
      enter-to-class="transform opacity-100 scale-100"
      leave-active-class="transition ease-in duration-75"
      leave-from-class="transform opacity-100 scale-100"
      leave-to-class="transform opacity-0 scale-95"
    >
      <div
        v-if="isOpen"
        class="absolute right-0 z-50 w-56 mt-2 origin-top-right shadow-lg max-h-96 overflow-y-auto"
      >
        <div class="py-1" role="menu">
          <button
            v-for="localeItem in availableLocales"
            :key="localeItem.code"
            @click="selectLocale(localeItem.code)"
            class="flex items-center w-full px-4 py-1 text-sm text-left hover:bg-gray-100"
            :class="{
              'bg-indigo-50 text-indigo-700 font-medium': localeItem.code === locale
            }"
            role="menuitem"
          >
            <span class="flex-1">{{ localeItem.nativeName }}</span>
          </button>
        </div>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
/* Additional styles if needed */
</style>
