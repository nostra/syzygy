package no.api.syzygy.loaders;

import no.api.syzygy.SyzygyConfig;
import no.api.syzygy.SyzygyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class SyzygyConvictSchemaConfig extends AbstractConfigLoader {
    private static final Logger log = LoggerFactory.getLogger(SyzygyConvictSchemaConfig.class);

    private Map map;

    public SyzygyConvictSchemaConfig(String name) {
        super(name);
    }

    protected SyzygyConfig load(final String filename ) {
        map = load( filename, createObjectMapper());
        return this;
    }

    public static SyzygyConfig readConvict(String filename) {
        // TODO make implementation more agnostic about type
        return new SyzygyConvictSchemaConfig("json").load(filename);
    }


    @Override
    public String lookup(String key) {
        return lookup(key, String.class);
    }

    /**
     * This may become an interface method later, if it is found how to easily add
     * doc for other elements besides convict
     */
    public String doc(String key) {
        Map<String, String> convict = (Map<String, String>) map.get(key);
        return convict == null
                ? null
                : convict.get("doc");
    }

    @Override
    public <T> T lookup(String key, Class<T> clazz) {
        Map<String,String> convict = (Map<String,String>) map.get(key);
        if ( convict == null ) {
            return null;
        }

        // TODO Work in progress. Inspiration from:
        // https://github.com/mozilla/node-convict

        // TODO Schema only works if the schema belongs to the current file.  Need a way to extract the schema to be used generally.
        // SCHEMA part
        Object value = convict.get("default");
        String format = convict.get("format");
        // Find potential override
        Object convertTo = convertTo(value, format);

        try {
            return clazz.cast(convertTo);
        } catch(ClassCastException e) {
            throw new SyzygyException("Unexpected type. Could not cast convict format "+format+
                    " (resolved to "+convertTo.getClass().getSimpleName()+") to "+clazz.getName(), e);
        }
    }

    private Object convertTo(Object value, String format) {
        return format == null
                ? value
                : convert(format, value);
    }

    @Override
    public Set<String> keys() {
        return map.keySet();
    }

    private Object convert(String format, Object value) {
        if ( value == null ) {
            return null;
        }
        switch ( format ) {
            case "*":
                return value;
            case "int":
                if ( !(value instanceof Integer)) {
                    try {
                        return Integer.valueOf(""+value);
                    } catch ( NumberFormatException nfe ) {
                        throw new SyzygyException(value+" is not an integer as schema says: "+nfe);
                    }
                }
                return value;
            case "url":
                try {
                    return URI.create(value.toString()).toURL();
                } catch (MalformedURLException e) {
                    throw new SyzygyException("Unable to create URL by malformed value: "+value, e);
                }
            default:
                throw new SyzygyException("Currently not supporting format "+format);
        }
    }

    /**
     * Add validation errors, if any to list
     */
    protected void validate(SyzygyConfig cfg, List<String> errs) {
        for ( String key : keys() ) {
            Map<String,String> convict = (Map<String,String>) map.get(key);
            Object value = cfg.lookup(key, Object.class);
            if ( convict != null ) {
                try {
                    convert( convict.get("format"), value);
                } catch (SyzygyException ignored ) {
                    String doc = convict.get("doc");
                    errs.add("Validation failed for element "+cfg.getName()+"/"+key+". "
                            +"Expecting it to be of type "+convict.get("format")+". "
                            +"Actual value: "+value+"."
                            + (doc == null
                                    ? ""
                                    : " Documentation: "+doc+". "));
                }
            }
        }
    }
}
