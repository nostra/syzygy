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
        etcd = EtcdConnector.attach(DirectoryHelper.ETCD_URL, DirectoryHelper.SYZYGY_JUNIT_PATH);
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
    public void demonstrateHowStorageOfMapWillRetainData() {
        Map<String, Object> map = new HashMap();
        map.put("somekey", "some value in map #1");
        etcd.store("structure", map );
        SyzygyConfig syzygyConfig = SyzygyEtcdConfig.connectAs(etcd, "structure");
        map = new HashMap();
        map.put("different", "value in map #2");
        etcd.store("structure", map );
        assertEquals("This documents that storage of 2 maps on top of each other will yield both values",
                     "some value in map #1", syzygyConfig.lookup("somekey"));
        assertEquals("value in map #2", syzygyConfig.lookup("different"));
        assertTrue(etcd.removeMap("structure"));
    }
}
