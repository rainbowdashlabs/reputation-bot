/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed, ref, watch} from 'vue'
import {useI18n} from 'vue-i18n'
import {useRoute, useRouter} from 'vue-router'
import {useSession} from '@/composables/useSession'

// Import step components
import SetupWelcomeStep from './setup/SetupWelcomeStep.vue'
import SetupLanguageStep from './setup/SetupLanguageStep.vue'
import SetupSystemChannelStep from './setup/SetupSystemChannelStep.vue'
import SetupReputationTypesStep from './setup/SetupReputationTypesStep.vue'
import SetupReputationModeStep from './setup/SetupReputationModeStep.vue'
import SetupRanksStep from './setup/SetupRanksStep.vue'
import SetupChannelsStep from './setup/SetupChannelsStep.vue'
import SetupThankwordsStep from './setup/SetupThankwordsStep.vue'
import SetupAnnouncementsStep from './setup/SetupAnnouncementsStep.vue'
import SetupCooldownStep from './setup/SetupCooldownStep.vue'
import SetupMainReactionStep from './setup/SetupMainReactionStep.vue'
import SetupRolesStep from './setup/SetupRolesStep.vue'
import SetupFinishedStep from './setup/SetupFinishedStep.vue'

const {t} = useI18n()
const router = useRouter()
const route = useRoute()
const {session} = useSession()

const SETUP_STEP_KEY = 'reputation-bot-setup-current-step'
const totalSteps = 13

// Initialize currentStep from URL query, then localStorage, or default to 1
const getInitialStep = (): number => {
  // First, try to get step from URL query parameter
  const urlStep = route.query.step
  if (urlStep) {
    const step = parseInt(String(urlStep), 10)
    if (step >= 1 && step <= totalSteps) {
      return step
    }
  }

  // Fall back to localStorage
  try {
    const saved = localStorage.getItem(SETUP_STEP_KEY)
    if (saved) {
      const step = parseInt(saved, 10)
      if (step >= 1 && step <= totalSteps) {
        return step
      }
    }
  } catch (error) {
    console.error('Failed to load saved setup step:', error)
  }

  return 1
}

const currentStep = ref(getInitialStep())

// Update URL and localStorage whenever currentStep changes
watch(currentStep, (newStep) => {
  // Update URL query parameter
  router.replace({
    path: '/setup',
    query: {step: String(newStep)}
  })

  // Save to localStorage as backup
  try {
    localStorage.setItem(SETUP_STEP_KEY, String(newStep))
  } catch (error) {
    console.error('Failed to save setup step:', error)
  }
})

// Watch for URL changes (e.g., browser back/forward)
watch(() => route.query.step, (newStepQuery) => {
  if (newStepQuery) {
    const step = parseInt(String(newStepQuery), 10)
    if (step >= 1 && step <= totalSteps && step !== currentStep.value) {
      currentStep.value = step
    }
  }
})

const steps = [
  {id: 1, component: SetupWelcomeStep, titleKey: 'setup.steps.welcome.title', required: false},
  {id: 2, component: SetupLanguageStep, titleKey: 'setup.steps.language.title', required: false},
  {id: 3, component: SetupSystemChannelStep, titleKey: 'setup.steps.systemChannel.title', required: true},
  {id: 4, component: SetupReputationTypesStep, titleKey: 'setup.steps.reputationTypes.title', required: false},
  {id: 5, component: SetupReputationModeStep, titleKey: 'setup.steps.reputationMode.title', required: false},
  {id: 6, component: SetupRanksStep, titleKey: 'setup.steps.ranks.title', required: true},
  {id: 7, component: SetupChannelsStep, titleKey: 'setup.steps.channels.title', required: true},
  {id: 8, component: SetupThankwordsStep, titleKey: 'setup.steps.thankwords.title', required: false},
  {id: 9, component: SetupAnnouncementsStep, titleKey: 'setup.steps.announcements.title', required: false},
  {id: 10, component: SetupCooldownStep, titleKey: 'setup.steps.cooldown.title', required: false},
  {id: 11, component: SetupMainReactionStep, titleKey: 'setup.steps.mainReaction.title', required: false},
  {id: 12, component: SetupRolesStep, titleKey: 'setup.steps.roles.title', required: false},
  {id: 13, component: SetupFinishedStep, titleKey: 'setup.steps.finished.title', required: false}
]

const currentStepData = computed(() => steps.find(s => s.id === currentStep.value))

const canProceed = ref(true)

const updateCanProceed = (value: boolean) => {
  canProceed.value = value
}

const goToPreviousStep = () => {
  if (currentStep.value > 1) {
    currentStep.value--
  }
}

const goToNextStep = () => {
  if (currentStep.value < totalSteps && canProceed.value) {
    currentStep.value++
  }
}

const finishSetup = () => {
  // Clear saved step from localStorage
  try {
    localStorage.removeItem(SETUP_STEP_KEY)
  } catch (error) {
    console.error('Failed to clear saved setup step:', error)
  }
  // Navigate to settings after setup is complete
  router.push('/settings')
}

const progressPercentage = computed(() => {
  return (currentStep.value / totalSteps) * 100
})
</script>

<template>
  <div class="bg-gray-50 dark:bg-gray-900">
    <!-- Navigation Bar -->
    <div class="bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700 sticky top-20 z-10">
      <div class="container mx-auto px-4 py-4">
        <div class="flex items-center justify-between mb-4">
          <div class="flex-1">
            <h1 class="text-2xl font-bold text-gray-900 dark:text-gray-100">
              {{ t('setup.title') }}
            </h1>
            <p class="text-sm text-gray-600 dark:text-gray-400 mt-1">
              {{ t('setup.stepProgress', {current: currentStep, total: totalSteps}) }}
            </p>
          </div>

          <div class="flex items-center gap-3">
            <button
                :disabled="currentStep === 1"
                class="px-4 py-2 text-sm font-medium rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-300 dark:hover:bg-gray-600"
                @click="goToPreviousStep"
            >
              {{ t('setup.navigation.previous') }}
            </button>

            <button
                v-if="currentStep < totalSteps"
                :disabled="!canProceed"
                class="px-4 py-2 text-sm font-medium rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed bg-indigo-600 text-white hover:bg-indigo-700"
                @click="goToNextStep"
            >
              {{ t('setup.navigation.next') }}
            </button>

            <button
                v-else
                class="px-4 py-2 text-sm font-medium rounded-lg transition-colors bg-green-600 text-white hover:bg-green-700"
                @click="finishSetup"
            >
              {{ t('setup.navigation.finish') }}
            </button>
          </div>
        </div>

        <!-- Progress Bar -->
        <div class="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-2">
          <div
              :style="{ width: `${progressPercentage}%` }"
              class="bg-indigo-600 h-2 rounded-full transition-all duration-300"
          />
        </div>
      </div>
    </div>

    <!-- Step Content -->
    <div class="container mx-auto px-4 py-8">
      <div class="max-w-4xl mx-auto">
        <div class="bg-white dark:bg-gray-800 rounded-lg shadow-lg p-6 md:p-8">
          <div class="mb-6">
            <h2 class="text-xl font-semibold text-gray-900 dark:text-gray-100 mb-2">
              {{ currentStepData ? t(currentStepData.titleKey) : '' }}
            </h2>
            <p v-if="currentStepData?.required" class="text-sm text-red-600 dark:text-red-400">
              {{ t('setup.requiredStep') }}
            </p>
          </div>

          <component
              :is="currentStepData?.component"
              v-if="currentStepData && session"
              @can-proceed="updateCanProceed"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Additional styles if needed */
</style>
