package no.api.syzygy.etcd;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import no.api.syzygy.SyzygyConfig;
import no.api.syzygy.SyzygyException;
import no.api.syzygy.loaders.SyzygyFileConfig;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 *
 */
public class MapFileBackAndForthTest {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private EtcdConnector etcd;

    private String readFrom;


    @Before
    public void determineBaseDirectory() throws IOException {
        etcd = EtcdConnector.attach("http://127.0.0.1:4001/v2/", "/syzygy/junit/");
        Assume.assumeTrue(etcd.isAlive());

        readFrom = findTestResourcesDirectory();
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
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        Map<String, Object> structure = mapper.readValue(new File(readFrom+"structure.yml"), Map.class);
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
        assertTrue(etcd.store("key1", "a"));
        assertTrue(etcd.store("key2", "b"));
        assertTrue(etcd.store("key3", "c"));
        Set<String> keys = etcd.keys("");
        assertEquals(3, keys.size());
        assertTrue( "Expecting key set to contain deterministic name. It did not. Set: "+keys, keys.contains("key2"));

        // Testing start / stop
        etcd.stop();
        etcd.stop();
        assertFalse(etcd.isAlive());
        etcd.start();
        etcd.start();
        assertTrue(etcd.isAlive());

        assertTrue(etcd.remove("key1"));
        assertTrue(etcd.remove("key2"));
        assertTrue(etcd.remove("key3"));
    }

