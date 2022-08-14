package de.chojo.repbot.commands.thankwords.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.util.Completion;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.Guilds;
import de.chojo.repbot.serialization.ThankwordsContainer;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;
import java.util.stream.Collectors;

public class LoadDefault implements SlashHandler {
    private final Guilds guilds;
    private final ThankwordsContainer thankwordsContainer;

    public LoadDefault(Guilds guilds, ThankwordsContainer thankwordsContainer) {
        this.guilds = guilds;
        this.thankwordsContainer = thankwordsContainer;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var languageOption = event.getOption("language");
        if (languageOption == null) {
            event.reply(context.localize("command.thankwords.loaddefault.message.available")
                        + " " + String.join(", ", thankwordsContainer.getAvailableLanguages())).queue();
            return;
        }
        var language = languageOption.getAsString();
        var words = thankwordsContainer.get(language.toLowerCase(Locale.ROOT));
        if (words == null) {
            event.reply(context.localize("command.locale.error.invalidLocale"))
                    .setEphemeral(true)
                    .queue();
            return;
        }
        for (var word : words) {
            guilds.guild(event.getGuild()).settings().thanking().thankwords().add(word);
        }

        var wordsJoined = words.stream().map(w -> StringUtils.wrap(w, "`")).collect(Collectors.joining(", "));

        event.reply(context.localize("command.thankwords.loaddefault.message.added") + wordsJoined).queue();
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, EventContext context) {
        var option = event.getFocusedOption();
        event.replyChoices(Completion.complete(option.getValue(), thankwordsContainer.getAvailableLanguages())).queue();
    }
}
