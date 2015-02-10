package no.api.syzygy.etcd;

import no.api.syzygy.loaders.SyzygyFileConfig;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 *
 */
public final class SynchronizationHelper {
    /**
     * To be used from some synchronizing application
     */
    public static List<String> performSync(String pathToFile, String URLToEtcd, String mountName) {
        SyzygyFileConfig sfc = new SyzygyFileConfig(mountName).load(new File(pathToFile));
        Map<String, Object> syzygyMap = sfc.getMap();
        EtcdConnector etcd = null;
        try {
            etcd = EtcdConnector.attach(URLToEtcd, "/syzygy/");
            return etcd.syncMapInto(mountName + "/", syzygyMap);
        } finally {
            if (etcd != null) {
                etcd.stop();
            }
        }
    }

}
