package de.chojo.repbot.commands.locale.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Format;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.util.Completion;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;

public class Set implements SlashHandler {
    private final Guilds guilds;

    public Set(Guilds guilds) {
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        DiscordLocale locale = null;
        for (var value : DiscordLocale.values()) {
            if (value.getNativeName().equals(event.getOption("language").getAsString())) {
                locale = value;
            }
        }

        if (locale == null || !context.guildLocalizer().localizer().languages().contains(locale)) {
            event.reply(context.localize("command.locale.set.message.invalidLocale")).setEphemeral(true).queue();
            return;
        }

        if (guilds.guild(event.getGuild()).settings().general().language(locale)) {
            event.reply(context.localize("command.locale.set.message.set",
                    Replacement.create("LOCALE", locale.getNativeName(), Format.CODE))).queue();
        }
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, EventContext context) {
        var option = event.getFocusedOption();
        if ("language".equalsIgnoreCase(option.getName())) {
            event.replyChoices(Completion.complete(option.getValue(), context.guildLocalizer().localizer().languages(), DiscordLocale::getNativeName)).queue();
        }
    }
}
