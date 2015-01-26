package no.api.syzygy.etcd;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import no.api.syzygy.SyzygyConfig;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 *
 */
public class UpdateEtcdWithFileContentsTest {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private EtcdConnector etcd;

    private String readFrom;


    @Before
    public void determineBaseDirectory() throws IOException {
        etcd = EtcdConnector.attach("http://127.0.0.1:4001/v2/", "/syzygy/junit/");
        Assume.assumeNotNull(etcd);

        readFrom = MapFileBackAndForthTest.findTestResourcesDirectory();
    }

    @After
    public void after() {
        if ( etcd != null ) {
            etcd.stop();
        }
    }

    @Test
    public void testThatChangesInEtcdGetsReflected() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        Map<String, Object> map = mapper.readValue(new File(readFrom+"structure.yml"), Map.class);
        assertTrue(etcd.store("structure", map));
        SyzygyConfig syzygyConfig = SyzygyEtcdConfig.connectAs(etcd, "structure");
        assertEquals("config overridden by rb", syzygyConfig.lookup("www.rb.no/config.key"));

        assertEquals("value1 for rb", syzygyConfig.lookup("www.rb.no/key1"));

        // Add a value
        assertTrue(etcd.store("structure/www.rb.no/key_which_is_new", "new key value"));
        // Remove a (different) value
        assertTrue(etcd.remove("structure/www.rb.no/key5"));
        // Change a value
        assertTrue(etcd.store("structure/www.rb.no/key1", "key1 update"));

        assertEquals("new key value", syzygyConfig.lookup("www.rb.no/key_which_is_new"));
        assertNull(syzygyConfig.lookup("www.rb.no/key5"));
        assertEquals("key1 update", syzygyConfig.lookup("www.rb.no/key1"));

        assertTrue(etcd.removeDirectory("structure", true));
    }

    @Test
    public void demonstrateHowStorageOfMapWillRemoveOtherData() {
        Map<String, Object> map = new HashMap();
        map.put("somekey", "somevalue");
        etcd.store("structure", map );
        SyzygyConfig syzygyConfig = SyzygyEtcdConfig.connectAs(etcd, "structure");
        map = new HashMap();
        map.put("different", "value");
        etcd.store("structure", map );
        assertEquals("This documents that storage of 2 maps on top of each other will yield both values",
                     "somevalue", syzygyConfig.lookup("somekey"));
        assertEquals("value", syzygyConfig.lookup("different"));
        assertTrue(etcd.removeMap("structure"));
    }
}
