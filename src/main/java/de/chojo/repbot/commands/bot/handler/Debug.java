/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.bot.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.pagination.PageService;
import de.chojo.jdautil.pagination.bag.ListPageBag;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.access.guild.RepGuild;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.dao.snapshots.ReputationLogEntry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.utils.TimeFormat;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import static de.chojo.jdautil.util.Guilds.prettyName;

public class Debug implements SlashHandler {
    private final GuildRepository guildRepository;
    private PageService pageService;
    private static final Map<String, List<Permission>> PERMISSION_CATEGORIES = new LinkedHashMap<>() {{
        put("General", List.of(Arrays.copyOfRange(Permission.values(), 0, 14)));
        put("Membership Permissions", List.of(Arrays.copyOfRange(Permission.values(), 14, 20)));
        put("Text Permissions", List.of(Arrays.copyOfRange(Permission.values(), 20, 34)));
        put("Thread Permissions", List.of(Arrays.copyOfRange(Permission.values(), 34, 38)));
        put("Voice Permissions", List.of(Arrays.copyOfRange(Permission.values(), 38, 49)));
        put("Stage Channel Permissions", List.of(Arrays.copyOfRange(Permission.values(), 49, 50)));
        put("Advanced", List.of(Arrays.copyOfRange(Permission.values(), 50, 51)));
    }};

    public Debug(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var guildId = event.getOption("guild_id").getAsLong();
        var guild = event.getJDA().getShardManager().getGuildById(guildId);
        if (guild == null) {
            event.reply("Guild not found").setEphemeral(true).queue();
            return;
        }

        var channel = guild.getGuildChannelById(event.getOption("channel_id", () -> 0L, OptionMapping::getAsLong));
        sendDebug(event, pageService, guildRepository.guild(guild), channel);
    }

    public static void sendDebug(IReplyCallback callback, PageService pageService, RepGuild repGuild, @Nullable GuildChannel channel) {
        Guild guild = repGuild.guild();
        var selfMember = guild.getSelfMember();
        var settings = repGuild.settings();
        var reputation = repGuild.reputation();

        List<MessageEmbed> embeds = new ArrayList<>();

        embeds.add(new EmbedBuilder()
                .setTitle("Information about guild " + prettyName(guild))
                .addField("Owner", guild.retrieveOwner().complete().getUser().getAsTag(), true)
                .addField("Member", String.valueOf(guild.getMemberCount()), true)
                .addField("Joined", timestamp(selfMember.getTimeJoined()), true)
                .addField("Total Reputation", String.valueOf(reputation.stats().totalReputation()), true)
                .addField("Week Reputation", String.valueOf(reputation.stats().weekReputation()), true)
                .addField("Today Reputation", String.valueOf(reputation.stats().todayReputation()), true)
                .addField("Latest reputation",
                        timestamp(reputation.log().getLatestReputation()
                                            .map(ReputationLogEntry::received)
                                            .orElse(LocalDateTime.ofEpochSecond(0L, 0, ZoneOffset.UTC))), true)
                .build());

        embeds.add(buildPermissions(selfMember::hasPermission, new EmbedBuilder().setTitle("Permissions")).build());

        if (channel != null) {
            embeds.add(buildPermissions(p -> selfMember.hasPermission(channel, p),
                    new EmbedBuilder().setTitle("Channel Permissions for %s".formatted(channel.getName()))).build());
        }

        embeds.add(new EmbedBuilder()
                .setTitle("Settings")
                .addField("Reputation Settings", settings.reputation().prettyString(), true)
                .addField("General", settings.general().prettyString(), true)
                .addBlankField(false)
                .addField("Abuse Protection", settings.abuseProtection().prettyString(), true)
                .addField("Announcements", settings.announcements().prettyString(), true)
                .addBlankField(false)
                .addField("Message States", settings.messages().prettyString(), true)
                .build());

        var thanks = settings.thanking();

        List<? extends GuildChannel> channels;
        List<Category> categories;

        if (thanks.channels().isWhitelist()) {
            channels = thanks.channels().channels();
            categories = thanks.channels().categories();
        } else {
            channels = guild.getChannels().stream()
                            .filter(c -> c instanceof GuildMessageChannel)
                            .filter(c -> thanks.channels().isEnabled((GuildMessageChannel) c))
                            .toList();
            categories = guild.getCategories().stream()
                              .filter(category -> thanks.channels().isEnabledByCategory(category))
                              .toList();
        }

        var channelNames = channels.stream().map(Channel::getName).limit(25).collect(Collectors.joining(", "));
        var categoryNames = categories.stream().map(Category::getName).limit(25).collect(Collectors.joining(", "));

        embeds.add(new EmbedBuilder()
                .setTitle("Thank settings")
                .addField("Donor Roles", thanks.donorRoles().prettyString(), true)
                .addField("Receiver Roles", thanks.receiverRoles().prettyString(), true)
                .addBlankField(false)
                .addField("Channel count", String.valueOf(channels.size()), true)
                .addField("Category Count", String.valueOf(categories.size()), true)
                .addField("Channel", channelNames, true)
                .addField("Categories", categoryNames, true)
                .addField("List Type", thanks.channels().isWhitelist() ? "whitelist" : "blacklist", true)
                .addField("Thankwords", thanks.thankwords().prettyString(), false)
                .addField("Main Reaction", thanks.reactions().reactionMention().orElse("None"), true)
                .addField("Additional Reactions", String.join(" ", thanks.reactions()
                                                                         .getAdditionalReactionMentions()), true)
                .build());

        embeds.add(new EmbedBuilder()
                .setTitle("Ranks")
                .addField("Reputation ranks", settings.ranks().prettyString(), false)
                .addField("Bot roles",
                        guild.getSelfMember().getRoles().stream()
                             .map(role -> "%s(%d)".formatted(role.getName(), role.getPosition()))
                             .collect(Collectors.joining("\n")), false)
                .build());

        var pages = new ListPageBag<>(embeds) {
            @Override
            public CompletableFuture<MessageEditData> buildPage() {
                return CompletableFuture.completedFuture(MessageEditData.fromEmbeds(currentElement()));
            }
        };

        pageService.registerPage(callback, pages, true);
    }

    private static String timestamp(LocalDateTime dateTime) {
        return TimeFormat.DATE_TIME_SHORT.format(dateTime.toEpochSecond(ZoneOffset.UTC) * 1000);
    }

    private static String timestamp(OffsetDateTime dateTime) {
        return TimeFormat.DATE_TIME_SHORT.format(dateTime.toEpochSecond() * 1000);
    }

    public void inject(PageService service) {
        pageService = service;
    }

    private static EmbedBuilder buildPermissions(Function<Permission, Boolean> permissionCheck, EmbedBuilder builder) {
        PERMISSION_CATEGORIES.entrySet()
                             .stream()
                             .map(
                                     entry -> new Field(
                                             entry.getKey(),
                                             entry.getValue().stream()
                                                  .sorted(Comparator.comparing(p -> permissionCheck.apply(p) ? 1 : 0, Integer::compareTo))
                                                  .sorted(Comparator.reverseOrder())
                                                  .map(perm -> "%s %s".formatted(permissionCheck.apply(perm) ? "✅" : "❌", perm.getName()))
                                                  .collect(Collectors.joining("\n")),
                                             true)
                             )
                             .forEachOrdered(builder::addField);
        return builder;
    }
}
