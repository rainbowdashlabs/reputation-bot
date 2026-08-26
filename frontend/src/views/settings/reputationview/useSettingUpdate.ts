/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {useSession} from '@/composables/useSession'
import {api} from '@/api'

type SettingType = 'reputation' | 'messages'

/**
 * Updates a boolean reputation or messages setting.
 *
 * The local state is updated first for a responsive ui and reverted when the request fails. The api client method is
 * resolved by convention: update + Reputation|Messages + the pascal cased setting key.
 */
export function useSettingUpdate() {
    const {session, updateReputationSettings, updateMessagesSettings} = useSession()

    const applyLocal = (type: SettingType, key: string, value: boolean) => {
        if (type === 'reputation') {
            updateReputationSettings({[key]: value})
        } else {
            updateMessagesSettings({[key]: value})
        }
    }

    const updateSetting = async (key: string, value: boolean, type: SettingType = 'reputation') => {
        if (!session.value?.settings?.[type]) return

        applyLocal(type, key, value)

        try {
            const prefix = type.charAt(0).toUpperCase() + type.slice(1)
            const methodName = `update${prefix}${key.charAt(0).toUpperCase()}${key.slice(1)}` as keyof typeof api
            if (typeof api[methodName] === 'function') {
                await (api[methodName] as Function)(value)
            } else {
                console.error(`Method ${methodName} not found in API client`)
            }
        } catch (error) {
            applyLocal(type, key, !value)
            console.error(`Failed to update ${type} setting ${key}:`, error)
        }
    }

    return {session, updateSetting}
}
