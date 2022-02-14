package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.ILocalizer;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.data.GuildData;
import de.chojo.repbot.data.wrapper.GuildSettings;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import javax.sql.DataSource;
import java.util.regex.Pattern;

public class Reactions extends SimpleCommand {
    private static final Pattern EMOTE_PATTERN = Pattern.compile("<a?:.*?:(?<id>[0-9]*?)>");
    private final GuildData guildData;
    private final ILocalizer loc;

    public Reactions(DataSource dataSource, ILocalizer loc) {
        super("reactions", null, "command.reaction.description",
                subCommandBuilder()
                        .add("main", "command.reaction.sub.main", argsBuilder()
                                .add(OptionType.STRING, "emote", "emote", true)
                                .build())
                        .add("add", "command.reaction.sub.add", argsBuilder()
                                .add(OptionType.STRING, "emote", "emote", true)
                                .build())
                        .add("remove", "command.reaction.sub.remove", argsBuilder()
                                .add(OptionType.STRING, "emote", "emote", true)
                                .build())
                        .add("info", "command.reaction.sub.info")
                        .build(), Permission.ADMINISTRATOR);
        this.loc = loc;
        guildData = new GuildData(dataSource);
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var cmd = event.getSubcommandName();

        if ("main".equalsIgnoreCase(cmd)) {
            reaction(event);
        }

        if ("add".equalsIgnoreCase(cmd)) {
            add(event);
        }
        if ("remove".equalsIgnoreCase(cmd)) {
            remove(event);
        }
        if ("info".equalsIgnoreCase(cmd)) {
            info(event);
        }
    }

    private boolean info(SlashCommandInteractionEvent event) {
        event.replyEmbeds(getInfoEmbed(guildData.getGuildSettings(event.getGuild()))).queue();
        return true;
    }

    private MessageEmbed getInfoEmbed(GuildSettings settings) {
        var mainEmote = settings.thankSettings().reactionMention(settings.guild());
        var emotes = String.join(" ", settings.thankSettings().getAdditionalReactionMentions(settings.guild()));

        return new LocalizedEmbedBuilder(loc, settings.guild())
                .setTitle("command.reaction.sub.info.title")
                .addField("command.reaction.sub.info.main", mainEmote, true)
                .addField("command.reaction.sub.info.additional", emotes, true)
                .build();
    }

    private void reaction(SlashCommandInteractionEvent event) {
        var emote = event.getOption("emote").getAsString();
        var message = event.reply(loc.localize("command.reaction.checking", event.getGuild()))
                .flatMap(InteractionHook::retrieveOriginal).complete();
        handleSetCheckResult(event.getGuild(), message, emote);
    }

    private void add(SlashCommandInteractionEvent event) {
        var emote = event.getOption("emote").getAsString();
        var message = event.reply(loc.localize("command.reaction.checking", event.getGuild()))
                .flatMap(InteractionHook::retrieveOriginal).complete();
        handleAddCheckResult(event.getGuild(), message, emote);
    }

    private void remove(SlashCommandInteractionEvent event) {
        var emote = event.getOption("emote").getAsString();
        var matcher = EMOTE_PATTERN.matcher(emote);
        if (matcher.find()) {
            if (guildData.removeReaction(event.getGuild(), matcher.group("id"))) {
                event.reply(loc.localize("command.reaction.sub.remove.removed", event.getGuild())).queue();
                return;
            }
            event.reply(loc.localize("command.reaction.sub.remove.notFound", event.getGuild())).setEphemeral(true).queue();
            return;
        }

        if (guildData.removeReaction(event.getGuild(), emote)) {
            event.reply(loc.localize("command.reaction.sub.remove.removed", event.getGuild())).queue();
            return;
        }
        event.reply(loc.localize("command.reaction.sub.remove.notFound", event.getGuild())).setEphemeral(true).queue();
    }

    private void handleSetCheckResult(Guild guild, Message message, String emote) {
        var result = checkEmoji(message, emote);
        switch (result.result) {
            case EMOJI_FOUND -> {
                if (guildData.setMainReaction(guild, emote)) {
                    message.editMessage(loc.localize("command.reaction.sub.main.set", guild,
                            Replacement.create("EMOTE", result.mention))).queue();
                }
            }
            case EMOTE_FOUND -> {
                if (guildData.setMainReaction(guild, result.id)) {
                    message.editMessage(loc.localize("command.reaction.sub.main.set", guild,
                            Replacement.create("EMOTE", result.mention))).queue();
                }
            }
            case NOT_FOUND -> message.editMessage(loc.localize("command.reaction.sub.reaction.get.error", guild)).queue();
            case UNKNOWN_EMOJI -> message.editMessage(loc.localize("command.reaction.error.emojiNotFound", guild)).queue();
        }
    }

    private void handleAddCheckResult(Guild guild, Message message, String emote) {
        var result = checkEmoji(message, emote);
        switch (result.result) {
            case EMOJI_FOUND -> {
                if (guildData.addReaction(guild, emote)) {
                    message.editMessage(loc.localize("command.reaction.sub.add.add", guild,
                            Replacement.create("EMOTE", result.mention))).queue();
                }
            }
            case EMOTE_FOUND -> {
                if (guildData.addReaction(guild, result.id)) {
                    message.editMessage(loc.localize("command.reaction.sub.add.add", guild,
                            Replacement.create("EMOTE", result.mention))).queue();
                }
            }
            case NOT_FOUND -> message.editMessage(loc.localize("command.reaction.sub.reaction.get.error", guild)).queue();
            case UNKNOWN_EMOJI -> message.editMessage(loc.localize("command.reaction.error.emojiNotFound", guild)).queue();
        }
    }

    private EmojiCheckResult checkEmoji(Message message, String emote) {
        var matcher = EMOTE_PATTERN.matcher(emote);
        if (!matcher.find()) {
            try {
                message.addReaction(emote).complete();
            } catch (ErrorResponseException e) {
                return new EmojiCheckResult(null, "", CheckResult.UNKNOWN_EMOJI);
            }
            return new EmojiCheckResult(emote, "", CheckResult.EMOJI_FOUND);
        }
        var id = matcher.group("id");
        var emoteById = message.getGuild().retrieveEmoteById(id).onErrorMap(err -> null).complete();
        if (emoteById == null) {
            return new EmojiCheckResult("", "", CheckResult.NOT_FOUND);
        }
        message.addReaction(emoteById).queue();
        return new EmojiCheckResult(emoteById.getAsMention(), emoteById.getId(), CheckResult.EMOTE_FOUND);
    }

    private enum CheckResult {
        EMOJI_FOUND, EMOTE_FOUND, NOT_FOUND, UNKNOWN_EMOJI
    }

    private static class EmojiCheckResult {
        private final String mention;
        private final String id;
        private final CheckResult result;

        public EmojiCheckResult(String mention, String id, CheckResult result) {
            this.mention = mention;
            this.id = id;
            this.result = result;
        }
    }
}
