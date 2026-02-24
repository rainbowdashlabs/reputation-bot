/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import { computed, ref } from 'vue'
import type { KofiPurchasePOJO, SKU } from '@/api/types'

const props = defineProps<{
  purchase: KofiPurchasePOJO
  guilds: { id: string, name: string }[]
  skus: SKU[]
}>()

const emit = defineEmits<{
  (e: 'assign', id: number, guildId: string): void
  (e: 'unassign', id: number): void
}>()

const selectedGuild = ref<string>(props.purchase.guildId && props.purchase.guildId !== '0' ? props.purchase.guildId : (props.guilds[0]?.id || ''))


const assignedGuildName = computed(() => {
  if (!props.purchase.guildId || props.purchase.guildId === '0') return ''
  const g = props.guilds.find(g => g.id === props.purchase.guildId)
  return g?.name || props.purchase.guildId
})

const purchaseName = computed(() => {
  const sku = props.skus.find(s => s.id === props.purchase.skuId)
  return sku?.name || props.purchase.key || props.purchase.skuId
})

function onAssign() {
  if (!selectedGuild.value) return
  emit('assign', props.purchase.id, selectedGuild.value)
}

function onUnassign() {
  emit('unassign', props.purchase.id)
}
</script>

<template>
  <div class="p-3 border border-gray-200 dark:border-gray-700 rounded-lg bg-white dark:bg-gray-800 flex items-center justify-between gap-4">
    <div class="flex items-center gap-3 min-w-0">
      <div class="w-10 h-10 rounded-md bg-indigo-100 dark:bg-indigo-900/40 flex items-center justify-center">
        <font-awesome-icon :icon="['fas', 'shopping-cart']" class="text-indigo-600 dark:text-indigo-400" />
      </div>
      <div class="min-w-0">
        <div class="text-sm font-medium text-gray-900 dark:text-gray-100 truncate">{{ purchaseName }}</div>
        <div class="text-xs text-gray-500 dark:text-gray-400">
          <span v-if="purchase.type === 'Subscription'">
            {{ $t('user.supporter.item.subscription') }}
            <template v-if="purchase.expiresAt">
              • {{ $t('user.supporter.item.expires', { date: new Date(purchase.expiresAt).toLocaleDateString() }) }}
            </template>
          </span>
          <span v-else>
            {{ $t('user.supporter.item.lifetime') }}
          </span>
          <template v-if="purchase.guildId && purchase.guildId !== '0'">
            • {{ $t('user.supporter.item.assignedTo', { guild: assignedGuildName }) }}
          </template>
        </div>
      </div>
    </div>

    <div class="flex items-center gap-2">
      <select v-model="selectedGuild" class="rounded-md border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 text-sm">
        <option v-for="g in guilds" :key="g.id" :value="g.id">{{ g.name }}</option>
      </select>
      <button @click="onAssign" class="px-3 py-1.5 text-sm rounded-md bg-indigo-600 text-white hover:bg-indigo-700">
        {{ $t('user.supporter.item.assign') }}
      </button>
      <button v-if="purchase.guildId && purchase.guildId !== '0'" @click="onUnassign" class="px-3 py-1.5 text-sm rounded-md bg-gray-200 dark:bg-gray-700 text-gray-800 dark:text-gray-100 hover:bg-gray-300 dark:hover:bg-gray-600">
        {{ $t('user.supporter.item.unassign') }}
      </button>
    </div>
  </div>
</template>
