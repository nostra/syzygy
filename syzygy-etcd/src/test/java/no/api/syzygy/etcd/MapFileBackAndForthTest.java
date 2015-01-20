package no.api.syzygy.etcd;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import no.api.syzygy.SyzygyConfig;
import no.api.syzygy.loaders.SyzygyLoader;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class MapFileBackAndForthTest {

    private EtcdConnector etcd;
    private Map<String, Object> structure;
    private String readFrom;

    @Before
    public void determineBaseDirectory() throws IOException {
        etcd = EtcdConnector.attach("http://127.0.0.1:4001/v2/", "/syzygy/junit/");
        Assume.assumeNotNull(etcd);

        readFrom = findTestResourcesDirectory();
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        structure = mapper.readValue(new File(readFrom+"structure.yml"), Map.class);
    }

    protected static String findTestResourcesDirectory() {
        File cur = new File(".");
        StringBuffer readFrom = new StringBuffer();
        if ( ! cur.getAbsolutePath().contains("syzygy-etcd")) {
            // IntelliJ
            readFrom.append("syzygy-etcd/");
        }
        readFrom.append("src/test/resources/");
        return readFrom.toString();
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

    @Test
    public void testCopyBackAndForth() {
        assertTrue(etcd.store("junit", "test"));
        assertEquals("test", etcd.valueBy("junit"));
        assertTrue(etcd.remove("junit"));

        Map<String, Object> map = new HashMap<>();
        map.put("a", "value a");
        map.put("b", "value b");
        assertTrue(etcd.store("somemap", map));
        assertEquals(2, etcd.numberOfChildElements("somemap"));

        Map<String, String> read = (Map<String, String>) etcd.valueBy("somemap");
        assertNotNull(read);
        assertEquals("Expected map to contain value. Read map: "+read, map.get("a"), read.get("a"));

        assertTrue(etcd.remove("somemap/a"));
        assertFalse("directory has to be empty before value can be removed", etcd.remove("somemap"));
        assertTrue(etcd.remove("somemap/b"));
        assertTrue(etcd.remove("somemap"));
    }

    @Test
    public void testNestedMaps() {
        Map<String, Object> nested = new HashMap<>();
        nested.put("aa", "nested a");
        Map<String, Object> map = new HashMap<>();
        map.put("a", "value a");
        map.put("nested", nested);
        assertTrue(etcd.store("somemap", map));

        map = (Map<String, Object>) etcd.valueBy("somemap");
        assertEquals("value a", map.get("a"));
        assertEquals("nested a", ((Map)map.get("nested")).get("aa"));

        assertTrue(etcd.removeMap("somemap"));
    }

    @Test
    public void testLoadKeys() {
        etcd.store("key1", "a");
        etcd.store("key2", "b");
        etcd.store("key3", "c");
        Set<String> keys = etcd.keys("");
    }

    @Test
    public void testSyzygyRead() {
        SyzygyConfig
                config = SyzygyLoader.loadConfigurationFile(new File(readFrom+"/syzygy.yaml")).configurationWithName(
                "structure");
        FromSyzygyToEtcd.mapSyzygyInto(config, etcd);
        assertEquals("top.level.config.value",  etcd.valueBy( config.getName()+"/config.key"));

        SyzygyConfig syzetcd = SyzygyEtcdConfig.connectAs( etcd, config.getName());
        assertEquals("top.level.config.value",  syzetcd.lookup("config.key"));
        for ( String key: config.keys() ) {
            Object syzygy = config.lookup(key, Object.class);
            Object etcdy = config.lookup(key, Object.class);
            assertNotNull(syzygy);
            assertNotNull(etcdy);
            assertEquals(syzygy.getClass().getName(), etcdy.getClass().getName());
        }
        Map syzygymap = config.lookup("www.rb.no", Map.class);
        Map etcdmap = config.lookup("www.rb.no", Map.class);
        assertEquals(syzygymap.size(), etcdmap.size());
        assertEquals("innervalue",
                ((Map) etcdmap.get("innermap")).get("innerkey"));
        assertEquals(((Map) syzygymap.get("innermap")).get("innerkey"),
                ((Map) etcdmap.get("innermap")).get("innerkey"));

        Set<String> syzgyKeys = config.keys();
        syzgyKeys.remove(SyzygyConfig.SYZYGY_CFG_FILE);
        assertEquals(syzgyKeys, syzetcd.keys());

        assertTrue("Remove the complete structure", etcd.removeMap(config.getName()));
    }
}
