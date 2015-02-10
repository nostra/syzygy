package no.api.syzygy.etcd;

import no.api.syzygy.SyzygyConfig;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SynchronizationHelperTest {

    public static final String SYZYGY_URL = "http://127.0.0.1:4001/v2/";

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private EtcdConnector etcd;

    private String readFrom;


    @Before
    public void determineBaseDirectory() throws IOException {
        etcd = EtcdConnector.attach(SYZYGY_URL, "/syzygy/junit/");
        Assume.assumeNotNull(etcd);

        readFrom = MapFileBackAndForthTest.findTestResourcesDirectory();
    }

    @After
    public void tearDown() {
        etcd.removeDirectory("synced", true);
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
        SynchronizationHelper.performSync(readFrom+"synctest/toplevel.yaml", SYZYGY_URL, "junit/synced");
        SyzygyConfig toplevel = SyzygyEtcdConfig.connectAs(etcd, "synced");
        assertEquals("value1", toplevel.lookup("key1"));

        SynchronizationHelper.performSync(readFrom+"synctest/sublevel/sublevel.yaml", SYZYGY_URL, "junit/synced/sublevel");
        SyzygyConfig sublevel = SyzygyEtcdConfig.connectAs(etcd, "synced/sublevel");
        assertEquals("sublevel value 1", sublevel.lookup("key1"));

        assertEquals("Value from top level still exists", "value1", toplevel.lookup("key1"));

        // Syncing top level again
        SynchronizationHelper.performSync(readFrom+"synctest/toplevel.yaml", SYZYGY_URL, "junit/synced");
        assertEquals("Value from top level still exists", "value1", toplevel.lookup("key1"));
        assertNull("Value from sub level is now lost", sublevel.lookup("key1"));

        // Syncing sub level to get it back.
        SynchronizationHelper.performSync(readFrom+"synctest/sublevel/sublevel.yaml", SYZYGY_URL, "junit/synced/sublevel");

        assertEquals("Looking into the sublevel by nesting classes", sublevel.lookup("key1"), toplevel.lookup("sublevel", Map.class ).get("key1"));
    }
}