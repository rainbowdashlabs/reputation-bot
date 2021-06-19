package de.chojo.repbot.listener;

import de.chojo.jdautil.localization.ILocalizer;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.data.GuildData;
import de.chojo.repbot.data.wrapper.GuildSettingUpdate;
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

public class StateListener extends ListenerAdapter implements Runnable {
    private static final Logger log = getLogger(StateListener.class);
    private final GuildData data;
    private final ILocalizer localizer;
    private final Configuration configuration;

    public StateListener(ILocalizer localizer, DataSource dataSource, Configuration configuration) {
        this.localizer = localizer;
        data = new GuildData(dataSource);
        this.configuration = configuration;
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        data.initGuild(event.getGuild());
        data.dequeueDeletion(event.getGuild());

        if (configuration.botlist().isBotlistGuild(event.getGuild().getIdLong())) return;

        var selfMember = event.getGuild().getSelfMember();
        for (var channel : event.getGuild().getTextChannels()) {
            if (selfMember.hasPermission(channel, Permission.VIEW_CHANNEL)
                    && selfMember.hasPermission(channel, Permission.MESSAGE_WRITE)) {
                channel.sendMessage(localizer.localize("message.welcome", event.getGuild())).queue();
                break;
            }
        }
    }

    @Override
    public void onGuildLeave(@NotNull GuildLeaveEvent event) {
        data.queueDeletion(event.getGuild());
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        data.dequeueDeletion(event.getMember());
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        data.queueDeletion(event.getUser(), event.getGuild());
    }

    @Override
    public void onRoleDelete(@NotNull RoleDeleteEvent event) {
        data.removeReputationRole(event.getGuild(), event.getRole());
    }

    @Override
    public void onTextChannelDelete(@NotNull TextChannelDeleteEvent event) {
        data.removeChannel(event.getGuild(), event.getChannel());
    }

    @Override
    public void onEmoteRemoved(@NotNull EmoteRemovedEvent event) {
        var guildSettings = data.getGuildSettings(event.getGuild());
        if (guildSettings.isEmpty()) return;
        if (!guildSettings.get().reactionIsEmote()) return;
        if (!guildSettings.get().reaction().equals(event.getEmote().getId())) return;
        data.updateMessageSettings(GuildSettingUpdate.builder(event.getGuild()).reaction("✅").build());
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        event.getJDA().getGuildCache().forEach(data::initGuild);
    }

    @Override
    public void run() {
        data.getRemovalTasks().forEach(data::executeRemovalTask);
    }
}
