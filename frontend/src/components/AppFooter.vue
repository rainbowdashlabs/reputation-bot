/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<template>
  <footer
      class="footer-with-fade w-full bg-white dark:bg-gray-800 border-t border-gray-200 dark:border-gray-700 py-6 mt-8">
    <div class="container mx-auto px-4">
      <div class="grid grid-cols-1 md:grid-cols-3 items-start gap-4 text-sm text-gray-600 dark:text-gray-400">
        <div class="flex order-3 md:order-1 gap-2 items-start">
          <div class="flex gap-5 items-center">
            <LocaleSwitcher/>
            <LightModeSwitch/>
          </div>
        </div>

        <div class="flex flex-col items-center gap-2 order-1 md:order-2">
          <div class="flex flex-col items-center gap-4 flex-wrap justify-center">
            <div>
              <a
                  class="hover:text-indigo-600 dark:hover:text-indigo-400 transition-colors inline-flex items-center gap-1"
                  href="https://github.com/RainbowDashLabs/reputation-bot"
                  rel="noopener noreferrer"
                  target="_blank"
              >
                <font-awesome-icon :icon="['fab', 'github']"/>
                GitHub
              </a>
              <template v-if="links">
                <template v-if="links.website">
                  <span> • </span>
                  <a
                      :href="links.website"
                      class="hover:text-indigo-600 dark:hover:text-indigo-400 transition-colors inline-flex items-center gap-1"
                      rel="noopener noreferrer"
                      target="_blank"
                  >
                    <font-awesome-icon :icon="['fas', 'globe']"/>
                    Website
                  </a>
                </template>
                <template v-if="links.faq">
                  <span> • </span>
                  <a
                      :href="links.faq"
                      class="hover:text-indigo-600 dark:hover:text-indigo-400 transition-colors inline-flex items-center gap-1"
                      rel="noopener noreferrer"
                      target="_blank"
                  >
                    <font-awesome-icon :icon="['fas', 'question-circle']"/>
                    FAQ
                  </a>
                </template>
                <template v-if="links.support">
                  <span> • </span>
                  <a
                      :href="links.support"
                      class="hover:text-indigo-600 dark:hover:text-indigo-400 transition-colors inline-flex items-center gap-1"
                      rel="noopener noreferrer"
                      target="_blank"
                  >
                    <font-awesome-icon :icon="['fas', 'life-ring']"/>
                    Support
                  </a>
                </template>
                <template v-if="links.tos">
                  <span> • </span>
                  <a
                      :href="links.tos"
                      class="hover:text-indigo-600 dark:hover:text-indigo-400 transition-colors inline-flex items-center gap-1"
                      rel="noopener noreferrer"
                      target="_blank"
                  >
                    <font-awesome-icon :icon="['fas', 'file-contract']"/>
                    TOS
                  </a>
                </template>
              </template>
            </div>
            <div>
              <span>© 2026 RainbowDashLabs and Contributor</span>
              <span> • </span>
            </div>
            <div>
              <span>Licensed under AGPL-3.0</span>
            </div>
          </div>
        </div>

        <div class="flex items-start justify-center md:justify-end gap-3 order-2 md:order-3">
          <GuildSwitcher/>
        </div>
      </div>
    </div>
  </footer>
</template>

<script lang="ts" setup>
import {onMounted, ref} from 'vue'
import {api} from '@/api'
import type {Links} from '@/api/types'
import GuildSwitcher from '@/components/GuildSwitcher.vue'
import LocaleSwitcher from '@/components/appheader/LocaleSwitcher.vue'
import LightModeSwitch from "@/components/appheader/LightModeSwitch.vue"

const links = ref<Links>({
  tos: '',
  invite: '',
  support: '',
  website: '',
  faq: ''
})

onMounted(async () => {
  try {
    links.value = await api.getLinks()
  } catch (error) {
    console.error('Failed to fetch links:', error)
  }
})
</script>

<style scoped>
.footer-with-fade {
  position: relative;
}

.footer-with-fade::after {
  content: '';
  position: absolute;
  bottom: -40px;
  left: 0;
  right: 0;
  height: 40px;
  background: linear-gradient(to bottom, rgb(255, 255, 255), transparent);
  pointer-events: none;
}

.dark .footer-with-fade::after {
  background: linear-gradient(to bottom, rgb(31, 41, 55), transparent);
}
</style>
