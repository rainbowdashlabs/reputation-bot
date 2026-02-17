/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
export interface RolePOJO {
    name: string;
    id: string;
    color: string;
    position: number;
}

export interface MemberPOJO {
    displayName: string;
    id: string;
    profilePictureUrl: string;
    color: string;
}

// abuseprotection.cooldown -> number. -1 means once
// abuseprotection.cooldowndirection -> enum
// abuseprotection.maxmessageage -> number
// abuseprotection.minmessages -> number
// abuseprotection.donorcontext -> boolean
// abuseprotection.receivercontext -> boolean
// abuseprotection.maxgiven -> number
// abuseprotection.maxgivenhours -> number
// abuseprotection.maxreceived -> number
// abuseprotection.maxreceivedhours -> number
// abuseprotection.maxmessagereputation -> number
// thanking.channels.whitelist -> enum
// thanking.channels.channels -> List of channel ids
// thanking.channels.categories -> List of category ids
// thanking.thankwords.words -> List of strings
// thanking.reactions.mainreaction -> reaction id
// thanking.reactions.reactions -> list of reaction ids or unicode emoji names
// thanking.denydonorroles -> list of role ids
// thanking.donorroles -> list of role ids
// thanking.denyreceiverroles -> list of role ids
// thanking.receiverroles -> list of role ids
// messages.reactionconfirmation -> boolean
// messages.commandreputationephemeral -> boolean
// profile.nickname -> string or null
// profile.profilepicture -> link as string
// profile.reputationname -> string or null
// reputation.reactionactive -> boolean
// reputation.answeractive -> boolean
// reputation.mentionactive -> boolean
// reputation.fuzzyactive -> boolean
// reputation.embedactive -> boolean
// reputation.directactive -> boolean
// reputation.commandactive -> boolean
// logchannel.active -> boolean
// logchannel.channel -> channel id
// general.language -> language name
// general.stackroles -> boolean
// general.reputationmode -> enum
// general.systemchannel -> channel id
// general.resetdate -> ISO string
// announcements.active -> boolean
// announcements.samechannel -> boolean
// announcements.channel -> channel id
// autopost.active -> boolean
// autopost.channel -> channel id
// autopost.refreshtype -> enum
// autopost.refreshinterval -> enum
// ranks
export interface SettingsAuditLogPOJO {
    /*
     * This is a log entry for a settings change. It contains the key of the setting that was changed,
     * the ID of the member who made the change, the old value of the setting, the new value of the setting,
     * and the timestamp of when the change was made.
     * The type of the values depends on the settings key.
     */
    settingsKey: string;
    memberId: string;
    oldValue: Object; // Type heavily depends on the settingsKey
    newValue: Object; // Type heavily depends on the settingsKey
    changed: string; // ISO string
}

export interface AuditLogPagePOJO {
    page: number;
    maxPages: number;
    content: SettingsAuditLogPOJO[];
    members: Map<string, MemberPOJO>;
}

export interface ReactionPOJO {
    name: string;
    id: string;
    url: string;
}

export interface GuildMetaPOJO {
    highestBotRole: RolePOJO | null;
    name: string;
    id: string;
    iconUrl: string | null;
}

export interface ChannelPOJO {
    name: string;
    id: string;
    type: string;
    visible: boolean;
}

export interface CategoryPOJO {
    channels: ChannelPOJO[];
    name: string;
    id: string;
}

export interface ChannelViewPOJO {
    channels: ChannelPOJO[];
    categories: CategoryPOJO[];
}

export interface GuildPOJO {
    meta: GuildMetaPOJO;
    roles: RolePOJO[];
    channels: ChannelViewPOJO;
    reactions: ReactionPOJO[];
    integrations: MemberPOJO[];
}

export enum CooldownDirection {
    UNIDIRECTIONAL = 'UNIDIRECTIONAL',
    BIDIRECTIONAL = 'BIDIRECTIONAL',
}

export interface AbuseProtectionPOJO {
    cooldown: number;
    cooldownDirection: CooldownDirection;
    maxMessageAge: number;
    minMessages: number;
    donorContext: boolean;
    receiverContext: boolean;
    maxGiven: number;
    maxGivenHours: number;
    maxReceived: number;
    maxReceivedHours: number;
    maxMessageReputation: number;
}

