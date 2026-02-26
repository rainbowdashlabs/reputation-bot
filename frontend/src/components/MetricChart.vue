/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<template>
  <div class="bg-white dark:bg-gray-800 p-4 rounded-lg shadow-md">
    <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center mb-4 gap-2">
      <h3 class="text-lg font-semibold text-gray-800 dark:text-gray-200">{{ title }}</h3>
      <div class="flex items-center gap-2 text-sm">
        <select
            v-if="availableUnits.length > 1"
            v-model="internalUnit"
            class="bg-gray-50 border border-gray-300 text-gray-900 rounded-lg focus:ring-blue-500 focus:border-blue-500 p-1 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
            @change="onUnitChange"
        >
          <option v-for="unit in availableUnits" :key="unit" :value="unit">
            {{ $t(`metrics.units.${unit}`) }}
          </option>
        </select>
        <div class="flex items-center gap-1">
          <label class="dark:text-gray-300">{{ $t('metrics.offset') }}:</label>
          <input
              v-model.number="internalOffset"
              type="number"
              min="0"
              :max="maxOffset"
              :class="['w-16 bg-gray-50 border text-gray-900 rounded-lg focus:ring-blue-500 focus:border-blue-500 p-1 dark:bg-gray-700 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500', offsetError ? 'border-red-500 dark:border-red-500' : 'border-gray-300 dark:border-gray-600']"
              @change="onOffsetChange"
          />
        </div>
        <div v-if="showCount" class="flex items-center gap-1">
          <label class="dark:text-gray-300">{{ $t('metrics.count') }}:</label>
          <input
              v-model.number="internalCount"
              type="number"
              min="2"
              :max="maxCount"
              :class="['w-16 bg-gray-50 border text-gray-900 rounded-lg focus:ring-blue-500 focus:border-blue-500 p-1 dark:bg-gray-700 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500', countError ? 'border-red-500 dark:border-red-500' : 'border-gray-300 dark:border-gray-600']"
              @change="onCountChange"
          />
        </div>
      </div>
    </div>
    <div v-if="offsetError || countError" class="mb-2 text-xs text-red-500">
      <span v-if="offsetError">{{ offsetError }}</span>
      <span v-if="countError">{{ countError }}</span>
    </div>

    <div class="h-64 relative">
      <div v-if="loading" class="absolute inset-0 flex items-center justify-center bg-white/50 dark:bg-gray-800/50 z-10">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
      </div>
      <v-chart
          v-if="chartOption"
          :option="chartOption"
          autoresize
          class="h-full w-full"
      />
      <div v-else-if="!loading" class="h-full flex items-center justify-center text-gray-500">
        {{ $t('common.noData') }}
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {computed, onMounted, ref, watch} from 'vue'
import {use} from 'echarts/core'
import {CanvasRenderer} from 'echarts/renderers'
import {LineChart, BarChart, PieChart} from 'echarts/charts'
import {
  GridComponent,
  TooltipComponent,
  LegendComponent,
  DataZoomComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import type {ActiveUsersStatistic, CommandsStatistic, CountsStatistic, DowsStatistic, LabeledCountStatistic, MetricLimits} from '@/api/types'
import {useI18n} from 'vue-i18n'

use([CanvasRenderer, LineChart, BarChart, PieChart, GridComponent, TooltipComponent, LegendComponent, DataZoomComponent])

const {t} = useI18n()

const props = defineProps<{
  title: string
  chartType: 'line' | 'bar'
  fetchFn: (unit: any, offset: number, count: number) => Promise<any>
  unit: string
  availableUnits?: string[]
  offset: number
  count?: number
  showCount?: boolean
  limits?: MetricLimits
}>()

const internalUnit = ref(props.unit)
const internalOffset = ref(props.offset)
const internalCount = ref(props.count || 24)
const loading = ref(false)
const rawData = ref<CountsStatistic | LabeledCountStatistic | DowsStatistic | ActiveUsersStatistic | CommandsStatistic | null>(null)

const availableUnits = computed(() => props.availableUnits || [props.unit])

const maxOffset = computed(() => {
  if (!props.limits) return undefined
  const unit = internalUnit.value
  if (unit === 'hour') return props.limits.maxHourOffset
  if (unit === 'day') return props.limits.maxDayOffset
  if (unit === 'week') return props.limits.maxWeekOffset
  if (unit === 'month') return props.limits.maxMonthOffset
  if (unit === 'year') return props.limits.maxYearOffset
  return undefined
})

const maxCount = computed(() => {
  if (!props.limits) return undefined
  const unit = internalUnit.value
  if (unit === 'hour') return props.limits.maxHours
  if (unit === 'day') return props.limits.maxDays
  if (unit === 'week') return props.limits.maxWeeks
  if (unit === 'month') return props.limits.maxMonths
  return undefined
})

const offsetError = computed(() => {
  if (maxOffset.value === undefined) return null
  if (internalOffset.value < 0) return t('metrics.validation.offsetMin')
  if (internalOffset.value > maxOffset.value) return t('metrics.validation.offsetMax', {max: maxOffset.value})
  return null
})

const countError = computed(() => {
  if (!props.showCount) return null
  if (maxCount.value === undefined) return null
  if (internalCount.value < 2) return t('metrics.validation.countMin')
  if (internalCount.value > maxCount.value) return t('metrics.validation.countMax', {max: maxCount.value})
  return null
})

const textColor = 'rgb(156, 163, 175)'
const gridLineColor = 'rgba(156, 163, 175, 0.1)'
const colors = ['#3b82f6', '#ef4444', '#10b981', '#f59e0b', '#8b5cf6', '#ec4899']

function hashName(name: string): number {
  let hash = 0
  for (let i = 0; i < name.length; i++) {
    hash = (hash * 31 + name.charCodeAt(i)) >>> 0
  }
  return hash
}

function colorFromName(name: string): string {
  const hash = hashName(name)
  const h = hash % 360
  return `hsl(${h}, 65%, 55%)`
}

function seriesColor(label: string, index: number): string {
  switch (label.toLowerCase()) {
    case 'failed':
    case 'removed':
      return '#ef4444'
    case 'success':
    case 'added':
      return '#10b981'
    default:
      return colors[index % colors.length] ?? '#3b82f6'
  }
}

const chartOption = computed(() => {
  if (!rawData.value) return null

  if ('commands' in rawData.value) {
    // CommandsStatistic - pie chart
    const data = rawData.value as CommandsStatistic
    if (!data.commands || data.commands.length === 0) return null
    return {
      tooltip: {trigger: 'item', formatter: '{b}: {c} ({d}%)'},
      series: [{
        type: 'pie',
        radius: ['30%', '60%'],
        center: ['50%', '50%'],
        data: data.commands.map((c) => ({
          name: c.command,
          value: c.count,
          itemStyle: {color: colorFromName(c.command)}
        })),
        label: {color: textColor, formatter: '{b}\n{d}%'},
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }]
    }
  }

  if (!('stats' in rawData.value)) return null

  const stats = (rawData.value as any).stats

  if (!Array.isArray(stats)) {
    // LabeledCountStatistic
    const labeledStats = (rawData.value as LabeledCountStatistic).stats
    const entries = Object.entries(labeledStats)
    if (entries.length === 0) return null

    const series = entries.map(([label, data], index) => {
      const color = seriesColor(label, index)
      return {
        name: label,
        type: 'line' as const,
        data: (data as any[]).map(s => [s.date, s.count]),
        smooth: false,
        lineStyle: {color},
        itemStyle: {color},
        areaStyle: {color: color + '20'}
      }
    })

    return {
      tooltip: {trigger: 'axis'},
      legend: {
        bottom: 0,
        textStyle: {color: textColor}
      },
      grid: {left: 40, right: 20, top: 20, bottom: 40},
      xAxis: {
        type: 'time',
        axisLabel: {color: textColor},
        splitLine: {lineStyle: {color: gridLineColor}}
      },
      yAxis: {
        type: 'value',
        axisLabel: {color: textColor},
        splitLine: {lineStyle: {color: gridLineColor}}
      },
      series
    }
  }

  if (stats.length === 0) return null

  if ('dow' in stats[0]) {
    // DowsStatistic
    const days = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday']
    return {
      tooltip: {trigger: 'axis'},
      grid: {left: 40, right: 20, top: 20, bottom: 40},
      xAxis: {
        type: 'category',
        data: stats.map((s: any) => days[s.dow - 1]),
        axisLabel: {color: textColor},
        splitLine: {lineStyle: {color: gridLineColor}}
      },
      yAxis: {
        type: 'value',
        axisLabel: {color: textColor},
        splitLine: {lineStyle: {color: gridLineColor}}
      },
      series: [{
        type: 'bar',
        data: stats.map((s: any) => s.count),
        itemStyle: {color: '#3b82f6'}
      }]
    }
  }

  if ('donors' in stats[0]) {
    // ActiveUsersStatistic
    return {
      tooltip: {trigger: 'axis'},
      legend: {
        bottom: 0,
        textStyle: {color: textColor}
      },
      grid: {left: 40, right: 20, top: 20, bottom: 40},
      xAxis: {
        type: 'time',
        axisLabel: {color: textColor},
        splitLine: {lineStyle: {color: gridLineColor}}
      },
      yAxis: {
        type: 'value',
        axisLabel: {color: textColor},
        splitLine: {lineStyle: {color: gridLineColor}}
      },
      series: [
        {
          name: t('metrics.users.total'),
          type: 'line' as const,
          data: stats.map((s: any) => [s.date, s.total]),
          lineStyle: {color: colors[0]},
          itemStyle: {color: colors[0]},
          areaStyle: {color: colors[0] + '20'}
        },
        {
          name: t('metrics.users.donors'),
          type: 'line' as const,
          data: stats.map((s: any) => [s.date, s.donors]),
          lineStyle: {color: colors[1]},
          itemStyle: {color: colors[1]},
          areaStyle: {color: colors[1] + '20'}
        },
        {
          name: t('metrics.users.receivers'),
          type: 'line' as const,
          data: stats.map((s: any) => [s.date, s.receivers]),
          lineStyle: {color: colors[2]},
          itemStyle: {color: colors[2]},
          areaStyle: {color: colors[2] + '20'}
        }
      ]
    }
  }

  // CountsStatistic (time series)
  const seriesType = props.chartType === 'bar' ? 'bar' : 'line'
  return {
    tooltip: {trigger: 'axis'},
    grid: {left: 40, right: 20, top: 20, bottom: 40},
    xAxis: {
      type: 'time',
      axisLabel: {color: textColor},
      splitLine: {lineStyle: {color: gridLineColor}}
    },
    yAxis: {
      type: 'value',
      axisLabel: {color: textColor},
      splitLine: {lineStyle: {color: gridLineColor}}
    },
    series: [{
      name: props.title,
      type: seriesType,
      data: stats.map((s: any) => [s.date, s.count]),
      ...(seriesType === 'line' ? {
        lineStyle: {color: '#3b82f6'},
        itemStyle: {color: '#3b82f6'},
        areaStyle: {color: 'rgba(59, 130, 246, 0.1)'}
      } : {
        itemStyle: {color: '#3b82f6'}
      })
    }]
  }
})

function validate(): boolean {
  if (offsetError.value || countError.value) return false
  return true
}

async function fetchData() {
  if (!validate()) return
  loading.value = true
  try {
    rawData.value = await props.fetchFn(internalUnit.value, internalOffset.value, internalCount.value)
  } catch (error) {
    console.error('Failed to fetch chart data:', error)
  } finally {
    loading.value = false
  }
}

function onUnitChange() {
  if (maxOffset.value !== undefined) {
    internalOffset.value = Math.min(internalOffset.value, maxOffset.value)
    internalOffset.value = Math.max(internalOffset.value, 0)
  }
  if (maxCount.value !== undefined) {
    internalCount.value = Math.min(internalCount.value, maxCount.value)
    internalCount.value = Math.max(internalCount.value, 2)
  }
  fetchData()
}

function onOffsetChange() {
  if (!offsetError.value) fetchData()
}

function onCountChange() {
  if (!countError.value) fetchData()
}

onMounted(fetchData)

watch(() => props.unit, (newVal) => { internalUnit.value = newVal; fetchData() })
watch(() => props.offset, (newVal) => { internalOffset.value = newVal; fetchData() })
watch(() => props.count, (newVal) => { internalCount.value = newVal || 24; fetchData() })
</script>
