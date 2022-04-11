package de.chojo.repbot.commands;

import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.util.Format;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.data.GuildData;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.Nullable;

import javax.sql.DataSource;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Prefix extends SimpleCommand {
    private final GuildData data;
    private final Configuration configuration;

    public Prefix(DataSource dataSource, Configuration configuration) {
        super(CommandMeta.builder("prefix", "command.prefix.description")
                .addSubCommand("set", "command.prefix.sub.set", argsBuilder()
                        .add(SimpleArgument.string("prefix", "prefix").asRequired()))
                .addSubCommand("reset", "command.prefix.sub.reset")
                .withPermission());
        data = new GuildData(dataSource);
        this.configuration = configuration;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var subCmd = event.getSubcommandName();
        if ("set".equalsIgnoreCase(subCmd)) {
            var response = set(event.getGuild(), context, event.getOption("prefix").getAsString());
            if (response != null) {
                event.reply(response).queue();
            }
        }

        if ("reset".equalsIgnoreCase(subCmd)) {
            reset(event, context);
        }
    }

    private void reset(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var response = changePrefix(event.getGuild(), context, configuration.baseSettings().defaultPrefix());
        if (response != null) {
            event.reply(response).queue();
        }
    }

    @Nullable
    private String changePrefix(Guild guild, SlashCommandContext context, String prefix) {
        if (data.setPrefix(guild, prefix)) {
            return context.localize("command.prefix.changed", Replacement.create("PREFIX", prefix, Format.CODE));
        }

        return null;
    }

    @Nullable
    private String set(Guild guild, SlashCommandContext context, String prefix) {
        if (!prefix.startsWith("re:") && prefix.length() > 3) {
            return context.localize("error.prefixTooLong");
        }
        if (prefix.startsWith("re:")) {
            if ("re:".equalsIgnoreCase(prefix)) {
                context.localize("error.invalidRegex");
            }
            var substring = prefix.substring(3);
            if (!substring.startsWith("^")) {
                substring = "^" + substring;
            }
            try {
                Pattern.compile(substring);
            } catch (PatternSyntaxException e) {
                context.localize("error.invalidRegex");
            }
            prefix = "re:" + substring;
        }
        return changePrefix(guild, context, prefix);
    }
}
