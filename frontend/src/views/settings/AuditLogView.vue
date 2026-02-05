/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {ref, onMounted, onUnmounted, computed} from 'vue'
import {useI18n} from 'vue-i18n'
import {api} from '@/api'
import type {AuditLogPagePOJO, MemberPOJO} from '@/api/types'
import SettingsContainer from '@/views/settings/components/SettingsContainer.vue'
import AuditLogEntry from './auditlogview/AuditLogEntry.vue'

const {t} = useI18n()

const auditLogPage = ref<AuditLogPagePOJO | null>(null)
const currentPage = ref(0)
const entriesPerPage = ref(25)
const loading = ref(false)
const error = ref<string | null>(null)

// Convert Map to object for easier access
const membersMap = computed(() => {
  if (!auditLogPage.value?.members) return {}
  // The backend returns a Map, but it comes as an object in JSON
  return auditLogPage.value.members as unknown as Record<string, MemberPOJO>
})

const getMember = (memberId: string): MemberPOJO | undefined => {
  return membersMap.value[memberId]
}

const loadAuditLog = async (page: number = 0) => {
  loading.value = true
  error.value = null
  try {
    auditLogPage.value = await api.getAuditLog(page, entriesPerPage.value)
    currentPage.value = page
  } catch (err) {
    console.error('Failed to load audit log:', err)
    error.value = t('auditLog.loadError')
  } finally {
    loading.value = false
  }
}

const goToPage = (page: number) => {
  if (page >= 0 && auditLogPage.value && page < auditLogPage.value.maxPages) {
    loadAuditLog(page)
  }
}

const nextPage = () => {
  if (auditLogPage.value && currentPage.value < auditLogPage.value.maxPages - 1) {
    goToPage(currentPage.value + 1)
  }
}

const previousPage = () => {
  if (currentPage.value > 0) {
    goToPage(currentPage.value - 1)
  }
}

const handleKeyDown = (event: KeyboardEvent) => {
  // Only handle arrow keys when not typing in an input field
  if (event.target instanceof HTMLInputElement || event.target instanceof HTMLTextAreaElement) {
    return
  }

  if (event.key === 'ArrowLeft') {
    previousPage()
  } else if (event.key === 'ArrowRight') {
    nextPage()
  }
}

onMounted(() => {
  loadAuditLog(0)
  window.addEventListener('keydown', handleKeyDown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeyDown)
})
</script>

<template>
  <SettingsContainer :title="t('settings.auditLog')" :description="t('auditLog.description')">
    <!-- Loading state -->
    <div v-if="loading" class="text-center py-8">
      <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600"></div>
      <p class="mt-2 text-gray-600 dark:text-gray-400">{{ t('auditLog.loading') }}</p>
    </div>

    <!-- Error state -->
    <div v-else-if="error" class="text-center py-8">
      <p class="text-red-600 dark:text-red-400">{{ error }}</p>
      <button
          @click="loadAuditLog(currentPage)"
          class="mt-4 px-4 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700 transition-colors"
      >
        {{ t('auditLog.retry') }}
      </button>
    </div>

    <!-- Audit log entries -->
    <div v-else-if="auditLogPage && auditLogPage.content" class="space-y-6">
      <!-- Empty state -->
      <div v-if="auditLogPage.content.length === 0" class="text-center py-8">
        <p class="text-gray-600 dark:text-gray-400">{{ t('auditLog.noEntries') }}</p>
      </div>

      <!-- Entries list -->
      <div v-else class="space-y-4">
        <AuditLogEntry
            v-for="(log, index) in auditLogPage.content"
            :key="`${log.memberId}-${log.changed}-${index}`"
            :log="log"
            :member="getMember(log.memberId)"
        />
      </div>

      <!-- Pagination -->
      <div v-if="auditLogPage.maxPages > 1" class="flex items-center justify-between pt-4 border-t border-gray-200 dark:border-gray-700">
        <button
            @click="previousPage"
            :disabled="currentPage === 0"
            class="px-4 py-2 bg-white dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded border border-gray-300 dark:border-gray-600 hover:bg-gray-50 dark:hover:bg-gray-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
        >
          {{ t('auditLog.previous') }}
        </button>

        <span class="text-sm text-gray-600 dark:text-gray-400">
          {{ t('auditLog.pageInfo', { current: currentPage + 1, total: auditLogPage.maxPages }) }}
        </span>

        <button
            @click="nextPage"
            :disabled="currentPage >= auditLogPage.maxPages - 1"
            class="px-4 py-2 bg-white dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded border border-gray-300 dark:border-gray-600 hover:bg-gray-50 dark:hover:bg-gray-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
        >
          {{ t('auditLog.next') }}
        </button>
      </div>
    </div>
  </SettingsContainer>
</template>

<style scoped>
/* Additional styles if needed */
</style>
