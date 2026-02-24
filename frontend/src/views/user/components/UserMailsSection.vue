<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import { api } from '@/api'
import type { MailEntryPOJO } from '@/api/types'
import { MailFailureReason } from '@/api/types'
import { AxiosError } from 'axios'
import MailAddress from './MailAddress.vue'
import VerifyBanner from './VerifyBanner.vue'
import MailAddForm from './MailAddForm.vue'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()

const mails = ref<MailEntryPOJO[]>([])
const mailLoading = ref(false)
const newMail = ref('')
const verifyMessage = ref<string>('')
const verifyError = ref<string>('')
const registerError = ref<string>('')

const getFailureMessage = (reasonRaw: unknown): string => {
  const reason = typeof reasonRaw === 'string' ? reasonRaw : ''
  if (reason && Object.values(MailFailureReason).includes(reason as MailFailureReason)) {
    return t(`user.settings.mails.errors.${reason}`) as string
  }
  return t('user.settings.mails.errors.UNKNOWN') as string
}

const fetchMails = async () => {
  try {
    mailLoading.value = true
    mails.value = await api.getUserMails()
  } catch (error) {
    console.error('Failed to fetch user mails:', error)
  } finally {
    mailLoading.value = false
  }
}

onMounted(async () => {
  await fetchMails()

  const hash = (route.query.hash as string) || (route.query.mailid as string) || ''
  const code = (route.query.code as string) || ''
  if (hash && code) {
    try {
      await api.verifyUserMail(hash, code)
      verifyMessage.value = t('user.settings.mails.verify.success') as string
      await fetchMails()
    } catch (e) {
      console.error('Failed to verify mail:', e)
      const reasonRaw = (e instanceof AxiosError) ? e.response?.data : (e as any)?.response?.data
      verifyError.value = getFailureMessage(reasonRaw)
    } finally {
      router.replace({ query: { ...route.query, hash: undefined, mailid: undefined, code: undefined } })
    }
  }
})

const addMail = async (value: string) => {
  registerError.value = ''
  try {
    await api.registerUserMail(value)
    await fetchMails()
    newMail.value = ''
  } catch (e) {
    console.error('Failed to register mail:', e)
    const reasonRaw = (e instanceof AxiosError) ? e.response?.data : (e as any)?.response?.data
    registerError.value = getFailureMessage(reasonRaw)
  }
}

const removeMail = async (hash: string) => {
  try {
    await api.deleteUserMail(hash)
    await fetchMails()
  } catch (error) {
    console.error('Failed to delete mail:', error)
  }
}
</script>

<template>
  <div class="flex flex-col gap-4">
    <div>
      <h3 class="text-base font-semibold text-gray-900 dark:text-gray-100">{{ t('user.settings.mails.title') }}</h3>
      <p class="text-sm text-gray-500 dark:text-gray-400">{{ t('user.settings.mails.description') }}</p>
    </div>

    <VerifyBanner v-if="verifyMessage" type="success">
      {{ verifyMessage }}
    </VerifyBanner>
    <VerifyBanner v-if="verifyError" type="warning">
      {{ verifyError }}
    </VerifyBanner>

    <MailAddForm v-model="newMail" :placeholder="t('user.settings.mails.add.placeholder') as string" @submit="addMail">
      {{ t('user.settings.mails.add.button') }}
    </MailAddForm>
    <VerifyBanner v-if="registerError" type="error">
      {{ registerError }}
    </VerifyBanner>

    <div v-if="mailLoading" class="flex justify-center py-4">
      <font-awesome-icon :icon="['fas','spinner']" class="animate-spin"/>
    </div>
    <template v-else>
      <div class="space-y-2" v-if="mails.length > 0">
        <MailAddress v-for="m in mails" :key="m.hash" :entry="m" @delete="removeMail" />
      </div>
      <div v-else class="text-sm text-gray-500 dark:text-gray-400">
        {{ t('common.noData') }}
      </div>
    </template>

    <p class="text-xs text-gray-500 dark:text-gray-400">
      {{ t('user.settings.mails.noteDeletion') }}
    </p>
  </div>
</template>
