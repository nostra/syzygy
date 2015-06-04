package no.api.syzygy.etcd;

import no.api.syzygy.SyzygyHelper;
import no.api.syzygy.loaders.SyzygyLoader;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
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
        Assume.assumeTrue(etcd.isAlive());
        readFrom = DirectoryHelper.findTestResourcesDirectory();
    }

    @After
    public void after() {
        if ( etcd != null ) {
            etcd.stop();
        }
    }

    @Test
    public void testValues() {
        assertTrue(etcd.store("key1", "etcd_a"));
        assertTrue(etcd.store("key3", "etcd_c"));

        SyzygyLoader syzygy = SyzygyLoader.loadConfigurationFile(new File(readFrom + "/etcdsyzygy/syzygy.yaml"));
        assertEquals("etcd_a", syzygy.lookup("key1"));
        assertEquals("fallback value 2", syzygy.lookup("key2"));
        assertEquals("etcd_c", syzygy.lookup("key3"));
        assertEquals("fallback value 4", syzygy.lookup("key4"));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SyzygyHelper.printConfigTo(syzygy.listAllProperties(), new OutputStreamWriter(baos));
        log.debug("\n" + baos.toString());

        SyzygyLoader onlyEtcd = SyzygyLoader.loadConfigurationFile(new File(readFrom + "/etcdsyzygy/onlyetcd.yaml"));
        assertEquals("etcd_a", onlyEtcd.lookup("key1"));
        assertNull(onlyEtcd.lookup("key2"));
        assertEquals("etcd_c", onlyEtcd.lookup("key3"));


        assertTrue(etcd.remove("key1"));
        assertTrue(etcd.remove("key3"));
    }

    @Test
    public void testFilePart() {
        SyzygyLoader syzygy = SyzygyLoader.loadConfigurationFile(new File(readFrom + "/etcdsyzygy/fileproblem.yaml"));
        assertEquals("fallback value 2", syzygy.lookup("key2"));
        assertEquals("fallback value 4", syzygy.lookup("key4"));
    }
}
