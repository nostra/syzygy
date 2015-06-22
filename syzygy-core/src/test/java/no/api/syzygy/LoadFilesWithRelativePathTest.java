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

import no.api.syzygy.loaders.SyzygyLoader;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class LoadFilesWithRelativePathTest {

    @Test
    public void testLoadWithRelativePath() {
        String basedir = HieradirectoryHelper.findTestDirectoryReference("yamlonly");
        SyzygyLoader syzygy = SyzygyLoader.loadConfigurationFile(new File(basedir + File.separator + "withpath.yaml"));
        syzygy.validate();
        assertEquals("Key1 in first", syzygy.lookup("key1"));
        assertEquals("from common", syzygy.lookup("ending"));
    }

    @Test
    public void testLoadWithDatadirADifferentPlace() {
        String basedir = HieradirectoryHelper.findTestDirectoryReference("yamlonly");
        SyzygyLoader syzygy = SyzygyLoader.loadConfigurationFile(new File(basedir + File.separator + "datadirpath.yaml"));
        syzygy.validate();
        assertEquals("Key1 in first", syzygy.lookup("key1"));
        assertEquals("from common", syzygy.lookup("ending"));
    }


}