    @Test
    public void testSyzygyRead() {
        SyzygyConfig
                config = SyzygyLoader.loadConfigurationFile(new File(readFrom+"/syzygy.yaml")).configurationWithName(
                "structure");
        FromSyzygyToEtcd.storeConfigInto(config, etcd);
        assertEquals("top.level.config.value", etcd.valueBy(config.getName() + "/config.key"));

        SyzygyConfig syzygyEtcd = SyzygyEtcdConfig.connectAs(etcd, config.getName());
        assertEquals(config.getName(),  syzygyEtcd.getName());
        assertEquals("top.level.config.value", syzygyEtcd.lookup("config.key"));
        for ( String key: config.keys() ) {
            Object syzygy = config.lookup(key, Object.class);
            Object etcdy = syzygyEtcd.lookup(key, Object.class);
            assertNotNull(syzygy);
            assertNotNull(etcdy);
            if ( syzygy instanceof Map && etcdy instanceof Map ) {
                compareMaps( (Map)syzygy, (Map)etcdy);
            } else {
                assertEquals(syzygy.getClass().getName(), etcdy.getClass().getName());
            }
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
        assertEquals(syzgyKeys, syzygyEtcd.keys());

        assertTrue("Remove the complete structure", etcd.removeMap(config.getName()));
    }

    private void compareMaps(Map<String,Object> a, Map<String,Object> b) {
        String[] akeys = a.keySet().toArray(new String[0]);
        String[] bkeys = b.keySet().toArray(new String[0]);
        Arrays.sort(akeys);
        Arrays.sort(bkeys);
        assertArrayEquals("Expecting the following to be equal: \n" + Arrays.toString(akeys) +
                " \n... and\n" + Arrays.toString(bkeys), akeys, bkeys);
        for ( String key : akeys ) {
            Object aobj = a.get(key);
            Object bobj = a.get(key);
            if ( aobj instanceof Map && bobj instanceof Map ) {
                compareMaps((Map<String,Object>)aobj, (Map<String,Object>)bobj);
            } else {
                assertEquals(aobj, bobj);
            }
        }

    }

    @Test
    public void testToFile() throws IOException {
        SyzygyConfig config = SyzygyLoader.loadConfigurationFile(new File(readFrom + "/syzygy.yaml")).configurationWithName(
                "structure");
        assertEquals("structure", config.getName());
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

        ByteArrayOutputStream syzygyFileConfig = new ByteArrayOutputStream();
        Map syzygyMap = new HashMap( ((SyzygyFileConfig)config).getMap() );
        syzygyMap.remove(SyzygyConfig.SYZYGY_CFG_FILE);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(syzygyFileConfig, syzygyMap);

        FromSyzygyToEtcd.storeConfigInto(config, etcd);
        Map mapBasedOnEtcdData = etcd.getMap(config.getName());
        assertNotNull(mapBasedOnEtcdData);

        compareMaps( syzygyMap, mapBasedOnEtcdData );
        ByteArrayOutputStream etcdFileConfig = new ByteArrayOutputStream();
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(etcdFileConfig, mapBasedOnEtcdData);

        log.debug("Syzgy as read from file looks like this:\n"+syzygyFileConfig.toString());
        log.debug("\n\nWhereas syzgy as read from etcd looks like this:\n"+etcdFileConfig.toString());
        // These will usually not be equal. That is OK: assertEquals(syzygyFileConfig.toString(), etcdFileConfig.toString());

        assertTrue(etcd.removeDirectory(config.getName(), true));
    }

    @Test
    public void testThatListsAreUnsupported() {
        SyzygyConfig config = SyzygyLoader.loadConfigurationFile(new File(readFrom + "/readarray.yaml")).configurationWithName(
                "array");
        assertEquals("array", config.getName());
        try {
            FromSyzygyToEtcd.storeConfigInto(config, etcd);
            fail("Did not expect to be able to read lists (currently)");
        } catch (SyzygyException ignored ) {}
    }

    @Test
    public void testSyncingSingleFile() {
        String configName = "structure";
        assertEquals("I want to start with a blank slate for this test. -1 means it does not exist",
                -1, etcd.numberOfChildElements(configName));
        SyzygyConfig config = SyzygyLoader
                .loadConfigurationFile(new File(readFrom + "/syzygy.yaml"))
                .configurationWithName("structure");
        FromSyzygyToEtcd.storeConfigInto(config, etcd);
        // The operations above have been demonstrated to work correctly in a different test.

        // Now I want to make the configurations become different
        assertTrue(etcd.remove(configName + "/www.rb.no/key5"));
        assertTrue(etcd.store(configName+"/key_not_in_yaml", "value only in etcd"));
        assertTrue(etcd.store(configName+"/www.ba.no/key1", "overridden key 1"));

        Map map = new HashMap( ((SyzygyFileConfig) config).getMap() );
        map.remove(SyzygyConfig.SYZYGY_CFG_FILE);
        assertNotNull(map);
        assertTrue(!map.isEmpty());

        assertNotNull(etcd.valueBy(configName + "/key_not_in_yaml"));
        assertNull(etcd.valueBy(configName + "/www.rb.no/key5"));

        List<String> result = etcd.syncMapInto(configName + "/", map);
        for ( String str : result ) {
            log.trace(str);
        }

        assertNull("After syncing, the value which was not in the map is gone", etcd.valueBy(configName+"/key_not_in_yaml"));
        assertNotNull("After syncing, deleted value should be back", etcd.valueBy(configName + "/www.rb.no/key5"));


        Map mapBasedOnEtcdData = etcd.getMap(config.getName());

        compareMaps( map, mapBasedOnEtcdData );

        //  etcdctl rm --recursive /syzygy/junit/structure/
        assertTrue(etcd.removeDirectory(configName, true));
    }

    /**
     * Illegal to put a map where value exists
     */
    @Test
    public void testValueInSyzygyValueInMap() {
        String configName = "somemap";
        assertEquals("I want to start with a blank slate for this test. -1 means it does not exist",
                -1, etcd.numberOfChildElements(configName));
        assertTrue(etcd.store(configName + "/key", "etcd_value"));

        Map<String, Object> mapToCompare = new HashMap();
        Map<String, Object> mapAsValue = new HashMap();
        mapAsValue.put("keyInMap", "value");
        mapToCompare.put("key", mapAsValue);

        try {
            etcd.syncMapInto(configName+"/", mapToCompare);
            fail("Expecting exception if you put map where key previously exist");
        } catch ( SyzygyException ignored ) {}

        // Not true: assertEquals(mapAsValue.get("keyInMap"), etcd.valueBy(configName+"/key/keyInMap"));
        assertTrue(etcd.removeDirectory(configName, true));
    }

    /**
     * Illegal to put value where a map exists
     */
    @Test
    public void testMapInSyzygyValueInMap() {
        String configName = "somemap";
        assertEquals("I want to start with a blank slate for this test. -1 means it does not exist",
                -1, etcd.numberOfChildElements(configName));
        assertTrue(etcd.store(configName + "/sub/key", "etcd_value"));
        Map<String, Object> map = new HashMap();
        map.put("sub", "conflicting value for sub");

        try {
            etcd.syncMapInto(configName+"/", map);
            fail("Expecting exception if you put value where map previously");
        } catch ( SyzygyException ignored ) {}
        assertTrue(etcd.removeDirectory(configName, true));
    }

}
