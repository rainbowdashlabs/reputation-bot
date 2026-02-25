/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {RouteRecordRaw} from 'vue-router'
import {createRouter, createWebHistory} from 'vue-router'

const routes: RouteRecordRaw[] = [
    {
        path: '/',
        redirect: '/guild/dashboard'
    },
    {
        path: '/guild',
        name: 'Guild',
        children: [
            {
                path: 'dashboard',
                name: 'GuildDashboard',
                component: () => import('@/views/guild/DashboardView.vue')
            },
            {
                path: 'token-shop',
                name: 'TokenShop',
                component: () => import('@/views/guild/TokenShopView.vue')
            }
        ]
    },
    {
        path: '/settings',
        name: 'Settings',
        component: () => import('@/views/SettingsView.vue'),
        redirect: '/settings/edit/general',
        children: [
            {
                path: 'edit',
                redirect: '/settings/edit/general',
                children: [
                    {
                        path: 'general',
                        name: 'SettingsGeneral',
                        component: () => import('@/views/settings/GeneralView.vue')
                    },
                    {
                        path: 'profile',
                        name: 'SettingsProfile',
                        component: () => import('@/views/settings/ProfileView.vue')
                    },
                    {
                        path: 'ranks',
                        name: 'SettingsRanks',
                        component: () => import('@/views/settings/RanksView.vue')
                    },
                    {
                        path: 'reputation',
                        name: 'SettingsReputation',
                        component: () => import('@/views/settings/ReputationView.vue')
                    },
                    {
                        path: 'channels',
                        name: 'SettingsChannels',
                        component: () => import('@/views/settings/ChannelsView.vue')
                    },
                    {
                        path: 'roles',
                        name: 'SettingsRoles',
                        component: () => import('@/views/settings/RolesView.vue')
                    },
                    {
                        path: 'reactions',
                        name: 'SettingsReactions',
                        component: () => import('@/views/settings/ReactionsView.vue')
                    },
                    {
                        path: 'thankwords',
                        name: 'SettingsThankwords',
                        component: () => import('@/views/settings/ThankwordsView.vue')
                    },
                    {
                        path: 'abuse-protection',
                        name: 'SettingsAbuseProtection',
                        component: () => import('@/views/settings/AbuseProtectionView.vue')
                    },
                    {
                        path: 'announcements',
                        name: 'SettingsAnnouncements',
                        component: () => import('@/views/settings/AnnouncementsView.vue')
                    },
                    {
                        path: 'autopost',
                        name: 'SettingsAutopost',
                        component: () => import('@/views/settings/AutopostView.vue')
                    },
                    {
                        path: 'log-channel',
                        name: 'SettingsLogChannel',
                        component: () => import('@/views/settings/LogChannelView.vue')
                    },
                    {
                        path: 'integration-bypass',
                        name: 'SettingsIntegrationBypass',
                        component: () => import('@/views/settings/IntegrationBypassView.vue')
                    }
                ]
            },
            {
                path: 'audit-log',
                name: 'SettingsAuditLog',
                component: () => import('@/views/settings/AuditLogView.vue')
            },
            {
                path: 'problems',
                name: 'SettingsProblems',
                component: () => import('@/views/settings/ProblemsView.vue')
            },
            {
                path: 'preset',
                name: 'Presets',
                component: () => import('@/views/PresetsView.vue')
            }
        ]
    },
    {
        path: '/user',
        name: 'User',
        children: [
            {
                path: 'vote',
                name: 'UserVote',
                component: () => import('@/views/user/VotingView.vue')
            },
            {
                path: 'settings',
                name: 'UserSettings',
                component: () => import('@/views/user/UserSettingsView.vue')
            },
            {
                path: 'supporter',
                name: 'UserSupporter',
                component: () => import('@/views/user/SupporterView.vue')
            }
            ]
    },
    {
        path: '/presets',
        redirect: '/settings/preset'
    },
    {
        path: '/setup',
        name: 'Setup',
        component: () => import('@/views/SetupView.vue')
    },
    {
        path: '/error/no-token',
        name: 'NoTokenError',
        component: () => import('@/views/NoTokenError.vue')
    },
    {
        path: '/tos',
        name: 'Tos',
        component: () => import('@/views/TosView.vue')
    },
    {
        path: '/privacy',
        name: 'Privacy',
        component: () => import('@/views/PrivacyView.vue')
    },
    {
        path: '/faq',
        name: 'Faq',
        component: () => import('@/views/FaqView.vue')
    },
    {
        path: '/metrics',
        name: 'Metrics',
        component: () => import('@/views/MetricsView.vue')
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router
