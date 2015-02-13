package no.api.syzygy.service;

import com.codahale.metrics.health.HealthCheck;

/**
 *
 */
public class AlwaysHealthy extends HealthCheck {

    @Override
    protected Result check() {
        //return Result.unhealthy(e);
        return Result.healthy();
    }
}
