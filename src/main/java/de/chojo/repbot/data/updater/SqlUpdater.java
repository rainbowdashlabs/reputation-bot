package de.chojo.repbot.data.updater;

import de.chojo.jdautil.container.Pair;
import de.chojo.repbot.commands.Info;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

public class SqlUpdater {
    private final int major;
    private final int patch;
    private static final Logger log = getLogger(SqlUpdater.class);
    private final DataSource source;
    private final String versionTable;
    private final String[] schemas;
    private final QueryReplacement[] replacements;

    private SqlUpdater(DataSource source, String versionTable, QueryReplacement[] replacements, String[] schemas, Pair<Integer, Integer> version) {
        this.source = source;
        this.versionTable = versionTable;
        this.replacements = replacements;
        this.schemas = schemas;
        this.major = version.first;
        this.patch = version.second;
    }

    public static SqlUpdaterBuilder builder(DataSource dataSource) throws IOException {
        var version = "";
        try (var in = Info.class.getClassLoader().getResourceAsStream("database/version")) {
            version = new String(in.readAllBytes()).trim();
        }

        var ver = version.split("\\.");
        return new SqlUpdaterBuilder(dataSource, Pair.of(Integer.valueOf(ver[0]), Integer.valueOf(ver[1])));
    }

    public void init() throws IOException, SQLException {
        forceDatabaseConsistency();

        var versionInfo = getVersionInfo();

        if (versionInfo.version() == major && versionInfo.patch() == patch) {
            log.info("Database is up to date. No update is required! Version {} Patch {}",
                    versionInfo.version(), versionInfo.patch());
            return;
        }

        var patches = getPatchesFrom(versionInfo.version(), versionInfo.patch());

        log.info("Database is {} versions behind.", patches.size());

        log.info("Performing update.");

        for (var patch : patches) {
            try {
                performUpdate(patch);
            } catch (SQLException e) {
                throw new RuntimeException("Database update failed!", e);
            }
        }
        log.info("Database update was successful!");
    }

    private void performUpdate(Patch patch) throws SQLException {
        log.info("Applying patch.");
        try (var conn = source.getConnection()) {
            try (var statement = conn.prepareStatement(adjust(patch.query()))) {
                statement.execute();
            }
        } catch (SQLException e) {
            log.error("Database update failed", e);
            throw e;
        }
        log.info("Patch applied.");
        updateVersion(patch.major(), patch.patch());
        if (patch.patch() != 0) {
            log.info("Deployed patch {}.{} to database.", patch.major(), patch.patch());
        } else {
            log.info("Migrated database to version {}.", patch.major());
        }
    }

    private void forceDatabaseConsistency() throws IOException, SQLException {
        // create schemas

        try (var conn = source.getConnection()) {
            for (var schema : schemas) {
                if (!schemaExists(schema)) {
                    try (var stmt = conn.prepareStatement(
                            "CREATE SCHEMA IF NOT EXISTS " + schema + ";")) {
                        stmt.execute();
                        log.info("Schema {} does not exists. Created.", schema);
                    }
                } else {
                    log.info("Schema {} does exists. Proceeding.", schema);
                }
            }

            var isSetup = false;
            if (!tableExists(versionTable)) {
                try (var stmt = conn.prepareStatement(
                        "create table if not exists " + versionTable + " (major integer, patch integer);")) {
                    stmt.execute();
                    log.info("Version table does not exist. Created.");
                }
            } else {
                isSetup = true;
                log.info("Version table exists. Database is ready for update check.");
            }

            if (!isSetup) {
                log.info("Setup database with version {}", major);
                var setup = getSetup();
                try (var stmt = conn.prepareStatement(adjust(setup))) {
                    stmt.execute();
                    log.info("Initial setup complete. Ready to patch.");
                }
                updateVersion(major, 0);
            }
        }
    }

    /**
     * Update the current database Version.
     *
     * @param version new version of database
     * @param patch   new patch of database
     */
    private void updateVersion(int version, int patch) {
        try (var conn = source.getConnection()) {
            try (var statement = conn
                    .prepareStatement("DELETE FROM " + versionTable)) {
                statement.execute();
            }
            try (var statement = conn
                    .prepareStatement("INSERT INTO " + versionTable + " VALUES (?, ?)")) {
                statement.setInt(1, version);
                statement.setInt(2, patch);
                statement.execute();
            } catch (SQLException e) {
                log.error("Failed change database version!", e);
                throw new Error("Failed change database version");
            }
            log.info("Set database to version {} patch {}!", version, patch);
        } catch (SQLException e) {
            log.error("Failed change database version!", e);
            throw new Error("Failed change database version");
        }
    }

