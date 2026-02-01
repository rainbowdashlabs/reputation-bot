export interface RolePOJO {
  name: string;
  id: string;
  color: string;
}

export interface ReactionPOJO {
  name: string;
  id: string;
  url: string;
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
  stackRoles: boolean;
  language?: string;
  reputationMode: ReputationMode;
  resetDate: string; // ISO string
  systemChannel: string;
}

export interface LogChannelPOJO {
  channelId: string;
  active: boolean;
}

export interface MessagesPOJO {
  reactionConfirmation: boolean;
  commandReputationEphemeral: boolean;
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

export interface ThankingPOJO {
  channels: ChannelsSettingsPOJO;
  donorRoles: RolesHolderPOJO;
  receiverRoles: RolesHolderPOJO;
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
  reputation: ReputationPOJO;
  thanking: ThankingPOJO;
}

export interface SkuInfo {
  id: number;
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
  nickname: SimpleFeature;
  reputationChannel: FeatureLimit;
  reputationCategories: FeatureLimit;
}

export interface GuildSessionPOJO {
  settings: SettingsPOJO;
  guild: GuildPOJO;
  premiumFeatures: PremiumFeaturesPOJO;
}
