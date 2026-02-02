<script lang="ts" setup>
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import RoleSelect from '@/components/RoleSelect.vue'
import BaseButton from '@/components/BaseButton.vue'
import Header2 from "@/components/heading/Header2.vue"
import type { RankEntry } from '@/api/types'

const props = defineProps<{
  existingRanks: RankEntry[]
}>()

const emit = defineEmits<{
  (e: 'add', rank: RankEntry): void
}>()

const { t } = useI18n()

const newRoleId = ref<number | null>(null)
const newReputation = ref<number | null>(null)
const errorMessage = ref('')

const validate = () => {
  if (newRoleId.value !== null) {
    const roleIdStr = newRoleId.value.toString()
    if (props.existingRanks.some(r => r.roleId.toString() === roleIdStr)) {
      errorMessage.value = t('general.ranks.roleAlreadyAdded')
      return
    }
  }

  if (newReputation.value !== null && !isNaN(newReputation.value)) {
    if (props.existingRanks.some(r => r.reputation === newReputation.value)) {
      errorMessage.value = t('general.ranks.reputationAlreadyUsed')
      return
    }
  }

  errorMessage.value = ''
}

watch([newRoleId, newReputation], validate)

const addRank = () => {
  if (newRoleId.value === null || newReputation.value === null || isNaN(newReputation.value) || newReputation.value < 0 || !!errorMessage.value) return

  const roleIdStr = newRoleId.value.toString()
  emit('add', {
    roleId: roleIdStr,
    reputation: newReputation.value
  })

  newRoleId.value = null
  newReputation.value = null
}
</script>

<template>
  <div class="space-y-4">
    <Header2>{{ t('general.ranks.addRank') }}</Header2>
    <div class="flex flex-col gap-4">
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <RoleSelect
            v-model="newRoleId"
            :label="t('settings.roles')"
            @keyup.enter="addRank"
        />
        <div class="flex flex-col gap-1.5">
          <label class="label mb-1.5">{{ t('general.ranks.reputationRequired') }}</label>
          <input
              v-model.number="newReputation"
              type="number"
              class="input"
              min="0"
              :placeholder="t('general.ranks.reputationPlaceholder')"
              @keyup.enter="addRank"
          />
        </div>
      </div>
      <div v-if="errorMessage" class="text-red-500 text-sm">
        {{ errorMessage }}
      </div>
      <div class="flex justify-end">
        <BaseButton
            color="primary"
            :disabled="newRoleId === null || newReputation === null || !!errorMessage"
            @click="addRank"
        >
          {{ t('common.confirm') }}
        </BaseButton>
      </div>
    </div>
  </div>
</template>
