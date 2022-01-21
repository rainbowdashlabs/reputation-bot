package de.chojo.repbot.listener;

import de.chojo.jdautil.command.dispatching.CommandHub;
import de.chojo.jdautil.localization.ILocalizer;
import de.chojo.jdautil.parsing.ArgumentUtil;
import de.chojo.jdautil.parsing.DiscordResolver;
import de.chojo.jdautil.parsing.Verifier;
import de.chojo.repbot.data.GuildData;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.util.regex.Pattern;

public class LegacyCommandListener extends ListenerAdapter {

    private final ShardManager shardManager;
    private final ILocalizer localizer;
    private final CommandHub<?> commandHub;
    private final GuildData guildData;

    public LegacyCommandListener(ShardManager shardManager, ILocalizer localizer, DataSource dataSource, CommandHub<?> commandHub) {
        this.shardManager = shardManager;
        this.localizer = localizer;
        this.commandHub = commandHub;
        guildData = new GuildData(dataSource);
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.isFromGuild() || event.getAuthor().isBot()) {
            return;
        }

        if (event.getMessage().getType() != MessageType.INLINE_REPLY && event.getMessage().getType() != MessageType.DEFAULT) {
            return;
        }

        var guildSettings = guildData.getGuildSettings(event.getGuild());
        var contentRaw = event.getMessage().getContentRaw();
        var prefix = guildSettings.generalSettings().prefix().orElse("!");

        var splitted = contentRaw.split(" ");
        var user = DiscordResolver.getUser(shardManager, splitted[0]);

        String[] stripped;
        if (user.isEmpty() || !Verifier.equalSnowflake(user.get(), event.getJDA().getSelfUser())) {
            if (prefix.startsWith("re:")) {
                var pattern = Pattern.compile(prefix.substring(3));
                if (!pattern.matcher(contentRaw).find()) return;
                stripped = pattern.matcher(contentRaw).replaceAll("").split("\\s+");
            } else {
                if (!contentRaw.startsWith(prefix)) return;
                stripped = contentRaw.substring(prefix.length()).split("\\s+");
            }
        } else {
            stripped = ArgumentUtil.getRangeAsList(splitted, 1).toArray(new String[0]);
        }

        if (stripped.length == 0) return;

        var optCommand = commandHub.getCommand(stripped[0]);

        if (optCommand.isPresent()) {
            event.getChannel().sendMessage(localizer.localize("error.textCommand", event.getGuild())).queue();
        }
    }
}
