package no.api.syzygy.service.admin;

import com.codahale.metrics.health.HealthCheck;
import no.api.syzygy.etcd.EtcdConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 *
 */
public class CheckEtcd extends HealthCheck {
    private static final Logger log = LoggerFactory.getLogger(CheckEtcd.class);
    private EtcdConnector etcd;
    private boolean isUp = false;
    private boolean hasBeenUpOnce;

    public CheckEtcd(EtcdConnector etcd) {
        this.etcd = etcd;
        this.hasBeenUpOnce = etcd.isAlive();
    }

    @Override
    protected Result check() {
        if ( ! hasBeenUpOnce ) {
            // Possible that we started up down
            etcd.start();
        }
        try {
            Set<String> keys = etcd.keys("app/syzygy/stop");
            if ( !keys.isEmpty() ) {
                isUp = logChange(false);
                return Result.unhealthy("If insert the key /syzygy/app/syzygy/stop, this app will stop - and it " +
                                                "has. Remove key to start it again");
            }
        } catch ( Exception e ) { // Accepting wide net catch
            isUp = logChange(false);
            return Result.unhealthy(e);
        }
        isUp = logChange(true);
        hasBeenUpOnce = true;
        return Result.healthy();
    }

    private boolean logChange(boolean upOrNot) {
        if ( isUp != upOrNot ) {
            log.info("Going from status up="+isUp+" to status up="+upOrNot);
        }
        return upOrNot;
    }
}
