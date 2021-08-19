package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.ILocalizer;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.data.GuildData;
import de.chojo.repbot.data.wrapper.GuildSettings;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.PropertyKey;

import javax.sql.DataSource;
import java.awt.Color;
import java.util.List;

public class AbuseProtection extends SimpleCommand {
    private final ILocalizer loc;
    private final GuildData guildData;

    public AbuseProtection(DataSource source, ILocalizer localizer) {
        super("abuseprotection",
                new String[]{"ap"},
                "command.abuseProtection.description",
                subCommandBuilder()
                        .add("info", "command.abuseProtection.sub.info", argsBuilder()
                                .build()
                        )
                        .add("maxmessageage", "command.abuseProtection.sub.maxMessageAge", argsBuilder()
                                .add(OptionType.INTEGER, "minutes", "minutes")
                                .build()
                        )
                        .add("minmessages", "command.abuseProtection.sub.minMessages", argsBuilder()
                                .add(OptionType.INTEGER, "messages", "messages")
                                .build()
                        )
                        .add("cooldown", "command.abuseProtection.sub.cooldown", argsBuilder()
                                .add(OptionType.INTEGER, "minutes", "minutes")
                                .build()
                        )
                        .add("donorcontext", "command.abuseProtection.sub.donorContext", argsBuilder()
                                .add(OptionType.INTEGER, "state", "state")
                                .build()
                        )
                        .add("receivercontext", "command.abuseProtection.sub.receiverContext", argsBuilder()
                                .add(OptionType.BOOLEAN, "state", "state")
                                .build()
                        )
                        .build(),
                Permission.MANAGE_SERVER);
        guildData = new GuildData(source);
        loc = localizer;
    }

    @Override
    public boolean onCommand(MessageEventWrapper eventWrapper, CommandContext context) {
        if (context.argsEmpty()) {
            return false;
        }

        var guildSettings = guildData.getGuildSettings(eventWrapper.getGuild());
        var subcmd = context.argString(0).get();
        if ("info".equalsIgnoreCase(subcmd)) {
            return sendSettings(eventWrapper, guildSettings);
        }

        if ("maxMessageAge".equalsIgnoreCase(subcmd)) {
            return maxMessageAge(eventWrapper, context.subContext(subcmd), guildSettings);
        }

        if ("minMessages".equalsIgnoreCase(subcmd)) {
            return minMessages(eventWrapper, context.subContext(subcmd), guildSettings);
        }

        if ("cooldown".equalsIgnoreCase(subcmd)) {
            return cooldown(eventWrapper, context.subContext(subcmd), guildSettings);
        }
        if ("donorContext".equalsIgnoreCase(subcmd)) {
            return donorContext(eventWrapper, context.subContext(subcmd), guildSettings);
        }
        if ("receiverContext".equalsIgnoreCase(subcmd)) {
            return receiverContext(eventWrapper, context.subContext(subcmd), guildSettings);
        }
        return false;
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event, SlashCommandContext context) {
        var guildSettings = guildData.getGuildSettings(event.getGuild());

        var subcmd = event.getSubcommandName();
        if ("info".equalsIgnoreCase(subcmd)) {
            sendSettings(event, guildSettings);
        }

        if ("maxMessageAge".equalsIgnoreCase(subcmd)) {
            maxMessageAge(event, guildSettings);
        }

        if ("minMessages".equalsIgnoreCase(subcmd)) {
            minMessages(event, guildSettings);
        }

        if ("cooldown".equalsIgnoreCase(subcmd)) {
            cooldown(event, guildSettings);
        }
        if ("donorContext".equalsIgnoreCase(subcmd)) {
            donorContext(event, guildSettings);
        }
        if ("receiverContext".equalsIgnoreCase(subcmd)) {
            receiverContext(event, guildSettings);
        }
    }

    private void donorContext(SlashCommandEvent event, GuildSettings guildSettings) {
        var loc = this.loc.getContextLocalizer(event.getGuild());
        if (event.getOptions().isEmpty()) {
            event.reply(loc.localize("command.abuseProtection.sub.donorContext." + guildSettings.abuseSettings().isDonorContext())).queue();
            return;
        }
        var state = event.getOption("state").getAsBoolean();

        guildSettings.abuseSettings().donorContext(state);
        if (guildData.updateAbuseSettings(event.getGuild(), guildSettings.abuseSettings())) {
            event.reply(loc.localize("command.abuseProtection.sub.donorContext." + state)).queue();
        }
    }

