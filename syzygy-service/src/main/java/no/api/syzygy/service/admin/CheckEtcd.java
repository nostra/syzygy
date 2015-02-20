package no.api.syzygy.service.admin;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheck;
import no.api.syzygy.etcd.EtcdConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class CheckEtcd extends HealthCheck {
    private static final Logger log = LoggerFactory.getLogger(CheckEtcd.class);
    private EtcdConnector etcd;

    private MetricRegistry metrics;

    private boolean isUp = false;
    private boolean hasBeenUpOnce;

    public CheckEtcd(EtcdConnector etcd, MetricRegistry metrics) {
        this.etcd = etcd;
        this.metrics = metrics;
        this.hasBeenUpOnce = etcd.isAlive();
    }

    @Override
    protected Result check() {
        metrics.meter("etcd.health.ping");
        if ( ! hasBeenUpOnce ) {
            // Possible that we started up down
            etcd.start();
        }
        try {
            String stop = (String) etcd.valueBy("app/syzygy/stop");
            if ( stop != null ) {
                isUp = logChange(false);
                metrics.meter("etcd.health.sick");
                return Result.unhealthy("If insert the key /syzygy/app/syzygy/stop, this app will stop - and it " +
                                                "has. Remove key to start it again");
            }
        } catch ( Exception e ) { // Accepting wide net catch
            isUp = logChange(false);
            metrics.meter("etcd.health.exception");
            return Result.unhealthy(e);
        }
        isUp = logChange(true);
        hasBeenUpOnce = true;
        metrics.meter("etcd.health.ok");
        return Result.healthy();
    }

    private boolean logChange(boolean upOrNot) {
        if ( isUp != upOrNot ) {
            log.info("Going from status up=" + isUp + " to status up=" + upOrNot);
        }
        return upOrNot;
    }
}
