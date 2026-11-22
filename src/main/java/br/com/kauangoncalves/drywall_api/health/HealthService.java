package br.com.kauangoncalves.drywall_api.health;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class HealthService {

    private final DataSource dataSource;
    private final String applicationName;

    public HealthService(DataSource dataSource,
                         @Value("${spring.application.name}") String applicationName) {
        this.dataSource = dataSource;
        this.applicationName = applicationName;
    }

    public HealthResponse check() {
        boolean databaseUp = isDatabaseUp();
        return new HealthResponse(
                databaseUp ? "UP" : "DEGRADED",
                applicationName,
                databaseUp ? "UP" : "DOWN",
                Instant.now()
        );
    }

    private boolean isDatabaseUp() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }
}