    private boolean donorContext(MessageEventWrapper eventWrapper, CommandContext context, GuildSettings guildSettings) {
        if (context.argsEmpty()) {
            eventWrapper.reply(eventWrapper.localize("command.abuseProtection.sub.donorContext." + guildSettings.abuseSettings().isDonorContext())).queue();
            return true;
        }
        var state = context.argBoolean(0);
        if (state.isEmpty()) {
            eventWrapper.replyErrorAndDelete(eventWrapper.localize("error.notABoolean",
                    Replacement.create("input", context.argString(0).get())), 10);
            return true;
        }

        guildSettings.abuseSettings().donorContext(state.get());
        if (guildData.updateAbuseSettings(eventWrapper.getGuild(), guildSettings.abuseSettings())) {
            eventWrapper.reply(loc.localize("command.abuseProtection.sub.donorContext." + state)).queue();
        }
        return true;
    }

    private void receiverContext(SlashCommandEvent event, GuildSettings guildSettings) {
        var loc = this.loc.getContextLocalizer(event.getGuild());
        if (event.getOptions().isEmpty()) {
            event.reply(loc.localize("command.abuseProtection.sub.receiverContext." + guildSettings.abuseSettings().isDonorContext())).queue();
            return;
        }
        var state = event.getOption("state").getAsBoolean();

        guildSettings.abuseSettings().donorContext(state);
        if (guildData.updateAbuseSettings(event.getGuild(), guildSettings.abuseSettings())) {
            event.reply(loc.localize("command.abuseProtection.sub.receiverContext." + state)).queue();
        }
    }

    private boolean receiverContext(MessageEventWrapper eventWrapper, CommandContext context, GuildSettings guildSettings) {
        if (context.argsEmpty()) {
            eventWrapper.reply(eventWrapper.localize("command.abuseProtection.sub.receiverContext." + guildSettings.abuseSettings().isDonorContext())).queue();
            return true;
        }
        var state = context.argBoolean(0);
        if (state.isEmpty()) {
            eventWrapper.replyErrorAndDelete(eventWrapper.localize("error.notABoolean",
                    Replacement.create("input", context.argString(0).get())), 10);
            return true;
        }

        guildSettings.abuseSettings().donorContext(state.get());
        if (guildData.updateAbuseSettings(eventWrapper.getGuild(), guildSettings.abuseSettings())) {
            eventWrapper.reply(loc.localize("command.abuseProtection.sub.receiverContext." + state)).queue();
        }
        return true;
    }

    private boolean maxMessageAge(MessageEventWrapper eventWrapper, CommandContext context, GuildSettings guildSettings) {
        if (context.argsEmpty()) {
            eventWrapper.reply(eventWrapper.localize("command.abuseProtection.sub.maxMessageAge.get",
                    Replacement.create("MINUTES", guildSettings.abuseSettings().maxMessageAge()))).queue();
            return true;
        }
        var optAge = context.argInt(0);

        if (optAge.isEmpty()) {
            eventWrapper.replyErrorAndDelete(context.argString(0).get() + " is not a number", 30);
            return true;
        }
        var age = Math.max(0, optAge.get());
        guildSettings.abuseSettings().maxMessageAge(age);
        if (guildData.updateAbuseSettings(eventWrapper.getGuild(), guildSettings.abuseSettings())) {
            eventWrapper.reply(eventWrapper.localize("command.abuseProtection.sub.maxMessageAge.get",
                    Replacement.create("MINUTES", age))).queue();
        }
        return true;
    }

    private void maxMessageAge(SlashCommandEvent event, GuildSettings guildSettings) {
        var loc = this.loc.getContextLocalizer(event.getGuild());
        if (event.getOptions().isEmpty()) {
            event.reply(loc.localize("command.abuseProtection.sub.maxMessageAge.get",
                    Replacement.create("MINUTES", guildSettings.abuseSettings().maxMessageAge()))).queue();
            return;
        }
        var age = event.getOption("minutes").getAsLong();

        age = Math.max(0, age);
        guildSettings.abuseSettings().maxMessageAge((int) age);
        if (guildData.updateAbuseSettings(event.getGuild(), guildSettings.abuseSettings())) {
            event.reply(loc.localize("command.abuseProtection.sub.maxMessageAge.get",
                    Replacement.create("MINUTES", age))).queue();
        }
    }

    private boolean minMessages(MessageEventWrapper eventWrapper, CommandContext context, GuildSettings guildSettings) {
        if (context.argsEmpty()) {
            eventWrapper.reply(eventWrapper.localize("command.abuseProtection.sub.minMessages.get",
                    Replacement.create("MINUTES", guildSettings.abuseSettings().minMessages()))).queue();
            return true;
        }
        var optAge = context.argInt(0);

        if (optAge.isEmpty()) {
            eventWrapper.replyErrorAndDelete(context.argString(0).get() + " is not a number", 30);
            return true;
        }
        var minMessages = Math.max(0, Math.min(optAge.get(), 100));
        guildSettings.abuseSettings().minMessages(minMessages);
        if (guildData.updateAbuseSettings(eventWrapper.getGuild(), guildSettings.abuseSettings())) {
            eventWrapper.reply(eventWrapper.localize("command.abuseProtection.sub.minMessages.get",
                    Replacement.create("AMOUNT", minMessages))).queue();
        }
        return true;
    }

