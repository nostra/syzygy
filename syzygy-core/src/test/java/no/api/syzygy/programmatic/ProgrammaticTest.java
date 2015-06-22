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

package no.api.syzygy.programmatic;

import no.api.syzygy.HieradirectoryHelper;
import no.api.syzygy.SyzygyException;
import no.api.syzygy.loaders.SyzygyLoader;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 *
 */
public class ProgrammaticTest {
    private SyzygyLoader hiera;
    private String readFrom;

    @Before
    public void setUp() {
        readFrom = HieradirectoryHelper.findTestDirectoryReference("programatic");
        hiera = SyzygyLoader.loadConfigurationFile(new File( readFrom+"/dummy.yaml"));
    }

    @Test
    public void testDummyconfig() {
        // Awaiting reimplementation
        assertEquals("dummy", hiera.lookup("dummy"));
        assertEquals("everything is just returned", hiera.lookup("everything is just returned"));
    }

    @Test
    public void testReadingWithError() {
        SyzygyLoader.loadConfigurationFile(new File( readFrom+"/error_is_ignored.yaml"));
        try {
            SyzygyLoader.loadConfigurationFile(new File( readFrom+"/error_is_not_ignored.yaml"));
            fail("Error should have been thrown");
        } catch (SyzygyException expected ) {}
    }

    @Test
    public void testName() {
        SyzygyLoader loader = SyzygyLoader.loadConfigurationFile(new File(readFrom + "/dummy.yaml"));
        assertEquals(1, loader.configurationNames().size());
        assertEquals("Expecting configuration name to be deterministic.", "config_a", loader.configurationNames().get(0));
    }
}
