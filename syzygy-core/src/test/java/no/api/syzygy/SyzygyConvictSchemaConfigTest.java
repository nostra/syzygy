/*
 * Copyright 2015 Amedia Utvikling
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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