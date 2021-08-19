package de.chojo.repbot.listener;

import de.chojo.jdautil.localization.ILocalizer;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.data.GdprData;
import de.chojo.repbot.data.GuildData;
import de.chojo.repbot.data.wrapper.GuildSettings;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.events.emote.EmoteRemovedEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.sql.DataSource;

import static org.slf4j.LoggerFactory.getLogger;

public class StateListener extends ListenerAdapter {
    private static final Logger log = getLogger(StateListener.class);
    private final GuildData guildData;
    private final GdprData gdprData;
    private final ILocalizer localizer;
    private final Configuration configuration;

    private StateListener(ILocalizer localizer, DataSource dataSource, Configuration configuration) {
        this.localizer = localizer;
        guildData = new GuildData(dataSource);
        this.gdprData = new GdprData(dataSource);
        this.configuration = configuration;
    }

    public static StateListener of(ILocalizer localizer, DataSource dataSource, Configuration configuration) {
        return new StateListener(localizer, dataSource, configuration);
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        guildData.initGuild(event.getGuild());
        gdprData.dequeueGuildDeletion(event.getGuild());

        if (configuration.botlist().isBotlistGuild(event.getGuild().getIdLong())) return;

        var selfMember = event.getGuild().getSelfMember();
        for (var channel : event.getGuild().getTextChannels()) {
            if (selfMember.hasPermission(channel, Permission.VIEW_CHANNEL)
                && selfMember.hasPermission(channel, Permission.MESSAGE_WRITE)) {
                channel.sendMessage(localizer.localize("message.welcome", event.getGuild())).queue();
                break;
            }
        }

        guildData.migrated(event.getGuild());
    }

    @Override
    public void onGuildLeave(@NotNull GuildLeaveEvent event) {
        if (!configuration.migration().isActive()) {
            gdprData.queueGuildDeletion(event.getGuild());
        }
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        gdprData.dequeueGuildUserDeletion(event.getMember());
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        gdprData.queueGuildUserDeletion(event.getUser(), event.getGuild());
    }

    @Override
    public void onRoleDelete(@NotNull RoleDeleteEvent event) {
        guildData.removeReputationRole(event.getGuild(), event.getRole());
    }

    @Override
    public void onTextChannelDelete(@NotNull TextChannelDeleteEvent event) {
        guildData.removeChannel(event.getGuild(), event.getChannel());
    }

    @Override
    public void onEmoteRemoved(@NotNull EmoteRemovedEvent event) {
        GuildSettings guildSettings = guildData.getGuildSettings(event.getGuild());
        if (!guildSettings.thankSettings().reactionIsEmote()) return;
        if (!guildSettings.thankSettings().reaction().equals(event.getEmote().getId())) return;
        guildData.setMainReaction(event.getGuild(), "🏅");
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        event.getJDA().getGuildCache().forEach(guildData::initGuild);
    }
}
