package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.Localizer;
import de.chojo.jdautil.localization.util.Format;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
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
                                .add(OptionType.STRING, "prefix", "prefix")
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
    public boolean onCommand(MessageEventWrapper eventWrapper, CommandContext context) {
        var optSubCmd = context.argString(0);
        if (optSubCmd.isPresent()) {
            var subCmd = optSubCmd.get();
            if ("set".equalsIgnoreCase(subCmd)) {
                return set(eventWrapper, context.subContext(subCmd));
            }

            if ("reset".equalsIgnoreCase(subCmd)) {
                return reset(eventWrapper);
            }
            return false;
        }
        return get(eventWrapper);
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        var subCmd = event.getSubcommandName();
        if ("set".equalsIgnoreCase(subCmd)) {
            final var response = set(event.getOption("prefix").getAsString(), event.getGuild());
            if (response != null) {
                event.reply(response).queue();
            }
        }

        if ("reset".equalsIgnoreCase(subCmd)) {
            reset(event);
        }
    }

    private boolean reset(MessageEventWrapper eventWrapper) {
        final var response = changePrefix(eventWrapper.getGuild(), configuration.defaultPrefix());
        if (response != null) {
            eventWrapper.reply(response).queue();
        }
        return true;
    }

    private void reset(SlashCommandEvent event) {
        final var response = changePrefix(event.getGuild(), configuration.defaultPrefix());
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

    private boolean set(MessageEventWrapper eventWrapper, CommandContext subContext) {
        var optArg = subContext.argString(0);
        if (optArg.isEmpty()) return false;
        var prefix = optArg.get();

        final var response = set(prefix, eventWrapper.getGuild());
        if (response != null) {
            eventWrapper.reply(response).queue();
        }
        return true;
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

    private boolean get(MessageEventWrapper eventWrapper) {
        var prefix = data.getPrefix(eventWrapper.getGuild()).orElse(configuration.defaultPrefix());
        eventWrapper.reply(eventWrapper.localize("command.prefix.show",
                Replacement.create("PREFIX", prefix, Format.CODE))).queue();
        return true;
    }


}
