package de.chojo.repbot.commands;

import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.ILocalizer;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.data.GuildData;
import de.chojo.repbot.data.wrapper.GuildSettings;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.PropertyKey;

import javax.sql.DataSource;
import java.awt.Color;
import java.util.List;

public class AbuseProtection extends SimpleCommand {
    private final GuildData guildData;

    public AbuseProtection(DataSource source) {
        super(CommandMeta.builder("abuseprotection", "command.abuseProtection.description")
                .addSubCommand("info", "command.abuseProtection.sub.info")
                .addSubCommand("maxmessageage", "command.abuseProtection.sub.maxMessageAge", argsBuilder()
                        .add(SimpleArgument.integer("minutes", "minutes")))
                .addSubCommand("minmessages", "command.abuseProtection.sub.minMessages", argsBuilder()
                        .add(SimpleArgument.integer("messages", "messages")))
                .addSubCommand("cooldown", "command.abuseProtection.sub.cooldown", argsBuilder()
                        .add(SimpleArgument.integer("minutes", "minutes")))
                .addSubCommand("donorcontext", "command.abuseProtection.sub.donorContext", argsBuilder()
                        .add(SimpleArgument.bool("state", "state")))
                .addSubCommand("receivercontext", "command.abuseProtection.sub.receiverContext", argsBuilder()
                        .add(SimpleArgument.bool("state", "state")))
                .withPermission());
        guildData = new GuildData(source);
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var guildSettings = guildData.getGuildSettings(event.getGuild());

        var subcmd = event.getSubcommandName();
        if ("info".equalsIgnoreCase(subcmd)) {
            sendSettings(event, context, guildSettings);
        }

        if ("maxMessageAge".equalsIgnoreCase(subcmd)) {
            maxMessageAge(event, context, guildSettings);
        }

        if ("minMessages".equalsIgnoreCase(subcmd)) {
            minMessages(event, context, guildSettings);
        }

        if ("cooldown".equalsIgnoreCase(subcmd)) {
            cooldown(event, context, guildSettings);
        }

        if ("donorContext".equalsIgnoreCase(subcmd)) {
            donorContext(event, context, guildSettings);
        }

        if ("receiverContext".equalsIgnoreCase(subcmd)) {
            receiverContext(event, context, guildSettings);
        }
    }

    private void donorContext(SlashCommandInteractionEvent event, SlashCommandContext context, GuildSettings guildSettings) {
        var abuseSettings = guildSettings.abuseSettings();
        if (event.getOptions().isEmpty()) {
            event.reply(getBooleanMessage(context, abuseSettings.isDonorContext(),
                    "command.abuseProtection.sub.donorContext.true", "command.abuseProtection.sub.donorContext.false")).queue();
            return;
        }
        var state = event.getOption("state").getAsBoolean();

        abuseSettings.donorContext(state);
        if (guildData.updateAbuseSettings(event.getGuild(), abuseSettings)) {
            event.reply(getBooleanMessage(context, state,
                    "command.abuseProtection.sub.donorContext.true", "command.abuseProtection.sub.donorContext.false")).queue();
        }
    }

    private void receiverContext(SlashCommandInteractionEvent event, SlashCommandContext context, GuildSettings guildSettings) {
        var abuseSettings = guildSettings.abuseSettings();
        if (event.getOptions().isEmpty()) {
            event.reply(getBooleanMessage(context, abuseSettings.isReceiverContext(),
                    "command.abuseProtection.sub.receiverContext.true", "command.abuseProtection.sub.receiverContext.false")).queue();
            return;
        }
        var state = event.getOption("state").getAsBoolean();

        abuseSettings.donorContext(state);
        if (guildData.updateAbuseSettings(event.getGuild(), abuseSettings)) {
            event.reply(getBooleanMessage(context, abuseSettings.isReceiverContext(),
                    "command.abuseProtection.sub.receiverContext.true", "command.abuseProtection.sub.receiverContext.false")).queue();
        }
    }

    private void maxMessageAge(SlashCommandInteractionEvent event, SlashCommandContext context, GuildSettings guildSettings) {
        var abuseSettings = guildSettings.abuseSettings();
        if (event.getOptions().isEmpty()) {
            event.reply(context.localize("command.abuseProtection.sub.maxMessageAge.get",
                    Replacement.create("MINUTES", abuseSettings.maxMessageAge()))).queue();
            return;
        }
        var age = event.getOption("minutes").getAsLong();

        age = Math.max(0L, age);
        abuseSettings.maxMessageAge((int) age);
        if (guildData.updateAbuseSettings(event.getGuild(), abuseSettings)) {
            event.reply(context.localize("command.abuseProtection.sub.maxMessageAge.get",
                    Replacement.create("MINUTES", age))).queue();
        }
    }

    private void minMessages(SlashCommandInteractionEvent event, SlashCommandContext context, GuildSettings guildSettings) {
        var abuseSettings = guildSettings.abuseSettings();
        if (event.getOptions().isEmpty()) {
            event.reply(context.localize("command.abuseProtection.sub.minMessages.get",
                    Replacement.create("AMOUNT", abuseSettings.minMessages()))).queue();
            return;
        }
        var minMessages = event.getOption("messages").getAsLong();

        minMessages = Math.max(0, Math.min(minMessages, 100));
        abuseSettings.minMessages((int) minMessages);
        if (guildData.updateAbuseSettings(event.getGuild(), abuseSettings)) {
            event.reply(context.localize("command.abuseProtection.sub.minMessages.get",
                    Replacement.create("AMOUNT", minMessages))).queue();
        }
    }

    private void cooldown(SlashCommandInteractionEvent event, SlashCommandContext context, GuildSettings guildSettings) {
        var abuseSettings = guildSettings.abuseSettings();
        if (event.getOptions().isEmpty()) {
            event.reply(context.localize("command.abuseProtection.sub.cooldown.get",
                    Replacement.create("MINUTES", abuseSettings.cooldown()))).queue();
            return;
        }
        var cooldown = event.getOption("minutes").getAsLong();

        abuseSettings.cooldown((int) cooldown);
        if (guildData.updateAbuseSettings(event.getGuild(), abuseSettings)) {
            event.reply(context.localize("command.abuseProtection.sub.cooldown.set",
                    Replacement.create("MINUTES", cooldown))).queue();
        }
    }

    private void sendSettings(SlashCommandInteractionEvent event, SlashCommandContext context, GuildSettings guildSettings) {
        event.replyEmbeds(getSettings(context, guildSettings)).queue();
    }

    private MessageEmbed getSettings(SlashCommandContext context, GuildSettings guildSettings) {
        var cooldown = guildSettings.abuseSettings();
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
