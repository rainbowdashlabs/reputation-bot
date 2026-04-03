/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {useI18n} from 'vue-i18n'
import SubHeader from '@/components/SubHeader.vue'
import SubHeaderTab from '@/components/SubHeaderTab.vue'

const {t} = useI18n()
</script>

<template>
  <div>
    <SubHeader>
      <SubHeaderTab
          to="/guild/dashboard/overview"
          :active="$route.path.startsWith('/guild/dashboard/overview')"
      >
        {{ t('dashboard.tabs.overview') }}
      </SubHeaderTab>
      <SubHeaderTab
          to="/guild/dashboard/ranking"
          :active="$route.path.startsWith('/guild/dashboard/ranking')"
      >
        {{ t('dashboard.tabs.ranking') }}
      </SubHeaderTab>
      <SubHeaderTab
          to="/guild/dashboard/profile"
          :active="$route.path.startsWith('/guild/dashboard/profile')"
      >
        {{ t('dashboard.tabs.profile') }}
      </SubHeaderTab>
    </SubHeader>

    <div class="mx-auto px-4" style="max-width: 1200px;">
      <router-view/>
    </div>
  </div>
</template>

<style scoped>
</style>
