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

import no.api.syzygy.SyzygyConfig;
import no.api.syzygy.SyzygyDynamicLoader;
import no.api.syzygy.SyzygyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This dynamic configuration takes 2 arguments:
 * <ul>
 *     <li><tt>global_etcd_url</tt>: For where etcd reside. Defaulting to localhost. You can also use refkey+_etcd_url</li>
 *     <li>refkey+<tt>_etcd_prefix</tt> : the subdirectory on which you attach this instance. It looks
 *     something like this: /syzygy/something </li>
 * </ul>
 */
public class EtcdDynamicConfiguration implements SyzygyDynamicLoader {
    private static final Logger log = LoggerFactory.getLogger(EtcdDynamicConfiguration.class);

    @Override
    public SyzygyConfig createSyzygyConfigWith(String refkey, SyzygyConfig loaderConfiguration) {
        String etcdUrl = loaderConfiguration.lookup(refkey+"_etcd_url");
        if ( etcdUrl == null ) {
            etcdUrl = loaderConfiguration.lookup("global_etcd_url");
        }
        String prefix = loaderConfiguration.lookup(refkey+"_etcd_prefix");
        if ( etcdUrl == null ) {
            etcdUrl = "http://127.0.0.1:4001/v2/";  // NOSONAR
            log.warn("Expected to find etcd url from "+refkey+"_etcd_url config element. Using fallback: "+etcdUrl );
        }
        if ( prefix != null && !prefix.startsWith("/syzygy")) {
            log.warn("Expected that etcdPrefix starts with /syzygy/. USE WITH CAUTION. " +
                             "Config key: "+refkey+"_etcd_prefix");
        }
        if ( prefix == null ) {
            prefix = "/syzygy/";
        }
        EtcdConnector etcd = EtcdConnector.attach(etcdUrl, prefix);
        if ( etcd == null || !etcd.isAlive()) {
            Boolean stopIfError = loaderConfiguration.lookup("stop_if_error", Boolean.class );
            if ( stopIfError != null && stopIfError.booleanValue() ) {
                throw new SyzygyException("Cannot attach etcd, and as this configuration is marked as " +
                        "'stop_if_error', exception is thrown.");
            }
            if ( etcd == null ) {
                return null;
            }
        }

        return SyzygyEtcdConfig.connectAs(etcd, refkey);
    }
}
