package de.chojo.repbot.dao.access.guild.settings.sub;

import de.chojo.jdautil.consumer.ThrowingConsumer;
import de.chojo.repbot.dao.access.guild.settings.Settings;
import de.chojo.repbot.dao.access.guild.settings.sub.thanking.Channels;
import de.chojo.repbot.dao.access.guild.settings.sub.thanking.DonorRoles;
import de.chojo.repbot.dao.access.guild.settings.sub.thanking.Reactions;
import de.chojo.repbot.dao.access.guild.settings.sub.thanking.ReceiverRoles;
import de.chojo.repbot.dao.access.guild.settings.sub.thanking.Thankwords;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.sqlutil.base.QueryFactoryHolder;
import de.chojo.sqlutil.wrapper.ParamBuilder;
import net.dv8tion.jda.api.entities.Guild;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Thanking extends QueryFactoryHolder implements GuildHolder {
    private Settings settings;
    private final String reaction;
    private boolean channelWhitelist;

    private Channels channels;
    private DonorRoles donorRoles;
    private ReceiverRoles receiverRoles;
    private Reactions reactions;
    private Thankwords thankwords;

    public Thanking(Settings settings) {
        this(settings, null, true);
    }

    public Thanking(Settings settings, String reaction, boolean channelWhitelist) {
        super(settings);
        this.settings = settings;
        this.reaction = reaction == null ? "✅" : reaction;
        this.channelWhitelist = channelWhitelist;
    }

    public Settings settings() {
        return settings;
    }

    public Channels channels() {
        return channels;
    }

    public DonorRoles donorRoles() {
        return donorRoles;
    }

    public ReceiverRoles receiverRoles() {
        return receiverRoles;
    }

    public Reactions reactions() {
        return reactions;
    }

    public Thankwords thankwords() {
        return thankwords;
    }

    public static Thanking build(Settings settings, ResultSet row) throws SQLException {
        return new Thanking(settings,
                row.getString("reaction"),
                row.getBoolean("channel_whitelist")
        );
    }

    @Override
    public Guild guild() {
        return settings.guild();
    }

    private boolean set(String parameter, ThrowingConsumer<ParamBuilder, SQLException> builder) {
        return builder()
                       .query("""
                               INSERT INTO thank_settings(guild_id, %s) VALUES (?, ?)
                               ON CONFLICT(guild_id)
                                   DO UPDATE SET %s = excluded.%s;
                               """, parameter, parameter, parameter)
                       .paramsBuilder(stmts -> {
                           stmts.setLong(guildId());
                           builder.accept(stmts);
                       }).insert()
                       .executeSync() > 0;
    }
}
