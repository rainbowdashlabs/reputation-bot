/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo;

import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.web.pojo.guild.GuildPOJO;
import de.chojo.repbot.web.pojo.premium.PremiumFeaturesPOJO;
import de.chojo.repbot.web.pojo.settings.SettingsPOJO;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.sharding.ShardManager;

public class GuildSessionPOJO {
    SettingsPOJO settings;
    GuildPOJO guild;
    PremiumFeaturesPOJO premiumFeatures;

    public GuildSessionPOJO(SettingsPOJO settings, GuildPOJO guild, PremiumFeaturesPOJO premiumFeatures) {
        this.settings = settings;
        this.guild = guild;
        this.premiumFeatures = premiumFeatures;
    }

    public static GuildSessionPOJO generate(Guild guild, GuildRepository guildRepository, ShardManager shardManager) {
        var repGuild = guildRepository.guild(guild);
        return new GuildSessionPOJO(
                SettingsPOJO.generate(guild, guildRepository),
                GuildPOJO.generate(guild),
                PremiumFeaturesPOJO.generate(repGuild, shardManager)
        );
    }
}
