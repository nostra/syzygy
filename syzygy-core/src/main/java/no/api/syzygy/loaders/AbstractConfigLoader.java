package no.api.syzygy.loaders;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.api.pantheon.io.PantheonFileReader;
import no.api.syzygy.SyzygyConfig;
import no.api.syzygy.SyzygyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Map;

/**
 *
 */
public abstract class AbstractConfigLoader implements SyzygyConfig {
    private static final Logger log = LoggerFactory.getLogger(AbstractConfigLoader.class);

    private final String name;

    public AbstractConfigLoader(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    protected Map load(final String filename, ObjectMapper mapper ) {
        return load( new File(filename), mapper);
    }

    protected Map load(final File file, ObjectMapper mapper ) {
        return load( file.toURI(), mapper);
    }

    protected Map load(final URI uri, ObjectMapper mapper ) {
        String yaml = null;
        try (InputStream is = uri.toURL().openStream()) {

            yaml = PantheonFileReader.createInstance().readIntoString( new InputStreamReader( is ));
        } catch ( IOException e) {
            throw new SyzygyException("Got exception trying to read "+uri, e);
        }
        if ( yaml == null ) {
            throw new SyzygyException("Did manage to read any data from "+uri+". This is unexpected.");
        }
        Map map = null;
        try {
            map = mapper.readValue(yaml, Map.class);
        } catch (IOException e) {
            log.error("Got exception.", e);
        }
        if ( map == null ) {
            throw new SyzygyException("Tree ended up with being null");
        }

        return map;
    }

}