export interface Bypass {
    integrationId: string;
    allowReactions: boolean;
    allowAnswer: boolean;
    allowMention: boolean;
    allowFuzzy: boolean;
    allowDirect: boolean;
    ignoreCooldown: boolean;
    ignoreLimit: boolean;
    ignoreContext: boolean;
}

export interface IntegrationBypassPOJO {
    bypasses: Record<string, Bypass>;
}

export interface AnnouncementsPOJO {
    active: boolean;
    sameChannel: boolean;
    channelId: string;
}

export enum RefreshType {
    DELETE_AND_REPOST = 'DELETE_AND_REPOST',
    REPOST = 'REPOST',
    UPDATE = 'UPDATE',
}

export enum RefreshInterval {
    HOURLY = 'HOURLY',
    DAILY = 'DAILY',
    WEEKLY = 'WEEKLY',
    MONTHLY = 'MONTHLY',
}

export interface AutopostPOJO {
    active: boolean;
    channelId: string;
    messageId: string;
    refreshType: RefreshType;
    refreshInterval: RefreshInterval;
}

export enum ReputationMode {
    TOTAL = 'TOTAL',
    ROLLING_WEEK = 'ROLLING_WEEK',
    ROLLING_MONTH = 'ROLLING_MONTH',
    WEEK = 'WEEK',
    MONTH = 'MONTH',
}

export interface GeneralPOJO {
    stackRoles: boolean; // Implemented in ranks
    language: string; // Internal name (e.g., "SPANISH")
    reputationMode: ReputationMode; // Implemented in reputation
    resetDate: string; // ISO string // Implemented in reputation
    systemChannel: string; // Channel ID as number
}

export interface LogChannelPOJO {
    channelId: string;
    active: boolean;
}

export interface MessagesPOJO {
    reactionConfirmation: boolean; // Implement in reputation
    commandReputationEphemeral: boolean; // Implement in reputation
}

export interface ProfilePOJO {
    nickname: string | null;
    description: string | null;
    profilePictureUrl: string | null;
    reputationName: string | null;
}

export interface ReputationPOJO {
    reactionActive: boolean;
    answerActive: boolean;
    mentionActive: boolean;
    fuzzyActive: boolean;
    embedActive: boolean;
    directActive: boolean;
    commandActive: boolean;
}

export interface ChannelsSettingsPOJO {
    channels: string[];
    categories: string[];
    whitelist: boolean;
}

export interface RolesHolderPOJO {
    roleIds: string[];
}

export interface ReactionsSettingsPOJO {
    reactions: string[];
    mainReaction: string;
}

export interface ThankwordsPOJO {
    thankwords: string[];
}

export interface RankEntry {
    roleId: string;
    reputation: number;
}

export interface RanksPOJO {
    ranks: RankEntry[];
}

export interface RefreshStatus {
    alreadyRunning: boolean;
}

export interface ThankingPOJO {
    channels: ChannelsSettingsPOJO;
    donorRoles: RolesHolderPOJO;
    receiverRoles: RolesHolderPOJO;
    denyDonorRoles: RolesHolderPOJO;
    denyReceiverRoles: RolesHolderPOJO;
    reactions: ReactionsSettingsPOJO;
    thankwords: ThankwordsPOJO;
}

export interface SettingsPOJO {
    abuseProtection: AbuseProtectionPOJO;
    integrationBypass: IntegrationBypassPOJO;
    announcements: AnnouncementsPOJO;
    autopost: AutopostPOJO;
    general: GeneralPOJO;
    logChannel: LogChannelPOJO;
    messages: MessagesPOJO;
    profile: ProfilePOJO;
    ranks: RanksPOJO;
    reputation: ReputationPOJO;
    thanking: ThankingPOJO;
}

export interface SkuInfo {
    id: string;
    name: string;
}

export interface SimpleFeature {
    unlocked: boolean;
    requiredSkus: SkuInfo[];
}

