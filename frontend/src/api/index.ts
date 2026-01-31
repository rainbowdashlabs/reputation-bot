import axios, {type AxiosInstance } from 'axios';
import * as Types from './types';

class ApiClient {
  private axiosInstance: AxiosInstance;

  constructor() {
    this.axiosInstance = axios.create({
      baseURL: '/v1',
    });

    const token = localStorage.getItem('token');
    if (token) {
      this.setToken(token);
    }
  }

  public setToken(token: string) {
    this.axiosInstance.defaults.headers.common['Authorization'] = token;
    localStorage.setItem('token', token);
  }

  public async getSession(): Promise<Types.GuildSessionPOJO> {
    const response = await this.axiosInstance.get<Types.GuildSessionPOJO>('/session');
    return response.data;
  }

  // General Settings
  public async updateGeneral(data: Types.GeneralPOJO) {
    await this.axiosInstance.post('/settings/general', data);
  }

  public async updateGeneralLanguage(language: string) {
    await this.axiosInstance.post('/settings/general/language', JSON.stringify(language), {
      headers: { 'Content-Type': 'application/json' }
    });
  }

  public async updateGeneralStackRoles(stackRoles: boolean) {
    await this.axiosInstance.post('/settings/general/stackroles', stackRoles, {
      headers: { 'Content-Type': 'application/json' }
    });
  }

  public async updateGeneralReputationMode(mode: Types.ReputationMode) {
    await this.axiosInstance.post('/settings/general/reputationmode', JSON.stringify(mode), {
      headers: { 'Content-Type': 'application/json' }
    });
  }

  public async updateGeneralSystemChannel(channelId: string) {
    await this.axiosInstance.post('/settings/general/systemchannel', channelId, {
      headers: { 'Content-Type': 'application/json' }
    });
  }

  // Abuse Protection
  public async updateAbuseProtection(data: Types.AbuseProtectionPOJO) {
    await this.axiosInstance.post('/settings/abuseprotection', data);
  }

  public async updateAbuseProtectionCooldown(cooldown: number) {
    await this.axiosInstance.post('/settings/abuseprotection/cooldown', cooldown, {
      headers: { 'Content-Type': 'application/json' }
    });
  }

  public async updateAbuseProtectionCooldownDirection(direction: Types.CooldownDirection) {
    await this.axiosInstance.post('/settings/abuseprotection/cooldowndirection', JSON.stringify(direction), {
      headers: { 'Content-Type': 'application/json' }
    });
  }

  public async updateAbuseProtectionMaxMessageAge(age: number) {
    await this.axiosInstance.post('/settings/abuseprotection/maxmessageage', age, {
      headers: { 'Content-Type': 'application/json' }
    });
  }

  public async updateAbuseProtectionMinMessages(min: number) {
    await this.axiosInstance.post('/settings/abuseprotection/minmessages', min, {
      headers: { 'Content-Type': 'application/json' }
    });
  }

  public async updateAbuseProtectionDonorContext(active: boolean) {
    await this.axiosInstance.post('/settings/abuseprotection/donorcontext', active, {
      headers: { 'Content-Type': 'application/json' }
    });
  }

  public async updateAbuseProtectionReceiverContext(active: boolean) {
    await this.axiosInstance.post('/settings/abuseprotection/receivercontext', active, {
      headers: { 'Content-Type': 'application/json' }
    });
  }

  public async updateAbuseProtectionMaxGiven(max: number) {
    await this.axiosInstance.post('/settings/abuseprotection/maxgiven', max, {
      headers: { 'Content-Type': 'application/json' }
    });
  }

  public async updateAbuseProtectionMaxGivenHours(hours: number) {
    await this.axiosInstance.post('/settings/abuseprotection/maxgivenhours', hours, {
      headers: { 'Content-Type': 'application/json' }
    });
  }

  public async updateAbuseProtectionMaxReceived(max: number) {
    await this.axiosInstance.post('/settings/abuseprotection/maxreceived', max, {
      headers: { 'Content-Type': 'application/json' }
    });
  }

  public async updateAbuseProtectionMaxReceivedHours(hours: number) {
    await this.axiosInstance.post('/settings/abuseprotection/maxreceivedhours', hours, {
      headers: { 'Content-Type': 'application/json' }
    });
  }

  public async updateAbuseProtectionMaxMessageReputation(max: number) {
    await this.axiosInstance.post('/settings/abuseprotection/maxmessagereputation', max, {
      headers: { 'Content-Type': 'application/json' }
    });
  }

  // Reputation Settings
  public async updateReputation(data: Types.ReputationPOJO) {
    await this.axiosInstance.post('/settings/reputation', data);
  }

  public async updateReputationReactionActive(active: boolean) {
    await this.axiosInstance.post('/settings/reputation/reactionactive', active, {
      headers: { 'Content-Type': 'application/json' }
    });
  }

  public async updateReputationAnswerActive(active: boolean) {
    await this.axiosInstance.post('/settings/reputation/answeractive', active, {
      headers: { 'Content-Type': 'application/json' }
    });
  }

  public async updateReputationMentionActive(active: boolean) {
    await this.axiosInstance.post('/settings/reputation/mentionactive', active, {
      headers: { 'Content-Type': 'application/json' }
    });
  }

  public async updateReputationFuzzyActive(active: boolean) {
    await this.axiosInstance.post('/settings/reputation/fuzzyactive', active, {
      headers: { 'Content-Type': 'application/json' }
    });
  }

  public async updateReputationEmbedActive(active: boolean) {
    await this.axiosInstance.post('/settings/reputation/embedactive', active, {
      headers: { 'Content-Type': 'application/json' }
    });
  }

