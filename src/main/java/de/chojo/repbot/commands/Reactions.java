package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.ILocalizer;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.data.GuildData;
import de.chojo.repbot.data.wrapper.GuildSettingUpdate;
import de.chojo.repbot.data.wrapper.GuildSettings;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import javax.sql.DataSource;
import java.util.regex.Pattern;

public class Reactions extends SimpleCommand {
    private static final Pattern EMOTE_PATTERN = Pattern.compile("<a?:.*?:(?<id>[0-9]*?)>");
    private final GuildData guildData;
    private final ILocalizer loc;

    protected Reactions(DataSource dataSource, ILocalizer loc) {
        super("reactions", null, "Manage reactions",
                subCommandBuilder()
                        .add("main", "Set the main emote",
                                argsBuilder()
                                        .add(OptionType.STRING, "emote", "emote", true)
                                        .build())
                        .add("add", "Add a emote as reputation emote", argsBuilder()
                                .add(OptionType.STRING, "emote", "emote", true)
                                .build())
                        .add("remove", "Remove a emote as reputation emote", argsBuilder()
                                .add(OptionType.STRING, "emote", "emote")
                                .build())
                        .add("info", "Information about currentlz used emotes")
                        .build(), Permission.ADMINISTRATOR);
        this.loc = loc;
        this.guildData = new GuildData(dataSource);
    }

    @Override
    public boolean onCommand(MessageEventWrapper eventWrapper, CommandContext context) {
        if (context.argsEmpty()) return false;

        var cmd = context.argString(0).get();

        if ("main".equalsIgnoreCase(cmd)) {
            return reaction(eventWrapper, context.subContext(cmd));
        }

        if ("add".equalsIgnoreCase(cmd)) {
            return add(eventWrapper, context.subContext(cmd));
        }
        if ("remove".equalsIgnoreCase(cmd)) {
            return remove(eventWrapper, context.subContext(cmd));
        }
        if ("info".equalsIgnoreCase(cmd)) {
            return info(eventWrapper);
        }


        return false;
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event, SlashCommandContext context) {
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

        var guildSettings = guildData.getGuildSettings(event.getGuild());
        if (guildSettings.isEmpty()) return;
        event.replyEmbeds(getInfoEmbed(guildSettings.get())).queue();
    }

    private boolean info(MessageEventWrapper eventWrapper) {
        var guildSettings = guildData.getGuildSettings(eventWrapper.getGuild());
        if (guildSettings.isEmpty()) return true;
        eventWrapper.reply(getInfoEmbed(guildSettings.get())).queue();
        return true;
    }

    private boolean info(SlashCommandEvent event) {
        var guildSettings = guildData.getGuildSettings(event.getGuild());
        if (guildSettings.isEmpty()) return true;
        event.replyEmbeds(getInfoEmbed(guildSettings.get())).queue();
        return true;
    }

    private MessageEmbed getInfoEmbed(GuildSettings settings) {
        var mainEmote = settings.reactionMention(settings.guild());
        var emotes = String.join(",", settings.reactionMention(settings.guild()));

        return new LocalizedEmbedBuilder(loc, settings.guild())
                .setTitle("Registered emotes")
                .addField("Main Emote", mainEmote, true)
                .addField("Additional Emotes", emotes, true)
                .build();
    }


    private boolean reaction(MessageEventWrapper event, CommandContext context) {
        var emote = context.argString(0).get();
        var message = event.reply(loc.localize("command.reaction.checking", event.getGuild())).complete();
        handleSetCheckResult(event.getGuild(), message, emote);
        return true;
    }

    private void reaction(SlashCommandEvent event) {
        var emote = event.getOption("emote").getAsString();
        var message = event.reply(loc.localize("command.reaction.checking", event.getGuild()))
                .flatMap(InteractionHook::retrieveOriginal).complete();
        handleSetCheckResult(event.getGuild(), message, emote);
    }

    private boolean add(MessageEventWrapper event, CommandContext context) {
        var emote = context.argString(0).get();
        var message = event.reply(loc.localize("command.reaction.checking", event.getGuild())).complete();
        handleAddCheckResult(event.getGuild(), message, emote);
        return true;
    }

    private void add(SlashCommandEvent event) {
        var emote = event.getOption("emote").getAsString();
        var message = event.reply(loc.localize("command.reaction.checking", event.getGuild()))
                .flatMap(InteractionHook::retrieveOriginal).complete();
        handleAddCheckResult(event.getGuild(), message, emote);
    }

    private boolean remove(MessageEventWrapper event, CommandContext context) {
        var emote = context.argString(0).get();
        var matcher = EMOTE_PATTERN.matcher(emote);
        if (matcher.find()) {
            if (guildData.removeReaction(event.getGuild(), matcher.group("id"))) {
                event.reply(loc.localize("command.reaction.sub.remove.removed", event.getGuild())).queue();
                return true;
            }
            event.replyErrorAndDelete(loc.localize("command.reaction.sub.remove.notFound", event.getGuild()), 10);
            return true;
        }

        if (guildData.removeReaction(event.getGuild(), emote)) {
            event.reply(loc.localize("command.reaction.sub.remove.removed", event.getGuild())).queue();
            return true;
        }
        event.replyErrorAndDelete(loc.localize("command.reaction.sub.remove.notFound", event.getGuild()), 10);
        return true;
    }

    private void remove(SlashCommandEvent event) {
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
                if (guildData.updateMessageSettings(GuildSettingUpdate.builder(guild).reaction(emote).build())) {
                    message.editMessage(loc.localize("command.reaction.sub.main.set", guild,
                            Replacement.create("EMOTE", result.mention))).queue();
                }
            }
            case EMOTE_FOUND -> {
                if (guildData.updateMessageSettings(GuildSettingUpdate.builder(guild).reaction(result.id).build())) {
                    message.editMessage(loc.localize("command.reaction.sub.main.set", guild,
                            Replacement.create("EMOTE", result.mention))).queue();
                }
            }
            case NOT_FOUND, UNKNOWN_EMOJI -> message.editMessage(loc.localize("command.repSettings.error.emojiNotFound", guild)).queue();
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
            case NOT_FOUND, UNKNOWN_EMOJI -> message.editMessage(loc.localize("command.repSettings.error.emojiNotFound", guild)).queue();
        }
    }

    private EmojiCheckResult checkEmoji(Message message, String emote) {
        var matcher = EMOTE_PATTERN.matcher(emote);
        if (!matcher.find()) {
            try {
                message.addReaction(emote).complete();
            } catch (ErrorResponseException e) {
                return new EmojiCheckResult(null, "", CheckResult.NOT_FOUND);
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

    private class EmojiCheckResult {
        private final String mention;
        private final String id;
        private final CheckResult result;

        public EmojiCheckResult(String mention, String id, CheckResult result) {
            this.mention = mention;
            this.id = id;
            this.result = result;
        }
    }

    private enum CheckResult {
        EMOJI_FOUND, EMOTE_FOUND, NOT_FOUND, UNKNOWN_EMOJI
    }
}
