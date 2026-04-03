/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed} from 'vue'
import {useI18n} from 'vue-i18n'
import {onMounted, ref, watch} from 'vue'
import {api} from '@/api'
import {useSession} from '@/composables/useSession'
import SubHeader from '@/components/SubHeader.vue'
import SubHeaderTab from '@/components/SubHeaderTab.vue'

const {t} = useI18n()
const {userSession} = useSession()

const isGuildAdmin = computed(() => {
  const guildId = localStorage.getItem('reputation_bot_guild_id')
  if (!guildId || !userSession.value) return false
  return userSession.value.guilds[guildId]?.accessLevel === 'GUILD_ADMIN' || userSession.value.isBotOwner
})

const hasProblems = ref(false)

const checkProblems = async () => {
  if (!userSession.value) {
    hasProblems.value = false
    return
  }
  try {
    const result = await api.getDebug()
    hasProblems.value = result.missingGlobalPermissions.length > 0 ||
        result.simpleProblems.length > 0 ||
        result.missingPermissions.length > 0 ||
        result.rankProblems.length > 0 ||
        result.reputationChannelProblems.length > 0 ||
        result.simpleWarnings.length > 0
  } catch (e) {
    console.error('Failed to fetch debug info', e)
    hasProblems.value = false
  }
}

onMounted(checkProblems)
watch(userSession, checkProblems)
</script>

<template>
  <SubHeader v-if="isGuildAdmin">
    <SubHeaderTab
        to="/settings/edit"
        :active="$route.path.startsWith('/settings/edit') && !$route.path.endsWith('/problems') && !$route.path.endsWith('/audit-log')"
    >
      {{ t('navigation.settings') }}
    </SubHeaderTab>
    <SubHeaderTab
        to="/settings/preset"
        :active="$route.path.startsWith('/settings/preset')"
    >
      {{ t('navigation.presets') }}
    </SubHeaderTab>
    <SubHeaderTab
        to="/settings/audit-log"
        :active="$route.path.endsWith('/audit-log')"
    >
      {{ t('settings.auditLog') }}
    </SubHeaderTab>
    <SubHeaderTab
        to="/settings/problems"
        :active="$route.path.endsWith('/problems')"
    >
      <span>{{ t('settings.problems') }}</span>
      <font-awesome-icon
          v-if="hasProblems"
          icon="circle-exclamation"
          class="text-red-500"
      />
    </SubHeaderTab>
  </SubHeader>
</template>
