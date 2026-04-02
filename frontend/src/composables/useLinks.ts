/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {ref} from 'vue'
import type {Links} from '@/api/types'
import {api} from '@/api'

const links = ref<Links>({
    tos: '',
    invite: '',
    support: '',
    website: '',
    faq: '',
    kofi: ''
})

let loaded = false

export function useLinks() {
    const loadLinks = async () => {
        if (loaded) return
        try {
            links.value = await api.getLinks()
            loaded = true
        } catch (error) {
            console.error('Failed to fetch links:', error)
        }
    }

    return {
        links,
        loadLinks
    }
}
