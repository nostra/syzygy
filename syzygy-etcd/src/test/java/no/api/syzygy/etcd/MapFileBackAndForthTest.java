package no.api.syzygy.etcd;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class MapFileBackAndForthTest {

    private StringBuffer readFrom;
    private EtcdConnector etcd;
    private Map<String, Object> structure;

    @Before
    public void determineBaseDirectory() throws IOException {
        etcd = EtcdConnector.attach("http://127.0.0.1:4001/v2/");
        Assume.assumeNotNull(etcd);

        File cur = new File(".");
        readFrom = new StringBuffer();
        if ( ! cur.getAbsolutePath().contains("syzygy-etcd")) {
            // IntelliJ
            readFrom.append("syzygy-etcd/");
        }
        readFrom.append("src/test/resources/");
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        structure = mapper.readValue(new File(readFrom.toString()+"structure.yml"), Map.class);
    }

    @After
    public void after() {
        if ( etcd != null ) {
            etcd.stop();
        }
    }


    @Test
    public void justMakingSureToBeAbleToReadFile() throws IOException {
        assertEquals("top.level.config.value", structure.get("config.key"));
        Map<String, Object> map = (Map<String, Object>) structure.get("www.rb.no");
        assertEquals("value1 for rb", map.get("key1"));
        Map<String, String> inner = (Map<String, String>) map.get("innermap");
        assertEquals("innervalue", inner.get("innerkey"));
    }

    /**
     * Work in progress
     */
    @Test
    @Ignore
    public void testCopyBackAndForth() {
        assertTrue(etcd.store("junit", "test"));
        assertEquals("test", etcd.valueBy("junit"));
        assertTrue(etcd.remove("junit"));

        Map<String, Object> map = new HashMap<>();
        map.put("a", "value a");
        map.put("b", "value b");
        assertTrue(etcd.store("somemap", map));

        Map<String, String> read = (Map<String, String>) etcd.valueBy("somemap");
        assertNotNull(read);
        assertEquals("Expected map to contain value. Read map: "+read, map.get("a"), read.get("a"));

        assertTrue(etcd.remove("/somemap/a"));
        assertFalse("directory has to be empty before value can be removed", etcd.remove("somemap"));
        assertTrue(etcd.remove("/somemap/b"));
        assertTrue(etcd.remove("somemap"));
    }

}