  public async updateReputationDirectActive(active: boolean) {
    await this.axiosInstance.post('/settings/reputation/directactive', active, {
      headers: { 'Content-Type': 'application/json' }
    });
  }

  public async updateReputationCommandActive(active: boolean) {
    await this.axiosInstance.post('/settings/reputation/commandactive', active, {
      headers: { 'Content-Type': 'application/json' }
    });
  }

  // Announcements
  public async updateAnnouncements(data: Types.AnnouncementsPOJO) {
    await this.axiosInstance.post('/settings/announcements', data);
  }

  public async updateAnnouncementsActive(active: boolean) {
    await this.axiosInstance.post('/settings/announcements/active', active, {
      headers: { 'Content-Type': 'application/json' }
    });
  }

  public async updateAnnouncementsSameChannel(same: boolean) {
    await this.axiosInstance.post('/settings/announcements/samechannel', same, {
      headers: { 'Content-Type': 'application/json' }
    });
  }

  public async updateAnnouncementsChannel(channelId: string) {
    await this.axiosInstance.post('/settings/announcements/channel', channelId, {
      headers: { 'Content-Type': 'application/json' }
    });
  }

  // Messages
  public async updateMessages(data: Types.MessagesPOJO) {
    await this.axiosInstance.post('/settings/messages', data);
  }

  public async updateMessagesReactionConfirmation(active: boolean) {
    await this.axiosInstance.post('/settings/messages/reactionconfirmation', active, {
      headers: { 'Content-Type': 'application/json' }
    });
  }

  public async updateMessagesCommandReputationEphemeral(active: boolean) {
    await this.axiosInstance.post('/settings/messages/commandreputationephemeral', active, {
      headers: { 'Content-Type': 'application/json' }
    });
  }

  // Autopost
  public async updateAutopost(data: Types.AutopostPOJO) {
    await this.axiosInstance.post('/settings/autopost', data);
  }

  public async updateAutopostActive(active: boolean) {
    await this.axiosInstance.post('/settings/autopost/active', active, {
      headers: { 'Content-Type': 'application/json' }
    });
  }

  public async updateAutopostChannel(channelId: string) {
    await this.axiosInstance.post('/settings/autopost/channel', channelId, {
      headers: { 'Content-Type': 'application/json' }
    });
  }

  public async updateAutopostMessage(messageId: string) {
    await this.axiosInstance.post('/settings/autopost/message', messageId, {
      headers: { 'Content-Type': 'application/json' }
    });
  }

  public async updateAutopostRefreshType(type: Types.RefreshType) {
    await this.axiosInstance.post('/settings/autopost/refreshtype', JSON.stringify(type), {
      headers: { 'Content-Type': 'application/json' }
    });
  }

  public async updateAutopostRefreshInterval(interval: Types.RefreshInterval) {
    await this.axiosInstance.post('/settings/autopost/refreshinterval', JSON.stringify(interval), {
      headers: { 'Content-Type': 'application/json' }
    });
  }

  // Log Channel
  public async updateLogChannel(data: Types.LogChannelPOJO) {
    await this.axiosInstance.post('/settings/logchannel', data);
  }

  public async updateLogChannelActive(active: boolean) {
    await this.axiosInstance.post('/settings/logchannel/active', active, {
      headers: { 'Content-Type': 'application/json' }
    });
  }

  public async updateLogChannelId(channelId: string) {
    await this.axiosInstance.post('/settings/logchannel/channel', channelId, {
      headers: { 'Content-Type': 'application/json' }
    });
  }

  // Thanking Settings
  public async updateThanking(data: Types.ThankingPOJO) {
    await this.axiosInstance.post('/settings/thanking', data);
  }

  public async updateThankingChannels(data: Types.ChannelsSettingsPOJO) {
    await this.axiosInstance.post('/settings/thanking/channels', data);
  }

  public async updateThankingChannelsWhitelist(whitelist: boolean) {
    await this.axiosInstance.post('/settings/thanking/channels/whitelist', whitelist, {
      headers: { 'Content-Type': 'application/json' }
    });
  }

  public async updateThankingChannelsList(channels: string[]) {
    await this.axiosInstance.post('/settings/thanking/channels/channels', channels);
  }

  public async updateThankingCategoriesList(categories: string[]) {
    await this.axiosInstance.post('/settings/thanking/channels/categories', categories);
  }

  public async updateThankingDonorRoles(data: Types.RolesHolderPOJO) {
    await this.axiosInstance.post('/settings/thanking/donorroles', data);
  }

  public async updateThankingReceiverRoles(data: Types.RolesHolderPOJO) {
    await this.axiosInstance.post('/settings/thanking/receiverroles', data);
  }

  public async updateThankingReactions(data: Types.ReactionsSettingsPOJO) {
    await this.axiosInstance.post('/settings/thanking/reactions', data);
  }

  public async updateThankingReactionsMain(reaction: string) {
    await this.axiosInstance.post('/settings/thanking/reactions/mainreaction', JSON.stringify(reaction), {
      headers: { 'Content-Type': 'application/json' }
    });
  }

  public async updateThankingReactionsList(reactions: string[]) {
    await this.axiosInstance.post('/settings/thanking/reactions/reactions', reactions);
  }

  public async updateThankingThankwords(data: Types.ThankwordsPOJO) {
    await this.axiosInstance.post('/settings/thanking/thankwords', data);
  }

  public async updateThankingThankwordsList(words: string[]) {
    await this.axiosInstance.post('/settings/thanking/thankwords/words', words);
  }
}

export const api = new ApiClient();
