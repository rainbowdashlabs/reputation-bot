<script lang="ts" setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useSession } from '@/composables/useSession'
import data from 'emoji-mart-vue-fast/data/all.json'
// @ts-ignore
import { Picker, EmojiIndex } from 'emoji-mart-vue-fast/src'
import 'emoji-mart-vue-fast/css/emoji-mart.css'

const props = defineProps<{
  modelValue: string
  disabled?: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const { session } = useSession()

const isOpen = ref(false)
const pickerRef = ref<HTMLElement | null>(null)

// Guild emojis from session
const guildEmojis = computed(() => session.value?.guild.reactions || [])

const emojiIndex = computed(() => {
  const custom = guildEmojis.value.map(emoji => ({
    id: emoji.name,
    name: emoji.name,
    short_names: [emoji.name],
    text: '',
    emoticons: [emoji.id.toString()],
    keywords: [emoji.name],
    imageUrl: emoji.url,
    custom: true
  }))
  const index = new EmojiIndex(data, {
    custom,
    include: ['custom', 'people', 'nature', 'foods', 'activity', 'places', 'objects', 'symbols', 'flags']
  })

  // Manual reorder because EmojiIndex.buildIndex pushes custom to the end and unshifts recent to the top.
  // We want: custom, recent, then others.
  const categories = (index as any)._categories
  const customIdx = categories.findIndex((c: any) => c.id === 'custom')
  if (customIdx !== -1) {
    const customCat = categories.splice(customIdx, 1)[0]
    categories.unshift(customCat)
  }

  return index
})

const selectEmoji = (emoji: any) => {
  if (emoji.custom) {
    // Custom guild emoji - ID is stored in emoticons array
    const snowflake = emoji.emoticons && emoji.emoticons[0] ? emoji.emoticons[0] : emoji.id
    emit('update:modelValue', snowflake)
  } else {
    // Standard unicode emoji
    emit('update:modelValue', emoji.native)
  }
  isOpen.value = false
}

const togglePicker = () => {
  if (props.disabled) return
  isOpen.value = !isOpen.value
}

const handleClickOutside = (event: MouseEvent) => {
  if (isOpen.value && pickerRef.value && !pickerRef.value.contains(event.target as Node)) {
    isOpen.value = false
  }
}

const getEmojiDisplay = (val: string) => {
  const guildEmoji = guildEmojis.value.find(e => e.id.toString() === val.toString())
  if (guildEmoji) return guildEmoji.url
  return val
}

const isGuildEmoji = (val: string) => {
  return guildEmojis.value.some(e => e.id.toString() === val.toString())
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<template>
  <div ref="pickerRef" class="relative inline-block">
    <button
        type="button"
        class="emoji-trigger flex items-center justify-center rounded p-0 bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors border border-gray-300 dark:border-gray-600"
        :disabled="disabled"
        @click.stop="togglePicker"
    >
      <img v-if="isGuildEmoji(modelValue)" :src="getEmojiDisplay(modelValue)" class="w-10 h-10 object-contain" alt="emoji"/>
      <span v-else class="text-3xl">{{ modelValue || '?' }}</span>
    </button>

    <div
        v-if="isOpen"
        class="absolute z-50 mt-2 shadow-xl"
        @click.stop
    >
      <Picker
          :data="emojiIndex"
          :native="true"
          :autoFocus="true"
          :showPreview="false"
          :showSkinTones="false"
          @select="selectEmoji"
      />
    </div>
  </div>
</template>

<style scoped>
@reference "@/style.css";

.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background-color: transparent;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: var(--color-gray-300);
  border-radius: 9999px;
}

.dark .custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: var(--color-gray-600);
}

.emoji-trigger:disabled {
  @apply opacity-50 cursor-not-allowed;
}

/* Ensure emoji-mart picker looks okay in dark mode */
:deep(.emoji-mart) {
  @apply border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100;
}

:deep(.emoji-mart-emoji) {
  padding: 6px !important;
  box-sizing: content-box;
  display: inline-flex !important;
  align-items: center;
  justify-content: center;
}

:deep(.emoji-mart-emoji span) {
  display: inline-block;
  width: 24px !important;
  height: 24px !important;
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
}

:deep(.emoji-mart-emoji .emoji-mart-emoji-custom) {
  width: 100% !important;
  height: 100% !important;
}

:deep(.emoji-mart-category-label span) {
  @apply bg-white dark:bg-gray-800 text-gray-500 dark:text-gray-400;
}

:deep(.emoji-mart-search input) {
  @apply input;
}

:deep(.emoji-mart-bar) {
  @apply border-gray-200 dark:border-gray-700;
}
</style>
