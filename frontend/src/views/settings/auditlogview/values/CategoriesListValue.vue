/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed} from 'vue'
import {useI18n} from 'vue-i18n'
import CategoryDisplay from '@/components/display/CategoryDisplay.vue'

interface Props {
  categoryIds?: string[]
  oldCategoryIds?: string[]
  newCategoryIds?: string[]
}

const props = defineProps<Props>()
const {t} = useI18n()

const addedItems = computed(() => {
  if (!props.oldCategoryIds || !props.newCategoryIds) return []
  const oldSet = new Set(props.oldCategoryIds)
  return props.newCategoryIds.filter(id => !oldSet.has(id))
})

const removedItems = computed(() => {
  if (!props.oldCategoryIds || !props.newCategoryIds) return []
  const newSet = new Set(props.newCategoryIds)
  return props.oldCategoryIds.filter(id => !newSet.has(id))
})

const isComparison = computed(() => {
  return props.oldCategoryIds !== undefined && props.newCategoryIds !== undefined
})

const effectiveCategoryIds = computed(() => {
  if (props.categoryIds !== undefined) return props.categoryIds
  return props.newCategoryIds !== undefined ? props.newCategoryIds : props.oldCategoryIds || []
})
</script>

<template>
  <template v-if="isComparison">
    <div class="flex flex-col gap-2">
      <div v-if="addedItems.length > 0" class="flex items-center gap-2">
        <span class="text-green-600 dark:text-green-400 shrink-0">
          <font-awesome-icon :icon="['fas', 'plus']" class="h-5 w-5" />
        </span>
        <CategoriesListValue :category-ids="addedItems" />
      </div>
      <div v-if="removedItems.length > 0" class="flex items-center gap-2">
        <span class="text-red-600 dark:text-red-400 shrink-0">
          <font-awesome-icon :icon="['fas', 'minus']" class="h-5 w-5" />
        </span>
        <CategoriesListValue :category-ids="removedItems" />
      </div>
    </div>
  </template>
  <template v-else>
    <template v-if="effectiveCategoryIds.length === 0">
      <span class="text-gray-500 dark:text-gray-400 italic">{{ t('auditLog.values.none') }}</span>
    </template>
    <template v-else>
      <div class="flex flex-col">
        <CategoryDisplay
            v-for="categoryId in effectiveCategoryIds"
            :key="categoryId"
            :category-id="categoryId"
        />
      </div>
    </template>
  </template>
</template>