    private VersionInfo getVersionInfo() {
        try (var conn = source.getConnection(); var statement = conn
                .prepareStatement("select major, patch from " + versionTable + " limit 1")) {
            var resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return new VersionInfo(resultSet.getInt("major"), resultSet.getInt("patch"));
            }
            throw new Error("Could not retrieve database version!");
        } catch (SQLException e) {
            log.error("Could not check if schema exists in database!", e);
        }
        throw new Error("Could not retrieve database version!");
    }

    private List<Patch> getPatchesFrom(int major, int patch) throws IOException {
        List<Patch> patches = new ArrayList<>();
        var currPatch = patch;
        for (var currMajor = major; currMajor <= this.major; currMajor++) {
            while (currPatch < this.patch) {
                currPatch++;
                if (patchExists(currMajor, currPatch)) {
                    patches.add(new Patch(major, currPatch, loadPatch(currMajor, currPatch)));
                } else if (currMajor != this.major) {
                    patches.add(new Patch(major + 1, 0, getMigrationFromVersion(major)));
                    currPatch = 0;
                    break;
                }
            }
        }
        return patches;
    }

    private boolean patchExists(int major, int patch) {
        return getClass().getClassLoader().getResource("database/" + major + "/patch_" + patch + ".sql") != null;
    }

    private String loadPatch(int major, int patch) throws IOException {
        return loadFromResource(major, "patch_" + patch + ".sql");
    }

    private String loadFromResource(Object... path) throws IOException {
        var p = Arrays.stream(path).map(Object::toString).collect(Collectors.joining("/"));
        try (var in = getClass().getClassLoader().getResourceAsStream("database/" + p)) {
            return new String(in.readAllBytes());
        }
    }

    private String getMigrationFromVersion(int major) throws IOException {
        return loadFromResource(major - 1, "migration.sql");
    }

    private String getSetup() throws IOException {
        return loadFromResource(major, "setup.sql");
    }

    private boolean tableExists(String table) {
        var split = table.split("\\.");
        if (split.length == 2) {
            return tableExists(split[0], split[1]);
        }
        return tableExists("public", split[0]);
    }

    private boolean tableExists(String schema, String table) {
        try (var conn = source.getConnection(); var stmt = conn.prepareStatement("""
                SELECT EXISTS (
                   SELECT FROM information_schema.tables\s
                   WHERE  table_schema = ?
                   AND    table_name   = ?
                   );
                """)) {
            stmt.setString(1, schema);
            stmt.setString(2, table);
            var rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getBoolean(1);
            }
        } catch (SQLException e) {
            log.error("Could not check if table {}.{} exists", schema, table, e);
        }
        return false;
    }

    private boolean schemaExists(String schema) {
        try (var conn = source.getConnection(); var stmt = conn.prepareStatement("""
                SELECT EXISTS (
                   SELECT FROM information_schema.tables\s
                   WHERE  table_schema = ?
                   );
                """)) {
            stmt.setString(1, schema);
            var rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getBoolean(1);
            }
        } catch (SQLException e) {
            log.error("Could not check if schema {} exists", schema, e);
        }
        return false;
    }

    private String adjust(String query) {
        var result = query;
        for (var replacement : replacements) {
            result = replacement.apply(result);
        }
        return result;
    }

    public static class SqlUpdaterBuilder {
        private final DataSource source;
        private Pair<Integer, Integer> version;
        private String versionTable = "version";
        private QueryReplacement[] replacements = new QueryReplacement[0];
        private String[] schemas = new String[0];

        public SqlUpdaterBuilder(DataSource dataSource, Pair<Integer, Integer> version) {
            this.source = dataSource;
            this.version = version;
        }

        public SqlUpdaterBuilder setVersionTable(String versionTable) {
            this.versionTable = versionTable;
            return this;
        }

        public SqlUpdaterBuilder setReplacements(QueryReplacement... replacements) {
            this.replacements = replacements;
            return this;
        }

        public SqlUpdaterBuilder setSchemas(String... schemas) {
            this.schemas = schemas;
            return this;
        }

        public void execute() throws SQLException, IOException {
            var sqlUpdater = new SqlUpdater(source, versionTable, replacements, schemas, version);
            sqlUpdater.init();
        }
    }
}
