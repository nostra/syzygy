package no.api.syzygy;

import no.api.syzygy.loaders.SyzygyConvictSchemaConfig;
import no.api.syzygy.loaders.SyzygyLoader;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.*;

public class SyzygyConvictSchemaConfigTest {

    private String basedir;

    @Before
    public void setUp() {
        basedir = HieradirectoryHelper.findTestDirectoryReference("json");
    }

    @Test
    public void testLoadOfJson() {
        SyzygyConvictSchemaConfig onefile =
                (SyzygyConvictSchemaConfig) SyzygyConvictSchemaConfig.readConvict(basedir + File.separator + "convict.json");
        assertEquals("default_value", onefile.lookup("example"));
        assertEquals("Example doc.", onefile.doc("example"));
    }

    @Test
    public void testConvictFormatInYaml() {
        SyzygyConvictSchemaConfig onefile =
                (SyzygyConvictSchemaConfig) SyzygyConvictSchemaConfig.readConvict(basedir + File.separator + "yamlconvict.yaml");
        assertEquals("example (yaml) default", onefile.lookup("example"));
        assertEquals("Example (yaml) doc.", onefile.doc("example"));
        assertEquals("key_1_from_yaml_convict", onefile.lookup("convict_key_1"));
    }

    @Test
    public void testReadingOfConvictFiles() throws Exception {
        SyzygyLoader config = SyzygyLoader.loadConfigurationFile(new File(basedir + File.separator + "syzygy.yaml"));
        assertEquals("default_value", config.lookup("example"));
        assertEquals("key_1_from_convict", config.lookup("convict_key_1"));
        assertEquals("random", config.lookup("random"));
        assertEquals("from_plain", config.lookup("convict_will_be_overridden_by_plain"));
        assertEquals(Integer.valueOf(12345), config.lookup("insist_on_int", Integer.class));
        assertEquals(3, config.configurationNames().size());
    }

    @Test
    public void testUrl() throws MalformedURLException {
        SyzygyConvictSchemaConfig onefile =
                (SyzygyConvictSchemaConfig) SyzygyConvictSchemaConfig.readConvict(basedir + File.separator + "convict.json");
        assertEquals(new URL("http://www.semispace.org"), onefile.lookup("semispace", URL.class));
        assertEquals("Example doc.", onefile.doc("example"));
    }

    @Test( expected = SyzygyException.class )
    public void testMalformedURL() {
        SyzygyConfig cfg = SyzygyConvictSchemaConfig.readConvict(basedir + File.separator + "malformed.json");
        cfg.lookup("malformed", URL.class);
    }
}