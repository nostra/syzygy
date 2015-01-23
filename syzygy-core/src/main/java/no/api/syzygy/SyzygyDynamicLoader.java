package no.api.syzygy;

/**
 * Dynamically loading programmatic configuration set. This class will be
 * instantiated and is supposed to create a SyzygyConfig configuration.
 * Note that the class needs a public argument free constructor.
 */
public interface SyzygyDynamicLoader {

    /**
     * @param configurationString A configuration string which can be used to get more information from the top
     *                            level configuration. At the very least, it can give you some way of differentiating
     *                            between two otherwise equal configuration sets.
     * @param loaderConfiguration The framework will send this in. It would be the
     *                            configuration of the top level configuration, i.e. <tt>syzygy.yaml</tt>
     */
    SyzygyConfig createSyzygyConfigWith( String configurationString, SyzygyConfig loaderConfiguration );
}
