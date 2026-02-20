/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {readonly, ref} from 'vue'
import type {GuildSessionPOJO, UserSessionPOJO} from '@/api/types'
import * as Types from '@/api/types'
import {api} from '@/api'

const SESSION_TOKEN_KEY = 'reputation_bot_token'
const GUILD_ID_KEY = 'reputation_bot_guild_id'

const session = ref<(GuildSessionPOJO & { settings?: Types.SettingsPOJO, premiumFeatures?: Types.PremiumFeaturesPOJO }) | null>(null)
const userSession = ref<UserSessionPOJO | null>(null)
const guildMeta = ref<Types.GuildMetaPOJO | null>(null)
const premiumFeatures = ref<Types.PremiumFeaturesPOJO | null>(null)
const isExpired = ref(false)

export function useSession() {
    const setSession = (data: GuildSessionPOJO) => {
        session.value = data
        isExpired.value = false
    }

    const setGuildMeta = (data: Types.GuildMetaPOJO) => {
        guildMeta.value = data
    }

    const setPremiumFeatures = (data: Types.PremiumFeaturesPOJO) => {
        premiumFeatures.value = data
    }

    const setUserSession = (data: UserSessionPOJO) => {
        userSession.value = data
        localStorage.setItem(SESSION_TOKEN_KEY, data.token)
        isExpired.value = false
    }

    const setExpired = (expired: boolean) => {
        isExpired.value = expired
    }

    const switchSession = (guildId: string) => {
        localStorage.setItem(GUILD_ID_KEY, guildId)
        window.location.reload()
    }

    const clearSession = () => {
        session.value = null
        userSession.value = null
        guildMeta.value = null
        premiumFeatures.value = null
        localStorage.removeItem(SESSION_TOKEN_KEY)
        localStorage.removeItem(GUILD_ID_KEY)
    }

    const logout = async () => {
        try {
            await api.logout()
        } catch (e) {
            // ignore errors, still clear local state
        } finally {
            clearSession()
            window.location.href = '/'
        }
    }

    const login = () => {
        const backendHost = import.meta.env.VITE_BACKEND_HOST || ''
        const backendPort = import.meta.env.VITE_BACKEND_PORT || ''

        let baseURL = '/v1'
        if (backendHost) {
            const protocol = backendHost.startsWith('http') ? '' : 'http://'
            const port = backendPort ? `:${backendPort}` : ''
            baseURL = `${protocol}${backendHost}${port}/v1`
        }

        const state = Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15)
        localStorage.setItem('reputation_bot_oauth_state', state)
        localStorage.setItem('reputation_bot_oauth_redirect', window.location.pathname + window.location.search)

        window.location.href = `${baseURL}/auth/discord/login?state=${encodeURIComponent(state)}`
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
        userSession: readonly(userSession),
        guildMeta: readonly(guildMeta),
        premiumFeatures: readonly(premiumFeatures),
        isExpired: readonly(isExpired),
        setSession,
        setUserSession,
        setGuildMeta,
        setPremiumFeatures,
        setExpired,
        switchSession,
        clearSession,
        login,
        logout,
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
        removeIntegrationBypass,
        loadSettings,
        loadPremiumFeatures
    }
}

async function loadSettings() {
    if (!session.value) return
    if (session.value.settings) return
    try {
        const settings = await api.getGuildSettings()
        session.value.settings = settings
    } catch (e) {
        console.error('Failed to load settings:', e)
    }
}

async function loadPremiumFeatures() {
    if (premiumFeatures.value) return
    try {
        const premium = await api.getGuildPremium()
        premiumFeatures.value = premium
        if (session.value) {
            session.value.premiumFeatures = premium
        }
    } catch (e) {
        console.error('Failed to load premium features:', e)
    }
}
