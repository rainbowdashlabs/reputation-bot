package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.Localizer;
import de.chojo.jdautil.localization.util.Format;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.data.GuildData;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.Nullable;

import javax.sql.DataSource;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Prefix extends SimpleCommand {
    private final GuildData data;
    private final Configuration configuration;
    private final Localizer localizer;

    public Prefix(DataSource dataSource, Configuration configuration, Localizer localizer) {
        super("prefix",
                null,
                "command.prefix.description",
                subCommandBuilder()
                        .add("set", "command.prefix.sub.set", argsBuilder()
                                .add(OptionType.STRING, "prefix", "prefix", true)
                                .build()
                        )
                        .add("reset", "command.prefix.sub.reset")
                        .build(),
                Permission.MANAGE_SERVER);
        data = new GuildData(dataSource);
        this.configuration = configuration;
        this.localizer = localizer;
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event, SlashCommandContext context) {
        var subCmd = event.getSubcommandName();
        if ("set".equalsIgnoreCase(subCmd)) {
            var response = set(event.getOption("prefix").getAsString(), event.getGuild());
            if (response != null) {
                event.reply(response).queue();
            }
        }

        if ("reset".equalsIgnoreCase(subCmd)) {
            reset(event);
        }
    }

    private void reset(SlashCommandEvent event) {
        var response = changePrefix(event.getGuild(), configuration.baseSettings().defaultPrefix());
        if (response != null) {
            event.reply(response).queue();
        }
    }

    @Nullable
    private String changePrefix(Guild guild, String prefix) {
        if (data.setPrefix(guild, prefix)) {
            return localizer.localize("command.prefix.changed", guild,
                    Replacement.create("PREFIX", prefix, Format.CODE));
        }

        return null;
    }

    @Nullable
    private String set(String prefix, Guild guild) {
        var loc = localizer.getContextLocalizer(guild);
        if (!prefix.startsWith("re:") && prefix.length() > 3) {
            return loc.localize("error.prefixTooLong");
        }
        if (prefix.startsWith("re:")) {
            if (prefix.equalsIgnoreCase("re:")) {
                loc.localize("error.invalidRegex");
            }
            var substring = prefix.substring(3);
            if (!substring.startsWith("^")) {
                substring = "^" + substring;
            }
            try {
                Pattern.compile(substring);
            } catch (PatternSyntaxException e) {
                loc.localize("error.invalidRegex");
            }
            prefix = "re:" + substring;
        }
        return changePrefix(guild, prefix);
    }
}
