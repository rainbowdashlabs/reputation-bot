package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.parsing.DiscordResolver;
import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
import de.chojo.repbot.data.ReputationData;
import de.chojo.repbot.data.wrapper.ReputationLogEntry;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.sharding.ShardManager;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

public class Log extends SimpleCommand {
    private final ShardManager shardManager;
    private final ReputationData reputationData;

    public Log(ShardManager shardManager, DataSource dataSource) {
        super("log",
                null,
                "command.log.description", null, subCommandBuilder()
                        .add("received", "<user> [count]", "command.log.sub.received")
                        .add("donated", "<user> [count]", "command.log.sub.donated")
                        .add("message", "<message_id>", "command.log.sub.message")
                        .build(),
                Permission.ADMINISTRATOR);
        this.shardManager = shardManager;
        this.reputationData = new ReputationData(dataSource);
    }

    @Override
    public boolean onCommand(MessageEventWrapper eventWrapper, CommandContext context) {
        if (context.argsEmpty()) return false;
        var cmd = context.argString(0).get();
        if ("received".equalsIgnoreCase(cmd)) {
            return received(eventWrapper, context.subContext(cmd));
        }
        if ("donated".equalsIgnoreCase(cmd)) {
            return donated(eventWrapper, context.subContext(cmd));
        }
        if ("message".equalsIgnoreCase(cmd)) {
            return message(eventWrapper, context.subContext(cmd));
        }
        return false;
    }

    private boolean message(MessageEventWrapper eventWrapper, CommandContext subContext) {
        if (subContext.argsEmpty()) return false;

        var optMessageId = subContext.argLong(0);

        if (optMessageId.isEmpty()) {
            eventWrapper.replyErrorAndDelete(eventWrapper.localize("error.invalidMessage"), 15);
            return false;
        }

        var messageLog = reputationData.getMessageLog(optMessageId.get(), eventWrapper.getGuild(), 50);

        var log = mapLogEntry(eventWrapper, messageLog, ReputationLogEntry::getDonorId);

        var message = new LocalizedEmbedBuilder(eventWrapper)
                .setAuthor(eventWrapper.localize("command.log.messageLog", Replacement.create("ID", optMessageId.get())))
                .setDescription(log)
                .build();
        eventWrapper.reply(message).queue();

        return true;
    }

    private boolean donated(MessageEventWrapper eventWrapper, CommandContext context) {
        if (context.argsEmpty()) return false;
        var userArg = context.argString(0).get();
        var optUser = DiscordResolver.getUser(shardManager, userArg);
        if (optUser.isEmpty()) {
            eventWrapper.replyErrorAndDelete(eventWrapper.localize("error.userNotFound"), 15);
            return true;
        }
        var user = optUser.get();

        var limit = context.argInt(1).orElse(10);

        var userDonatedLog = reputationData.getUserDonatedLog(user, eventWrapper.getGuild(), Math.max(5, Math.min(limit, 50)));

        var log = mapLogEntry(eventWrapper, userDonatedLog, ReputationLogEntry::getReceiverId);

        var message = new LocalizedEmbedBuilder(eventWrapper)
                .setAuthor(eventWrapper.localize("command.log.donatedLog", Replacement.create("USER", user.getAsTag())),
                        user.getAvatarUrl())
                .setDescription(log)
                .build();
        eventWrapper.reply(message).queue();
        return true;
    }

    private boolean received(MessageEventWrapper eventWrapper, CommandContext context) {
        if (context.argsEmpty()) return false;
        var userArg = context.argString(0).get();
        var optUser = DiscordResolver.getUser(shardManager, userArg);
        if (optUser.isEmpty()) {
            eventWrapper.replyErrorAndDelete(eventWrapper.localize("error.userNotFound"), 15);
            return true;
        }
        var user = optUser.get();

        var limit = context.argInt(1).orElse(10);

        var userDonatedLog = reputationData.getUserReceivedLog(user, eventWrapper.getGuild(), Math.max(5, Math.min(limit, 50)));

        var log = mapLogEntry(eventWrapper, userDonatedLog, ReputationLogEntry::getDonorId);

        var message = new LocalizedEmbedBuilder(eventWrapper)
                .setAuthor(eventWrapper.localize("command.log.receivedLog", Replacement.create("USER", user.getAsTag())),
                        user.getAvatarUrl())
                .setDescription(log)
                .build();
        eventWrapper.reply(message).queue();
        return true;
    }

    private String mapLogEntry(MessageEventWrapper wrapper, List<ReputationLogEntry> logEntries, Function<ReputationLogEntry, Long> userId) {
        List<String> entries = new ArrayList<>();
        for (var logEntry : logEntries) {
            var received = wrapper.getGuild().retrieveMemberById(userId.apply(logEntry)).complete();
            var thankType = wrapper.localize("thankType." + logEntry.getType().name().toLowerCase(Locale.ROOT));
            var jump = wrapper.localize("words.jumpMarker",
                    Replacement.create("TARGET", "$words.message$"),
                    Replacement.create("URL", logEntry.getMessageJumpLink()));
            String refJump = null;
            if (logEntry.hasRefMessage()) {
                refJump = wrapper.localize("words.jumpMarker",
                        Replacement.create("TARGET", "$words.refMessage$"),
                        Replacement.create("URL", logEntry.getMessageJumpLink()));
            }
            entries.add(String.format("**%s** %s **%s** %s",
                    thankType, received == null ? "----" : received.getAsMention(), jump, refJump == null ? "" : "➜ **"+refJump + "**"));
        }
        return String.join("\n", entries);
    }
}
