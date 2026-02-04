/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {useSession} from '@/composables/useSession'
import {computed, onMounted, onUnmounted, ref} from 'vue'
import {useI18n} from 'vue-i18n'
import BaseButton from '@/components/BaseButton.vue'

const {t} = useI18n()
const modelValue = defineModel<string | null>({required: true})
const emit = defineEmits(['select'])
const {session} = useSession()

interface Props {
  label?: string
  disabled?: boolean
  disableRolesAbovePosition?: number | null
}

const props = withDefaults(defineProps<Props>(), {
  label: '',
  disabled: false,
  disableRolesAbovePosition: null
})

const searchQuery = ref('')
const isSelecting = ref(false)
const containerRef = ref<HTMLElement | null>(null)

const roles = computed(() => {
  return session.value?.guild?.roles || []
})

const selectedRole = computed(() => {
  if (modelValue.value === null || modelValue.value === undefined || modelValue.value === '0') return null

  const stringId = modelValue.value
  return roles.value.find(r => r.id === stringId) || null
})

const filteredRoles = computed(() => {
  if (!searchQuery.value) return roles.value
  const query = searchQuery.value.toLowerCase()
  return roles.value.filter(r => r.name.toLowerCase().includes(query))
})

const startSelecting = () => {
  if (!props.disabled) {
    isSelecting.value = true
    searchQuery.value = ''
  }
}

const isRoleDisabled = (role: { position: number }) => {
  if (props.disableRolesAbovePosition === null || props.disableRolesAbovePosition === undefined) {
    return false
  }
  return role.position >= props.disableRolesAbovePosition
}

const selectRole = (id: string | number, role: any) => {
  if (isRoleDisabled(role)) return
  modelValue.value = typeof id === 'string' ? id : id.toString()
  isSelecting.value = false
  emit('select', modelValue.value)
}

const getRoleColor = (role: { color: string | number }) => {
  if (!role.color || role.color === '#000000' || role.color === '#ffffff' || role.color === '0' || role.color === 0) return 'inherit'
  if (typeof role.color === 'number') {
    if (role.color === 0xFFFFFF) return 'inherit'
    return '#' + role.color.toString(16).padStart(6, '0')
  }
  const colorStr = role.color.startsWith('#') ? role.color : `#${role.color}`
  return colorStr.toLowerCase() === '#ffffff' ? 'inherit' : colorStr
}

const handleClickOutside = (event: MouseEvent) => {
  if (containerRef.value && !containerRef.value.contains(event.target as Node)) {
    isSelecting.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<template>
  <div ref="containerRef" class="flex flex-col gap-1.5 relative">
    <label v-if="label" class="label mb-1.5">
      {{ label }}
    </label>

    <div class="relative">
      <!-- Select Mode -->
      <Transition
          enter-active-class="transition ease-out duration-100"
          enter-from-class="transform opacity-0 scale-95"
          enter-to-class="transform opacity-100 scale-100"
          leave-active-class="transition ease-in duration-75"
          leave-from-class="transform opacity-100 scale-100"
          leave-to-class="transform opacity-0 scale-95"
      >
        <div
            v-if="isSelecting"
            class="absolute z-[100] w-full mt-0 bg-white dark:bg-gray-800 shadow-xl rounded-md border border-gray-200 dark:border-gray-700 overflow-hidden flex flex-col max-h-72"
        >
          <!-- Search Input -->
          <div class="p-2 border-b border-gray-100 dark:border-gray-700 bg-white dark:bg-gray-800">
            <input
                v-model="searchQuery"
                :placeholder="t('general.roleSelect.searchPlaceholder')"
                autofocus
                class="input py-1 text-sm"
                type="text"
                @click.stop
            />
          </div>

          <!-- Options List -->
          <div class="overflow-y-auto custom-scrollbar bg-white dark:bg-gray-800">
            <BaseButton
                v-for="role in filteredRoles"
                :key="role.id"
                :class="{
                  'bg-indigo-50 dark:bg-indigo-900/50 font-medium': modelValue?.toString() === role.id.toString(),
                  'hover:bg-gray-100 dark:hover:bg-indigo-900/30': modelValue?.toString() !== role.id.toString() && !isRoleDisabled(role),
                  'opacity-50 cursor-not-allowed': isRoleDisabled(role)
                }"
                :disabled="isRoleDisabled(role)"
                :rounded="false"
                class="w-full text-left pl-4 pr-3 py-2 text-sm transition-colors flex items-center gap-2"
                color="secondary"
                style="background-color: transparent; border: none; box-shadow: none;"
                @click="selectRole(role.id, role)"
            >
              <div
                  :style="{ backgroundColor: getRoleColor(role) }"
                  class="ml-2 w-3 h-3 rounded-full shrink-0"
              ></div>
              <span :style="{ color: getRoleColor(role) }" class="truncate">{{ role.name }}</span>
            </BaseButton>

            <div v-if="filteredRoles.length === 0" class="px-3 py-4 text-center text-sm text-gray-500">
              {{ t('general.roleSelect.noRolesFound') }}
            </div>
          </div>
        </div>
      </Transition>

      <!-- Display Mode -->
      <div
          v-if="!isSelecting"
          class="input flex items-center min-h-[38px] w-full truncate cursor-pointer hover:border-indigo-400 dark:hover:border-indigo-500 transition-colors"
          @click.stop="startSelecting"
      >
        <div v-if="selectedRole" class="flex items-center gap-2 truncate">
          <div
              :style="{ backgroundColor: getRoleColor(selectedRole) }"
              class="pl-2 w-3 h-3 rounded-full shrink-0"
          ></div>
          <span :style="{ color: getRoleColor(selectedRole) }" class="truncate pl-2">
            {{ selectedRole.name }}
          </span>
        </div>
        <span v-else class="text-gray-500">
          {{ t('general.roleSelect.searchPlaceholder') }}
        </span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #d1d5db; /* gray-300 */
  border-radius: 9999px;
}

.dark .custom-scrollbar::-webkit-scrollbar-thumb {
  background: #4b5563; /* gray-600 */
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #9ca3af; /* gray-400 */
}

.dark .custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #6b7280; /* gray-500 */
}
</style>
