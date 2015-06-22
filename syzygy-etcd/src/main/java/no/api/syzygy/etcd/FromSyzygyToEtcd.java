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
import no.api.syzygy.SyzygyException;

import java.util.Map;

/**
 * WARNING: Intended to be used from junit tests
 */
public class FromSyzygyToEtcd {

    private EtcdConnector etcdConnector;

    /**
     * NOTE that this is additive - it does not remove elements not present in etcd.
     */
    protected static void storeConfigInto(SyzygyConfig syzgyConfig, EtcdConnector etcdConnector) {
        new FromSyzygyToEtcd( etcdConnector ).mapFrom( syzgyConfig );
    }

    private FromSyzygyToEtcd(EtcdConnector etcdConnector) {
        this.etcdConnector = etcdConnector;
    }

    private void mapFrom(SyzygyConfig syzgyConfig) {
        final String prefix = syzgyConfig.getName()+"/";
        int counter = 0;
        for ( String key : syzgyConfig.keys()) {
            String translatedKey = prefix+key;
            counter++;
            Object obj = syzgyConfig.lookup(key, Object.class);
            boolean storedOk = false;
            if ( obj instanceof Map) {
                storedOk = etcdConnector.store(translatedKey, (Map)obj);
            } else if ( obj instanceof String) {
                storedOk = etcdConnector.store(translatedKey, (String)obj);
            } else {
                throw new SyzygyException("Unexpected data type in syzygy. Got: "+obj.getClass());
            }
            if ( !storedOk ) {
                throw new SyzygyException("Trouble storing element with key "+translatedKey+". This was item "+counter
                        +" out of "+syzgyConfig.keys().size());
            }
        }
    }

}
