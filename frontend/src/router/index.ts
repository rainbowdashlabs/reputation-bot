/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/settings'
  },
  {
    path: '/settings',
    name: 'Settings',
    component: () => import('@/views/SettingsView.vue'),
    redirect: '/settings/general',
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
      }
    ]
  },
  {
    path: '/presets',
    name: 'Presets',
    component: () => import('@/views/PresetsView.vue')
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
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
