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
import RoleSelect from '@/components/RoleSelect.vue'
import BaseButton from '@/components/BaseButton.vue'
import Header2 from "@/components/heading/Header2.vue"
import type {RankEntry} from '@/api/types'
import NumberInput from "@/components/NumberInput.vue";

const {t} = useI18n()
const {session, updateRanksSettings} = useSession()

const ranks = computed<RankEntry[]>(() => (session.value?.settings?.ranks?.ranks ?? []) as RankEntry[])

const newRoleId = ref<string | null>(null)
const newReputation = ref<number | null>(null)
const errorMessage = ref('')

const highestBotRolePosition = computed(() => {
  return session.value?.guild?.meta?.highestBotRole?.position ?? null
})

const validate = () => {
  if (newRoleId.value !== null) {
    if (ranks.value.some(r => r.roleId === newRoleId.value)) {
      errorMessage.value = t('general.ranks.roleAlreadyAdded')
      return
    }
  }

  if (newReputation.value !== null && !isNaN(newReputation.value)) {
    if (ranks.value.some(r => r.reputation === newReputation.value)) {
      errorMessage.value = t('general.ranks.reputationAlreadyUsed')
      return
    }
  }

  errorMessage.value = ''
}

watch([newRoleId, newReputation], validate)

const addRank = async () => {
  if (newRoleId.value === null || newReputation.value === null || isNaN(newReputation.value) || newReputation.value < 0 || !!errorMessage.value) return

  try {
    const nextRanks = [...ranks.value, {
      roleId: newRoleId.value,
      reputation: newReputation.value
    }]
    await api.updateRanks({ranks: nextRanks})
    updateRanksSettings({ranks: nextRanks})

    newRoleId.value = null
    newReputation.value = null
  } catch (e) {
    // Error is already handled by ApiClient interceptor and shown via errorStore
  }
}
</script>

<template>
  <div class="space-y-4">
    <Header2>{{ t('general.ranks.addRank') }}</Header2>
    <p class="text-sm text-gray-500 dark:text-gray-400">
      {{ t('general.ranks.selectionNote') }}
    </p>
    <div class="flex flex-col gap-4">
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <RoleSelect
            v-model="newRoleId"
            :disable-roles-above-position="highestBotRolePosition"
            :label="t('settings.roles')"
            @keyup.enter="addRank"
        />
        <div class="flex flex-col gap-1.5">
          <label class="label mb-1.5">{{ t('general.ranks.reputationRequired') }}</label>
          <NumberInput
              v-model="newReputation"
              :placeholder="t('general.ranks.reputationPlaceholder')"
              :min="0"
              :max="100000"
              @keyup.enter="addRank"
          />
        </div>
      </div>
      <div v-if="errorMessage" class="text-red-500 text-sm">
        {{ errorMessage }}
      </div>
      <div class="flex justify-end">
        <BaseButton
            :disabled="newRoleId === null || newReputation === null || !!errorMessage"
            color="primary"
            @click="addRank"
        >
          {{ t('common.confirm') }}
        </BaseButton>
      </div>
    </div>
  </div>
</template>
