package integration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;
import java.util.UUID;

/** Imports the delivery scripts into an isolated schema with real constraints. */
public final class DatabaseScriptIntegrationTest {
    public static void main(String[] args) throws Exception {
        Properties config = new Properties();
        try (var reader = Files.newBufferedReader(Path.of("config.txt"))) {
            config.load(reader);
        }
        String schema = "erp_lh_verify_" + UUID.randomUUID().toString().replace("-", "");
        if (!schema.matches("erp_lh_verify_[a-f0-9]{32}")) {
            throw new IllegalStateException("Unsafe verification schema name");
        }
        try (Connection connection = DriverManager.getConnection(
                config.getProperty("url"), config.getProperty("user"), config.getProperty("password"))) {
            try (var query = connection.prepareStatement(
                    "SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name=?")) {
                query.setString(1, schema);
                try (ResultSet result = query.executeQuery()) {
                    result.next();
                    if (result.getInt(1) != 0) throw new IllegalStateException("Schema already exists");
                }
            }
            boolean created = false;
            try {
                try (Statement create = connection.createStatement()) {
                    create.execute("CREATE DATABASE " + schema + " CHARACTER SET utf8mb4");
                    created = true;
                }
                connection.setCatalog(schema);
                try (Statement statement = connection.createStatement()) {
                    int checks = 0;
                    for (String file : new String[]{"docs/database.sql", "docs/verify_database.sql"}) {
                        String source = Files.readString(Path.of(file)).replace("erp_little_horizons", schema);
                        // Ignore line comments and split delimiters outside SQL string literals.
                        source = source.replaceAll("(?m)^\\s*--.*$", "");
                        for (String sql : splitStatements(source)) {
                            if (sql.isBlank()) continue;
                            if (statement.execute(sql.trim())) {
                                try (ResultSet result = statement.getResultSet()) {
                                    var metadata = result.getMetaData();
                                    while (result.next()) {
                                        for (int column = 1; column <= metadata.getColumnCount(); column++) {
                                            String label = metadata.getColumnLabel(column);
                                            if ("cumple".equals(label) && !result.getBoolean(column))
                                                throw new AssertionError("Insufficient seed records");
                                            if ("inconsistencias".equals(label) && result.getInt(column) != 0)
                                                throw new AssertionError(result.getString(1) + ": " + result.getInt(column));
                                        }
                                        checks++;
                                    }
                                }
                            }
                        }
                    }
                    try (ResultSet result = statement.executeQuery(
                            "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='" + schema + "'")) {
                        result.next();
                        if (result.getInt(1) != 26) throw new AssertionError("Expected 26 tables");
                    }
                    System.out.println("PASS: 26 tables imported with real constraints; " + checks + " verification rows passed.");
                }
            } finally {
                if (created) {
                    try (Statement cleanup = connection.createStatement()) {
                        cleanup.execute("DROP DATABASE " + schema);
                    }
                    System.out.println("Isolated verification schema removed.");
                }
            }
        }
    }
    private static java.util.List<String> splitStatements(String source) {
        var statements = new java.util.ArrayList<String>();
        var buffer = new StringBuilder();
        boolean quoted = false;
        for (int index = 0; index < source.length(); index++) {
            char current = source.charAt(index);
            if (current == 39) {
                if (quoted && index + 1 < source.length() && source.charAt(index + 1) == 39) {
                    buffer.append(current).append(source.charAt(++index));
                    continue;
                }
                quoted = !quoted;
            }
            if (current == ';' && !quoted) {
                statements.add(buffer.toString());
                buffer.setLength(0);
            } else {
                buffer.append(current);
            }
        }
        if (quoted) throw new IllegalArgumentException("Unclosed SQL string");
        if (!buffer.toString().isBlank()) statements.add(buffer.toString());
        return statements;
    }

}
