package de.chojo.repbot.commands;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.conversation.Conversation;
import de.chojo.jdautil.conversation.builder.ConversationBuilder;
import de.chojo.jdautil.conversation.elements.ButtonDialog;
import de.chojo.jdautil.conversation.elements.ComponenAction;
import de.chojo.jdautil.conversation.elements.ConversationContext;
import de.chojo.jdautil.conversation.elements.Result;
import de.chojo.jdautil.conversation.elements.Step;
import de.chojo.jdautil.localization.util.Format;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.parsing.ArgumentUtil;
import de.chojo.jdautil.parsing.DiscordResolver;
import de.chojo.jdautil.parsing.ValueParser;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.dao.provider.Guilds;
import de.chojo.repbot.serialization.ThankwordsContainer;
import de.chojo.repbot.util.FilterUtil;
import de.chojo.repbot.util.PermissionErrorHandler;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

public class Setup extends SimpleCommand {

    private static final Logger log = getLogger(Setup.class);
    private final Guilds guilds;
    private final ThankwordsContainer thankwordsContainer;

    public Setup(Guilds guilds, ThankwordsContainer thankwordsContainer) {
        super(CommandMeta.builder("setup", "command.setup.description").withPermission());
        this.guilds = guilds;
        this.thankwordsContainer = thankwordsContainer;
    }

    public static Setup of(Guilds guilds) {
        ThankwordsContainer thankwordsContainer;
        try {
            thankwordsContainer = new ObjectMapper()
                    .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                    .readValue(Thankwords.class.getClassLoader().getResourceAsStream("Thankswords.json"),
                            ThankwordsContainer.class);
        } catch (IOException e) {
            thankwordsContainer = null;
            log.error("Could not read thankwords", e);
        }
        return new Setup(guilds, thankwordsContainer);
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        PermissionErrorHandler.assertPermissions(event.getTextChannel(), Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL);
        event.reply(context.localize("command.setup.dialog.starting")).queue();
        context.conversationService().startDialog(event.getUser(), event.getTextChannel(), getConversation(context));
    }

    private Conversation getConversation(SlashCommandContext context) {
        var builder = ConversationBuilder.builder(
                        Step.button("**$command.setup.dialog.welcome$**\n$command.setup.dialog.continueToProceed$",
                                        buttons -> buttons
                                                .add(Button.success("continue", "word.continue"), ctx -> Result.proceed(1)))
                                .build())
                .addStep(1, buildSelectLanguage(context))
                .addStep(3, buildRoles())
                .addStep(4, buildLoadDefaults())
                .addStep(5, buildChannels());

        return builder.build();
    }

    private Step buildSelectLanguage(SlashCommandContext context) {
        return Step.button("command.setup.dialog.selectLanguage", but -> buildLanguageButtons(but, context))
                .build();
    }

    private void buildLanguageButtons(ButtonDialog buttons, SlashCommandContext context) {
        for (var language : context.localizer().localizer().languages()) {
            buttons.add(Button.of(ButtonStyle.PRIMARY, language.getCode(), language.getLanguage()),
                    con -> {
                        guilds.guild(con.getGuild()).settings().general().language(language);
                        con.reply(con.localize("command.locale.sub.set.set",
                                Replacement.create("LOCALE", language.getLanguage()))).queue();
                        return Result.proceed(3);
                    });
        }
    }

    private Step buildRoles() {
        return Step
                .message("command.setup.dialog.roles".stripIndent(), this::buildRolesButton)
                .button(buttons -> buttons
                        .add(Button.success("done", "word.done"), ctx -> Result.proceed(4)))
                .build();
    }

    private Result buildRolesButton(ConversationContext context) {
        var args = ArgumentUtil.parseQuotedArgs(context.getContentRaw(), true);
        if (args.length != 2) {
            return responseInvalid(context, "command.setup.dialog.rolesFormat");
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

    @NotNull
    private Result responseRolesSubAdded(ConversationContext context, Role role, Integer reputation) {
        guilds.guild(context.getGuild()).settings().ranks().add(role, reputation);
        context.reply(context.localize("command.roles.sub.add.added",
                        Replacement.createMention(role),
                        Replacement.create("POINTS", reputation, Format.BOLD)))
                .queue();
        return Result.freeze();
    }

    private Step buildLoadDefaults() {
        return Step.button("command.setup.dialog.loadDefaults",
                        this::buildLoadDefaultsButton)
                .build();
    }

    private void buildLoadDefaultsButton(ButtonDialog buttons) {
        var languages = thankwordsContainer.getAvailableLanguages();
        for (var language : languages) {
            buttons.add(Button.of(ButtonStyle.PRIMARY, language, language),
                    context -> {
                        var words = thankwordsContainer.get(language.toLowerCase(Locale.ROOT));
                        words.forEach(word -> guilds.guild(context.getGuild()).settings().thanking().thankwords().add(word));
                        var wordsJoined = words.stream().map(w -> StringUtils.wrap(w, "`"))
                                .collect(Collectors.joining(", "));
                        context.reply(context.localize("command.thankwords.sub.loadDefault.added") + wordsJoined)
                                .queue();
                        return Result.freeze();
                    });
        }
        buttons.add(Button.success("done", "word.done"), ctx -> Result.proceed(5));
    }

    private Step buildChannels() {
        return Step.button("command.setup.dialog.channels", this::buildChannelsButton)
                .message(this::handleChannels)
                .build();
    }

    private void buildChannelsButton(ButtonDialog buttons) {
        buttons.add(new ComponenAction(Button.success("done", "word.done"), ctx -> {
            ctx.reply(ctx.localize("command.setup.dialog.setupComplete"))
                    .queue();
            return Result.finish();
        })).add(Button.primary("all", "command.setup.dialog.channels.allChannel"), ctx -> {
            var guild = ctx.getGuild();
            FilterUtil.getAccessableTextChannel(guild).forEach(channel -> guilds
                    .guild(guild).settings().thanking().channels().add(channel));
            ctx.reply(ctx.localize("command.channel.sub.addAll.added")).queue();
            return Result.finish();
        });
    }

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
                        context.localize("command.channel.sub.add.added",
                                Replacement.create("CHANNEL", addedChannel)))
                .allowedMentions(Collections.emptyList())
                .queue();
        return Result.freeze();
    }

    @NotNull
    private Result responseInvalid(ConversationContext context, String s) {
        context.reply(context.localize(s)).queue();
        return Result.freeze();
    }
}
