/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.setup.handler;

import de.chojo.jdautil.conversation.Conversation;
import de.chojo.jdautil.conversation.builder.ConversationBuilder;
import de.chojo.jdautil.conversation.elements.ButtonDialog;
import de.chojo.jdautil.conversation.elements.ComponenAction;
import de.chojo.jdautil.conversation.elements.ConversationContext;
import de.chojo.jdautil.conversation.elements.Result;
import de.chojo.jdautil.conversation.elements.Step;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Format;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.parsing.ArgumentUtil;
import de.chojo.jdautil.parsing.DiscordResolver;
import de.chojo.jdautil.parsing.ValueParser;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.provider.Guilds;
import de.chojo.repbot.serialization.ThankwordsContainer;
import de.chojo.repbot.util.PermissionErrorHandler;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Handler for the "start setup" slash command.
 * This command initiates the setup process for the bot.
 */
public class Start implements SlashHandler {
    private final Guilds guilds;
    private final ThankwordsContainer thankwordsContainer;
    private final Configuration configuration;

    /**
     * Constructs a new Start handler.
     *
     * @param guilds the guilds provider
     * @param thankwordsContainer the thankwords container
     * @param configuration the bot configuration
     */
    public Start(Guilds guilds, ThankwordsContainer thankwordsContainer, Configuration configuration) {
        this.guilds = guilds;
        this.thankwordsContainer = thankwordsContainer;
        this.configuration = configuration;
    }

