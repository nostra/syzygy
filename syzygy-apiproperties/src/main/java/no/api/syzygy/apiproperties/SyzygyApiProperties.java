package no.api.syzygy.apiproperties;

import no.api.properties.api.ApiPropertiesException;
import no.api.properties.api.ApiPropertiesManager;
import no.api.properties.api.ApiProperty;
import no.api.properties.api.ApiPublication;
import no.api.properties.api.ApiPublicationManager;
import no.api.syzygy.loaders.SyzygyLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class SyzygyApiProperties implements ApiPropertiesManager {
    private static final Logger log = LoggerFactory.getLogger( SyzygyApiProperties.class );
    private static SyzygyApiProperties instance = null;
    private final ApiPublicationManager publications;
    private SyzygyLoader syzygy = null;

    /**
     * Protected for the benefit of junit tests.
     */
    protected SyzygyApiProperties(SyzygyLoader syzygy, ApiPublicationManager publications) {
        this.syzygy = syzygy;
        this.publications = publications;
    }

    public synchronized static ApiPropertiesManager fetchInstanceWith( ApiPublicationManager publications ) {
        if ( instance == null ) {
            File syzygyFile = new File( System.getProperty("V3_CONFIG_HOME", "/etc/api/syzygy/syzygy.yaml"));

            if ( syzygyFile.exists() ) {
                instance = new SyzygyApiProperties(  SyzygyLoader.loadConfigurationFile(syzygyFile), publications);
            } else {
                throw new ApiPropertiesException("Cannot find syzygy configuration file.");
            }
        }
        return instance;
    }

    @Override
    public void flush() {
        publications.flush();
        syzygy.flush();
    }

    @Override
    public boolean hasGlobalProperty(String key) {
        return getGlobalProperty(key) != null;
    }

    @Override
    public ApiProperty getGlobalProperty(String key) {
        String value = syzygy.lookup(key);
        ApiProperty prop = null;
        if ( value != null ) {
            prop = new SyzygyProperty(key, value);
        }
        return prop;
    }

    @Override
    public boolean hasProperty(String key, Integer pubId) {
        return getProperty(key, pubId) != null;
    }

    @Override
    public ApiProperty getProperty(String key, Integer pubId) {
        ApiPublication publicatication = publications.getPublication(pubId);
        if ( publicatication == null ) {
            log.error("Could not resolve publication with id "+pubId+", returning null as property");
            return null;
        }
        // Have to re-implement deepLookup due to logic regarding domain aliases
        for ( String firstHit : domainListFrom( publicatication )) {
            Map<String, String> map = syzygy.lookup(firstHit, Map.class);
            if ( map != null ) {
                String value = map.get(key);
                log.info("Got "+firstHit);
                if ( value != null ) {
                    log.info("... and it got property value "+value);
                    return new SyzygyProperty(key, value);
                }
            }
        }
        String value = syzygy.lookup(key);
        if ( value != null ) {
            return new SyzygyProperty(key, value);
        }
        return null;
    }

    private List<String> domainListFrom(ApiPublication publicatication) {
        List<String> result = new ArrayList<>();
        result.add(publicatication.getWwwDomain());
        result.add(publicatication.getMobDomain());
        for ( String alias : publicatication.getDomainAliases().split(",")) {
            if (!alias.trim().isEmpty()) {
                result.add(alias.trim());
            }
        }

        return result;
    }

    @Override
    public boolean hasProperty(String key, Integer pubId, String sectionId) {
        log.error("hasProperty("+key+", "+pubId+", "+sectionId+") is not supported");
        return false;
    }

    @Override
    public ApiProperty getProperty(String key, Integer pubId, String sectionId) {
        log.error("getProperty("+key+", "+pubId+", "+sectionId+") is not supported");
        return null;
    }

    private static class SyzygyProperty implements ApiProperty {
        private String name;
        private String value;

        public SyzygyProperty(String name, String value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public boolean isOverridable() {
            return false;
        }

        @Override
        public boolean isMedusa() {
            return false;
        }
    }
}
