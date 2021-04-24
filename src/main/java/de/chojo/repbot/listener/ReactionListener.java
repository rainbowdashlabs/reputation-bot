package de.chojo.repbot.listener;

import de.chojo.repbot.manager.RoleAssigner;
import de.chojo.repbot.data.GuildData;
import de.chojo.repbot.data.ReputationData;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class ReactionListener extends ListenerAdapter {
    private final GuildData guildData;
    private final ReputationData reputationData;
    private final RoleAssigner roleAssigner;

    public ReactionListener(DataSource dataSource, RoleAssigner roleAssigner) {
        guildData = new GuildData(dataSource);
        reputationData = new ReputationData(dataSource);
        this.roleAssigner = roleAssigner;
    }

    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        if (event.getUser().isBot()) return;
        var optGuildSettings = guildData.getGuildSettings(event.getGuild());
        if (optGuildSettings.isEmpty()) return;
        var guildSettings = optGuildSettings.get();
        if (!guildSettings.isReputationChannel(event.getChannel())) return;

        if (!guildSettings.isReactionActive()) return;

        if (!guildSettings.isReaction(event.getReaction().getReactionEmote())) return;

        event.getChannel().retrieveMessageById(event.getMessageId()).queue(m -> {
            var until = m.getTimeCreated().toInstant().until(Instant.now(), ChronoUnit.MINUTES);
            if (until > guildSettings.getMaxMessageAge()) return;

            var lastRatedDuration = reputationData.getLastRatedDuration(event.getGuild(), event.getUser(), m.getAuthor(), ChronoUnit.MINUTES);
            if (lastRatedDuration > guildSettings.getCooldown()) return;
            reputationData.logReputation(event.getGuild(), event.getUser(), m.getAuthor(), m);
            roleAssigner.update(m.getMember());
        });

    }
}
