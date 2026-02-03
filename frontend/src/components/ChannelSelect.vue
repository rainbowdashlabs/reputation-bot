<script setup lang="ts">
import { useSession } from '@/composables/useSession'
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import BaseButton from '@/components/BaseButton.vue'

const { t } = useI18n()
const modelValue = defineModel<number | null>({ required: true })

// Helper function to get icon for channel type
const getChannelIcon = (type: string) => {
  switch (type) {
    case 'TEXT':
      return 'hashtag'
    case 'VOICE':
      return 'volume-high'
    case 'NEWS':
      return 'bullhorn'
    case 'FORUM':
      return 'comments'
    default:
      return 'hashtag'
  }
}

interface Props {
  label?: string
  disabled?: boolean
  placeholder?: string
  allowClear?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  label: '',
  disabled: false,
  placeholder: 'Select a channel',
  allowClear: false
})

const { session } = useSession()
const isSelecting = ref(false)
const searchQuery = ref('')
const containerRef = ref<HTMLElement | null>(null)

const categories = computed(() => {
  return session.value?.guild?.channels?.categories || []
})

const uncategorizedChannels = computed(() => {
  return session.value?.guild?.channels?.channels || []
})

const selectedChannel = computed(() => {
  if (modelValue.value === null || modelValue.value === undefined || modelValue.value === 0) return null
  
  const stringId = modelValue.value.toString()
  
  // Combine all channels from categories and uncategorized
  const allChannels = [
    ...uncategorizedChannels.value,
    ...categories.value.flatMap(cat => cat.channels)
  ]
  
  return allChannels.find(c => c.id.toString() === stringId) || null
})

const filteredUncategorized = computed(() => {
  if (!searchQuery.value) return uncategorizedChannels.value
  const query = searchQuery.value.toLowerCase()
  return uncategorizedChannels.value.filter(c => c.name.toLowerCase().includes(query))
})

const filteredCategories = computed(() => {
  if (!searchQuery.value) return categories.value
  const query = searchQuery.value.toLowerCase()
  return categories.value.map(cat => ({
    ...cat,
    channels: cat.channels.filter(c => c.name.toLowerCase().includes(query))
  })).filter(cat => cat.channels.length > 0)
})

const startSelecting = () => {
  if (!props.disabled) {
    isSelecting.value = true
    searchQuery.value = ''
  }
}

const selectChannel = (id: string) => {
  modelValue.value = parseInt(id)
  isSelecting.value = false
}

const clearChannel = () => {
  modelValue.value = 0
  isSelecting.value = false
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
          class="relative z-50 w-full mt-0 bg-white dark:bg-gray-800 shadow-xl rounded-md border border-gray-200 dark:border-gray-700 overflow-hidden flex flex-col max-h-72"
        >
          <!-- Search Input -->
          <div class="p-2 border-b border-gray-100 dark:border-gray-700 bg-white dark:bg-gray-800">
            <input
              v-model="searchQuery"
              type="text"
              class="input py-1 text-sm"
              :placeholder="t('general.systemChannel.selectPlaceholder')"
              @click.stop
              autofocus
            />
          </div>

          <!-- Options List -->
          <div class="overflow-y-auto custom-scrollbar bg-white dark:bg-gray-800">
            <!-- Uncategorized Channels -->
            <BaseButton
              v-for="channel in filteredUncategorized"
              :key="channel.id"
              @click="selectChannel(channel.id.toString())"
              class="w-full text-left px-3 py-2 text-sm transition-colors flex gap-2"
              :class="{ 'bg-indigo-50 dark:bg-indigo-900/50 text-indigo-700 dark:text-indigo-300 font-medium': modelValue?.toString() === channel.id.toString(), 'hover:bg-gray-100 dark:hover:bg-indigo-900/30 text-gray-700 dark:text-gray-300': modelValue?.toString() !== channel.id.toString() }"
              :rounded="false"
              color="secondary"
              style="background-color: transparent; border: none; box-shadow: none; color: inherit;"
            >
              <font-awesome-icon :icon="getChannelIcon(channel.type)" class="text-gray-400 dark:text-gray-500" />
              <span class="truncate">{{ channel.name }}</span>
            </BaseButton>

            <!-- Grouped Channels -->
            <div v-for="category in filteredCategories" :key="category.id">
              <div class="px-3 py-1.5 text-xs font-bold text-gray-500 dark:text-gray-400 uppercase tracking-wider bg-gray-50/80 dark:bg-gray-700/50 border-y border-gray-100 dark:border-gray-700/50">
                {{ category.name }}
              </div>
              <BaseButton
                v-for="channel in category.channels"
                :key="channel.id"
                @click="selectChannel(channel.id.toString())"
                class="w-full text-left px-3 py-2 text-sm transition-colors gap-2 pl-6"
                :class="{ 'bg-indigo-50 dark:bg-indigo-900/50 text-indigo-700 dark:text-indigo-300 font-medium': modelValue?.toString() === channel.id.toString(), 'hover:bg-gray-100 dark:hover:bg-indigo-900/30 text-gray-700 dark:text-gray-300': modelValue?.toString() !== channel.id.toString() }"
                :rounded="false"
                color="indigo"
                style="background-color: transparent; border: none; box-shadow: none; color: inherit;"
              >
                <font-awesome-icon :icon="getChannelIcon(channel.type)" class="text-gray-400 dark:text-gray-500" />
                <span class="truncate">{{ channel.name }}</span>
              </BaseButton>
            </div>

            <div v-if="filteredUncategorized.length === 0 && filteredCategories.length === 0" class="px-3 py-4 text-center text-sm text-gray-500">
              No channels found
            </div>
          </div>
        </div>
      </Transition>

      <!-- Display Mode -->
      <div v-if="!isSelecting" class="flex items-center gap-2">
        <div class="input flex items-center min-h-[38px] flex-1 truncate">
          <span v-if="selectedChannel" class="truncate flex items-center gap-2">
            <font-awesome-icon :icon="getChannelIcon(selectedChannel.type)" class="text-gray-400 dark:text-gray-500" />
            {{ selectedChannel.name }}
          </span>
          <span v-else class="text-gray-500">
            {{ placeholder }}
          </span>
        </div>
        <BaseButton
          type="button"
          @click.stop="startSelecting"
          :disabled="disabled"
          color="secondary"
          class="shrink-0 px-3 py-1.5"
        >
          {{ t('common.change') }}
        </BaseButton>
        <BaseButton
          v-if="allowClear && selectedChannel"
          type="button"
          @click="clearChannel"
          :disabled="disabled"
          color="danger"
          class="shrink-0 px-3 py-1.5"
        >
          {{ t('common.clear') }}
        </BaseButton>
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
