/*
*     SPDX-License-Identifier: AGPL-3.0-only
*
*     Copyright (C) RainbowDashLabs and Contributor
*/
<script lang="ts" setup>
import {ref} from 'vue'
import {useI18n} from 'vue-i18n'
import SettingsContainer from './components/SettingsContainer.vue'
import ThankwordList from './thankwordsview/ThankwordList.vue'
import ThankwordInput from './thankwordsview/ThankwordInput.vue'
import DefaultThankwords from './thankwordsview/DefaultThankwords.vue'

const {t} = useI18n()
const isUpdating = ref(false)
</script>

<template>
  <SettingsContainer :description="t('thankwords.description')" :title="t('settings.thankwords')">
    <div class="space-y-6">
      <div>

        <ThankwordInput
            v-model:is-updating="isUpdating"
        />

        <ThankwordList
            v-model:is-updating="isUpdating"
        />
      </div>

      <DefaultThankwords
          v-model:is-updating="isUpdating"
      />
    </div>
  </SettingsContainer>
</template>

<style scoped>
/* Additional styles if needed */
</style>
