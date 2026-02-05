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
