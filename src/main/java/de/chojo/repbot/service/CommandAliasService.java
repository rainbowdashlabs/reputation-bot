/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service;

import de.chojo.jdautil.interactions.base.InteractionMeta;
import de.chojo.jdautil.interactions.dispatching.InteractionHub;
import de.chojo.jdautil.util.Premium;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.provider.GuildRepository;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static de.chojo.repbot.util.Guilds.prettyName;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Manages the custom names a guild can give to the reputation and the profile command.
 * <p>
 * Aliases are only registered on guilds entitled to the command alias feature. Entitlements change through discord,
 * token purchases, ko-fi and expiry, so instead of hooking into each of them, the guilds with stored aliases are
 * rechecked every minute and their commands are refreshed once their entitlement changed.
 */
public class CommandAliasService {
    public static final Set<String> COMMANDS = Set.of("reputation", "profile");
    private static final Logger log = getLogger(CommandAliasService.class);
    private static final Pattern NAME = Pattern.compile("^[-_\\p{L}\\p{N}\\p{IsDevanagari}\\p{IsThai}]{1,32}$");

    private final GuildRepository guildRepository;
    private final Configuration configuration;
    private final ShardManager shardManager;
    private final Map<Long, Boolean> registeredEntitlement = new ConcurrentHashMap<>();
    private InteractionHub<?, ?, ?> hub;

    public CommandAliasService(
            GuildRepository guildRepository, Configuration configuration, ShardManager shardManager) {
        this.guildRepository = guildRepository;
        this.configuration = configuration;
        this.shardManager = shardManager;
    }

    /**
     * Connects the service to the hub the aliases are registered on and starts the entitlement check.
     *
     * @param hub    interaction hub using {@link #aliasesOf(InteractionMeta)}
     * @param worker executor for the periodic entitlement check
     */
    public void start(InteractionHub<?, ?, ?> hub, ScheduledExecutorService worker) {
        this.hub = hub;
        worker.scheduleAtFixedRate(this::refreshChangedEntitlements, 1, 1, TimeUnit.MINUTES);
    }

    /**
     * Alias mapper for the interaction hub. Only returns aliases of guilds that are entitled to the feature.
     *
     * @param meta command meta
     * @return alias keyed by guild id
     */
    public Map<Long, String> aliasesOf(InteractionMeta meta) {
        if (!COMMANDS.contains(meta.name())) return Map.of();
        return guildRepository.commandAliases(meta.name()).entrySet().stream()
                .filter(entry -> rememberEntitlement(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Sets or removes the alias of a command and deploys the commands of the guild again.
     * An empty alias or the name of the command itself removes the alias.
     *
     * @param guild   guild
     * @param command name of the command
     * @param alias   new name of the command
     * @return the reason the alias was rejected, empty if it was applied
     */
    public Optional<String> setAlias(Guild guild, String command, String alias) {
        var name = alias == null ? "" : alias.trim().toLowerCase(Locale.ROOT);
        var aliases = guildRepository.guild(guild).settings().commandAliases();
        var error = validate(command, name, hub.getSlash(name).isPresent(), aliases.aliases());
        if (error.isPresent()) return error;
        if (name.isEmpty() || name.equals(command)) {
            aliases.remove(command);
        } else {
            aliases.set(command, name);
        }
        hub.refreshGuildCommands(guild);
        return Optional.empty();
    }

    /**
     * Checks whether a name can be used as alias of a command on a guild.
     *
     * @param command      name of the command
     * @param name         lowercased alias, empty to reset
     * @param isBotCommand whether the alias is the name of a command of the bot
     * @param guildAliases current aliases of the guild keyed by command
     * @return the reason the alias is rejected, empty if it is accepted
     */
    static Optional<String> validate(
            String command, String name, boolean isBotCommand, Map<String, String> guildAliases) {
        if (!COMMANDS.contains(command)) return Optional.of("Unknown command " + command + ".");
        if (name.isEmpty() || name.equals(command)) return Optional.empty();
        if (!NAME.matcher(name).matches()) {
            return Optional.of("Names are 1 to 32 characters long and may only contain letters, numbers, - and _.");
        }
        if (isBotCommand) return Optional.of("/" + name + " is already a command of the bot.");
        for (var entry : guildAliases.entrySet()) {
            if (!entry.getKey().equals(command) && entry.getValue().equals(name)) {
                return Optional.of("/" + name + " is already the name of /" + entry.getKey() + ".");
            }
        }
        return Optional.empty();
    }

    private boolean rememberEntitlement(long guildId) {
        var entitled = isEntitled(guildId);
        registeredEntitlement.put(guildId, entitled);
        return entitled;
    }

    private boolean isEntitled(long guildId) {
        return !Premium.isNotEntitled(
                guildRepository.byId(guildId).subscriptions(),
                configuration.skus().features().commandAliases().fullSkuEntry());
    }

    private void refreshChangedEntitlements() {
        for (var entry : registeredEntitlement.entrySet()) {
            if (isEntitled(entry.getKey()) == entry.getValue()) continue;
            var guild = shardManager.getGuildById(entry.getKey());
            if (guild == null) {
                registeredEntitlement.remove(entry.getKey());
                continue;
            }
            try {
                log.info("Entitlement for command aliases changed on {}. Refreshing commands.", prettyName(guild));
                hub.refreshGuildCommands(guild);
            } catch (RuntimeException e) {
                log.error("Could not refresh commands on {}.", prettyName(guild), e);
            }
        }
    }
}
