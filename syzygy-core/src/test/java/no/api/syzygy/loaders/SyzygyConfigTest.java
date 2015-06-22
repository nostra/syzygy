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

import no.api.syzygy.HieradirectoryHelper;
import no.api.syzygy.SyzygyConfig;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

public class SyzygyConfigTest {
    private String basedir;

    @Before
    public void setUp() {
        basedir = HieradirectoryHelper.findTestDirectoryReference("yamlonly");
    }

    @Test
    public void testEquality() throws IOException {
        SyzygyConfig hyc_1 = new SyzygyFileConfig("same name").load(basedir+ File.separator+ "hieradata" +File.separator+"common.yaml");
        SyzygyConfig hyc_2 = new SyzygyFileConfig("same name").load(basedir+ File.separator+ "hieradata" +File.separator+"common.yaml");
        assertEquals("When reading the same config file, they should be equal", hyc_1, hyc_2);
        hyc_2 = new SyzygyFileConfig("same name").load(basedir+ File.separator+ "hieradata" +File.separator+"common_similar.yaml");
        assertNotEquals("When having different contents, the config sets are different.", hyc_1, hyc_2);
    }

    @Test
    public void testNoDuplicatesAllowed() throws InterruptedException {
        // For some reason, @Test (expected = ... ) did not work
        try {
            new SyzygyFileConfig("erroneous file")
                .load(basedir + File.separator + "hieradata" + File.separator + "error_duplication.yaml");
            fail("Expected duplicate element to make reading of file fail");
        } catch (RuntimeException ignore) {
            // Intended
        }

    }
}