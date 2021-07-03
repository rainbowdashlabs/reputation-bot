package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.ILocalizer;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.data.GuildData;
import de.chojo.repbot.data.wrapper.GuildSettingUpdate;
import de.chojo.repbot.data.wrapper.GuildSettings;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
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
            return true;
        }
        if ("remove".equalsIgnoreCase(cmd)) {
            return true;
        }
        if ("info".equalsIgnoreCase(cmd)) {
            return info(eventWrapper);
            return true;
        }


        return false;
    }

    private boolean info(MessageEventWrapper eventWrapper) {
        guildData.getGuildSettings()
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event, SlashCommandContext context) {

    }

    private boolean reaction(MessageEventWrapper eventWrapper, CommandContext context, GuildSettings guildSettings) {
        if (context.argsEmpty()) {
            if (guildSettings.reactionIsEmote()) {
                eventWrapper.getGuild().retrieveEmoteById(guildSettings.reaction())
                        .flatMap(e -> eventWrapper.reply(eventWrapper.localize("command.repSettings.sub.reaction.get.emote",
                                Replacement.create("EMOTE", e.getAsMention()))))
                        .onErrorFlatMap(
                                err -> eventWrapper.reply(eventWrapper.localize("command.repSettings.sub.reaction.get.error"))
                        ).queue();
                return true;
            }
            eventWrapper.reply(eventWrapper.localize("command.repSettings.sub.reaction.get.emoji",
                    Replacement.create("EMOJI", guildSettings.reaction()))).queue();
            return true;
        }

        var emote = context.argString(0).get();
        var matcher = EMOTE_PATTERN.matcher(emote);
        if (!matcher.find()) {
            eventWrapper.reply("Checking Emote").flatMap(origM -> origM.addReaction(emote)
                    .onErrorFlatMap(err -> origM.editMessage("").map(x -> null))
                    .map(succ -> {
                        if (guildData.updateMessageSettings(GuildSettingUpdate.builder(eventWrapper.getGuild()).reaction(emote).build())) {
                            return origM.editMessage(loc.localize("command.repSettings.sub.reaction.set.emoji",
                                    Replacement.create("EMOJI", emote)));
                        }
                        return null;
                    })).queue();
            return true;
        }
        var id = matcher.group("id");
        var emoteById = eventWrapper.getGuild().getEmoteById(id);
        if (emoteById == null) {
            eventWrapper.reply(eventWrapper.localize("command.repSettings.error.emojiNotFound")).queue();
            return true;
        }

        if (guildData.updateMessageSettings(GuildSettingUpdate.builder(eventWrapper.getGuild()).reaction(id).build())) {
            eventWrapper.reply(loc.localize("command.repSettings.sub.reaction.set.emote",
                    Replacement.create("EMOTE", emoteById.getAsMention()))).queue();
        }
        return true;
    }

    private void reaction(SlashCommandEvent event, GuildSettings guildSettings) {
        var loc = this.loc.getContextLocalizer(event.getGuild());
        if (event.getOptions().isEmpty()) {
            if (guildSettings.reactionIsEmote()) {
                event.getGuild().retrieveEmoteById(guildSettings.reaction())
                        .flatMap(e -> event.reply(
                                loc.localize("command.repSettings.sub.reaction.get.emote",
                                        Replacement.create("EMOTE", e.getAsMention()))))
                        .onErrorFlatMap(
                                err -> event.reply(loc.localize("command.repSettings.sub.reaction.get.error")))
                        .queue();
                return;
            }
            event.reply(loc.localize("command.repSettings.sub.reaction.get.emoji",
                    Replacement.create("EMOJI", guildSettings.reaction()))).queue();
            return;
        }

        var emote = event.getOption("emote").getAsString();

        var matcher = EMOTE_PATTERN.matcher(emote);
        if (!matcher.find()) {
            event.reply("Checking Emote")
                    .flatMap(InteractionHook::retrieveOriginal)
                    .flatMap(origM -> origM.addReaction(emote)
                            .onErrorFlatMap(err -> origM.editMessage(loc.localize("command.repSettings.error.emojiNotFound")).map(x -> null))
                            .map(succ -> {
                                if (guildData.updateMessageSettings(GuildSettingUpdate.builder(event.getGuild()).reaction(emote).build())) {
                                    origM.editMessage(loc.localize("command.repSettings.sub.reaction.set.emoji",
                                            Replacement.create("EMOJI", emote))).queue();
                                }
                                return null;
                            }))
                    .queue();

            return;
        }
        var id = matcher.group("id");
        var emoteById = event.getGuild().getEmoteById(id);
        if (emoteById == null) {
            event.reply("command.repSettings.error.emojiNotFound").setEphemeral(true).queue();
            return;
        }

        if (guildData.updateMessageSettings(GuildSettingUpdate.builder(event.getGuild()).reaction(id).build())) {
            event.reply(loc.localize("command.repSettings.sub.reaction.set.emote",
                    Replacement.create("EMOTE", emoteById.getAsMention()))).flatMap(InteractionHook::retrieveOriginal).flatMap(m2 -> m2.addReaction(emoteById)).queue();
        }
    }
}
