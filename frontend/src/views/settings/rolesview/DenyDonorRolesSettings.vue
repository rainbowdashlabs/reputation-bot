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
import Toggle from '@/components/Toggle.vue'
import RoleList from './RoleList.vue'

const {t} = useI18n()
const {session, updateThankingDenyDonorRolesSettings} = useSession()

const denyDonorRoles = computed(() => session.value?.settings?.thanking?.denyDonorRoles)
const availableRoles = computed(() => session.value?.guild?.roles || [])

const isEnabled = ref(false)

watch(denyDonorRoles, (newVal) => {
  if (newVal && newVal.roleIds.length > 0) {
    isEnabled.value = true
  }
}, {immediate: true})

const onToggleChange = async (value: boolean) => {
  isEnabled.value = value
  if (!value) {
    try {
      await api.updateThankingDenyDonorRoles({roleIds: []})
      updateThankingDenyDonorRolesSettings({roleIds: []})
    } catch (error) {
      console.error('Failed to clear deny donor roles:', error)
      // Revert toggle if API call fails
      isEnabled.value = true
    }
  }
}

const updateRoles = async (newRoleIds: string[]) => {
  try {
    await api.updateThankingDenyDonorRoles({roleIds: newRoleIds})
    updateThankingDenyDonorRolesSettings({roleIds: newRoleIds})
  } catch (error) {
    console.error('Failed to update deny donor roles:', error)
  }
}
</script>

<template>
  <div v-if="denyDonorRoles" class="space-y-6">
    <div class="flex flex-col gap-2">
      <Toggle
          v-model="isEnabled"
          :label="t('general.roles.denyDonor.label')"
          @update:model-value="onToggleChange"
      />
      <p class="description">{{ t('general.roles.denyDonor.description') }}</p>
    </div>

    <RoleList
        v-if="isEnabled"
        :available-roles="availableRoles"
        :selected-role-ids="denyDonorRoles.roleIds"
        @update:selected-role-ids="updateRoles"
    />
  </div>
</template>