export interface FeatureLimit {
    max: number;
    unlocked: boolean;
    requiredSkus: SkuInfo[];
}

export interface PremiumFeaturesPOJO {
    reputationLog: SimpleFeature;
    analyzerLog: SimpleFeature;
    channelBlacklist: SimpleFeature;
    localeOverrides: SimpleFeature;
    autopost: SimpleFeature;
    advancedRankings: SimpleFeature;
    detailedProfile: SimpleFeature;
    logChannel: SimpleFeature;
    additionalEmojis: SimpleFeature;
    profile: SimpleFeature;
    integrationBypass: SimpleFeature;
    reputationChannel: FeatureLimit;
    reputationCategories: FeatureLimit;
}

export interface Links {
    tos: string;
    invite: string;
    support: string;
    website: string;
    faq: string;
}

export interface GuildSessionPOJO {
    settings: SettingsPOJO;
    guild: GuildPOJO;
    premiumFeatures: PremiumFeaturesPOJO;
}

export interface PremiumFeatureErrorDetails {
    feature: string;
    requiredSkus: SkuInfo[];
    currentValue?: number;
    maxValue?: number;
}

export interface ApiErrorResponse {
    error: string;
    message: string;
    details?: PremiumFeatureErrorDetails | any;
}

export interface ThankwordsContainer {
    defaults: Record<string, string[]>;
}

export interface LanguageInfo {
    code: string;
    name: string;
    nativeName: string;
    internalName: string;
}

export enum Permission {
    CREATE_INSTANT_INVITE = 'CREATE_INSTANT_INVITE',
    KICK_MEMBERS = 'KICK_MEMBERS',
    BAN_MEMBERS = 'BAN_MEMBERS',
    ADMINISTRATOR = 'ADMINISTRATOR',
    MANAGE_CHANNEL = 'MANAGE_CHANNEL',
    MANAGE_SERVER = 'MANAGE_SERVER',
    MESSAGE_ADD_REACTION = 'MESSAGE_ADD_REACTION',
    VIEW_AUDIT_LOG = 'VIEW_AUDIT_LOG',
    PRIORITY_SPEAKER = 'PRIORITY_SPEAKER',
    VIEW_CHANNEL = 'VIEW_CHANNEL',
    MESSAGE_SEND = 'MESSAGE_SEND',
    MESSAGE_TTS = 'MESSAGE_TTS',
    MESSAGE_MANAGE = 'MESSAGE_MANAGE',
    MESSAGE_EMBED_LINKS = 'MESSAGE_EMBED_LINKS',
    MESSAGE_ATTACH_FILES = 'MESSAGE_ATTACH_FILES',
    MESSAGE_HISTORY = 'MESSAGE_HISTORY',
    MESSAGE_MENTION_EVERYONE = 'MESSAGE_MENTION_EVERYONE',
    MESSAGE_EXT_EMOJI = 'MESSAGE_EXT_EMOJI',
    VIEW_GUILD_INSIGHTS = 'VIEW_GUILD_INSIGHTS',
    VOICE_CONNECT = 'VOICE_CONNECT',
    VOICE_SPEAK = 'VOICE_SPEAK',
    VOICE_MUTE_MEMBERS = 'VOICE_MUTE_MEMBERS',
    VOICE_DEAFEN_MEMBERS = 'VOICE_DEAFEN_MEMBERS',
    VOICE_MOVE_MEMBERS = 'VOICE_MOVE_MEMBERS',
    VOICE_USE_VAD = 'VOICE_USE_VAD',
    NICKNAME_CHANGE = 'NICKNAME_CHANGE',
    NICKNAME_MANAGE = 'NICKNAME_MANAGE',
    MANAGE_ROLES = 'MANAGE_ROLES',
    MANAGE_PERMISSIONS = 'MANAGE_PERMISSIONS',
    MANAGE_WEBHOOKS = 'MANAGE_WEBHOOKS',
    MANAGE_GUILD_EXPRESSIONS = 'MANAGE_GUILD_EXPRESSIONS',
    USE_APPLICATION_COMMANDS = 'USE_APPLICATION_COMMANDS',
    MESSAGE_SEND_IN_THREADS = 'MESSAGE_SEND_IN_THREADS',
    CREATE_PUBLIC_THREADS = 'CREATE_PUBLIC_THREADS',
    CREATE_PRIVATE_THREADS = 'CREATE_PRIVATE_THREADS',
    MESSAGE_EXT_STICKER = 'MESSAGE_EXT_STICKER',
    MESSAGE_MANAGE_THREADS = 'MESSAGE_MANAGE_THREADS',
    USE_EMBEDDED_ACTIVITIES = 'USE_EMBEDDED_ACTIVITIES',
    MODERATE_MEMBERS = 'MODERATE_MEMBERS',
    VIEW_CREATOR_MONETIZATION_ANALYTICS = 'VIEW_CREATOR_MONETIZATION_ANALYTICS',
    USE_SOUNDBOARD = 'USE_SOUNDBOARD',
    USE_EXTERNAL_SOUNDS = 'USE_EXTERNAL_SOUNDS',
    MESSAGE_ATTACH_VOICE_MESSAGE = 'MESSAGE_ATTACH_VOICE_MESSAGE',
    USE_CLYDE_AI = 'USE_CLYDE_AI',
    SET_VOICE_CHANNEL_STATUS = 'SET_VOICE_CHANNEL_STATUS',
    SEND_POLLS = 'SEND_POLLS',
    USE_EXTERNAL_APPS = 'USE_EXTERNAL_APPS',
    BYPASS_SLOWMODE = 'BYPASS_SLOWMODE',
}

