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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 *
 */
public final class SyzygyHelper {
    private static final Logger log = LoggerFactory.getLogger(SyzygyHelper.class);

    private SyzygyHelper() {
        // Intentional
    }

    public static void printConfigTo( List<SyzygyPayload> configs, Writer writer ) {
        try {
            for ( SyzygyPayload c : configs ) {
                writer.write(trimOrPad( ""+c.getPath().get(0), 10));
                writer.write(" ");
                writer.write(trimOrPad( ""+c.getHits(), 5));
                writer.write(" ");
                writer.write(trimOrPad( c.getName(), 15));
                writer.write(" ");
                writer.write(trimOrPad( ""+c.getValue(), 50));
                writer.write("\n");
            }
            writer.flush();
        } catch (IOException e) {
            log.error("Got exception - output ignored", e);
        }
    }

    private static String trimOrPad( String str, int desiredLength ) {
        String result = str.substring(0, Math.min(str.length(), desiredLength));
        while ( result.length() < desiredLength ) {
            result = " "+result;
        }
        return result;
    }

}
