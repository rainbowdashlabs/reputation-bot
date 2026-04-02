/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import FeatureCard from '@/components/landing/FeatureCard.vue'

const props = defineProps<{
  features: { icon: string[]; title: string; description: string }[]
}>()

const COLS = 3
const currentRow = ref(0)
const direction = ref<'up' | 'down'>('down')
const isHovered = ref(false)
let autoTimer: ReturnType<typeof setInterval> | null = null

const rows = computed(() => {
  const result = []
  for (let i = 0; i < props.features.length; i += COLS) {
    result.push(props.features.slice(i, i + COLS))
  }
  return result
})

const totalRows = computed(() => rows.value.length)

function navigate(dir: 'up' | 'down') {
  direction.value = dir
  if (dir === 'down') {
    currentRow.value = (currentRow.value + 1) % totalRows.value
  } else {
    currentRow.value = (currentRow.value - 1 + totalRows.value) % totalRows.value
  }
}

function prevRow() { navigate('up') }
function nextRow() { navigate('down') }

function onWheel(e: WheelEvent) {
  e.preventDefault()
  if (e.deltaY > 0) nextRow()
  else prevRow()
}

function startAuto() {
  autoTimer = setInterval(() => {
    if (!isHovered.value) nextRow()
  }, 5000)
}

function stopAuto() {
  if (autoTimer) { clearInterval(autoTimer); autoTimer = null }
}

onMounted(startAuto)
onUnmounted(stopAuto)

const peekAboveIndex = computed(() => (currentRow.value - 1 + totalRows.value) % totalRows.value)
const peekBelowIndex = computed(() => (currentRow.value + 1) % totalRows.value)

</script>

<template>
  <div
    class="flex flex-col items-center gap-3 w-full select-none"
    @mouseenter="isHovered = true"
    @mouseleave="isHovered = false"
  >
    <!-- Scroll Up Button -->
    <button
      @click="prevRow"
      class="w-10 h-10 rounded-full flex items-center justify-center transition-colors bg-indigo-600 hover:bg-indigo-700 text-white shadow"
    >
      <font-awesome-icon :icon="['fas', 'chevron-up']" />
    </button>

    <!-- Scrollable Window -->
    <div class="w-full overflow-hidden" @wheel.prevent="onWheel">
      <!-- Peek row above (bottom third visible) -->
      <div
        class="opacity-30 pointer-events-none mb-2 overflow-hidden"
        style="height: 2.5rem;"
      >
        <div
          class="grid grid-cols-3 gap-4"
          style="transform: translateY(calc(-100% + 2.5rem));"
        >
          <FeatureCard
            v-for="(card, i) in rows[peekAboveIndex]"
            :key="'peek-top-' + peekAboveIndex + '-' + i"
            :icon="card.icon"
            :title="card.title"
            :description="card.description"
          />
        </div>
      </div>

      <!-- Visible row with slide animation -->
      <div class="relative overflow-hidden">
        <!-- Invisible spacer to maintain container height -->
        <div class="grid grid-cols-3 gap-4 invisible" aria-hidden="true">
          <FeatureCard
            v-for="(card, i) in rows[currentRow]"
            :key="'spacer-' + currentRow + '-' + i"
            :icon="card.icon"
            :title="card.title"
            :description="card.description"
          />
        </div>
        <!-- Animated row on top -->
        <Transition :name="direction === 'down' ? 'slide-down' : 'slide-up'">
          <div
            :key="currentRow"
            class="grid grid-cols-3 gap-4 absolute inset-0"
          >
            <FeatureCard
              v-for="(card, i) in rows[currentRow]"
              :key="'row-' + currentRow + '-' + i"
              :icon="card.icon"
              :title="card.title"
              :description="card.description"
            />
          </div>
        </Transition>
      </div>

      <!-- Peek row below (top third visible) -->
      <div
        class="opacity-30 pointer-events-none mt-2 overflow-hidden"
        style="height: 2.5rem;"
      >
        <div class="grid grid-cols-3 gap-4">
          <FeatureCard
            v-for="(card, i) in rows[peekBelowIndex]"
            :key="'peek-bot-' + peekBelowIndex + '-' + i"
            :icon="card.icon"
            :title="card.title"
            :description="card.description"
          />
        </div>
      </div>
    </div>

    <!-- Scroll Down Button -->
    <button
      @click="nextRow"
      class="w-10 h-10 rounded-full flex items-center justify-center transition-colors bg-indigo-600 hover:bg-indigo-700 text-white shadow"
    >
      <font-awesome-icon :icon="['fas', 'chevron-down']" />
    </button>
  </div>
</template>

<style scoped>
.slide-down-enter-active,
.slide-down-leave-active,
.slide-up-enter-active,
.slide-up-leave-active {
  transition: transform 0.35s ease;
  position: absolute;
  width: 100%;
}

.slide-down-enter-from {
  transform: translateY(100%);
}
.slide-down-enter-to {
  transform: translateY(0);
}
.slide-down-leave-from {
  transform: translateY(0);
}
.slide-down-leave-to {
  transform: translateY(-100%);
}

.slide-up-enter-from {
  transform: translateY(-100%);
}
.slide-up-enter-to {
  transform: translateY(0);
}
.slide-up-leave-from {
  transform: translateY(0);
}
.slide-up-leave-to {
  transform: translateY(100%);
}
</style>
