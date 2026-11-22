package br.com.kauangoncalves.drywall_api.health;

import java.time.Instant;

public record HealthResponse(
        String status,
        String application,
        String database,
        Instant timestamp
) {}
