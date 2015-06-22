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

package no.api.syzygy.loaders;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class CeteraReadTest {
    private static final Logger log = LoggerFactory.getLogger(CeteraReadTest.class);

    private File syzygyfile;

    @Before
    public void setUp() {
        syzygyfile = new File("/etc/api/syzygy/syzygy.yaml");
        Assume.assumeTrue(syzygyfile.exists());
    }


    /**
     * This test is expected to break sooner or later. Just add it to Ignore, if it does.
     * This code is mainly here in order easily test syzygy setup
     */
    @Test
    public void testCeteraRead() {
        SyzygyLoader loader = SyzygyLoader.loadConfigurationFile(syzygyfile);
        String values_might_change = "If this test fails, you either have a problem, OR a change in " +
                "cetera. Sorry about that. It is due to testing on real data. Don't want to pollute " +
                "real server configuration with test data...";
        assertEquals(values_might_change, "16", loader.lookup("acpcomposer.sectiongrid.maxfloors"));
        assertEquals(values_might_change, "Origin - Romerikes Blad", loader.deepLookup("adtech.websiteName", "www.rb.no"));
        assertEquals(values_might_change, "Ukjent", loader.deepLookup("adtech.websiteName", "www.api.no"));
        assertEquals(values_might_change, "Ukjent", loader.lookup("adtech.websiteName"));
    }

}
