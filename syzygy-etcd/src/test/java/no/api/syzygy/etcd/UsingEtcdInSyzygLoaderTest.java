package no.api.syzygy.etcd;

import no.api.syzygy.loaders.SyzygyLoader;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class UsingEtcdInSyzygLoaderTest {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private EtcdConnector etcd;

    private String readFrom;

    @Before
    public void determineBaseDirectory() throws IOException {
        etcd = EtcdConnector.attach("http://127.0.0.1:4001/v2/", "/syzygy/junit/etcdmap/");
        Assume.assumeNotNull(etcd);

        assertTrue(etcd.store("key1", "etcd_a"));
        assertTrue(etcd.store("key3", "etcd_c"));
        readFrom = MapFileBackAndForthTest.findTestResourcesDirectory();
    }

    @After
    public void after() {
        if ( etcd != null ) {
            etcd.stop();
        }
    }

    /**
     * Work in progress - seems like only etcd version of readers is used.
     */
    @Test
    @Ignore
    public void testValues() {
        SyzygyLoader loader = SyzygyLoader.loadConfigurationFile(new File(readFrom + "/etcdsyzygy/syzygy.yaml"));
        assertEquals("etcd_a", loader.lookup("key1"));
        assertEquals("fallback value 2", loader.lookup("key2"));
        assertEquals("etcd_c", loader.lookup("key3"));
        assertEquals("fallback value 4", loader.lookup("key4"));
    }
}
