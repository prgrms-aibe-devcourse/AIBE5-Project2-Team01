package com.example.meetball.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnProperty(name = "app.postgres-schema-guard.enabled", havingValue = "true", matchIfMissing = true)
public class PostgreSqlSchemaGuard implements ApplicationRunner {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!isPostgreSql()) {
            return;
        }

        widenColumn("users", "tech_stack", "varchar(1000)");
        widenColumn("project", "tech_stack_csv", "varchar(1000)");
        widenColumn("project", "position", "varchar(1000)");
        widenColumn("application", "status", "varchar(30)");
        dropApplicationStatusCheckConstraints();
    }

    private boolean isPostgreSql() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return connection.getMetaData()
                    .getDatabaseProductName()
                    .toLowerCase(Locale.ROOT)
                    .contains("postgresql");
        }
    }

    private void widenColumn(String tableName, String columnName, String columnType) {
        if (!columnExists(tableName, columnName)) {
            return;
        }
        jdbcTemplate.execute("alter table " + quoteIdentifier(tableName)
                + " alter column " + quoteIdentifier(columnName)
                + " type " + columnType
                + " using " + quoteIdentifier(columnName) + "::text");
    }

    private void dropApplicationStatusCheckConstraints() {
        if (!columnExists("application", "status")) {
            return;
        }

        List<String> constraintNames = jdbcTemplate.queryForList("""
                select con.conname
                from pg_constraint con
                join pg_class rel on rel.oid = con.conrelid
                join pg_namespace nsp on nsp.oid = rel.relnamespace
                where nsp.nspname = current_schema()
                  and rel.relname = ?
                  and con.contype = 'c'
                  and lower(pg_get_constraintdef(con.oid)) like '%status%'
                """, String.class, "application");

        for (String constraintName : constraintNames) {
            jdbcTemplate.execute("alter table " + quoteIdentifier("application")
                    + " drop constraint if exists " + quoteIdentifier(constraintName));
        }
    }

    private boolean columnExists(String tableName, String columnName) {
        Boolean exists = jdbcTemplate.queryForObject("""
                select exists (
                    select 1
                    from information_schema.columns
                    where table_schema = current_schema()
                      and table_name = ?
                      and column_name = ?
                )
                """, Boolean.class, tableName, columnName);
        return Boolean.TRUE.equals(exists);
    }

    private String quoteIdentifier(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
