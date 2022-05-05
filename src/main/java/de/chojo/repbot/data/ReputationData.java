package de.chojo.repbot.data;

import de.chojo.repbot.analyzer.ThankType;
import de.chojo.repbot.data.wrapper.GuildRanking;
import de.chojo.repbot.data.wrapper.GuildReputationStats;
import de.chojo.repbot.data.wrapper.ReputationLogAccess;
import de.chojo.repbot.dao.snapshots.ReputationLogEntry;
import de.chojo.repbot.dao.snapshots.ReputationUser;
import de.chojo.repbot.util.LogNotify;
import de.chojo.sqlutil.base.QueryFactoryHolder;
import de.chojo.sqlutil.exceptions.ExceptionTransformer;
import de.chojo.sqlutil.wrapper.QueryBuilderConfig;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

public class ReputationData extends QueryFactoryHolder {
    private static final Logger log = getLogger(ReputationData.class);

    public ReputationData(DataSource dataSource) {
        super(dataSource, QueryBuilderConfig.builder().withExceptionHandler(e ->
                        log.error(LogNotify.NOTIFY_ADMIN, ExceptionTransformer.prettyException("Query execution failed", e), e))
                .build());
    }













}
