<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useSession } from '@/composables/useSession'
import { api } from '@/api'
import PremiumFeatureWarning from '@/components/PremiumFeatureWarning.vue'

const { t } = useI18n()
const { session } = useSession()

// Local state for form fields
const nickname = ref<string>('')
const profilePictureUrl = ref<string>('')
const reputationName = ref<string>('')

// Check if profile feature is unlocked
const isProfileUnlocked = computed(() => {
  return session.value?.premiumFeatures?.profile?.unlocked ?? false
})

// Check if locale overrides feature is unlocked (required for reputation name)
const isLocaleOverridesUnlocked = computed(() => {
  return session.value?.premiumFeatures?.localeOverrides?.unlocked ?? false
})

const profileRequiredSkus = computed(() => {
  return session.value?.premiumFeatures?.profile?.requiredSkus ?? []
})

const localeOverridesRequiredSkus = computed(() => {
  return session.value?.premiumFeatures?.localeOverrides?.requiredSkus ?? []
})

// Initialize form fields from session data
watch(session, (newSession) => {
  if (newSession?.settings?.profile) {
    nickname.value = newSession.settings.profile.nickname || ''
    profilePictureUrl.value = newSession.settings.profile.profilePictureUrl || ''
    reputationName.value = newSession.settings.profile.reputationName || ''
  }
}, { immediate: true })

// Auto-save functions with debounce
let nicknameTimeout: ReturnType<typeof setTimeout> | null = null
const updateNickname = () => {
  if (!isProfileUnlocked.value) return
  
  if (nicknameTimeout) clearTimeout(nicknameTimeout)
  nicknameTimeout = setTimeout(async () => {
    try {
      await api.updateProfileNickname(nickname.value || null)
    } catch (error) {
      console.error('Failed to update nickname:', error)
    }
  }, 500)
}

let reputationNameTimeout: ReturnType<typeof setTimeout> | null = null
const updateReputationName = () => {
  if (!isLocaleOverridesUnlocked.value) return
  
  if (reputationNameTimeout) clearTimeout(reputationNameTimeout)
  reputationNameTimeout = setTimeout(async () => {
    try {
      await api.updateProfileReputationName(reputationName.value || null)
    } catch (error) {
      console.error('Failed to update reputation name:', error)
    }
  }, 500)
}

// Reset functions
const resetNickname = async () => {
  if (!isProfileUnlocked.value) return
  
  try {
    await api.deleteProfileNickname()
    nickname.value = ''
    // Refresh session to get updated data
    const sessionData = await api.getSession()
    nickname.value = sessionData.settings.profile.nickname || ''
  } catch (error) {
    console.error('Failed to reset nickname:', error)
  }
}

const resetProfilePicture = async () => {
  if (!isProfileUnlocked.value) return
  
  try {
    await api.deleteProfilePicture()
    // Refresh session to get updated profile picture URL
    const sessionData = await api.getSession()
    profilePictureUrl.value = sessionData.settings.profile.profilePictureUrl || ''
  } catch (error) {
    console.error('Failed to reset profile picture:', error)
  }
}

const resetReputationName = async () => {
  if (!isLocaleOverridesUnlocked.value) return
  
  try {
    await api.deleteProfileReputationName()
    reputationName.value = ''
    // Refresh session to get updated data
    const sessionData = await api.getSession()
    reputationName.value = sessionData.settings.profile.reputationName || ''
  } catch (error) {
    console.error('Failed to reset reputation name:', error)
  }
}

// Profile picture upload
const fileInputRef = ref<HTMLInputElement | null>(null)
const isUploading = ref(false)

const triggerFileInput = () => {
  if (!isProfileUnlocked.value) return
  fileInputRef.value?.click()
}

const handleFileUpload = async (event: Event) => {
  if (!isProfileUnlocked.value) return
  
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  
  if (!file) return
  
  // Validate file type
  if (!file.type.startsWith('image/')) {
    console.error('Invalid file type. Please select an image.')
    return
  }
  
  // Validate file size (max 2MB)
  const maxSize = 2 * 1024 * 1024
  if (file.size > maxSize) {
    console.error('File too large. Maximum size is 2MB.')
    return
  }
  
  // Validate image dimensions (max 512x512)
  const img = new Image()
  const imageUrl = URL.createObjectURL(file)
  
  img.onload = async () => {
    URL.revokeObjectURL(imageUrl)
    
    if (img.width > 512 || img.height > 512) {
      console.error('Image dimensions too large. Maximum size is 512x512 pixels.')
      if (target) target.value = ''
      return
    }
    
    isUploading.value = true
    
    try {
      await api.updateProfilePicture(file)
      // Refresh the profile picture URL after successful upload
      // The backend will update Discord's avatar, so we need to reload session
      const sessionData = await api.getSession()
      profilePictureUrl.value = sessionData.settings.profile.profilePictureUrl || ''
    } catch (error) {
      console.error('Failed to upload profile picture:', error)
    } finally {
      isUploading.value = false
      // Reset file input
      if (target) target.value = ''
    }
  }
  
  img.onerror = () => {
    URL.revokeObjectURL(imageUrl)
    console.error('Failed to load image for validation.')
    if (target) target.value = ''
  }
  
  img.src = imageUrl
}
</script>

