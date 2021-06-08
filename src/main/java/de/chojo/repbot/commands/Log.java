package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.Localizer;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.parsing.DiscordResolver;
import de.chojo.jdautil.parsing.ValueParser;
import de.chojo.jdautil.parsing.Verifier;
import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
import de.chojo.repbot.data.ReputationData;
import de.chojo.repbot.data.wrapper.ReputationLogEntry;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.sharding.ShardManager;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

public class Log extends SimpleCommand {
    private final ShardManager shardManager;
    private final ReputationData reputationData;
    private final Localizer loc;

    public Log(ShardManager shardManager, DataSource dataSource, Localizer loc) {
        super("log",
                null,
                "command.log.description", subCommandBuilder()
                        .add("received", "command.log.sub.received", argsBuilder()
                                .add(OptionType.USER, "user", "user", true)
                                .add(OptionType.INTEGER, "count", "count")
                                .build()
                        )
                        .add("donated", "command.log.sub.donated", argsBuilder()
                                .add(OptionType.USER, "user", "user", true)
                                .add(OptionType.INTEGER, "count", "count")
                                .build()
                        )
                        .add("message", "command.log.sub.message", argsBuilder()
                                .add(OptionType.STRING, "message_id", "message_id", true)
                                .build()
                        )
                        .build(),
                Permission.ADMINISTRATOR);
        this.shardManager = shardManager;
        this.reputationData = new ReputationData(dataSource);
        this.loc = loc;
    }

    @Override
    public boolean onCommand(MessageEventWrapper eventWrapper, CommandContext context) {
        if (context.argsArray().length <= 1) return false;
        var cmd = context.argString(0).get();
        if ("received".equalsIgnoreCase(cmd) || "donated".equalsIgnoreCase(cmd)) {
            var userArg = context.argString(1).get();
            var optUser = Verifier.getIdRaw(userArg)
                    .map(c -> eventWrapper.getGuild().retrieveMemberById(c).complete().getUser())
                    .or(() -> DiscordResolver.getUser(shardManager, userArg));
            if (optUser.isEmpty()) {
                eventWrapper.replyErrorAndDelete(eventWrapper.localize("error.userNotFound"), 15);
                return true;
            }
            if ("received".equalsIgnoreCase(cmd)) {
                return received(eventWrapper, context.subContext(cmd), optUser.get());
            }
            return donated(eventWrapper, context.subContext(cmd), optUser.get());
        }
        if ("message".equalsIgnoreCase(cmd)) {
            return message(eventWrapper, context.subContext(cmd));
        }
        return false;
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        var cmd = event.getSubcommandName();
        if ("received".equalsIgnoreCase(cmd)) {
            received(event, event.getOption("user").getAsUser());
        }
        if ("donated".equalsIgnoreCase(cmd)) {
            donated(event, event.getOption("user").getAsUser());
        }
        if ("message".equalsIgnoreCase(cmd)) {
            message(event);
        }
    }

    private MessageEmbed getMessageLog(Guild guild, long messageId) {
        var loc = this.loc.getContextLocalizer(guild);
        var messageLog = reputationData.getMessageLog(messageId, guild, 50);

        var log = mapMessageLogEntry(guild, messageLog);

        return new LocalizedEmbedBuilder(this.loc, guild)
                .setAuthor(loc.localize("command.log.messageLog", Replacement.create("ID", messageId)))
                .setDescription(log)
                .build();
    }

    private boolean message(MessageEventWrapper eventWrapper, CommandContext subContext) {
        var optMessageId = subContext.argLong(0);

        if (optMessageId.isEmpty()) {
            eventWrapper.replyErrorAndDelete(eventWrapper.localize("error.invalidMessage"), 15);
            return false;
        }

        eventWrapper.reply(getMessageLog(eventWrapper.getGuild(), optMessageId.get())).queue();
        return true;
    }

    private void message(SlashCommandEvent event) {
        event.getOption("message_id");
        var optMessageId = ValueParser.parseLong(event.getOption("message_id").getAsString());
        if (optMessageId.isEmpty()) {
            event.reply(loc.localize("error.invalidMessage", event.getGuild())).setEphemeral(true).queue();
            return;
        }

        event.reply(wrap(getMessageLog(event.getGuild(), event.getOption("message_id").getAsLong()))).queue();
    }

