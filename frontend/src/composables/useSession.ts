/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import { ref, readonly } from 'vue'
import type { GuildSessionPOJO } from '@/api/types'
import * as Types from '@/api/types'

const session = ref<GuildSessionPOJO | null>(null)

export function useSession() {
  const setSession = (data: GuildSessionPOJO) => {
    session.value = data
  }

  const clearSession = () => {
    session.value = null
  }

  const updateGeneralSettings = (updates: Partial<Types.GeneralPOJO>) => {
    if (session.value?.settings?.general) {
      Object.assign(session.value.settings.general, updates)
    }
  }

  const updateReputationSettings = (updates: Partial<Types.ReputationPOJO>) => {
    if (session.value?.settings?.reputation) {
      Object.assign(session.value.settings.reputation, updates)
    }
  }

  const updateProfileSettings = (updates: Partial<Types.ProfilePOJO>) => {
    if (session.value?.settings?.profile) {
      Object.assign(session.value.settings.profile, updates)
    }
  }

  const updateThankingChannelsSettings = (updates: Partial<Types.ChannelsSettingsPOJO>) => {
    if (session.value?.settings?.thanking?.channels) {
      Object.assign(session.value.settings.thanking.channels, updates)
    }
  }

  const updateThankingDonorRolesSettings = (updates: Partial<Types.RolesHolderPOJO>) => {
    if (session.value?.settings?.thanking?.donorRoles) {
      Object.assign(session.value.settings.thanking.donorRoles, updates)
    }
  }

  const updateThankingReceiverRolesSettings = (updates: Partial<Types.RolesHolderPOJO>) => {
    if (session.value?.settings?.thanking?.receiverRoles) {
      Object.assign(session.value.settings.thanking.receiverRoles, updates)
    }
  }

  const updateThankingReactionsSettings = (updates: Partial<Types.ReactionsSettingsPOJO>) => {
    if (session.value?.settings?.thanking?.reactions) {
      Object.assign(session.value.settings.thanking.reactions, updates)
    }
  }

  const updateThankingThankwordsSettings = (updates: Partial<Types.ThankwordsPOJO>) => {
    if (session.value?.settings?.thanking?.thankwords) {
      if (updates.thankwords) {
        updates.thankwords = [...new Set(updates.thankwords)]
      }
      Object.assign(session.value.settings.thanking.thankwords, updates)
    }
  }

  const updateAbuseProtectionSettings = (updates: Partial<Types.AbuseProtectionPOJO>) => {
    if (session.value?.settings?.abuseProtection) {
      Object.assign(session.value.settings.abuseProtection, updates)
    }
  }

  const updateMessagesSettings = (updates: Partial<Types.MessagesPOJO>) => {
    if (session.value?.settings?.messages) {
      Object.assign(session.value.settings.messages, updates)
    }
  }

  const updateAnnouncementsSettings = (updates: Partial<Types.AnnouncementsPOJO>) => {
    if (session.value?.settings?.announcements) {
      Object.assign(session.value.settings.announcements, updates)
    }
  }

  const updateAutopostSettings = (updates: Partial<Types.AutopostPOJO>) => {
    if (session.value?.settings?.autopost) {
      Object.assign(session.value.settings.autopost, updates)
    }
  }

  const updateLogChannelSettings = (updates: Partial<Types.LogChannelPOJO>) => {
    if (session.value?.settings?.logChannel) {
      Object.assign(session.value.settings.logChannel, updates)
    }
  }

  return {
    session: readonly(session),
    setSession,
    clearSession,
    updateGeneralSettings,
    updateReputationSettings,
    updateProfileSettings,
    updateThankingChannelsSettings,
    updateThankingDonorRolesSettings,
    updateThankingReceiverRolesSettings,
    updateThankingReactionsSettings,
    updateThankingThankwordsSettings,
    updateAbuseProtectionSettings,
    updateMessagesSettings,
    updateAnnouncementsSettings,
    updateAutopostSettings,
    updateLogChannelSettings
  }
}
