package de.chojo.repbot.commands;

import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.parsing.Verifier;
import de.chojo.jdautil.util.Choice;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.dao.access.guild.settings.Settings;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ListedEmote;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.slf4j.Logger;

import java.util.regex.Pattern;

import static org.slf4j.LoggerFactory.getLogger;

public class Reactions extends SimpleCommand {
    private static final Pattern EMOTE_PATTERN = Pattern.compile("<a?:.*?:(?<id>[0-9]*?)>");
    private final Guilds guilds;
    private static final Logger log = getLogger(Reactions.class);

    public Reactions(Guilds guilds) {
        super(CommandMeta.builder("reactions", "command.reaction.description")
                .addSubCommand("main", "command.reaction.sub.main", argsBuilder()
                        .add(SimpleArgument.string("emote", "command.reaction.sub.main.arg.emote").asRequired()))
                .addSubCommand("add", "command.reaction.sub.add", argsBuilder()
                        .add(SimpleArgument.string("emote", "command.reaction.sub.add.arg.emote").asRequired()))
                .addSubCommand("remove", "command.reaction.sub.remove", argsBuilder()
                        .add(SimpleArgument.string("emote", "command.reaction.sub.remove.arg.emote").withAutoComplete().asRequired()))
                .addSubCommand("info", "command.reaction.sub.info")
                .withPermission());
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var cmd = event.getSubcommandName();

        if ("main".equalsIgnoreCase(cmd)) {
            reaction(event, context);
        }

        if ("add".equalsIgnoreCase(cmd)) {
            add(event, context);
        }
        if ("remove".equalsIgnoreCase(cmd)) {
            remove(event, context);
        }
        if ("info".equalsIgnoreCase(cmd)) {
            info(event, context);
        }
    }

    private void info(SlashCommandInteractionEvent event, SlashCommandContext context) {
        event.replyEmbeds(getInfoEmbed(guilds.guild(event.getGuild()).settings(), context)).queue();
    }

    private MessageEmbed getInfoEmbed(Settings settings, SlashCommandContext context) {
        var reactions = settings.thanking().reactions();
        var mainEmote = reactions.reactionMention();
        var emotes = String.join(" ", reactions.getAdditionalReactionMentions());

        return new LocalizedEmbedBuilder(context.localizer())
                .setTitle("command.reaction.sub.info.title")
                .addField("command.reaction.sub.info.main", mainEmote.orElse("words.unknown"), true)
                .addField("command.reaction.sub.info.additional", emotes, true)
                .build();
    }

    private void reaction(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var emote = event.getOption("emote").getAsString();
        var message = event.reply(context.localize("command.reaction.checking"))
                .flatMap(InteractionHook::retrieveOriginal).complete();
        handleSetCheckResult(event.getGuild(), context, message, emote);
    }

    private void add(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var emote = event.getOption("emote").getAsString();
        var message = event.reply(context.localize("command.reaction.checking"))
                .flatMap(InteractionHook::retrieveOriginal).complete();
        handleAddCheckResult(event.getGuild(), context, message, emote);
    }

    private void remove(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var reactions = guilds.guild(event.getGuild()).settings().thanking().reactions();
        var emote = event.getOption("emote").getAsString();
        var matcher = EMOTE_PATTERN.matcher(emote);
        if (matcher.find()) {
            if (reactions.remove(matcher.group("id"))) {
                event.reply(context.localize("command.reaction.sub.remove.removed")).queue();
                return;
            }
            event.reply(context.localize("command.reaction.sub.remove.notFound")).setEphemeral(true).queue();
            return;
        }

        if (reactions.remove(emote)) {
            event.reply(context.localize("command.reaction.sub.remove.removed")).queue();
            return;
        }
        event.reply(context.localize("command.reaction.sub.remove.notFound")).setEphemeral(true).queue();
    }

