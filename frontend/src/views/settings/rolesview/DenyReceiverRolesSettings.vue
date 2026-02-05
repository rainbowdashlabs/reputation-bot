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
const {session, updateThankingDenyReceiverRolesSettings} = useSession()

const denyReceiverRoles = computed(() => session.value?.settings?.thanking?.denyReceiverRoles)
const availableRoles = computed(() => session.value?.guild?.roles || [])

const isEnabled = ref(false)

watch(denyReceiverRoles, (newVal) => {
  if (newVal && newVal.roleIds.length > 0) {
    isEnabled.value = true
  }
}, {immediate: true})

const onToggleChange = async (value: boolean) => {
  isEnabled.value = value
  if (!value) {
    try {
      await api.updateThankingDenyReceiverRoles({roleIds: []})
      updateThankingDenyReceiverRolesSettings({roleIds: []})
    } catch (error) {
      console.error('Failed to clear deny receiver roles:', error)
      // Revert toggle if API call fails
      isEnabled.value = true
    }
  }
}

const updateRoles = async (newRoleIds: string[]) => {
  try {
    await api.updateThankingDenyReceiverRoles({roleIds: newRoleIds})
    updateThankingDenyReceiverRolesSettings({roleIds: newRoleIds})
  } catch (error) {
    console.error('Failed to update deny receiver roles:', error)
  }
}
</script>

<template>
  <div v-if="denyReceiverRoles" class="space-y-6">
    <div class="flex flex-col gap-2">
      <Toggle
          v-model="isEnabled"
          :label="t('general.roles.denyReceiver.label')"
          @update:model-value="onToggleChange"
      />
      <p class="description">{{ t('general.roles.denyReceiver.description') }}</p>
    </div>

    <RoleList
        v-if="isEnabled"
        :available-roles="availableRoles"
        :selected-role-ids="denyReceiverRoles.roleIds"
        @update:selected-role-ids="updateRoles"
    />
  </div>
</template>
