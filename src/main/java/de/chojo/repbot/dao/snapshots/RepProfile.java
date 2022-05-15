package de.chojo.repbot.dao.snapshots;

import de.chojo.jdautil.util.MentionUtil;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.apache.commons.lang3.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Snapshot of a user reputation profile.
 */
public record RepProfile(long rank, Long userId, long reputation) {

    public static RepProfile empty(User user) {
        return new RepProfile(0, user.getIdLong(), 0);
    }

    public static RepProfile build(ResultSet rs) throws SQLException {
        return new RepProfile(
                rs.getLong("rank"),
                rs.getLong("user_id"),
                rs.getLong("reputation")
        );
    }

    public String fancyString(int maxRank) {
        var length = String.valueOf(maxRank).length();
        var rank = StringUtils.rightPad(String.valueOf(this.rank), length);
        return "`" + rank + "` **|** " + MentionUtil.user(userId) + " ➜ " + reputation;
    }
}