    private void handleSetCheckResult(Guild guild, SlashCommandContext context, Message message, String emote) {
        var reactions = guilds.guild(guild).settings().thanking().reactions();
        var result = checkEmoji(message, emote);
        switch (result.result) {
            case EMOJI_FOUND -> {
                if (reactions.mainReaction(emote)) {
                    message.editMessage(context.localize("command.reaction.sub.main.set",
                            Replacement.create("EMOTE", result.mention))).queue();
                }
            }
            case EMOTE_FOUND -> {
                if (reactions.mainReaction(result.id)) {
                    message.editMessage(context.localize("command.reaction.sub.main.set",
                            Replacement.create("EMOTE", result.mention))).queue();
                }
            }
            case NOT_FOUND -> message.editMessage(context.localize("command.reaction.sub.reaction.get.error")).queue();
            case UNKNOWN_EMOJI -> message.editMessage(context.localize("command.reaction.error.emojiNotFound")).queue();
        }
    }

    private void handleAddCheckResult(Guild guild, SlashCommandContext context, Message message, String emote) {
        var reactions = guilds.guild(guild).settings().thanking().reactions();
        var result = checkEmoji(message, emote);
        switch (result.result) {
            case EMOJI_FOUND -> {
                reactions.add(emote);
                message.editMessage(context.localize("command.reaction.sub.add.add",
                        Replacement.create("EMOTE", result.mention))).queue();
            }
            case EMOTE_FOUND -> {
                reactions.add(result.id);
                message.editMessage(context.localize("command.reaction.sub.add.add",
                        Replacement.create("EMOTE", result.mention))).queue();
            }
            case NOT_FOUND -> message.editMessage(context.localize("command.reaction.sub.reaction.get.error")).queue();
            case UNKNOWN_EMOJI -> message.editMessage(context.localize("command.reaction.error.emojiNotFound")).queue();
        }
    }

    private EmojiCheckResult checkEmoji(Message message, String emote) {
        // Check for emote id
        if (Verifier.isValidId(emote)) {
            var emoteById = message.getGuild()
                    .retrieveEmoteById(Verifier.getIdRaw(emote).get())
                    .onErrorMap(err -> null)
                    .complete();
            if (!canUse(emoteById, message)) {
                return new EmojiCheckResult("", "", CheckResult.NOT_FOUND);
            }
            return new EmojiCheckResult(emoteById.getAsMention(), emoteById.getId(), CheckResult.EMOTE_FOUND);
        }

        // Check for name
        var emoteByName = message.getGuild().retrieveEmotes().complete().stream()
                .filter(e -> e.getName().equals(emote))
                .findFirst();

        if (emoteByName.isPresent()) {
            if (!canUse(emoteByName.get(), message)) {
                return new EmojiCheckResult("", "", CheckResult.NOT_FOUND);
            }
            return new EmojiCheckResult(emoteByName.get().getAsMention(), emoteByName.get().getId(), CheckResult.EMOTE_FOUND);
        }

        // check for unicode
        try {
            message.addReaction(emote).complete();
        } catch (ErrorResponseException e) {

            return new EmojiCheckResult(null, "", CheckResult.UNKNOWN_EMOJI);
        }
        return new EmojiCheckResult(emote, "", CheckResult.EMOJI_FOUND);
    }

    private boolean canUse(ListedEmote emote, Message message) {
        if (emote == null) {
            return false;
        }
        try {
            message.addReaction(emote).queue();
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, SlashCommandContext slashCommandContext) {
        var react = guilds.guild(event.getGuild()).settings().thanking().reactions();
        if ("emote".equals(event.getFocusedOption().getName()) && "remove".equals(event.getSubcommandName())) {
            var reactions = react.reactions()
                    .stream()
                    .limit(25)
                    .map(Choice::toChoice)
                    .toList();
            event.replyChoices(reactions).queue();
        }
    }

    private enum CheckResult {
        EMOJI_FOUND, EMOTE_FOUND, NOT_FOUND, UNKNOWN_EMOJI
    }

    private record EmojiCheckResult(String mention, String id, CheckResult result) {
    }
}
