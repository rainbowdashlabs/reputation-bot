/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed, ref, watch} from 'vue'
import {useI18n} from 'vue-i18n'
import {useSession} from '@/composables/useSession'
import {api} from '@/api'
import BaseButton from '@/components/BaseButton.vue'

const props = defineProps<{
  initialNickname: string
  disabled: boolean
}>()

const {t} = useI18n()
const {updateProfileSettings} = useSession()
const nickname = ref(props.initialNickname)
const hasChanges = computed(() => nickname.value !== props.initialNickname)

watch(() => props.initialNickname, (newVal) => {
  nickname.value = newVal || ''
})

const isUpdating = ref(false)

const updateNickname = async () => {
  if (props.disabled || !hasChanges.value || isUpdating.value) return

  isUpdating.value = true
  try {
    await api.updateProfileNickname(nickname.value || null)
    updateProfileSettings({nickname: nickname.value || null})
  } catch (error) {
    console.error('Failed to update nickname:', error)
  } finally {
    isUpdating.value = false
  }
}

const resetNickname = async () => {
  if (props.disabled) return

  try {
    await api.deleteProfileNickname()
    updateProfileSettings({nickname: null})
    nickname.value = ''
  } catch (error) {
    console.error('Failed to reset nickname:', error)
  }
}
</script>

<template>
  <div>
    <label class="label mb-2" for="nickname">
      {{ t('profile.nickname.label') }}
    </label>
    <div class="flex gap-2">
      <input
          id="nickname"
          v-model="nickname"
          :disabled="disabled || isUpdating"
          :placeholder="t('profile.nickname.placeholder')"
          class="input"
          type="text"
          @keyup.enter="updateNickname"
      />
      <BaseButton
          v-if="hasChanges"
          :disabled="disabled || isUpdating"
          color="primary"
          @click="updateNickname"
      >
        {{ isUpdating ? t('common.loading') : t('common.confirm') }}
      </BaseButton>
      <BaseButton
          :disabled="disabled || isUpdating"
          :title="t('profile.nickname.reset')"
          color="secondary"
          @click="resetNickname"
      >
        {{ t('profile.reset') }}
      </BaseButton>
    </div>
    <p class="description">{{ t('profile.nickname.description') }}</p>
    <p class="description mt-1 text-red-500 dark:text-red-400 font-medium">{{ t('profile.tosNote') }}</p>
  </div>
</template>