    private void minMessages(SlashCommandEvent event, GuildSettings guildSettings) {
        var loc = this.loc.getContextLocalizer(event.getGuild());
        if (event.getOptions().isEmpty()) {
            event.reply(loc.localize("command.abuseProtection.sub.minMessages.get",
                    Replacement.create("AMOUNT", guildSettings.abuseSettings().minMessages()))).queue();
            return;
        }
        var minMessages = event.getOption("messages").getAsLong();

        minMessages = Math.max(0, Math.min(minMessages, 100));
        guildSettings.abuseSettings().minMessages((int) minMessages);
        if (guildData.updateAbuseSettings(event.getGuild(), guildSettings.abuseSettings())) {
            event.reply(loc.localize("command.abuseProtection.sub.minMessages.get",
                    Replacement.create("AMOUNT", minMessages))).queue();
        }
    }

    private boolean cooldown(MessageEventWrapper eventWrapper, CommandContext context, GuildSettings guildSettings) {
        if (context.argsEmpty()) {
            eventWrapper.reply(eventWrapper.localize("command.abuseProtection.sub.cooldown.get",
                    Replacement.create("MINUTES", guildSettings.abuseSettings().cooldown()))).queue();
            return true;
        }
        var optCooldown = context.argInt(0);

        if (optCooldown.isEmpty()) {
            eventWrapper.replyErrorAndDelete(eventWrapper.localize("error.notANumber",
                    Replacement.create("INPUT", context.argString(0).get())), 30);
            return false;
        }

        guildSettings.abuseSettings().cooldown(optCooldown.get());
        if (guildData.updateAbuseSettings(eventWrapper.getGuild(), guildSettings.abuseSettings())) {
            eventWrapper.reply(eventWrapper.localize("command.abuseProtection.sub.cooldown.set",
                    Replacement.create("MINUTES", optCooldown.get()))).queue();
        }
        return true;
    }

    private void cooldown(SlashCommandEvent event, GuildSettings guildSettings) {
        var loc = this.loc.getContextLocalizer(event.getGuild());
        if (event.getOptions().isEmpty()) {
            event.reply(loc.localize("command.abuseProtection.sub.cooldown.get",
                    Replacement.create("MINUTES", guildSettings.abuseSettings().cooldown()))).queue();
            return;
        }
        var cooldown = event.getOption("minutes").getAsLong();

        guildSettings.abuseSettings().cooldown((int) cooldown);
        if (guildData.updateAbuseSettings(event.getGuild(), guildSettings.abuseSettings())) {
            event.reply(loc.localize("command.abuseProtection.sub.cooldown.set",
                    Replacement.create("MINUTES", cooldown))).queue();
        }
    }


    private boolean sendSettings(MessageEventWrapper eventWrapper, GuildSettings guildSettings) {
        eventWrapper.reply(getSettings(eventWrapper.getGuild(), guildSettings)).queue();
        return true;
    }

    private void sendSettings(SlashCommandEvent event, GuildSettings guildSettings) {
        event.replyEmbeds(getSettings(event.getGuild(), guildSettings)).queue();
    }

    private MessageEmbed getSettings(Guild guild, GuildSettings guildSettings) {
        var cooldown = guildSettings.abuseSettings();
        var setting = List.of(
                getSetting("command.abuseProtection.embed.descr.maxMessageAge", cooldown.maxMessageAge()),
                getSetting("command.abuseProtection.embed.descr.minMessages", cooldown.minMessages()),
                getSetting("command.abuseProtection.embed.descr.cooldown", cooldown.cooldown()),
                getSetting("command.abuseProtection.embed.descr.donorContext", cooldown.isDonorContext()),
                getSetting("command.abuseProtection.embed.descr.receiverContext", cooldown.isReceiverContext())
        );

        var settings = String.join("\n", setting);

        return new LocalizedEmbedBuilder(this.loc, guild)
                .setTitle("command.abuseProtection.embed.title")
                .appendDescription(this.loc.localize(settings, guild))
                .setColor(Color.GREEN)
                .build();
    }

    private String getSetting(@PropertyKey(resourceBundle = "locale") String locale, Object object) {
        return String.format("$%s$: %s", locale, object);
    }
}
