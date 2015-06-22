/*
 * Copyright 2015 Amedia Utvikling
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
