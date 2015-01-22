package no.api.syzygy.loaders;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import no.api.syzygy.HieradirectoryHelper;
import no.api.syzygy.SyzygyConfig;
import no.api.syzygy.SyzygyException;
import no.api.syzygy.SyzygyHelper;
import no.api.syzygy.SyzygyPayload;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


public class SyzygyLoaderTest {
    private static final Logger log = LoggerFactory.getLogger(SyzygyLoaderTest.class);

    private SyzygyLoader loader;
    private String readFrom;

    @Before
    public void setUp() {
        readFrom = HieradirectoryHelper.findTestDirectoryReference("yamlonly");
        loader = SyzygyLoader.loadConfigurationFile(new File(readFrom+"/syzygy.yaml"));
        loader.validate();
    }


    /**
     * Just checking default functionality
     */
    @Test
    public void testJackson() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        Map<String, String> map = new HashMap<>();
        map.put("key", "value");
        assertEquals("---\nkey: \"value\"\n", mapper.writeValueAsString(map));

        Map result = mapper.readValue("k: 'f'", Map.class);
        assertEquals("f", result.get("k"));

        result = mapper.readValue(":yaml:\n" +
                "        :datadir: hieradata", Map.class);
        assertNotNull(result.get(":yaml"));
        assertEquals("hieradata",((Map)result.get(":yaml")).get(":datadir"));
    }

    @Test
    public void singleValues() {
        assertEquals("value from common", loader.lookup("key2"));
        assertEquals("from specific", loader.lookup("key1"));
        assertNotNull("Expecting element to exist", loader.lookup("array2", Map.class));
        assertEquals("overridden array, one key", loader.lookup("array2", Map.class).get( "key2" ));

        SyzygyPayload<String> payload = loader.lookupFor("key1", String.class);
        assertEquals("specific", payload.getName());
        double timesChecked = payload.getHits();
        assertEquals(timesChecked+1, loader.lookupFor("key1", String.class).getHits(), 0);
    }


    @Test
    public void arrayValues() {
        List facit = new ArrayList();
        Map map = new LinkedHashMap();
        map.put("key1", "array key1");
        facit.add(map);
        map = new LinkedHashMap();
        map.put("key2", "array key2");
        facit.add(map);

        assertEquals("A list of map items becomes a list with map size 1 for each entry",
                "" + facit, "" + loader.lookup("array1", List.class));
    }

    @Test
    public void testMetadata() {
        SyzygyPayload<String> payload = loader.lookupFor("key1", String.class);
        assertEquals("from specific", payload.getValue());
        SyzygyPayload<String> again = loader.lookupFor("key1", String.class);
        assertEquals(payload.getHits() + 1, again.getHits(), 0);
        SyzygyPayload<String> different = loader.lookupFor("key2", String.class);
        if ( different.getHits() == again.getHits()) {
            different = loader.lookupFor("key2", String.class);
        }
        assertNotEquals(again.getHits(), different.getHits());
    }

    @Test
    public void testJsonOverride() {
        String payload = loader.lookup("key3", String.class);
        assertEquals("Key3 from json", payload);
        assertEquals("key4_value", loader.lookup("key4", String.class));
        List<String> path = loader.lookupFor("key4", String.class).getPath();
        assertEquals("convict", path.get(path.size()-1));

        assertEquals("value from common", loader.lookup("key2"));
        path = loader.lookupFor("key2", String.class).getPath();
        assertEquals(5, path.size());
        assertArrayEquals(new String[]{"ending", "convict", "key3", "specific", "common"}, path.toArray(new String[0]));
    }

    @Test
    public void testFileNamedYml() {
        assertEquals("from ending", loader.lookup("ending"));
    }

    @Test
    public void testConvictDirectly() {
        SyzygyConfig config = SyzygyConvictSchemaConfig
                .readConvict(readFrom + File.separator + "hieradata" + File.separator + "convict.json");
        assertEquals("key4_value", config.lookup("key4"));
        assertNull(config.lookup("non_existing"));
    }

    @Test
    public void testListAllProperties() {
        List<SyzygyPayload> result = loader.listAllProperties();
        assertTrue("Quite feeble test - just know that I will get a number of elements. Got: "+result.size(),
                result.size() > 4);
        SyzygyPayload second = result.get(1);
        assertEquals("convict", second.getPath().get(0));
        assertEquals("key4", second.getName());
        assertEquals("key4_value", second.getValue());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SyzygyHelper.printConfigTo(result, new OutputStreamWriter(baos));
        String allConf = baos.toString();
        assertTrue(!allConf.isEmpty());
        log.debug("\n" + allConf);
    }

    @Test( expected = SyzygyException.class )
    public void testValidation() {
        SyzygyLoader validation = SyzygyLoader.loadConfigurationFile(new File(readFrom + "/validation.yaml"));
        validation.validate();
    }

    @Test
    public void testDoc() {
        SyzygyPayload<Integer> payload = loader.lookupFor("shall_be_integer", Integer.class);
        assertEquals(Integer.valueOf(123), payload.getValue());
        assertEquals("Must be integer", payload.getDoc());
    }

    @Test
    public void testMapInJson() {
        Map jsonmap = loader.lookup("jsonmap", Map.class);
        assertNotNull(jsonmap);
        assertEquals("somevalue_1", jsonmap.get("internal_1"));
        assertEquals("somevalue_2", loader.deepLookup("internal_2", "jsonmap"));
    }

    @Test
    public void testFindLoaderWithName() {
        SyzygyConfig convict = loader.configurationWithName("convict");
        assertNotNull(convict);
        assertNotNull(convict.lookup("shall_be_integer", Integer.class));
        assertEquals("Expecting the file size to be deterministic", 5, loader.configurationNames().size());
    }

    @Test
    public void testLoaderWithMultipleDirectories() {
        loader = SyzygyLoader.loadConfigurationFile(
                new File(HieradirectoryHelper.findTestDirectoryReference("twoconfigdirs")
                +"/syzygy.yaml"));
        loader.validate();
        assertEquals("Key1 in first", loader.lookup("key1"));
        assertEquals("Key2 in first", loader.lookup("key2"));
        assertEquals("Key3 only exists in second", loader.lookup("key3"));
        assertNull("Key4 does not exist", loader.lookup("key4"));
    }
}