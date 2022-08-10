package de.chojo.repbot.service;

import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RoleUpdater extends ListenerAdapter implements Runnable {
    private final Guilds guilds;
    private final Map<Long, Set<Long>> checked = new HashMap<>();
    private final RoleAssigner roleAssigner;

    public static RoleUpdater create(Guilds guilds, RoleAssigner roleAssigner, ScheduledExecutorService executorService) {
        RoleUpdater roleUpdater = new RoleUpdater(guilds, roleAssigner);
        executorService.scheduleAtFixedRate(roleUpdater, 30, 30, TimeUnit.MINUTES);
        return roleUpdater;
    }

    public RoleUpdater(Guilds guilds, RoleAssigner roleAssigner) {
        this.guilds = guilds;
        this.roleAssigner = roleAssigner;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.isFromGuild()) return;
        if (!guilds.guild(event.getGuild()).settings().general().reputationMode().isAutoRefresh()) return;
        if (isChecked(event.getMember())) return;
        roleAssigner.updateReporting(event.getMember(), event.getGuildChannel());
        guildSet(event.getGuild()).add(event.getMember().getIdLong());
    }

    public boolean isChecked(Member member) {
        return guildSet(member.getGuild()).contains(member.getIdLong());
    }

    public Set<Long> guildSet(Guild guild) {
        return checked.computeIfAbsent(guild.getIdLong(), k -> new HashSet<>());
    }

    @Override
    public void run() {
        checked.clear();
    }
}
