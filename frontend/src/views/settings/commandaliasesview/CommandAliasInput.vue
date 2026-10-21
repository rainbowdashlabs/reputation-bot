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
import type {AliasableCommand} from '@/api/types'
import BaseButton from '@/components/BaseButton.vue'

const COMMAND_NAME = /^[-_\p{L}\p{N}\p{sc=Deva}\p{sc=Thai}]{1,32}$/u

const props = defineProps<{
  command: AliasableCommand
  initialAlias: string
  description: string
}>()

const {t} = useI18n()
const {updateCommandAlias} = useSession()
const alias = ref(props.initialAlias)
const isUpdating = ref(false)
const apiError = ref<string | null>(null)

watch(() => props.initialAlias, (newVal) => {
  alias.value = newVal || ''
})

watch(alias, (newVal) => {
  const lowered = newVal.toLowerCase()
  if (lowered !== newVal) alias.value = lowered
  apiError.value = null
})

const hasChanges = computed(() => alias.value !== props.initialAlias)
const isValid = computed(() => alias.value === '' || COMMAND_NAME.test(alias.value))

const error = computed(() => {
  if (!isValid.value) return t('commandAliases.invalid')
  return apiError.value
})

const save = async (value: string) => {
  if (isUpdating.value) return
  isUpdating.value = true
  try {
    const stored = value === props.command ? '' : value
    await api.updateCommandAlias(props.command, stored)
    updateCommandAlias(props.command, stored)
    alias.value = stored
  } catch (err: any) {
    apiError.value = err.response?.status === 400
        ? badRequestMessage(err.response.data)
        : t('commandAliases.saveFailed')
  } finally {
    isUpdating.value = false
  }
}

const badRequestMessage = (data: any): string => {
  if (typeof data === 'string' && data) return data
  return data?.title ?? t('commandAliases.invalid')
}

const updateAlias = async () => {
  if (!hasChanges.value || !isValid.value) return
  await save(alias.value)
}

const resetAlias = async () => {
  await save('')
}
</script>

<template>
  <div>
    <label :for="`alias-${command}`" class="label mb-2">/{{ command }}</label>
    <div class="flex gap-2">
      <input
          :id="`alias-${command}`"
          v-model="alias"
          :disabled="isUpdating"
          :placeholder="command"
          class="input"
          maxlength="32"
          type="text"
          @keyup.enter="updateAlias"
      />
      <BaseButton
          v-if="hasChanges"
          :disabled="isUpdating || !isValid"
          color="primary"
          @click="updateAlias"
      >
        {{ isUpdating ? t('common.loading') : t('common.confirm') }}
      </BaseButton>
      <BaseButton
          :disabled="isUpdating || !initialAlias"
          :title="t('commandAliases.reset')"
          color="secondary"
          @click="resetAlias"
      >
        {{ t('commandAliases.reset') }}
      </BaseButton>
    </div>
    <p v-if="error" class="description mt-1 text-red-500 dark:text-red-400 font-medium">{{ error }}</p>
    <p class="description">{{ description }}</p>
  </div>
</template>
