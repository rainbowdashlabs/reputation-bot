package de.chojo.repbot.data;

import de.chojo.repbot.util.LogNotify;
import de.chojo.sqlutil.base.QueryFactoryHolder;
import de.chojo.sqlutil.exceptions.ExceptionTransformer;
import de.chojo.sqlutil.wrapper.QueryBuilderConfig;
import org.slf4j.Logger;

import javax.sql.DataSource;

import static org.slf4j.LoggerFactory.getLogger;

public class ReputationData extends QueryFactoryHolder {
    private static final Logger log = getLogger(ReputationData.class);

    public ReputationData(DataSource dataSource) {
        super(dataSource, QueryBuilderConfig.builder().withExceptionHandler(e ->
                        log.error(LogNotify.NOTIFY_ADMIN, ExceptionTransformer.prettyException("Query execution failed", e), e))
                .build());
    }













}
