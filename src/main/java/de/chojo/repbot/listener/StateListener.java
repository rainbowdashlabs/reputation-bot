package de.chojo.repbot.listener;

import de.chojo.jdautil.localization.ILocalizer;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.provider.Guilds;
import de.chojo.repbot.dao.provider.Metrics;
import de.chojo.repbot.dao.snapshots.ReputationRank;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.emoji.EmojiRemovedEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

public class StateListener extends ListenerAdapter {
    private static final Logger log = getLogger(StateListener.class);
    private final Guilds guilds;
    private final ILocalizer localizer;
    private final Configuration configuration;
    private final Metrics metrics;

    private StateListener(Guilds guilds, ILocalizer localizer, Configuration configuration, Metrics metrics) {
        this.guilds = guilds;
        this.localizer = localizer;
        this.configuration = configuration;
        this.metrics = metrics;
    }

    @Override
    public void onGenericInteractionCreate(@NotNull GenericInteractionCreateEvent event) {
        metrics.service().countInteraction();
    }

    public static StateListener of(ILocalizer localizer, Guilds guilds, Configuration configuration, Metrics metrics) {
        return new StateListener(guilds, localizer, configuration, metrics);
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        guilds.guild(event.getGuild()).gdpr().dequeueDeletion();

        if (configuration.botlist().isBotlistGuild(event.getGuild().getIdLong())) return;

        var selfMember = event.getGuild().getSelfMember();
        for (var channel : event.getGuild().getTextChannels()) {
            if (selfMember.hasPermission(channel, Permission.VIEW_CHANNEL)
                && selfMember.hasPermission(channel, Permission.MESSAGE_SEND)) {
                channel.sendMessage(localizer.localize("message.welcome", event.getGuild()))
                       .queueAfter(5, TimeUnit.SECONDS);
                break;
            }
        }
    }

    @Override
    public void onGuildLeave(@NotNull GuildLeaveEvent event) {
        // We want to delete all data of a guild after the bot left.
        guilds.guild(event.getGuild()).gdpr().queueDeletion();
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        // We want to abort deletion of user data if a user rejoins a guild during grace period
        guilds.guild(event.getGuild()).reputation().user(event.getMember()).gdpr().dequeueDeletion();
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        // When a user leaves a guild, there is no reason for us to keep their data.
        guilds.guild(event.getGuild()).reputation().user(event.getUser()).gdpr().queueDeletion();
    }

    @Override
    public void onRoleDelete(@NotNull RoleDeleteEvent event) {
        guilds.guild(event.getGuild()).settings().ranks().rank(event.getRole()).ifPresent(ReputationRank::remove);
    }

    @Override
    public void onChannelDelete(@NotNull ChannelDeleteEvent event) {
        guilds.guild(event.getGuild()).settings().thanking().channels().remove(event.getChannel());
    }

    @Override
    public void onEmojiRemoved(@NotNull EmojiRemovedEvent event) {
        var guildSettings = guilds.guild(event.getGuild()).settings();
        if (!guildSettings.thanking().reactions().reactionIsEmote()) return;
        if (!guildSettings.thanking().reactions().mainReaction().equals(event.getEmoji().getId())) return;
        guildSettings.thanking().reactions().mainReaction("🏅");
    }
}
