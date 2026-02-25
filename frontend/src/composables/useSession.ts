/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {readonly, ref} from 'vue'
import type {UserSessionPOJO} from '@/api/types'
import * as Types from '@/api/types'
import {api} from '@/api'

const SESSION_TOKEN_KEY = 'reputation_bot_token'
const GUILD_ID_KEY = 'reputation_bot_guild_id'

const session = ref<(Types.GuildPOJO & { settings?: Types.SettingsPOJO, premiumFeatures?: Types.PremiumFeaturesPOJO }) | null>(null)
const userSession = ref<UserSessionPOJO | null>(null)
const hasToken = ref(!!localStorage.getItem(SESSION_TOKEN_KEY))
const premiumFeatures = ref<Types.PremiumFeaturesPOJO | null>(null)
const userTokens = ref<number>(0)
const guildTokens = ref<number>(0)
const isExpired = ref(false)
const currentGuildId = ref<string | null>(localStorage.getItem(GUILD_ID_KEY))

export function useSession() {
    const setSession = (data: Types.GuildPOJO) => {
        session.value = data
        isExpired.value = false
    }

    const setPremiumFeatures = (data: Types.PremiumFeaturesPOJO) => {
        premiumFeatures.value = data
    }

    const setUserSession = (data: UserSessionPOJO) => {
        userSession.value = data
        localStorage.setItem(SESSION_TOKEN_KEY, data.token)
        hasToken.value = true
        isExpired.value = false
    }

    const setToken = (token: string) => {
        localStorage.setItem(SESSION_TOKEN_KEY, token)
        hasToken.value = true
    }

    const setGuildId = (guildId: string | null) => {
        if (guildId) {
            localStorage.setItem(GUILD_ID_KEY, guildId)
        } else {
            localStorage.removeItem(GUILD_ID_KEY)
        }
        currentGuildId.value = guildId
    }

    const setUserTokens = (tokens: number) => {
        userTokens.value = tokens
    }

    const setGuildTokens = (tokens: number) => {
        guildTokens.value = tokens
    }

    const refreshUserTokens = async () => {
        try {
            const response = await api.getUserTokens()
            userTokens.value = response.tokens
        } catch (e) {
            console.error('Failed to refresh user tokens:', e)
        }
    }

    const refreshGuildTokens = async () => {
        try {
            const response = await api.getGuildTokens()
            guildTokens.value = response.tokens
        } catch (e) {
            console.error('Failed to refresh guild tokens:', e)
        }
    }

    const setExpired = (expired: boolean) => {
        isExpired.value = expired
    }

    const switchSession = (guildId: string) => {
        setGuildId(guildId)
        window.location.reload()
    }

    const clearSession = () => {
        session.value = null
        userSession.value = null
        premiumFeatures.value = null
        localStorage.removeItem(SESSION_TOKEN_KEY)
        setGuildId(null)
        hasToken.value = false
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

    const login = (redirectPath?: string | any) => {
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

        const finalRedirectPath = (typeof redirectPath === 'string' && redirectPath)
            ? redirectPath
            : (window.location.pathname + window.location.search)

        localStorage.setItem('reputation_bot_oauth_redirect', finalRedirectPath)

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
        premiumFeatures: readonly(premiumFeatures),
        userTokens: readonly(userTokens),
        guildTokens: readonly(guildTokens),
        isExpired: readonly(isExpired),
        hasToken: readonly(hasToken),
        currentGuildId: readonly(currentGuildId),
        setSession,
        setUserSession,
        setToken,
        setGuildId,
        setPremiumFeatures,
        setUserTokens,
        setGuildTokens,
        refreshUserTokens,
        refreshGuildTokens,
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
        loadPremiumFeatures,
        refreshGuildPremium: () => loadPremiumFeatures(true)
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

const loadPremiumFeatures = async (force = false) => {
        if (premiumFeatures.value && !force) return
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
