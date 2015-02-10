package no.api.syzygy.etcd;

import no.api.syzygy.SyzygyConfig;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

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
        //etcd.removeDirectory("synced", true);
    }

    @Test
    public void demonstratefunctionality() {
        SynchronizationHelper.performSync(readFrom+"synctest/toplevel.yaml", SYZYGY_URL, "junit/synced");
        SyzygyConfig syzygy = SyzygyEtcdConfig.connectAs(etcd, "synced");
        assertEquals("value1", syzygy.lookup("key1"));

    }
}