export const PermissionNames: Record<Permission, string> = {
    [Permission.CREATE_INSTANT_INVITE]: 'Create Instant Invite',
    [Permission.KICK_MEMBERS]: 'Kick Members',
    [Permission.BAN_MEMBERS]: 'Ban Members',
    [Permission.ADMINISTRATOR]: 'Administrator',
    [Permission.MANAGE_CHANNEL]: 'Manage Channel',
    [Permission.MANAGE_SERVER]: 'Manage Server',
    [Permission.MESSAGE_ADD_REACTION]: 'Add Reactions',
    [Permission.VIEW_AUDIT_LOG]: 'View Audit Log',
    [Permission.PRIORITY_SPEAKER]: 'Priority Speaker',
    [Permission.VIEW_CHANNEL]: 'View Channel',
    [Permission.MESSAGE_SEND]: 'Send Messages',
    [Permission.MESSAGE_TTS]: 'Send TTS Messages',
    [Permission.MESSAGE_MANAGE]: 'Manage Messages',
    [Permission.MESSAGE_EMBED_LINKS]: 'Embed Links',
    [Permission.MESSAGE_ATTACH_FILES]: 'Attach Files',
    [Permission.MESSAGE_HISTORY]: 'Read Message History',
    [Permission.MESSAGE_MENTION_EVERYONE]: 'Mention Everyone',
    [Permission.MESSAGE_EXT_EMOJI]: 'Use External Emojis',
    [Permission.VIEW_GUILD_INSIGHTS]: 'View Guild Insights',
    [Permission.VOICE_CONNECT]: 'Connect',
    [Permission.VOICE_SPEAK]: 'Speak',
    [Permission.VOICE_MUTE_MEMBERS]: 'Mute Members',
    [Permission.VOICE_DEAFEN_MEMBERS]: 'Deafen Members',
    [Permission.VOICE_MOVE_MEMBERS]: 'Move Members',
    [Permission.VOICE_USE_VAD]: 'Use Voice Activity',
    [Permission.NICKNAME_CHANGE]: 'Change Nickname',
    [Permission.NICKNAME_MANAGE]: 'Manage Nicknames',
    [Permission.MANAGE_ROLES]: 'Manage Roles',
    [Permission.MANAGE_PERMISSIONS]: 'Manage Permissions',
    [Permission.MANAGE_WEBHOOKS]: 'Manage Webhooks',
    [Permission.MANAGE_GUILD_EXPRESSIONS]: 'Manage Guild Expressions',
    [Permission.USE_APPLICATION_COMMANDS]: 'Use Application Commands',
    [Permission.MESSAGE_SEND_IN_THREADS]: 'Send Messages in Threads',
    [Permission.CREATE_PUBLIC_THREADS]: 'Create Public Threads',
    [Permission.CREATE_PRIVATE_THREADS]: 'Create Private Threads',
    [Permission.MESSAGE_EXT_STICKER]: 'Use External Stickers',
    [Permission.MESSAGE_MANAGE_THREADS]: 'Manage Threads',
    [Permission.USE_EMBEDDED_ACTIVITIES]: 'Use Embedded Activities',
    [Permission.MODERATE_MEMBERS]: 'Moderate Members',
    [Permission.VIEW_CREATOR_MONETIZATION_ANALYTICS]: 'View Creator Monetization Analytics',
    [Permission.USE_SOUNDBOARD]: 'Use Soundboard',
    [Permission.USE_EXTERNAL_SOUNDS]: 'Use External Sounds',
    [Permission.MESSAGE_ATTACH_VOICE_MESSAGE]: 'Send Voice Messages',
    [Permission.USE_CLYDE_AI]: 'Use Clyde AI',
    [Permission.SET_VOICE_CHANNEL_STATUS]: 'Set Voice Channel Status',
    [Permission.SEND_POLLS]: 'Send Polls',
    [Permission.USE_EXTERNAL_APPS]: 'Use External Apps',
    [Permission.BYPASS_SLOWMODE]: 'Bypass Slowmode',
};

