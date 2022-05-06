package de.chojo.repbot.data;

import de.chojo.repbot.data.wrapper.RemovalTask;
import de.chojo.repbot.util.LogNotify;
import de.chojo.sqlutil.base.QueryFactoryHolder;
import de.chojo.sqlutil.exceptions.ExceptionTransformer;
import de.chojo.sqlutil.wrapper.QueryBuilderConfig;
import de.chojo.sqlutil.wrapper.stage.ResultStage;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

public class GdprData extends QueryFactoryHolder {
    private static final Logger log = getLogger(GdprData.class);

    /**
     * Create a new QueryFactoryholder
     *
     * @param dataSource datasource
     */
    public GdprData(DataSource dataSource) {
        super(dataSource, QueryBuilderConfig.builder().withExceptionHandler(e ->
                        log.error(LogNotify.NOTIFY_ADMIN, ExceptionTransformer.prettyException("Query execution failed", e), e))
                .build());
    }

}
