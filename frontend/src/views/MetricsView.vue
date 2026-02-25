/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<template>
  <ViewContainer>
    <div class="max-w-6xl mx-auto px-4 py-8">
      <div class="mb-8">
        <Header1>{{ $t('metrics.title') }}</Header1>
        <p class="text-gray-600 dark:text-gray-400 mt-2">
          {{ $t('metrics.description') }}
        </p>
      </div>

      <div class="space-y-12">
        <!-- Reputation Section -->
        <section>
          <Header2 class="mb-6 border-b border-gray-200 dark:border-gray-700 pb-2">
            {{ $t('metrics.sections.reputation') }}
          </Header2>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <MetricChart
                :title="$t('metrics.charts.reputationCount')"
                chart-type="line"
                :fetch-fn="api.getMetricsReputationCount.bind(api)"
                unit="month"
                :available-units="['week', 'month']"
                :offset="1"
                :count="24"
                show-count
                :limits="limits"
            />
            <MetricChart
                :title="$t('metrics.charts.reputationTotal')"
                chart-type="line"
                :fetch-fn="api.getMetricsReputationTotal.bind(api)"
                unit="month"
                :available-units="['week', 'month']"
                :offset="0"
                :count="24"
                show-count
                :limits="limits"
            />
            <MetricChart
                :title="$t('metrics.charts.reputationDow')"
                chart-type="bar"
                :fetch-fn="api.getMetricsReputationDow.bind(api)"
                unit="month"
                :available-units="['week', 'month', 'year']"
                :offset="1"
                :limits="limits"
            />
            <MetricChart
                :title="$t('metrics.charts.reputationTypeTotal')"
                chart-type="line"
                :fetch-fn="api.getMetricsReputationTypeTotal.bind(api)"
                unit="month"
                :available-units="['week', 'month']"
                :offset="0"
                :count="48"
                show-count
                :limits="limits"
            />
            <MetricChart
                :title="$t('metrics.charts.reputationTypeCount')"
                chart-type="line"
                :fetch-fn="api.getMetricsReputationTypeCount.bind(api)"
                unit="month"
                :available-units="['week', 'month']"
                :offset="0"
                :count="48"
                show-count
                :limits="limits"
            />
            <MetricChart
                :title="$t('metrics.charts.reputationChanges')"
                chart-type="line"
                :fetch-fn="api.getMetricsReputationChanges.bind(api)"
                unit="week"
                :available-units="['week', 'month']"
                :offset="0"
                :count="52"
                show-count
                :limits="limits"
            />
          </div>
        </section>

        <!-- Commands Section -->
        <section>
          <Header2 class="mb-6 border-b border-gray-200 dark:border-gray-700 pb-2">
            {{ $t('metrics.sections.commands') }}
          </Header2>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <MetricChart
                :title="$t('metrics.charts.commandsCount')"
                chart-type="line"
                :fetch-fn="api.getMetricsCommandsCount.bind(api)"
                unit="week"
                :available-units="['week', 'month']"
                :offset="1"
                :count="52"
                show-count
                :limits="limits"
            />
            <MetricChart
                :title="$t('metrics.charts.commandsUsage')"
                chart-type="line"
                :fetch-fn="api.getMetricsCommandsUsage.bind(api)"
                unit="week"
                :available-units="['week', 'month']"
                :offset="1"
                :limits="limits"
            />
          </div>
        </section>

        <!-- Messages Section -->
        <section>
          <Header2 class="mb-6 border-b border-gray-200 dark:border-gray-700 pb-2">
            {{ $t('metrics.sections.messages') }}
          </Header2>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <MetricChart
                :title="$t('metrics.charts.messagesCount')"
                chart-type="line"
                :fetch-fn="api.getMetricsMessagesCount.bind(api)"
                unit="week"
                :available-units="['week', 'month']"
                :offset="1"
                :count="52"
                show-count
                :limits="limits"
            />
            <MetricChart
                :title="$t('metrics.charts.messagesTotal')"
                chart-type="line"
                :fetch-fn="api.getMetricsMessagesTotal.bind(api)"
                unit="week"
                :available-units="['week', 'month']"
                :offset="0"
                :count="52"
                show-count
                :limits="limits"
            />
          </div>
        </section>

        <!-- Users Section -->
        <section>
          <Header2 class="mb-6 border-b border-gray-200 dark:border-gray-700 pb-2">
            {{ $t('metrics.sections.users') }}
          </Header2>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <MetricChart
                :title="$t('metrics.charts.usersActiveWeek')"
                chart-type="line"
                :fetch-fn="api.getMetricsUsersActive.bind(api)"
                unit="week"
                :available-units="['week']"
                :offset="1"
                :count="52"
                show-count
                :limits="limits"
            />
            <MetricChart
                :title="$t('metrics.charts.usersActiveMonth')"
                chart-type="line"
                :fetch-fn="api.getMetricsUsersActive.bind(api)"
                unit="month"
                :available-units="['month']"
                :offset="1"
                :count="24"
                show-count
                :limits="limits"
            />
          </div>
        </section>

        <!-- Service Section -->
        <section>
          <Header2 class="mb-6 border-b border-gray-200 dark:border-gray-700 pb-2">
            {{ $t('metrics.sections.service') }}
          </Header2>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <MetricChart
                :title="$t('metrics.charts.serviceCountHour')"
                chart-type="line"
                :fetch-fn="api.getMetricsServiceCount.bind(api)"
                unit="hour"
                :available-units="['hour']"
                :offset="0"
                :count="120"
                show-count
                :limits="limits"
            />
            <MetricChart
                :title="$t('metrics.charts.serviceCountWeek')"
                chart-type="line"
                :fetch-fn="api.getMetricsServiceCount.bind(api)"
                unit="week"
                :available-units="['week']"
                :offset="0"
                :count="104"
                show-count
                :limits="limits"
            />
          </div>
        </section>
      </div>
    </div>
  </ViewContainer>
</template>

<script lang="ts" setup>
import {onMounted, ref} from 'vue'
import {api} from '@/api'
import ViewContainer from '@/components/ViewContainer.vue'
import Header1 from '@/components/heading/Header1.vue'
import Header2 from '@/components/heading/Header2.vue'
import MetricChart from '@/components/MetricChart.vue'
import type {MetricLimits} from '@/api/types'

const limits = ref<MetricLimits | undefined>(undefined)

onMounted(async () => {
  try {
    limits.value = await api.getMetricsLimits()
  } catch (e) {
    console.error('Failed to fetch metric limits', e)
  }
})
</script>
