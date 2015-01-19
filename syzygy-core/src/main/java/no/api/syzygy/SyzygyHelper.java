package no.api.syzygy;

import no.api.pantheon.lang.Text;
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
                writer.write(Text.trimOrPad( ""+c.getPath().get(0), 10));
                writer.write(" ");
                writer.write(Text.trimOrPad( ""+c.getHits(), 5));
                writer.write(" ");
                writer.write(Text.trimOrPad( c.getName(), 15));
                writer.write(" ");
                writer.write(Text.trimOrPad( ""+c.getValue(), 50));
                writer.write("\n");
            }
            writer.flush();
        } catch (IOException e) {
            log.error("Got exception - output ignored", e);
        }
    }
}
