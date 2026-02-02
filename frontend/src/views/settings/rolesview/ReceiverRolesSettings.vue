<script lang="ts" setup>
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useSession } from '@/composables/useSession'
import { api } from '@/api'
import Toggle from '@/components/Toggle.vue'
import RoleList from './RoleList.vue'

const { t } = useI18n()
const { session, updateThankingReceiverRolesSettings } = useSession()

const receiverRoles = computed(() => session.value?.settings?.thanking?.receiverRoles)
const availableRoles = computed(() => session.value?.guild?.roles || [])

const isEnabled = ref(false)

watch(receiverRoles, (newVal) => {
  if (newVal && newVal.roleIds.length > 0) {
    isEnabled.value = true
  }
}, { immediate: true })

const onToggleChange = async (value: boolean) => {
  isEnabled.value = value
  if (!value) {
    try {
      await api.updateThankingReceiverRoles({ roleIds: [] })
      updateThankingReceiverRolesSettings({ roleIds: [] })
    } catch (error) {
      console.error('Failed to clear receiver roles:', error)
      // Revert toggle if API call fails
      isEnabled.value = true
    }
  }
}

const updateRoles = async (newRoleIds: string[]) => {
  try {
    await api.updateThankingReceiverRoles({ roleIds: newRoleIds })
    updateThankingReceiverRolesSettings({ roleIds: newRoleIds })
  } catch (error) {
    console.error('Failed to update receiver roles:', error)
  }
}
</script>

<template>
  <div v-if="receiverRoles" class="space-y-6">
    <div class="flex flex-col gap-2">
      <Toggle
        v-model="isEnabled"
        :label="t('general.roles.receiver.label')"
        @update:model-value="onToggleChange"
      />
      <p class="description">{{ t('general.roles.receiver.description') }}</p>
    </div>

    <RoleList
      v-if="isEnabled"
      :selected-role-ids="receiverRoles.roleIds"
      :available-roles="availableRoles"
      @update:selected-role-ids="updateRoles"
    />
  </div>
</template>
