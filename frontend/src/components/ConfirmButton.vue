/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {onUnmounted, ref} from 'vue'
import {useI18n} from 'vue-i18n'

const {t} = useI18n()

interface Props {
  disabled?: boolean
  label: string
  confirmLabel?: string
  icon?: string
  confirmIcon?: string
  baseClass?: string
  activeClass?: string
  timeout?: number
}

const props = withDefaults(defineProps<Props>(), {
  disabled: false,
  confirmLabel: '',
  icon: '',
  confirmIcon: 'check',
  baseClass: 'bg-indigo-600 hover:bg-indigo-700 focus:ring-indigo-500 text-white',
  activeClass: 'bg-red-600 hover:bg-red-700 focus:ring-red-500 text-white',
  timeout: 10000
})

const emit = defineEmits<{
  (e: 'confirm'): void
}>()

const showConfirm = ref(false)
let timer: ReturnType<typeof setTimeout> | null = null

const handleClick = () => {
  if (showConfirm.value) {
    emit('confirm')
    cancelConfirm()
  } else {
    showConfirm.value = true
    timer = setTimeout(() => {
      cancelConfirm()
    }, props.timeout)
  }
}

const cancelConfirm = () => {
  showConfirm.value = false
  if (timer) {
    clearTimeout(timer)
    timer = null
  }
}

onUnmounted(() => {
  cancelConfirm()
})

defineExpose({
  cancelConfirm
})
</script>

<template>
  <button
    @click="handleClick"
    :disabled="disabled"
    :class="[
      'inline-flex justify-center items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transition-colors',
      showConfirm ? activeClass : baseClass
    ]"
  >
    <font-awesome-icon v-if="showConfirm ? (confirmIcon || icon) : icon" :icon="showConfirm ? (confirmIcon || icon) : icon" :class="((showConfirm ? (confirmLabel || t('common.confirm')) : label)) ? 'mr-2' : ''" />
    {{ showConfirm ? (confirmLabel || t('common.confirm')) : label }}
  </button>
</template>
