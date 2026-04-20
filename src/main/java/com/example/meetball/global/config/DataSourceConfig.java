package com.example.meetball.global.config;

import com.zaxxer.hikari.HikariDataSource;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

@Configuration
public class DataSourceConfig {

    private static final String POSTGRESQL_PREFIX = "postgresql://";
    private static final String POSTGRES_PREFIX = "postgres://";
    private static final int POSTGRESQL_DEFAULT_PORT = 5432;

    @Bean
    public DataSource dataSource(Environment environment) {
        DatabaseConnection connection = resolveConnection(environment);

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(connection.jdbcUrl());
        dataSource.setUsername(connection.username());
        dataSource.setPassword(connection.password());
        dataSource.setDriverClassName("org.postgresql.Driver");
        return dataSource;
    }

    private DatabaseConnection resolveConnection(Environment environment) {
        String springDatasourceUrl = environment.getProperty("SPRING_DATASOURCE_URL");
        if (StringUtils.hasText(springDatasourceUrl)) {
            return fromUrl(
                    springDatasourceUrl,
                    environment.getProperty("SPRING_DATASOURCE_USERNAME"),
                    environment.getProperty("SPRING_DATASOURCE_PASSWORD")
            );
        }

        String databaseUrl = environment.getProperty("DATABASE_URL");
        if (StringUtils.hasText(databaseUrl)) {
            return fromUrl(databaseUrl, null, null);
        }

        String host = environment.getProperty("SPRING_DATASOURCE_HOST", "localhost");
        String port = environment.getProperty("SPRING_DATASOURCE_PORT", "5432");
        String database = environment.getProperty("SPRING_DATASOURCE_DB", "meetball");
        String username = environment.getProperty("SPRING_DATASOURCE_USERNAME", "postgres");
        String password = environment.getProperty("SPRING_DATASOURCE_PASSWORD", "");

        return new DatabaseConnection(
                "jdbc:postgresql://" + host + ":" + port + "/" + database,
                username,
                password
        );
    }

    private DatabaseConnection fromUrl(String rawUrl, String fallbackUsername, String fallbackPassword) {
        if (rawUrl.startsWith("jdbc:postgresql://")) {
            return new DatabaseConnection(rawUrl, valueOrDefault(fallbackUsername, "postgres"), valueOrDefault(fallbackPassword, ""));
        }

        String normalizedUrl = normalizePostgresUrl(rawUrl);
        URI uri = URI.create(normalizedUrl);
        String[] userInfo = parseUserInfo(uri.getUserInfo());
        String username = valueOrDefault(fallbackUsername, userInfo[0]);
        String password = valueOrDefault(fallbackPassword, userInfo[1]);
        int port = uri.getPort() > 0 ? uri.getPort() : POSTGRESQL_DEFAULT_PORT;
        String query = StringUtils.hasText(uri.getQuery()) ? "?" + uri.getQuery() : "";
        String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + port + uri.getPath() + query;

        return new DatabaseConnection(jdbcUrl, username, password);
    }

    private String normalizePostgresUrl(String rawUrl) {
        if (rawUrl.startsWith(POSTGRESQL_PREFIX)) {
            return rawUrl;
        }
        if (rawUrl.startsWith(POSTGRES_PREFIX)) {
            return POSTGRESQL_PREFIX + rawUrl.substring(POSTGRES_PREFIX.length());
        }
        throw new IllegalArgumentException("Unsupported PostgreSQL URL format: " + rawUrl);
    }

    private String[] parseUserInfo(String userInfo) {
        if (!StringUtils.hasText(userInfo)) {
            return new String[]{"postgres", ""};
        }

        String[] parts = userInfo.split(":", 2);
        String username = decode(parts[0]);
        String password = parts.length > 1 ? decode(parts[1]) : "";
        return new String[]{username, password};
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private String valueOrDefault(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    private record DatabaseConnection(String jdbcUrl, String username, String password) {
    }
}
