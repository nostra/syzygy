package no.api.syzygy.loaders;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
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
import java.util.HashMap;
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
        if (yaml.trim().isEmpty()) {
            log.warn("Empty file creates empty hashmap");
            return new HashMap<>();
        }
        Map map = null;
        try {
            // Validate: readTree will throw exception if not valid
            mapper.readTree(yaml);
            map = mapper.readValue(yaml, Map.class);

            // No content to map due to end-of-input
        } catch (IOException e) {
            throw new SyzygyException("Error reading file "+uri, e);
        }
        if ( map == null ) {
            // TODO At the time of writing, this does not seem to be caught by any test.
            log.warn("Tree ended up with being null. Maybe the file is empty. Contents: "+yaml);
        }

        return map;
    }

    protected ObjectMapper createObjectMapper() {
        return new ObjectMapper(new YAMLFactory())
                .enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY)
                .enable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE)
                .enable(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS)
                .enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
    }
}
