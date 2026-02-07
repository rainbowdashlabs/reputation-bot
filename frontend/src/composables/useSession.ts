/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {readonly, ref} from 'vue'
import type {GuildSessionPOJO} from '@/api/types'
import * as Types from '@/api/types'

export interface GuildSessionInfo {
    id: string
    name: string
    iconUrl: string | null
    token: string
}

const SESSION_STORAGE_KEY = 'reputation_bot_sessions'

const session = ref<GuildSessionPOJO | null>(null)
const sessions = ref<GuildSessionInfo[]>(JSON.parse(localStorage.getItem(SESSION_STORAGE_KEY) || '[]'))
const isExpired = ref(false)

export function useSession() {
    const setSession = (data: GuildSessionPOJO) => {
        session.value = data
        isExpired.value = false
        updateSessionsList(data)
    }

    const setExpired = (expired: boolean) => {
        isExpired.value = expired
    }

    const updateSessionsList = (data: GuildSessionPOJO) => {
        const token = localStorage.getItem('token')
        if (!token) return

        const index = sessions.value.findIndex(s => s.id === data.guild.meta.id)
        const info: GuildSessionInfo = {
            id: data.guild.meta.id,
            name: data.guild.meta.name,
            iconUrl: data.guild.meta.iconUrl,
            token: token
        }

        if (index === -1) {
            sessions.value.push(info)
        } else {
            sessions.value[index] = info
        }
        saveSessions()
    }

    const saveSessions = () => {
        localStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(sessions.value))
    }

    const switchSession = (guildId: string) => {
        const target = sessions.value.find(s => s.id === guildId)
        if (target) {
            localStorage.setItem('token', target.token)
            window.location.reload()
        }
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

    const updateThankingDenyDonorRolesSettings = (updates: Partial<Types.RolesHolderPOJO>) => {
        if (session.value?.settings?.thanking?.denyDonorRoles) {
            Object.assign(session.value.settings.thanking.denyDonorRoles, updates)
        }
    }

    const updateThankingDenyReceiverRolesSettings = (updates: Partial<Types.RolesHolderPOJO>) => {
        if (session.value?.settings?.thanking?.denyReceiverRoles) {
            Object.assign(session.value.settings.thanking.denyReceiverRoles, updates)
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

    const updateIntegrationBypass = (bypass: Types.Bypass) => {
        if (session.value?.settings?.integrationBypass) {
            session.value.settings.integrationBypass.bypasses[bypass.integrationId] = bypass
        }
    }

    const removeIntegrationBypass = (integrationId: string) => {
        if (session.value?.settings?.integrationBypass) {
            delete session.value.settings.integrationBypass.bypasses[integrationId]
        }
    }

    const updateRanksSettings = (updates: Partial<Types.RanksPOJO>) => {
        if (session.value?.settings?.ranks) {
            // Ensure ranks array deduplication by roleId if provided
            if (updates.ranks) {
                const seen = new Set<string>()
                updates.ranks = updates.ranks.filter(r => {
                    const key = String(r.roleId)
                    if (seen.has(key)) return false
                    seen.add(key)
                    return true
                })
            }
            Object.assign(session.value.settings.ranks, updates)
        }
    }

    return {
        session: readonly(session),
        sessions: readonly(sessions),
        isExpired: readonly(isExpired),
        setSession,
        setExpired,
        switchSession,
        clearSession,
        updateGeneralSettings,
        updateReputationSettings,
        updateProfileSettings,
        updateThankingChannelsSettings,
        updateThankingDonorRolesSettings,
        updateThankingReceiverRolesSettings,
        updateThankingDenyDonorRolesSettings,
        updateThankingDenyReceiverRolesSettings,
        updateThankingReactionsSettings,
        updateThankingThankwordsSettings,
        updateAbuseProtectionSettings,
        updateMessagesSettings,
        updateAnnouncementsSettings,
        updateAutopostSettings,
        updateLogChannelSettings,
        updateRanksSettings,
        updateIntegrationBypass,
        removeIntegrationBypass
    }
}
