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
            @change="fetchData"
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
              class="w-16 bg-gray-50 border border-gray-300 text-gray-900 rounded-lg focus:ring-blue-500 focus:border-blue-500 p-1 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
              @change="fetchData"
          />
        </div>
        <div v-if="showCount" class="flex items-center gap-1">
          <label class="dark:text-gray-300">{{ $t('metrics.count') }}:</label>
          <input
              v-model.number="internalCount"
              type="number"
              min="1"
              class="w-16 bg-gray-50 border border-gray-300 text-gray-900 rounded-lg focus:ring-blue-500 focus:border-blue-500 p-1 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
              @change="fetchData"
          />
        </div>
      </div>
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
import {LineChart, BarChart} from 'echarts/charts'
import {
  GridComponent,
  TooltipComponent,
  LegendComponent,
  DataZoomComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import type {CountsStatistic, DowsStatistic, LabeledCountStatistic} from '@/api/types'

use([CanvasRenderer, LineChart, BarChart, GridComponent, TooltipComponent, LegendComponent, DataZoomComponent])

const props = defineProps<{
  title: string
  chartType: 'line' | 'bar'
  fetchFn: (unit: any, offset: number, count: number) => Promise<any>
  unit: string
  availableUnits?: string[]
  offset: number
  count?: number
  showCount?: boolean
}>()

const internalUnit = ref(props.unit)
const internalOffset = ref(props.offset)
const internalCount = ref(props.count || 24)
const loading = ref(false)
const rawData = ref<CountsStatistic | LabeledCountStatistic | DowsStatistic | null>(null)

const availableUnits = computed(() => props.availableUnits || [props.unit])

const textColor = 'rgb(156, 163, 175)'
const gridLineColor = 'rgba(156, 163, 175, 0.1)'
const colors = ['#3b82f6', '#ef4444', '#10b981', '#f59e0b', '#8b5cf6', '#ec4899']

const chartOption = computed(() => {
  if (!rawData.value) return null

  if (!('stats' in rawData.value)) return null

  const stats = (rawData.value as any).stats

  if (!Array.isArray(stats)) {
    // LabeledCountStatistic
    const labeledStats = (rawData.value as LabeledCountStatistic).stats
    const entries = Object.entries(labeledStats)
    if (entries.length === 0) return null

    const series = entries.map(([label, data], index) => ({
      name: label,
      type: 'line' as const,
      data: (data as any[]).map(s => [s.date, s.count]),
      smooth: false,
      lineStyle: {color: colors[index % colors.length]},
      itemStyle: {color: colors[index % colors.length]},
      areaStyle: {color: colors[index % colors.length] + '20'}
    }))

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

async function fetchData() {
  loading.value = true
  try {
    rawData.value = await props.fetchFn(internalUnit.value, internalOffset.value, internalCount.value)
  } catch (error) {
    console.error('Failed to fetch chart data:', error)
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)

watch(() => props.unit, (newVal) => { internalUnit.value = newVal; fetchData() })
watch(() => props.offset, (newVal) => { internalOffset.value = newVal; fetchData() })
watch(() => props.count, (newVal) => { internalCount.value = newVal || 24; fetchData() })
</script>