    /**
     * Handles the slash command interaction event.
     *
     * @param event the slash command interaction event
     * @param context the event context
     */
    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        PermissionErrorHandler.assertAndHandle(event.getChannel().asGuildMessageChannel(), context.guildLocalizer(),
                configuration, Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL);
        event.reply(context.localize("command.setup.message.starting")).queue();
        context.conversationService()
                .startDialog(event.getUser(), event.getChannel().asGuildMessageChannel(), getConversation(context));
    }

    /**
     * Builds the conversation for the setup process.
     *
     * @param context the event context
     * @return the conversation
     */
    private Conversation getConversation(EventContext context) {
        var builder = ConversationBuilder.builder(
                        Step.button("**$%s$**%n$%s$".formatted("command.setup.dialog.welcome", "command.setup.message.continueToProceed"),
                                        buttons -> buttons
                                                .add(Button.success("continue", "word.continue"), ctx -> Result.proceed(1)))
                                .build())
                .addStep(1, buildSelectLanguage(context))
                .addStep(3, buildRoles())
                .addStep(4, buildLoadDefaults())
                .addStep(5, buildChannels());

        return builder.build();
    }

    /**
     * Builds the step for selecting the language.
     *
     * @param context the event context
     * @return the step
     */
    private Step buildSelectLanguage(EventContext context) {
        return Step.button("command.setup.message.language", but -> buildLanguageButtons(but, context))
                .build();
    }

    /**
     * Builds the language selection buttons.
     *
     * @param buttons the button dialog
     * @param context the event context
     */
    private void buildLanguageButtons(ButtonDialog buttons, EventContext context) {
        for (var language : context.guildLocalizer().localizer().languages()) {
            buttons.add(Button.of(ButtonStyle.PRIMARY, language.getLocale(), language.getNativeName()),
                    con -> {
                        guilds.guild(con.getGuild()).settings().general().language(language);
                        con.reply(con.localize("command.locale.set.message.set",
                                Replacement.create("LOCALE", language.getNativeName()))).queue();
                        return Result.proceed(3);
                    });
        }
    }

    /**
     * Builds the step for setting roles.
     *
     * @return the step
     */
    private Step buildRoles() {
        return Step
                .message("command.setup.message.roles".stripIndent(), this::buildRolesButton)
                .button(buttons -> buttons
                        .add(Button.success("done", "word.done"), ctx -> Result.proceed(4)))
                .build();
    }

    /**
     * Builds the roles button.
     *
     * @param context the conversation context
     * @return the result
     */
    private Result buildRolesButton(ConversationContext context) {
        var args = ArgumentUtil.parseQuotedArgs(context.getContentRaw(), true);
        if (args.length != 2) {
            return responseInvalid(context, "command.setup.message.rolesformat");
        }
        var role = DiscordResolver.getRole(context.getGuild(), args[0]);
        if (role.isEmpty()) {
            return responseInvalid(context, "error.invalidRole");
        }
        var optionalReputation = ValueParser.parseInt(args[1]);
        return optionalReputation
                .map(reputation -> responseRolesSubAdded(context, role.get(), reputation))
                .orElseGet(() -> responseInvalid(context, "error.invalidNumber"));
    }

    /**
     * Responds with a message indicating the role was added.
     *
     * @param context the conversation context
     * @param role the role
     * @param reputation the reputation points
     * @return the result
     */
    @NotNull
    private Result responseRolesSubAdded(ConversationContext context, Role role, Integer reputation) {
        guilds.guild(context.getGuild()).settings().ranks().add(role, reputation);
        context.reply(context.localize("command.roles.add.message.added",
                        Replacement.createMention(role),
                        Replacement.create("POINTS", reputation, Format.BOLD)))
                .queue();
        return Result.freeze();
    }

    /**
     * Builds the step for loading default thankwords.
     *
     * @return the step
     */
    private Step buildLoadDefaults() {
        return Step.button("command.setup.message.loadDefaults",
                        this::buildLoadDefaultsButton)
                .build();
    }

    /**
     * Builds the load defaults button.
     *
     * @param buttons the button dialog
     */
    private void buildLoadDefaultsButton(ButtonDialog buttons) {
        var languages = thankwordsContainer.getAvailableLanguages();
        for (var language : languages) {
            buttons.add(Button.of(ButtonStyle.PRIMARY, language, language),
                    context -> {
                        var words = thankwordsContainer.get(language.toLowerCase(Locale.ROOT));
                        words.forEach(word -> guilds.guild(context.getGuild()).settings().thanking().thankwords()
                                .add(word));
                        var wordsJoined = words.stream().map(w -> StringUtils.wrap(w, "`"))
                                .collect(Collectors.joining(", "));
                        context.reply(context.localize("command.thankwords.loaddefault.message.added") + wordsJoined)
                                .queue();
                        return Result.freeze();
                    });
        }
        buttons.add(Button.success("done", "word.done"), ctx -> Result.proceed(5));
    }

    /**
     * Builds the step for setting channels.
     *
     * @return the step
     */
    private Step buildChannels() {
        return Step.button("command.setup.message.channels", this::buildChannelsButton)
                .message(this::handleChannels)
                .build();
    }

    /**
     * Builds the channels button.
     *
     * @param buttons the button dialog
     */
    private void buildChannelsButton(ButtonDialog buttons) {
        buttons.add(new ComponenAction(Button.success("done", "word.done"), ctx -> {
            ctx.reply(ctx.localize("command.setup.message.complete"))
                    .queue();
            return Result.finish();
        })).add(Button.primary("all", "command.setup.message.allchannel"), ctx -> {
            var guild = ctx.getGuild();
            guilds.guild(guild).settings().thanking().channels().listType(false);
            ctx.reply(ctx.localize("command.channel.list.message.blacklist")).queue();
            return Result.finish();
        });
    }

    /**
     * Handles the channels input.
     *
     * @param context the conversation context
     * @return the result
     */
    private Result handleChannels(ConversationContext context) {
        var args = context.getContentRaw().replaceAll("\\s+", " ").split("\\s");
        var channels = DiscordResolver.getTextChannels(context.getGuild(), List.of(args));
        var addedChannel = channels.stream()
                .map(channel -> {
                    guilds.guild(context.getGuild()).settings().thanking().channels().add(channel);
                    return channel.getAsMention();
                })
                .collect(Collectors.joining(", "));
        context.reply(
                        context.localize("command.channel.add.message.added",
                                Replacement.create("CHANNEL", addedChannel)))
                .setAllowedMentions(Collections.emptyList())
                .queue();
        return Result.freeze();
    }

    /**
     * Responds with an invalid input message.
     *
     * @param context the conversation context
     * @param s the message key
     * @return the result
     */
    @NotNull
    private Result responseInvalid(ConversationContext context, String s) {
        context.reply(context.localize(s)).queue();
        return Result.freeze();
    }
}
