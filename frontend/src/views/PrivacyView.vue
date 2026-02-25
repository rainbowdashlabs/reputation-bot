/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import { onMounted, ref } from 'vue'
import { api } from '@/api'

const html = ref('')
const loading = ref(true)
const error = ref<string | null>(null)

onMounted(async () => {
  try {
    html.value = await api.getAsset('privacy')
  } catch (e) {
    console.error('Failed to load Privacy:', e)
    error.value = 'Failed to load Privacy information.'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="container mx-auto px-4 py-8 max-w-4xl">
    <div v-if="loading" class="flex justify-center py-12">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600"></div>
    </div>
    <div v-else-if="error" class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
      {{ error }}
    </div>
    <div v-else class="markdown-content max-w-none bg-white dark:bg-gray-800 p-8 rounded-lg shadow border border-gray-200 dark:border-gray-700" v-html="html">
    </div>
  </div>
</template>

<style scoped>
.markdown-content :deep(h1) {
  font-size: 1.875rem;
  line-height: 2.25rem;
  font-weight: 700;
  margin-bottom: 1.5rem;
  border-bottom-width: 1px;
  padding-bottom: 0.5rem;
  --tw-text-opacity: 1;
  color: rgb(17 24 39 / var(--tw-text-opacity));
}

.dark .markdown-content :deep(h1) {
  --tw-text-opacity: 1;
  color: rgb(255 255 255 / var(--tw-text-opacity));
  border-color: rgb(55 65 81 / var(--tw-text-opacity));
}

.markdown-content :deep(h2) {
  font-size: 1.5rem;
  line-height: 2rem;
  font-weight: 600;
  margin-top: 2rem;
  margin-bottom: 1rem;
  color: rgb(31 41 55);
}

.dark .markdown-content :deep(h2) {
  color: rgb(243 244 246);
}

.markdown-content :deep(h3) {
  font-size: 1.25rem;
  line-height: 1.75rem;
  font-weight: 600;
  margin-top: 1.5rem;
  margin-bottom: 0.75rem;
  color: rgb(31 41 55);
}

.dark .markdown-content :deep(h3) {
  color: rgb(229 231 235);
}

.markdown-content :deep(p) {
  margin-bottom: 1rem;
  color: rgb(75 85 99);
  line-height: 1.625;
}

.dark .markdown-content :deep(p) {
  color: rgb(209 213 219);
}

.markdown-content :deep(ul) {
  list-style-type: disc;
  list-style-position: inside;
  margin-bottom: 1rem;
  color: rgb(75 85 99);
}

.dark .markdown-content :deep(ul) {
  color: rgb(209 213 219);
}

.markdown-content :deep(li) {
  margin-bottom: 0.25rem;
}

.markdown-content :deep(strong) {
  font-weight: 600;
  color: rgb(17 24 39);
}

.dark .markdown-content :deep(strong) {
  color: rgb(255 255 255);
}

.markdown-content :deep(a) {
  color: rgb(79 70 229);
  text-decoration-line: none;
}

.markdown-content :deep(a:hover) {
  text-decoration-line: underline;
}

.dark .markdown-content :deep(a) {
  color: rgb(129 140 248);
}
</style>
