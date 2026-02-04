/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useSession } from '@/composables/useSession'
import { api } from '@/api'
import BaseButton from '@/components/BaseButton.vue'

const props = defineProps<{
  initialReputationName: string
}>()

const { t } = useI18n()
const { session, updateProfileSettings } = useSession()
const reputationName = ref(props.initialReputationName)

watch(() => props.initialReputationName, (newVal) => {
  reputationName.value = newVal || ''
})

const isLocaleOverridesUnlocked = computed(() => {
  return session.value?.premiumFeatures?.localeOverrides?.unlocked ?? false
})

let reputationNameTimeout: ReturnType<typeof setTimeout> | null = null
const updateReputationName = () => {
  if (!isLocaleOverridesUnlocked.value) return

  if (reputationNameTimeout) clearTimeout(reputationNameTimeout)
  reputationNameTimeout = setTimeout(async () => {
    try {
      await api.updateProfileReputationName(reputationName.value || null)
      updateProfileSettings({ reputationName: reputationName.value || null })
    } catch (error) {
      console.error('Failed to update reputation name:', error)
    }
  }, 500)
}

const resetReputationName = async () => {
  if (!isLocaleOverridesUnlocked.value) return

  try {
    await api.deleteProfileReputationName()
    updateProfileSettings({ reputationName: null })
    reputationName.value = ''
  } catch (error) {
    console.error('Failed to reset reputation name:', error)
  }
}
</script>

<template>
  <div>
    <label class="label mb-2" for="reputationName">
      {{ t('profile.reputationName.label') }}
    </label>

    <div class="flex gap-2">
      <input
          id="reputationName"
          v-model="reputationName"
          :disabled="!isLocaleOverridesUnlocked"
          :placeholder="t('profile.reputationName.placeholder')"
          class="input"
          type="text"
          @input="updateReputationName"
      />
      <BaseButton
          :disabled="!isLocaleOverridesUnlocked"
          :title="t('profile.reputationName.reset')"
          color="secondary"
          @click="resetReputationName"
      >
        {{ t('profile.reset') }}
      </BaseButton>
    </div>
    <p class="description">{{ t('profile.reputationName.description') }}</p>
  </div>
</template>
