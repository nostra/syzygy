package no.api.syzygy.loaders;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.api.pantheon.io.PantheonFileReader;
import no.api.syzygy.SyzygyConfig;
import no.api.syzygy.SyzygyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
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
        String yaml = PantheonFileReader.createInstance().readIntoString( file );
        if ( yaml == null ) {
            throw new SyzygyException("Did manage to read any data from "+file+". This is unexpected.");
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