    private MessageEmbed sendUserLog(Guild guild, User user, String title, String log) {
        return new LocalizedEmbedBuilder(loc, guild)
                .setAuthor(loc.localize(title, guild,
                        Replacement.create("USER", user.getAsTag())),
                        null, user.getEffectiveAvatarUrl())
                .setDescription(log)
                .build();
    }

    private boolean donated(MessageEventWrapper eventWrapper, CommandContext context, User user) {
        var limit = context.argInt(1).orElse(10);
        eventWrapper.reply(sendUserLog(eventWrapper.getGuild(), user, "command.log.donatedLog",
                getDonatedLog(user, eventWrapper.getGuild(), limit))).queue();
        return true;
    }

    private void donated(SlashCommandEvent event, User user) {
        var limit = Optional.ofNullable(event.getOption("count")).map(OptionMapping::getAsLong).orElse(10L);
        event.reply(wrap(sendUserLog(event.getGuild(), user, "command.log.donatedLog",
                getDonatedLog(user, event.getGuild(), limit.intValue())))).queue();
    }

    private String getDonatedLog(User user, Guild guild, int limit) {
        var userDonatedLog = reputationData.getUserDonatedLog(user, guild, Math.max(5, Math.min(limit, 50)));
        return mapUserLogEntry(guild, userDonatedLog, ReputationLogEntry::receiverId);
    }

    private boolean received(MessageEventWrapper eventWrapper, CommandContext context, User user) {
        var limit = context.argInt(1).orElse(10);
        eventWrapper.reply(sendUserLog(eventWrapper.getGuild(), user, "command.log.receivedLog",
                getReceivedLog(user, eventWrapper.getGuild(), limit))).queue();
        return true;
    }

    private void received(SlashCommandEvent event, User user) {
        var limit = Optional.ofNullable(event.getOption("count")).map(OptionMapping::getAsLong).orElse(10L);
        event.reply(wrap(sendUserLog(event.getGuild(), user, "command.log.receivedLog",
                getReceivedLog(user, event.getGuild(), limit.intValue())))).queue();
    }

    private String getReceivedLog(User user, Guild guild, int limit) {
        var userDonatedLog = reputationData.getUserReceivedLog(user, guild, Math.max(5, Math.min(limit, 50)));
        return mapUserLogEntry(guild, userDonatedLog, ReputationLogEntry::donorId);
    }

    private String mapUserLogEntry(Guild wrapper, List<ReputationLogEntry> logEntries, Function<ReputationLogEntry, Long> userId) {
        var loc = this.loc.getContextLocalizer(wrapper);
        List<String> entries = new ArrayList<>();
        for (var logEntry : logEntries) {
            var thankType = loc.localize("thankType." + logEntry.type().name().toLowerCase(Locale.ROOT));
            var jumpLink = createJumpLink(wrapper, logEntry);
            entries.add(String.format("**%s** %s %s",
                    thankType, User.fromId(userId.apply(logEntry)).getAsMention(), jumpLink));
        }
        return String.join("\n", entries);
    }

    private String mapMessageLogEntry(Guild guild, List<ReputationLogEntry> logEntries) {
        if (logEntries.isEmpty()) return "";

        var loc = this.loc.getContextLocalizer(guild);

        List<String> entries = new ArrayList<>();
        for (var logEntry : logEntries) {
            var jumpLink = createJumpLink(guild, logEntry);
            var thankType = loc.localize("thankType." + logEntry.type().name().toLowerCase(Locale.ROOT));
            entries.add(String.format("**%s** %s ➜ %s **|** %s",
                    thankType, User.fromId(logEntry.receiverId()).getAsMention(), User.fromId(logEntry.receiverId()).getAsMention(), jumpLink));
        }
        return String.join("\n", entries);
    }

    private String createJumpLink(Guild guild, ReputationLogEntry log) {
        var loc = this.loc.getContextLocalizer(guild);
        var jump = loc.localize("words.jumpMarker",
                Replacement.create("TARGET", "$words.message$"),
                Replacement.create("URL", log.getMessageJumpLink()));

        String refJump = null;
        if (log.hasRefMessage()) {
            refJump = loc.localize("words.jumpMarker",
                    Replacement.create("TARGET", "$words.refMessage$"),
                    Replacement.create("URL", log.getMessageJumpLink()));
        }

        return String.format("**%s** %s", jump, refJump == null ? "" : "➜ **" + refJump + "**");
    }
}
