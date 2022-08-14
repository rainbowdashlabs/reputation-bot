package de.chojo.repbot.commands.thankwords.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Collectors;

public class List implements SlashHandler {
    private final Guilds guilds;

    public List(Guilds guilds) {
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var pattern = getGuildPattern(event.getGuild());
        if (pattern == null) return;

        event.reply(context.localize("command.thankwords.list.message.list") + "\n" + pattern).queue();
    }

    @Nullable
    private String getGuildPattern(Guild guild) {
        return guilds.guild(guild).settings().thanking().thankwords().words().stream()
                .map(w -> StringUtils.wrap(w, "`"))
                .collect(Collectors.joining(", "));
    }
}
