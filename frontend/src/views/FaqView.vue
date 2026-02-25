/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import { onMounted, ref } from 'vue'
import { api } from '@/api'

interface FaqItem {
  question: string
  answer: string
  open: boolean
}

const faqs = ref<FaqItem[]>([])
const loading = ref(true)
const error = ref<string | null>(null)

onMounted(async () => {
  try {
    const html = await api.getAsset('faq')
    parseFaq(html)
  } catch (e) {
    console.error('Failed to load FAQ:', e)
    error.value = 'Failed to load FAQ information.'
  } finally {
    loading.value = false
  }
})

function parseFaq(html: string) {
  const container = document.createElement('div')
  container.innerHTML = html
  
  // Find all h3 tags which are usually questions in the markdown
  const headers = container.querySelectorAll('h3')
  const items: FaqItem[] = []
  
  headers.forEach((header) => {
    const question = header.textContent || ''
    let answer = ''
    let next = header.nextElementSibling
    
    while (next && next.tagName !== 'H3') {
      answer += next.outerHTML
      next = next.nextElementSibling
    }
    
    items.push({
      question,
      answer,
      open: false
    })
  })
  
  faqs.value = items
}

function toggleFaq(index: number) {
  const item = faqs.value[index]
  if (item) {
    item.open = !item.open
  }
}
</script>

<template>
  <div class="container mx-auto px-4 py-8 max-w-4xl">
    <h1 class="text-3xl font-bold mb-8 text-gray-900 dark:text-white border-b pb-2 border-gray-200 dark:border-gray-700">
      Frequently Asked Questions
    </h1>

    <div v-if="loading" class="flex justify-center py-12">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600"></div>
    </div>
    <div v-else-if="error" class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
      {{ error }}
    </div>
    <div v-else class="space-y-4">
      <div v-for="(item, index) in faqs" :key="index" class="bg-white dark:bg-gray-800 rounded-lg shadow border border-gray-200 dark:border-gray-700 overflow-hidden">
        <button 
          @click="toggleFaq(index)"
          class="w-full text-left px-6 py-4 flex justify-between items-center hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors focus:outline-none"
        >
          <span class="text-lg font-semibold text-gray-900 dark:text-white">{{ item.question }}</span>
          <font-awesome-icon 
            :icon="['fas', item.open ? 'chevron-up' : 'chevron-down']" 
            class="text-gray-500"
          />
        </button>
        <div 
          v-show="item.open"
          class="px-6 py-4 border-t border-gray-100 dark:border-gray-700 markdown-content"
          v-html="item.answer"
        >
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
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
