/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed, ref, watch} from 'vue'
import {useI18n} from 'vue-i18n'
import {useSession} from '@/composables/useSession'
import BaseButton from '@/components/BaseButton.vue'
import {api} from '@/api'

const props = defineProps<{ maxTokens: number | null }>()
const emit = defineEmits<{ (e: 'transferred'): void }>()

const { t } = useI18n()
const { userSession } = useSession()

const selectedGuild = ref<string>('')
const amount = ref<number>(1)
const busy = ref(false)

const guilds = computed(() => {
  if (!userSession.value) return []
  return Object.values(userSession.value.guilds).sort((a, b) => a.name.localeCompare(b.name))
})

watch(() => props.maxTokens, (val) => {
  if (val !== null && amount.value > val) amount.value = Math.max(1, val)
})

const submit = async () => {
  if (!selectedGuild.value || amount.value <= 0) return
  // Cap by maxTokens if provided
  const finalAmount = props.maxTokens !== null ? Math.min(amount.value, props.maxTokens) : amount.value
  busy.value = true
  try {
    await api.transferTokensToGuild(selectedGuild.value, finalAmount)
    emit('transferred')
    amount.value = 1
  } catch (e) {
    // errors are handled globally via interceptor
  } finally {
    busy.value = false
  }
}
</script>

<template>
    <h3 class="text-base font-semibold text-gray-800 dark:text-gray-100 mb-1">{{ t('voting.transfer.title') }}</h3>
    <p class="text-sm text-gray-500 dark:text-gray-400 mb-4">{{ t('voting.transfer.description') }}</p>
    <div class="flex flex-col sm:flex-row gap-4 items-end">
      <div class="flex-1 w-full space-y-1.5">
        <label class="block text-sm font-medium text-gray-700 dark:text-gray-200">{{ t('voting.transfer.guild') }}</label>
        <select v-model="selectedGuild" class="block w-full rounded-lg border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm transition-colors">
          <option disabled value="">{{ t('voting.transfer.selectGuild') }}</option>
          <option v-for="g in guilds" :key="g.id" :value="g.id">{{ g.name }}</option>
        </select>
        <div class="h-4"></div>
      </div>
      <div class="w-full sm:w-40 space-y-1.5">
        <label class="block text-sm font-medium text-gray-700 dark:text-gray-200">{{ t('voting.transfer.amount') }}</label>
        <input type="number" min="1" :max="props.maxTokens ?? undefined" v-model.number="amount" class="block w-full rounded-lg border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm transition-colors"/>
        <p v-if="props.maxTokens !== null" class="text-xs text-gray-500 dark:text-gray-400">
          {{ t('voting.transfer.max', { max: props.maxTokens }) }}
        </p>
        <div v-else class="h-4"></div>
      </div>
      <div class="mb-4">
        <BaseButton :disabled="busy || !selectedGuild || (props.maxTokens !== null && props.maxTokens <= 0)" class="px-4 py-2" color="primary" @click="submit">
          <font-awesome-icon :icon="['fas','coins']" class="w-4 mr-2"/>
          {{ busy ? t('common.loading') : t('voting.transfer.submit') }}
        </BaseButton>
      </div>
    </div>
</template>

<style scoped>
</style>
