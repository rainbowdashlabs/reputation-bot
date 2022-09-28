package de.chojo.repbot.commands.roles.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.util.Futures;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.service.RoleAccessException;
import de.chojo.repbot.service.RoleAssigner;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.RestAction;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

import static de.chojo.repbot.util.Guilds.prettyName;
import static org.slf4j.LoggerFactory.getLogger;

public class Refresh implements SlashHandler {
    private static final Logger log = getLogger(Refresh.class);
    private final RoleAssigner roleAssigner;
    private final Set<Long> running = new HashSet<>();

    public Refresh(RoleAssigner roleAssigner) {
        this.roleAssigner = roleAssigner;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        if (running.contains(event.getGuild().getIdLong())) {
            event.reply(context.localize("command.roles.refresh.message.running")).queue();
            return;
        }

        running.add(event.getGuild().getIdLong());

        var message = event.reply(context.localize("command.roles.refresh.message.started"))
                           .flatMap(InteractionHook::retrieveOriginal)
                           .complete();
        var start = Instant.now();

        roleAssigner
                .updateBatch(event.getGuild(), context, message)
                .whenComplete(Futures.whenComplete(res -> {
                    var duration = DurationFormatUtils.formatDuration(start.until(Instant.now(), ChronoUnit.MILLIS), "mm:ss");
                    log.info("Update of roles on {} took {}.", prettyName(event.getGuild()), duration);
                    message.editMessage(context.localize("command.roles.refresh.message.finished",
                                   Replacement.create("CHECKED", res.checked()), Replacement.create("UPDATED", res.updated())))
                           .queue(RestAction.getDefaultSuccess(), ErrorResponseException.ignore(ErrorResponse.UNKNOWN_MESSAGE));
                    running.remove(event.getGuild().getIdLong());
                }, err -> {
                    log.warn("Update of role failed on guild {}", prettyName(event.getGuild()), err);
                    if (err instanceof RoleAccessException roleException) {
                        message.editMessage(context.localize("error.roleAccess",
                                       Replacement.createMention("ROLE", roleException.role())))
                               .queue(RestAction.getDefaultSuccess(), ErrorResponseException.ignore(ErrorResponse.UNKNOWN_MESSAGE));
                    }
                    running.remove(event.getGuild().getIdLong());
                }));
    }

    public boolean refreshActive(Guild guild) {
        return running.contains(guild.getIdLong());
    }
}
