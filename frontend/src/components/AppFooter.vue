/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<template>
  <footer
      class="footer-with-fade w-full bg-white dark:bg-gray-800 border-t border-gray-200 dark:border-gray-700 py-6">
    <div class="container mx-auto px-4">
      <div class="grid grid-cols-1 md:grid-cols-3 items-start gap-4 text-sm text-gray-600 dark:text-gray-400">
        <div class="flex order-3 md:order-1 flex-col gap-3 items-start">
          <div class="flex gap-5 items-center">
            <LocaleSwitcher/>
            <LightModeSwitch/>
          </div>
          <LegalLinks/>
        </div>

        <div class="flex flex-col items-center gap-2 order-1 md:order-2">
          <div class="flex flex-col items-center gap-4 flex-wrap justify-center">
            <FooterLinks :links="links"/>
            <div>
              <span>© 2026 RainbowDashLabs and Contributor</span>
            </div>
            <div>
              <span>Made with ❤️ and 🏳️‍🌈</span>
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
import {onMounted} from 'vue'
import {useLinks} from '@/composables/useLinks'
import GuildSwitcher from '@/components/GuildSwitcher.vue'
import FooterLinks from '@/components/FooterLinks.vue'
import LegalLinks from '@/components/LegalLinks.vue'
import LocaleSwitcher from '@/components/appheader/LocaleSwitcher.vue'
import LightModeSwitch from "@/components/appheader/LightModeSwitch.vue"

const {links, loadLinks} = useLinks()

onMounted(loadLinks)
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
