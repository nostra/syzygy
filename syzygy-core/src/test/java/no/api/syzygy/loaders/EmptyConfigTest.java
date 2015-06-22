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
import no.api.syzygy.SyzygyException;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/**
 *
 */
public class EmptyConfigTest {

    @Test
    public void see_what_happens_with_empty_file_with_comment() {
        String readFrom = HieradirectoryHelper.findTestDirectoryReference("yamlonly");
        try {
            SyzygyLoader.loadConfigurationFile(new File(readFrom+"/emptyfile.yaml"));
            fail("If this test fails, it may be because filtering of comments in yaml file now works. " +
                         "In that case, you need to rework this test.");
        } catch ( SyzygyException se ) {
            assertTrue("Expecting specific error string, but got: "+se.getCause().getMessage(),
                       se.getCause().getMessage().startsWith("No content to map due to end-of-input"));
        }
        // You should not get a different error message out of this.
    }


    @Test
    public void see_what_happens_with_empty_file() {
        String readFrom = HieradirectoryHelper.findTestDirectoryReference("yamlonly");
        SyzygyLoader config = SyzygyLoader.loadConfigurationFile(new File(readFrom + "/emptyvalid.yaml"));
        assertNotNull(config);
        assertEquals(0, config.listAllProperties().size());
    }

    @Test
    public void see_what_happens_when_having_non_existing_file_DEVOPS_505() {
        String readFrom = HieradirectoryHelper.findTestDirectoryReference("yamlonly");
        SyzygyLoader config = SyzygyLoader.loadConfigurationFile(new File(readFrom + "/missingfile.yaml"));
        assertNotNull(config);
        assertEquals(0, config.listAllProperties().size());
    }

}
