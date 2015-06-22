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
import no.api.syzygy.SyzygyHelper;
import no.api.syzygy.SyzygyPayload;
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
import java.util.Map;

import static org.junit.Assert.*;

/**
 *
 */
public class UsingEtcdInSyzygLoaderTest {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private EtcdConnector etcd;

    private String readFrom;

    @Before
    public void determineBaseDirectory() throws IOException {
        etcd = EtcdConnector.attach(DirectoryHelper.ETCD_URL, DirectoryHelper.SYZYGY_JUNIT_PATH + "etcdmap/");
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

    @Test
    public void demonstrateThatConvictIsNotSupported() throws IOException {
        Map convictMap = new ObjectMapper().readValue(new File(readFrom + File.separator + "convict/convict.json"), Map.class);
        assertTrue(convictMap.size() > 0);

        assertTrue(etcd.store("convict", convictMap));

        SyzygyLoader syzygy = SyzygyLoader.loadConfigurationFile(new File(readFrom + "/convict/syzygy.yaml"));


        // *********************************
        // NOTE
        // The code below will fail if / when convict support has been implemented for etcd
        SyzygyPayload<Map> example = syzygy.lookupFor("example", Map.class);
        assertNotNull(example);
        assertNull("When this test fails, etcd has gotten convict support", example.getDoc());
        assertEquals("default_value", example.getValue().get("default"));

        assertTrue(etcd.removeMap("convict"));
    }

}
