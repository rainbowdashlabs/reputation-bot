<script lang="ts" setup>
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useSession } from '@/composables/useSession'
import { api } from '@/api'
import BaseButton from '@/components/BaseButton.vue'

const props = defineProps<{
  initialProfilePictureUrl: string
  disabled: boolean
}>()

const { t } = useI18n()
const { updateProfileSettings } = useSession()
const profilePictureUrl = ref(props.initialProfilePictureUrl)
const isUploading = ref(false)
const fileInputRef = ref<HTMLInputElement | null>(null)

watch(() => props.initialProfilePictureUrl, (newVal) => {
  profilePictureUrl.value = newVal || ''
})

const triggerFileInput = () => {
  if (props.disabled) return
  fileInputRef.value?.click()
}

const resetProfilePicture = async () => {
  if (props.disabled) return

  try {
    await api.deleteProfilePicture()
    updateProfileSettings({ profilePictureUrl: null })
    profilePictureUrl.value = ''
  } catch (error) {
    console.error('Failed to reset profile picture:', error)
  }
}

const handleFileUpload = async (event: Event) => {
  if (props.disabled) return

  const target = event.target as HTMLInputElement
  const file = target.files?.[0]

  if (!file) return

  if (!file.type.startsWith('image/')) {
    console.error('Invalid file type. Please select an image.')
    return
  }

  const maxSize = 2 * 1024 * 1024
  if (file.size > maxSize) {
    console.error('File too large. Maximum size is 2MB.')
    return
  }

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
      const sessionData = await api.getSession()
      const newUrl = sessionData.settings.profile.profilePictureUrl || ''
      updateProfileSettings({ profilePictureUrl: newUrl })
      profilePictureUrl.value = newUrl
    } catch (error) {
      console.error('Failed to upload profile picture:', error)
    } finally {
      isUploading.value = false
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
    <label class="label mb-2">
      {{ t('profile.profilePicture.label') }}
    </label>
    <div class="flex items-center space-x-4">
      <img
          v-if="profilePictureUrl"
          :src="profilePictureUrl"
          alt="Profile picture"
          class="w-16 h-16 rounded-full object-cover border-2 border-gray-200 dark:border-gray-700"
          @error="() => {}"
      />
      <div v-else class="w-16 h-16 rounded-full bg-gray-200 dark:bg-gray-700 flex items-center justify-center">
        <svg class="w-8 h-8 text-gray-400 dark:text-gray-500" fill="none" stroke="currentColor"
             viewBox="0 0 24 24">
          <path d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" stroke-linecap="round" stroke-linejoin="round"
                stroke-width="2"/>
        </svg>
      </div>
      <div class="flex-1">
        <input
            ref="fileInputRef"
            accept="image/*"
            class="hidden"
            type="file"
            @change="handleFileUpload"
        />
        <div class="flex gap-2">
          <BaseButton
              :disabled="disabled || isUploading"
              color="blue"
              @click="triggerFileInput"
          >
            <span v-if="isUploading">{{ t('profile.profilePicture.uploading') }}</span>
            <span v-else>{{ t('profile.profilePicture.uploadButton') }}</span>
          </BaseButton>
          <BaseButton
              :disabled="disabled"
              :title="t('profile.profilePicture.reset')"
              color="secondary"
              @click="resetProfilePicture"
          >
            {{ t('profile.reset') }}
          </BaseButton>
        </div>
        <p class="description">{{ t('profile.profilePicture.description') }}</p>
      </div>
    </div>
  </div>
</template>
