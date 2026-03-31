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
import ViewContainer from '@/components/ViewContainer.vue'

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
import SetupScanStep from './setup/SetupScanStep.vue'
import SetupFinishedStep from './setup/SetupFinishedStep.vue'

const {t} = useI18n()
const router = useRouter()
const route = useRoute()
const {session, loadSettings, settingsLoading, settingsError} = useSession()

// Watch session and load settings as soon as session becomes available
watch(session, (newSession) => {
  if (newSession) {
    loadSettings()
  }
}, {immediate: true})

const SETUP_STEP_KEY = 'reputation-bot-setup-current-step'
const totalSteps = 14

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
  {id: 1, component: SetupWelcomeStep, titleKey: 'setup.steps.welcome.title', required: false, requiresSettings: false},
  {id: 2, component: SetupLanguageStep, titleKey: 'setup.steps.language.title', required: false, requiresSettings: true},
  {id: 3, component: SetupSystemChannelStep, titleKey: 'setup.steps.systemChannel.title', required: true, requiresSettings: true},
  {id: 4, component: SetupReputationTypesStep, titleKey: 'setup.steps.reputationTypes.title', required: false, requiresSettings: true},
  {id: 5, component: SetupReputationModeStep, titleKey: 'setup.steps.reputationMode.title', required: false, requiresSettings: true},
  {id: 6, component: SetupRanksStep, titleKey: 'setup.steps.ranks.title', required: true, requiresSettings: true},
  {id: 7, component: SetupThankwordsStep, titleKey: 'setup.steps.thankwords.title', required: false, requiresSettings: true},
  {id: 8, component: SetupChannelsStep, titleKey: 'setup.steps.channels.title', required: true, requiresSettings: true},
  {id: 9, component: SetupScanStep, titleKey: 'setup.steps.scan.title', required: false, requiresSettings: false},
  {id: 10, component: SetupAnnouncementsStep, titleKey: 'setup.steps.announcements.title', required: false, requiresSettings: true},
  {id: 11, component: SetupCooldownStep, titleKey: 'setup.steps.cooldown.title', required: false, requiresSettings: true},
  {id: 12, component: SetupMainReactionStep, titleKey: 'setup.steps.mainReaction.title', required: false, requiresSettings: true},
  {id: 13, component: SetupRolesStep, titleKey: 'setup.steps.roles.title', required: false, requiresSettings: true},
  {id: 14, component: SetupFinishedStep, titleKey: 'setup.steps.finished.title', required: false, requiresSettings: false}
]

const currentStepData = computed(() => steps.find(s => s.id === currentStep.value))

const canProceed = ref(true)
const scanStarted = ref(false)

const updateCanProceed = (value: boolean) => {
  canProceed.value = value
}

const onScanStarted = (value: boolean) => {
  scanStarted.value = value
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

  if (scanStarted.value) {
    router.push('/settings/edit/scan')
  } else {
    // Navigate to settings after setup is complete
    router.push('/settings/edit')
  }
}

const progressPercentage = computed(() => {
  return (currentStep.value / totalSteps) * 100
})
</script>

<template>
  <div>
    <!-- Navigation Bar -->
    <div class="bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700 sticky top-[73px] z-10">
      <ViewContainer class="py-4">
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
      </ViewContainer>
    </div>

    <!-- Step Content -->
    <ViewContainer class="py-8">
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

          <div v-if="currentStepData?.requiresSettings && settingsLoading" class="flex items-center justify-center py-12">
            <span class="text-gray-500 dark:text-gray-400">{{ t('common.loading') }}</span>
          </div>

          <div v-else-if="currentStepData?.requiresSettings && settingsError === 'forbidden'" class="rounded-lg bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 p-6">
            <p class="text-red-700 dark:text-red-400 font-medium">{{ t('setup.error.forbidden.title') }}</p>
            <p class="text-red-600 dark:text-red-300 text-sm mt-1">{{ t('setup.error.forbidden.description') }}</p>
          </div>

          <div v-else-if="currentStepData?.requiresSettings && settingsError" class="rounded-lg bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 p-6">
            <p class="text-red-700 dark:text-red-400 font-medium">{{ t('setup.error.unknown.title') }}</p>
            <p class="text-red-600 dark:text-red-300 text-sm mt-1">{{ t('setup.error.unknown.description') }}</p>
          </div>

          <div v-else-if="currentStepData?.requiresSettings && !session" class="rounded-lg bg-yellow-50 dark:bg-yellow-900/20 border border-yellow-200 dark:border-yellow-800 p-6">
            <p class="text-yellow-700 dark:text-yellow-400 font-medium">{{ t('setup.error.noSession.title') }}</p>
            <p class="text-yellow-600 dark:text-yellow-300 text-sm mt-1">{{ t('setup.error.noSession.description') }}</p>
          </div>

          <component
              v-else-if="currentStepData && (!currentStepData.requiresSettings || (session && session.settings))"
              :is="currentStepData.component"
              @can-proceed="updateCanProceed"
              @scan-started="onScanStarted"
          />
        </div>
      </div>
    </ViewContainer>
  </div>
</template>

<style scoped>
/* Additional styles if needed */
</style>