<template>
  <div>
    <h2 class="text-2xl font-bold mb-4">{{ t('settings.profile') }}</h2>
    
    <!-- Premium Feature Warning for Profile -->
    <PremiumFeatureWarning
      v-if="!isProfileUnlocked"
      :message="t('profile.premiumRequired.message')"
      :required-skus="profileRequiredSkus"
      variant="large"
    />

    <!-- Profile Settings Form -->
    <div class="bg-white shadow rounded-lg p-6">
      <div class="space-y-6">
        <!-- Nickname Field -->
        <div>
          <label for="nickname" class="block text-sm font-medium text-gray-700 mb-2">
            {{ t('profile.nickname.label') }}
          </label>
          <div class="flex gap-2">
            <input
              id="nickname"
              v-model="nickname"
              type="text"
              :disabled="!isProfileUnlocked"
              :placeholder="t('profile.nickname.placeholder')"
              class="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:bg-gray-100 disabled:cursor-not-allowed"
              @input="updateNickname"
            />
            <button
              type="button"
              :disabled="!isProfileUnlocked"
              @click="resetNickname"
              class="px-4 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-700 focus:ring-2 focus:ring-gray-500 focus:ring-offset-2 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors"
              :title="t('profile.nickname.reset')"
            >
              {{ t('profile.reset') }}
            </button>
          </div>
          <p class="mt-1 text-sm text-gray-500">{{ t('profile.nickname.description') }}</p>
        </div>

        <!-- Profile Picture Upload -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">
            {{ t('profile.profilePicture.label') }}
          </label>
          <div class="flex items-center space-x-4">
            <img 
              v-if="profilePictureUrl"
              :src="profilePictureUrl" 
              alt="Profile picture" 
              class="w-16 h-16 rounded-full object-cover border-2 border-gray-200"
              @error="() => {}"
            />
            <div v-else class="w-16 h-16 rounded-full bg-gray-200 flex items-center justify-center">
              <svg class="w-8 h-8 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
              </svg>
            </div>
            <div class="flex-1">
              <input
                ref="fileInputRef"
                type="file"
                accept="image/*"
                class="hidden"
                @change="handleFileUpload"
              />
              <div class="flex gap-2">
                <button
                  type="button"
                  :disabled="!isProfileUnlocked || isUploading"
                  @click="triggerFileInput"
                  class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors"
                >
                  <span v-if="isUploading">{{ t('profile.profilePicture.uploading') }}</span>
                  <span v-else>{{ t('profile.profilePicture.uploadButton') }}</span>
                </button>
                <button
                  type="button"
                  :disabled="!isProfileUnlocked"
                  @click="resetProfilePicture"
                  class="px-4 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-700 focus:ring-2 focus:ring-gray-500 focus:ring-offset-2 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors"
                  :title="t('profile.profilePicture.reset')"
                >
                  {{ t('profile.reset') }}
                </button>
              </div>
              <p class="mt-2 text-sm text-gray-500">{{ t('profile.profilePicture.description') }}</p>
            </div>
          </div>
        </div>

        <!-- Reputation Name Field -->
        <div>
          <label for="reputationName" class="block text-sm font-medium text-gray-700 mb-2">
            {{ t('profile.reputationName.label') }}
          </label>
          
          <!-- Premium warning for locale overrides -->
          <PremiumFeatureWarning
            v-if="!isLocaleOverridesUnlocked"
            :message="t('profile.localeOverridesRequired.message')"
            :required-skus="localeOverridesRequiredSkus"
            variant="small"
          />
          
          <div class="flex gap-2">
            <input
              id="reputationName"
              v-model="reputationName"
              type="text"
              :disabled="!isLocaleOverridesUnlocked"
              :placeholder="t('profile.reputationName.placeholder')"
              class="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:bg-gray-100 disabled:cursor-not-allowed"
              @input="updateReputationName"
            />
            <button
              type="button"
              :disabled="!isLocaleOverridesUnlocked"
              @click="resetReputationName"
              class="px-4 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-700 focus:ring-2 focus:ring-gray-500 focus:ring-offset-2 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors"
              :title="t('profile.reputationName.reset')"
            >
              {{ t('profile.reset') }}
            </button>
          </div>
          <p class="mt-1 text-sm text-gray-500">{{ t('profile.reputationName.description') }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Additional styles if needed */
</style>
