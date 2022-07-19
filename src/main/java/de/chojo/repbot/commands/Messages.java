package de.chojo.repbot.commands;

import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.menus.EntryContext;
import de.chojo.jdautil.menus.MenuAction;
import de.chojo.jdautil.menus.entries.MenuEntry;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.dao.access.guild.settings.Settings;
import de.chojo.repbot.dao.provider.Guilds;
import de.chojo.repbot.util.EmojiDebug;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import org.jetbrains.annotations.PropertyKey;

import java.awt.Color;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class Messages extends SimpleCommand {
    private final Guilds guilds;

    public Messages(Guilds guilds) {
        super(CommandMeta.builder("messages", "command.messages.description")
                .addSubCommand("states", "command.messages.sub.states")
                .adminCommand());
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var guildSettings = guilds.guild(event.getGuild()).settings();

        var subcmd = event.getSubcommandName();
        if ("states".equalsIgnoreCase(subcmd)) {
            sendSettings(event, context, guildSettings);
        }
    }

    private void sendSettings(SlashCommandInteractionEvent event, SlashCommandContext context, Settings guildSettings) {
        var setting = SelectMenu.create("setting")
                .setPlaceholder("command.messages.embed.choose")
                .setRequiredRange(1, 1)
                .addOption("command.messages.embed.reactionConfirmation", "reaction_confirmation", "command.messages.embed.reactionConfirmation.descr")
                .build();
        var reactions = getMenu("reaction_confirmation",
                "command.messages.embed.reactionConfirmation",
                "command.messages.embed.reactionConfirmation.true",
                "command.messages.embed.reactionConfirmation.false",
                guildSettings.reputation().isReactionActive());

        context.registerMenu(MenuAction.forCallback(getSettings(context, guildSettings), event)
                .addComponent(MenuEntry.of(setting, ctx -> {
                    var option = ctx.event().getValues().get(0);
                    var entry = ctx.container().entry(option).get();
                    ctx.container().entries().forEach(MenuEntry::hidden);
                    ctx.entry().visible(true);
                    entry.visible(true);
                    var copy = ctx.entry().component().createCopy();
                    copy.setDefaultValues(Collections.singleton(option));
                    ctx.entry().component(copy.build());
                    ctx.refresh();
                }))
                .addComponent(MenuEntry.of(reactions, ctx -> {
                    refresh(ctx, res -> guildSettings.messages().reactionConfirmation(res), context, guildSettings);
                }).hidden())
                .asEphemeral()
                .build());
    }

    private SelectMenu getMenu(String id, String placeholder, String enabledDescr, String disabledDescr, boolean state) {
        return SelectMenu.create(id)
                .setPlaceholder(placeholder)
                .setRequiredRange(1, 1)
                .addOption("words.enabled", "enabled", enabledDescr)
                .addOption("words.disabled", "disabled", disabledDescr)
                .setDefaultValues(Collections.singleton(state ? "enabled" : "disabled"))
                .build();
    }

    private void refresh(EntryContext<SelectMenuInteractionEvent, SelectMenu> ctx, Consumer<Boolean> result, SlashCommandContext context, Settings guildSettings) {
        var value = ctx.event().getValues().get(0);
        var copy = ctx.entry().component().createCopy();
        copy.setDefaultValues(Collections.singleton(value));
        result.accept("enabled".equals(value));
        var settings = getSettings(context, guildSettings);
        ctx.entry().component(copy.build());
        ctx.refresh(settings);
    }

    private MessageEmbed getSettings(SlashCommandContext context, Settings guildSettings) {
        var messages = guildSettings.messages();

        return new LocalizedEmbedBuilder(context.localizer())
                .setTitle("command.messages.embed.title")
                .appendDescription(messages.toLocalizedString())
                .setColor(Color.GREEN)
                .build();
    }
}
