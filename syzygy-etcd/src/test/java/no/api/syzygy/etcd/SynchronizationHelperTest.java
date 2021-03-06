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

import no.api.syzygy.SyzygyConfig;
import no.api.syzygy.loaders.SyzygyLoader;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SynchronizationHelperTest {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private EtcdConnector etcd;

    private String readFrom;


    @Before
    public void determineBaseDirectory() throws IOException {
        etcd = EtcdConnector.attach(DirectoryHelper.ETCD_URL, DirectoryHelper.SYZYGY_JUNIT_PATH);
        Assume.assumeTrue(etcd.isAlive());

        readFrom = DirectoryHelper.findTestResourcesDirectory();
    }

    @After
    public void cleanup() {
        // Cleanup
        if ( etcd.isAlive() ) {
            etcd.removeDirectory("synced", true);
        }

    }


    /**
     * java -Dlogback.configurationFile=syzygy-service/src/test/resources/logback.xml -jar
     *      syzygy-service/target/syzygy-service-0.0.2-SNAPSHOT.jar
     *      syzygy-etcd/src/test/resources/synctest/toplevel.yaml
     *      http://127.0.0.1:4001/v2/ junit/synced
     *
     * java -Dlogback.configurationFile=syzygy-service/src/test/resources/logback.xml
     *       -jar syzygy-service/target/syzygy-service-0.0.2-SNAPSHOT.jar
     *       syzygy-etcd/src/test/resources/synctest/sublevel/sublevel.yaml
     *       http://127.0.0.1:4001/v2/ junit/synced/sublevel
     *
     * This demonstrates that when you have overlapping keys, unexpected things may happen
     */
    @Test
    public void demonstratefunctionality() {
        SynchronizationHelper.performSync(readFrom+"synctest/toplevel.yaml", DirectoryHelper.ETCD_URL, "junit/synced");
        SyzygyConfig toplevel = SyzygyEtcdConfig.connectAs(etcd, "synced");
        assertEquals("value1", toplevel.lookup("key1"));

        SynchronizationHelper.performSync(readFrom+"synctest/sublevel/sublevel.yaml", DirectoryHelper.ETCD_URL, "junit/synced/sublevel");
        SyzygyConfig sublevel = SyzygyEtcdConfig.connectAs(etcd, "synced/sublevel");
        assertEquals("sublevel value 1", sublevel.lookup("key1"));

        assertEquals("Value from top level still exists", "value1", toplevel.lookup("key1"));

        // Syncing top level again
        SynchronizationHelper.performSync(readFrom+"synctest/toplevel.yaml", DirectoryHelper.ETCD_URL, "junit/synced");
        assertEquals("Value from top level still exists", "value1", toplevel.lookup("key1"));
        assertNull("Value from sub level is now lost", sublevel.lookup("key1"));

        // Syncing sub level to get it back.
        SynchronizationHelper.performSync(readFrom+"synctest/sublevel/sublevel.yaml", DirectoryHelper.ETCD_URL, "junit/synced/sublevel");

        assertEquals("Looking into the sublevel by nesting classes", sublevel.lookup("key1"), toplevel.lookup("sublevel", Map.class ).get("key1"));

        SyzygyLoader syzygy = SyzygyLoader.loadConfigurationFile(new File(readFrom + "/synctest/syzygy.yaml"));

        assertEquals("sublevel value 1", syzygy.lookup("key1"));
    }
}