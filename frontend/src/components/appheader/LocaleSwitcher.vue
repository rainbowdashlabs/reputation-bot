/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { setLocale } from '@/i18n'
import { getAvailableLocales, getLocaleMetadata } from '@/locales'
import BaseButton from '@/components/BaseButton.vue'

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
    <BaseButton
      @click="toggleDropdown"
      class="w-full justify-between"
      color="indigo"
      style=""
      :aria-label="t('locale.selectLanguage')"
    >
      <span class="flex items-center gap-2">
        {{ currentLocaleMetadata.nativeName }}
      </span>
    </BaseButton>

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
        class="absolute right-0 z-50 w-56 mt-2 origin-top-right shadow-lg max-h-96 overflow-y-auto bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700"
      >
        <div class="py-1" role="menu">
          <BaseButton
            v-for="localeItem in availableLocales"
            :key="localeItem.code"
            @click="selectLocale(localeItem.code)"
            class="w-full text-left px-4 py-2 text-sm transition-colors"
            :class="{
              'bg-indigo-50 dark:bg-indigo-900/50 text-indigo-700 dark:text-indigo-300 font-medium': localeItem.code === locale,
              'text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-indigo-900/30': localeItem.code !== locale
            }"
            :rounded="false"
            color="secondary"
            style="background-color: transparent; border: none; box-shadow: none; color: inherit;"
            role="menuitem"
          >
            <span class="flex-1">{{ localeItem.nativeName }}</span>
          </BaseButton>
        </div>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
/* Additional styles if needed */
</style>
