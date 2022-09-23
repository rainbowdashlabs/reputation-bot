package de.chojo.repbot.dao.access.guild.settings.sub.thanking;

import de.chojo.repbot.dao.access.guild.settings.sub.Thanking;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.sadu.base.QueryFactory;
import net.dv8tion.jda.api.entities.Guild;

import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Thankwords extends QueryFactory implements GuildHolder {
    private static final String THANKWORD = "((?:^|\\b)%s(?:$|\\b))";
    private static final String PATTERN = "(?i)(%s)";

    private final Thanking thanking;

    private final Set<String> thankwords;

    public Thankwords(Thanking thanking, Set<String> thankwords) {
        super(thanking);
        this.thanking = thanking;
        this.thankwords = thankwords;
    }

    @Override
    public Guild guild() {
        return thanking.guild();
    }

    @Override
    public long guildId() {
        return thanking.guildId();
    }

    public Set<String> words() {
        return thankwords;
    }

    public Pattern thankwordPattern() {
        if (thankwords.isEmpty()) return Pattern.compile("");
        var twPattern = thankwords.stream()
                                  .map(t -> String.format(THANKWORD, t))
                                  .collect(Collectors.joining("|"));
        return Pattern.compile(String.format(PATTERN, twPattern),
                Pattern.CASE_INSENSITIVE + Pattern.MULTILINE + Pattern.DOTALL + Pattern.COMMENTS);
    }

    public boolean add(String pattern) {
        var result = builder()
                .query("""
                       INSERT INTO
                           thankwords(guild_id, thankword) VALUES(?,?)
                               ON CONFLICT(guild_id, thankword)
                                   DO NOTHING;
                       """)
                .parameter(stmt -> stmt.setLong(guildId()).setString(pattern))
                .update()
                .sendSync()
                .changed();
        if (result) {
            thankwords.add(pattern);
        }
        return result;
    }

    public boolean remove(String pattern) {
        var result = builder()
                .query("""
                       DELETE FROM
                           thankwords
                       WHERE
                           guild_id = ?
                           AND thankword = ?
                       """)
                .parameter(stmt -> stmt.setLong(guildId()).setString(pattern))
                .update()
                .sendSync()
                .changed();
        if (result) {
            thankwords.remove(pattern);
        }
        return result;
    }
}
