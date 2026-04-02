/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import DiscordChat from './DiscordChat.vue'
import DiscordMessage from './DiscordMessage.vue'
import DiscordEmbed from './DiscordEmbed.vue'

const { t } = useI18n()

const methods = ['command', 'embed', 'reaction', 'answer', 'mention', 'fuzzy'] as const
type Method = typeof methods[number]

const active = ref<Method>('command')
</script>

<template>
  <div class="w-full">
    <!-- Tab bar -->
    <div class="flex flex-wrap justify-center gap-2 mb-8">
      <button
        v-for="method in methods"
        :key="method"
        @click="active = method"
        :class="[
          'px-4 py-2 rounded-lg text-sm font-medium transition-colors',
          active === method
            ? 'bg-indigo-600 text-white shadow'
            : 'bg-white dark:bg-gray-800 text-gray-600 dark:text-gray-300 border border-gray-200 dark:border-gray-700 hover:border-indigo-400 dark:hover:border-indigo-500'
        ]"
      >
        {{ t(`landing.reputation_methods.${method}.tab`) }}
      </button>
    </div>

    <!-- Description -->
    <p class="text-center text-gray-600 dark:text-gray-400 mb-6 max-w-xl mx-auto text-sm">
      {{ t(`landing.reputation_methods.${active}.description`) }}
    </p>

    <!-- Command -->
    <DiscordChat v-if="active === 'command'">
      <DiscordMessage username="Anna" avatar-color="#e67e22" :message="t('landing.reputation_methods.common.anna_question')" />
      <DiscordMessage username="Josy" avatar-color="#2ecc71" :message="t('landing.reputation_methods.common.josy_answer')" />
      <DiscordMessage username="Reputation Bot" avatar-color="#5865F2" is-bot
        :reply-to="t('landing.reputation_methods.command.system_user')"
        :reply-text="t('landing.reputation_methods.command.system_message')"
      >
        <span class="text-indigo-500 dark:text-indigo-400 font-medium">@Anna</span>
        {{ t('landing.reputation_methods.command.bot_confirm_mid') }}
        <span class="text-indigo-500 dark:text-indigo-400 font-medium">@Josy</span>
        {{ t('landing.reputation_methods.command.bot_confirm_end') }}
      </DiscordMessage>
    </DiscordChat>

    <!-- Embed -->
    <DiscordChat v-else-if="active === 'embed'">
      <DiscordMessage username="Anna" avatar-color="#e67e22" :message="t('landing.reputation_methods.common.anna_question')" />
      <DiscordMessage username="Josy" avatar-color="#2ecc71" :message="t('landing.reputation_methods.common.josy_answer')" />
      <DiscordMessage username="Anna" avatar-color="#e67e22" :message="t('landing.reputation_methods.embed.thank_you')" />
      <DiscordMessage username="Reputation Bot" avatar-color="#5865F2" is-bot
        reply-to="Anna" :reply-text="t('landing.reputation_methods.embed.thank_you')"
        :message="t('landing.reputation_methods.embed.bot_message')"
      />
      <DiscordEmbed
        :title="t('landing.reputation_methods.embed.embed_title')"
        :description="t('landing.reputation_methods.embed.embed_description')"
        :targets="['Josy']"
      />
    </DiscordChat>

    <!-- Reaction -->
    <DiscordChat v-else-if="active === 'reaction'">
      <DiscordMessage username="Anna" avatar-color="#e67e22" :message="t('landing.reputation_methods.common.anna_question')" />
      <DiscordMessage username="Josy" avatar-color="#2ecc71" :message="t('landing.reputation_methods.common.josy_answer')" medal />
      <DiscordMessage username="Reputation Bot" avatar-color="#5865F2" is-bot :message="t('landing.reputation_methods.reaction.bot_confirm')" />
    </DiscordChat>

    <!-- Answer -->
    <DiscordChat v-else-if="active === 'answer'">
      <DiscordMessage username="Anna" avatar-color="#e67e22" :message="t('landing.reputation_methods.common.anna_question')" />
      <DiscordMessage username="Josy" avatar-color="#2ecc71" :message="t('landing.reputation_methods.common.josy_answer')" />
      <DiscordMessage
        username="Anna"
        avatar-color="#e67e22"
        reply-to="Josy"
        :reply-text="t('landing.reputation_methods.common.josy_answer')"
        :message="t('landing.reputation_methods.answer.thank_you')"
        medal
      />
    </DiscordChat>

    <!-- Mention -->
    <DiscordChat v-else-if="active === 'mention'">
      <DiscordMessage username="Anna" avatar-color="#e67e22" :message="t('landing.reputation_methods.common.anna_question')" />
      <DiscordMessage username="Josy" avatar-color="#2ecc71" :message="t('landing.reputation_methods.common.josy_answer')" />
      <DiscordMessage username="Anna" avatar-color="#e67e22" medal>
        {{ t('landing.reputation_methods.mention.thank_you_prefix') }}<span class="text-indigo-500 dark:text-indigo-400 font-medium"> @Josy</span>
      </DiscordMessage>
    </DiscordChat>

    <!-- Fuzzy -->
    <DiscordChat v-else-if="active === 'fuzzy'">
      <DiscordMessage username="Anna" avatar-color="#e67e22" :message="t('landing.reputation_methods.common.anna_question')" />
      <DiscordMessage username="Josy" avatar-color="#2ecc71" :message="t('landing.reputation_methods.common.josy_answer')" />
      <DiscordMessage username="Anna" avatar-color="#e67e22" :message="t('landing.reputation_methods.fuzzy.thank_you')" medal />
    </DiscordChat>
  </div>
</template>
