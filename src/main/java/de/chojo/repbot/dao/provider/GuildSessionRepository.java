package de.chojo.repbot.dao.provider;

import de.chojo.repbot.dao.access.guildsession.GuildSessionMeta;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.util.Optional;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

public class GuildSessionRepository {

    public Optional<GuildSessionMeta> byToken(String token) {
        return query("SELECT * FROM guild_session WHERE token = ?")
                .single(call().bind(token))
                .mapAs(GuildSessionMeta.class)
                .first();
    }

    public Optional<GuildSessionMeta> byGuildAndMemer(Guild guild, Member member) {
        return query("SELECT * FROM guild_session WHERE guild_id = ? AND member_id = ?")
                .single(call().bind(guild.getIdLong()).bind(member.getIdLong()))
                .mapAs(GuildSessionMeta.class)
                .first();
    }

    public GuildSessionMeta createNewSession(String token, long guildId, long memberId) {
        return query("INSERT INTO guild_session (token, guild_id, member_id) VALUES (?, ?, ?) RETURNING token, guild_id, member_id, created, last_used")
                .single(call().bind(token).bind(guildId).bind(memberId))
                .mapAs(GuildSessionMeta.class)
                .first()
                .orElseThrow();
    }
}
