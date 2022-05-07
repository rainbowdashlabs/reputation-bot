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
import java.util.HashSet;

public class Thanking extends QueryFactoryHolder implements GuildHolder {
    private final String mainReaction;
    private final Settings settings;
    private final boolean channelWhitelist;

    private Channels channels;
    private DonorRoles donorRoles;
    private ReceiverRoles receiverRoles;
    private Reactions reactions;
    private Thankwords thankwords;

    public Thanking(Settings settings) {
        this(settings, null, true);
    }

    public Thanking(Settings settings, String mainReaction, boolean channelWhitelist) {
        super(settings);
        this.settings = settings;
        this.mainReaction = mainReaction == null ? "🏅" : mainReaction;
        this.channelWhitelist = channelWhitelist;
    }

    public static Thanking build(Settings settings, ResultSet row) throws SQLException {
        return new Thanking(settings,
                row.getString("reaction"),
                row.getBoolean("channel_whitelist")
        );
    }

    public Channels channels() {
        if (channels != null) {
            return channels;
        }
        var channels = builder(Long.class)
                .query("""
                        SELECT channel_id
                        FROM active_channel
                        WHERE guild_id = ?
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guildId()))
                .readRow(r -> r.getLong("channel_id"))
                .allSync();
        this.channels = new Channels(this, channelWhitelist, new HashSet<>(channels));
        return this.channels;
    }

    public DonorRoles donorRoles() {
        if (donorRoles != null) {
            return donorRoles;
        }
        var roles = builder(Long.class)
                .query("""
                        SELECT role_id
                        FROM donor_roles
                        WHERE guild_id = ?
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guildId()))
                .readRow(r -> r.getLong("role_id"))
                .allSync();

        donorRoles = new DonorRoles(this, new HashSet<>(roles));
        return donorRoles;
    }

    public ReceiverRoles receiverRoles() {
        if (receiverRoles != null) {
            return receiverRoles;
        }
        var roles = builder(Long.class)
                .query("""
                        SELECT role_id
                        FROM receiver_roles
                        WHERE guild_id = ?
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guildId()))
                .readRow(r -> r.getLong("role_id"))
                .allSync();

        receiverRoles = new ReceiverRoles(this, new HashSet<>(roles));
        return receiverRoles;
    }

    public Reactions reactions() {
        if (reactions != null) {
            return reactions;
        }
        var reactions = builder(String.class)
                .query("""
                        SELECT reaction
                        FROM guild_reactions
                        WHERE guild_id = ?
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guildId()))
                .readRow(r -> r.getString("reaction"))
                .allSync();
        this.reactions = new Reactions(this, mainReaction, new HashSet<>(reactions));
        return this.reactions;
    }

    public Thankwords thankwords() {
        if (thankwords != null) {
            return thankwords;
        }
        var thankwords = builder(String.class)
                .query("""
                        SELECT thankword
                        FROM thankwords
                        WHERE guild_id = ?
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guildId()))
                .readRow(r -> r.getString("thankword"))
                .allSync();

        this.thankwords = new Thankwords(this, new HashSet<>(thankwords));
        return this.thankwords;
    }

    @Override
    public Guild guild() {
        return settings.guild();
    }
}
