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

import java.io.File;

/**
 *
 */
public class DirectoryHelper {

    public static final String ETCD_URL = "http://127.0.0.1:4001/v2/";
    public static final String SYZYGY_JUNIT_PATH = "/syzygy/junit/";

    protected static String findTestResourcesDirectory() {
        File cur = new File(".");
        StringBuffer readFrom = new StringBuffer();
        if ( ! cur.getAbsolutePath().contains("syzygy-etcd")) {
            // IntelliJ
            readFrom.append("syzygy-etcd/");
        }
        readFrom.append("src/test/resources/");
        return readFrom.toString();
    }
}
