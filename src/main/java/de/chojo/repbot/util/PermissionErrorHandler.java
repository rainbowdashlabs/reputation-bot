package de.chojo.repbot.util;

import de.chojo.jdautil.localization.ILocalizer;
import de.chojo.jdautil.localization.util.Format;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.repbot.config.Configuration;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.sharding.ShardManager;

public class PermissionErrorHandler {
    public static void handle(InsufficientPermissionException permissionException, ShardManager shardManager, ILocalizer localizer, Configuration configuration) {
        var permission = permissionException.getPermission();
        var guildById = shardManager.getGuildById(permissionException.getGuildId());
        var channel = (TextChannel) permissionException.getChannel(guildById.getJDA());
        if (channel == null) return;
        var errorMessage = localizer.localize("error.missingPermission", guildById,
                Replacement.create("PERM", permission.getName(), Format.BOLD));
        if (guildById.getSelfMember().hasPermission(permission)) {
            errorMessage += "\n" + localizer.localize("error.missingPermissionChannel", guildById,
                    Replacement.createMention(channel));
        } else {
            errorMessage += "\n" + localizer.localize("error.missingPermissionGuild", guildById);
        }
        if (permissionException.getPermission() != Permission.MESSAGE_WRITE) {
            channel.sendMessage(errorMessage).queue();
            return;
        }
        // botlists always have permission issues. We will ignore them and wont try to notify anyone...
        if (configuration.botlist().isBotlistGuild(permissionException.getGuildId())) return;
        var ownerId = guildById.getOwnerIdLong();
        var finalErrorMessage = errorMessage;
        guildById.retrieveMemberById(ownerId)
                .flatMap(member -> member.getUser().openPrivateChannel())
                .flatMap(privateChannel -> privateChannel.sendMessage(finalErrorMessage))
                .onErrorMap(t -> null)
                .queue();
    }
}
