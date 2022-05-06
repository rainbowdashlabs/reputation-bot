package de.chojo.repbot.commands;

import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.dao.access.guild.RepGuild;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.PropertyKey;

import java.awt.Color;
import java.util.List;

public class AbuseProtection extends SimpleCommand {
    private final Guilds guilds;

    public AbuseProtection(Guilds guilds) {
        super(CommandMeta.builder("abuseprotection", "command.abuseProtection.description")
                .addSubCommand("info", "command.abuseProtection.sub.info")
                .addSubCommand("maxmessageage", "command.abuseProtection.sub.maxMessageAge", argsBuilder()
                        .add(SimpleArgument.integer("minutes", "command.abuseProtection.sub.maxMessageAge.arg.minutes")))
                .addSubCommand("minmessages", "command.abuseProtection.sub.minMessages", argsBuilder()
                        .add(SimpleArgument.integer("messages", "command.abuseProtection.sub.minMessages.arg.messages")))
                .addSubCommand("cooldown", "command.abuseProtection.sub.cooldown", argsBuilder()
                        .add(SimpleArgument.integer("minutes", "command.abuseProtection.sub.cooldown.arg.minutes")))
                .addSubCommand("donorcontext", "command.abuseProtection.sub.donorContext", argsBuilder()
                        .add(SimpleArgument.bool("state", "command.abuseProtection.sub.donorContext.arg.state")))
                .addSubCommand("receivercontext", "command.abuseProtection.sub.receiverContext", argsBuilder()
                        .add(SimpleArgument.bool("state", "command.abuseProtection.sub.receiverContext.arg.state")))
                .withPermission());
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {

        var guild = guilds.guild(event.getGuild());

        var subcmd = event.getSubcommandName();
        if ("info".equalsIgnoreCase(subcmd)) {
            sendSettings(event, context, guild);
        }

        if ("maxMessageAge".equalsIgnoreCase(subcmd)) {
            maxMessageAge(event, context, guild);
        }

        if ("minMessages".equalsIgnoreCase(subcmd)) {
            minMessages(event, context, guild);
        }

        if ("cooldown".equalsIgnoreCase(subcmd)) {
            cooldown(event, context, guild);
        }

        if ("donorContext".equalsIgnoreCase(subcmd)) {
            donorContext(event, context, guild);
        }

        if ("receiverContext".equalsIgnoreCase(subcmd)) {
            receiverContext(event, context, guild);
        }
    }

    private void donorContext(SlashCommandInteractionEvent event, SlashCommandContext context, RepGuild guild) {
        var abuseSettings = guild.settings().abuseProtection();
        if (event.getOptions().isEmpty()) {
            event.reply(getBooleanMessage(context, abuseSettings.isDonorContext(),
                    "command.abuseProtection.sub.donorContext.true", "command.abuseProtection.sub.donorContext.false")).queue();
            return;
        }
        var state = event.getOption("state").getAsBoolean();

        if (abuseSettings.donorContext(state)) {
            event.reply(getBooleanMessage(context, state,
                    "command.abuseProtection.sub.donorContext.true", "command.abuseProtection.sub.donorContext.false")).queue();
        }
    }

    private void receiverContext(SlashCommandInteractionEvent event, SlashCommandContext context, RepGuild guild) {
        var abuseSettings = guild.settings().abuseProtection();
        if (event.getOptions().isEmpty()) {
            event.reply(getBooleanMessage(context, abuseSettings.isReceiverContext(),
                    "command.abuseProtection.sub.receiverContext.true", "command.abuseProtection.sub.receiverContext.false")).queue();
            return;
        }
        var state = event.getOption("state").getAsBoolean();

        if (abuseSettings.donorContext(state)) {
            event.reply(getBooleanMessage(context, abuseSettings.isReceiverContext(),
                    "command.abuseProtection.sub.receiverContext.true", "command.abuseProtection.sub.receiverContext.false")).queue();
        }
    }

    private void maxMessageAge(SlashCommandInteractionEvent event, SlashCommandContext context, RepGuild guild) {
        var abuseSettings = guild.settings().abuseProtection();
        if (event.getOptions().isEmpty()) {
            event.reply(context.localize("command.abuseProtection.sub.maxMessageAge.get",
                    Replacement.create("MINUTES", abuseSettings.maxMessageAge()))).queue();
            return;
        }
        var age = event.getOption("minutes").getAsLong();

        age = Math.max(0L, age);
        if (abuseSettings.maxMessageAge((int) age)) {
            event.reply(context.localize("command.abuseProtection.sub.maxMessageAge.get",
                    Replacement.create("MINUTES", age))).queue();
        }
    }

    private void minMessages(SlashCommandInteractionEvent event, SlashCommandContext context, RepGuild guild) {
        var abuseSettings = guild.settings().abuseProtection();
        if (event.getOptions().isEmpty()) {
            event.reply(context.localize("command.abuseProtection.sub.minMessages.get",
                    Replacement.create("AMOUNT", abuseSettings.minMessages()))).queue();
            return;
        }
        var minMessages = event.getOption("messages").getAsLong();

        minMessages = Math.max(0, Math.min(minMessages, 100));
        if (abuseSettings.minMessages((int) minMessages)) {
            event.reply(context.localize("command.abuseProtection.sub.minMessages.get",
                    Replacement.create("AMOUNT", minMessages))).queue();
        }
    }

    private void cooldown(SlashCommandInteractionEvent event, SlashCommandContext context, RepGuild guild) {
        var abuseSettings = guild.settings().abuseProtection();
        if (event.getOptions().isEmpty()) {
            event.reply(context.localize("command.abuseProtection.sub.cooldown.get",
                    Replacement.create("MINUTES", abuseSettings.cooldown()))).queue();
            return;
        }
        var cooldown = event.getOption("minutes").getAsLong();

        if (abuseSettings.cooldown((int) cooldown)) {
            event.reply(context.localize("command.abuseProtection.sub.cooldown.set",
                    Replacement.create("MINUTES", cooldown))).queue();
        }
    }

    private void sendSettings(SlashCommandInteractionEvent event, SlashCommandContext context, RepGuild guild) {
        event.replyEmbeds(getSettings(context, guild)).queue();
    }

    private MessageEmbed getSettings(SlashCommandContext context, RepGuild guild) {
        var cooldown = guild.settings().abuseProtection();
        var setting = List.of(
                getSetting("command.abuseProtection.embed.descr.maxMessageAge", cooldown.maxMessageAge()),
                getSetting("command.abuseProtection.embed.descr.minMessages", cooldown.minMessages()),
                getSetting("command.abuseProtection.embed.descr.cooldown", cooldown.cooldown()),
                getSetting("command.abuseProtection.embed.descr.donorContext", cooldown.isDonorContext()),
                getSetting("command.abuseProtection.embed.descr.receiverContext", cooldown.isReceiverContext())
        );

        var settings = String.join("\n", setting);

        return new LocalizedEmbedBuilder(context.localizer())
                .setTitle("command.abuseProtection.embed.title")
                .appendDescription(settings)
                .setColor(Color.GREEN)
                .build();
    }

    private String getSetting(@PropertyKey(resourceBundle = "locale") String locale, Object object) {
        return String.format("$%s$: %s", locale, object);
    }

    private String getBooleanMessage(SlashCommandContext context, boolean value, String whenTrue, String whenFalse) {
        return context.localize(value ? whenTrue : whenFalse);
    }
}