export enum PermissionScope {
    GUILD = 'GUILD',
    CHANNEL = 'CHANNEL',
    CATEGORY = 'CATEGORY',
}

export interface MissingPermissions {
    scope: PermissionScope;
    id: string;
    permissions: Permission[];
}

export enum RanksProblemType {
    CANT_ASSIGN_ROLE = 'CANT_ASSIGN_ROLE',
    MISSING_ROLE = 'MISSING_ROLE',
}

export interface RankProblem {
    id: string;
    types: RanksProblemType[];
}

export enum ReputationChannelProblemType {
    MISSING_CHANNEL = 'MISSING_CHANNEL',
    MISSING_CATEGORY = 'MISSING_CATEGORY',
    NOT_TEXT_TYPE = 'NOT_TEXT_TYPE',
}

export interface ReputationChannelProblem {
    id: string;
    type: ReputationChannelProblemType;
}

export enum SimpleProblems {
    SYSTEM_CHANNEL_NOT_DEFINED = 'SYSTEM_CHANNEL_NOT_DEFINED',
    SYSTEM_CHANNEL_NOT_FOUND = 'SYSTEM_CHANNEL_NOT_FOUND',
    NO_ANNOUNCEMENT_CHANNEL_DEFINED = 'NO_ANNOUNCEMENT_CHANNEL_DEFINED',
    ANNOUNCEMENT_CHANNEL_NOT_FOUND = 'ANNOUNCEMENT_CHANNEL_NOT_FOUND',
    NO_AUTOPOST_CHANNEL_DEFINED = 'NO_AUTOPOST_CHANNEL_DEFINED',
    AUTOPOST_CANNEL_NOT_FOUND = 'AUTOPOST_CANNEL_NOT_FOUND',
    NO_LOG_CHANNEL_DEFINED = 'NO_LOG_CHANNEL_DEFINED',
    LOG_CHANNEL_NOT_FOUND = 'LOG_CHANNEL_NOT_FOUND',
    NO_THANKWORDS_DEFINED = 'NO_THANKWORDS_DEFINED',
    NO_REPUTATION_CHANNEL_DEFINED = 'NO_REPUTATION_CHANNEL_DEFINED',
}

export enum SimpleWarning {
    MAX_MESSAGE_AGE_LOW = 'MAX_MESSAGE_AGE_LOW',
}

export interface DebugResultPOJO {
    missingGlobalPermissions: Permission[];
    simpleProblems: SimpleProblems[];
    missingPermissions: MissingPermissions[];
    rankProblems: RankProblem[];
    reputationChannelProblems: ReputationChannelProblem[];
    simpleWarnings: SimpleWarning[];
}
