package no.api.syzygy;

/**
 * Dynamically loading programmatic configuration set. This class will be
 * instantiated and is supposed to create a SyzygyConfig configuration.
 * Note that the class needs a public argument free constructor.
 */
public interface SyzygyDynamicLoader {
    SyzygyConfig createSyzygyConfigWith( SyzygyConfig loaderConfiguration );
}
