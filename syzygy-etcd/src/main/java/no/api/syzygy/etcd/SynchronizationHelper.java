package no.api.syzygy.etcd;

import no.api.syzygy.loaders.SyzygyFileConfig;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 *
 */
public final class SynchronizationHelper {

    private SynchronizationHelper() {
        // Intentional
    }

    /**
     * To be used from some synchronizing application
     * @param pathToFile Path to the file to synchronize
     * @param URLToEtcd Where is etcd? Typically http://127.0.0.1:4001/v2/
     * @param mountName Target for copy. If you give a/b/c, the data will end up under /syzygy/a/b/c
     * @return Human readable result of operation
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
