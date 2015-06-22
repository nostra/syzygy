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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Mount an etcd structure as syzygy config. You would typically mount an
 * etcd sub structure.
 */
public class SyzygyEtcdConfig implements SyzygyConfig {

    private static final Logger log = LoggerFactory.getLogger(SyzygyEtcdConfig.class);

    public static final int EXCEPTION_QUIET_TIME = 5000;

    public static final int EXCEPTION_LIMIT = 5;

    private final EtcdConnector etcd;

    private final String name;

    private long blackout = 0;

    private int problemCounter = 0;

    public SyzygyEtcdConfig(EtcdConnector etcd, String name) {
        if ( etcd == null ) {
            throw new SyzygyException("Not allowing etcd be null");
        }
        this.etcd = etcd;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String lookup(String key) {
        return lookup(key, String.class);
    }

    @Override
    public <T> T lookup(String key, Class<T> clazz) {
        if ( isBlacklisted()) {
            return null;
        }
        Object obj = null;
        try {
            obj = etcd.valueBy(getName() + "/" + key);
        } catch (Exception e) {
            // This is typically connection refused or something from etcd
            log.error("Cannot perform call for keys, due to exception, which masked is: "+e);
            blacklistPerhaps();
        }
        if  (obj == null ) {
            return null;
        }
        if ( clazz.isAssignableFrom(obj.getClass())) {
            return (T) obj;
        }
        // TODO If integer, or some other supported type, convert it.
        throw new SyzygyException("Not implemented conversion from "+obj.getClass().getSimpleName()+" to "
                +clazz.getSimpleName()+" on key "+key);
    }

    /**
     * Notice: Will return empty set if etcd is down
     */
    @Override
    public Set<String> keys() {
        if ( isBlacklisted()) {
            return null;
        }
        // Plus / as a wrapped config really lives in a map
        Set<String> result = new HashSet<>();
        try {
            result = etcd.keys(getName() + "/");
        } catch (Exception e) { // Ignore
            // This is typically connection refused or something from etcd
            log.error("Cannot perform call for keys, due to exception, which masked is: "+ e);
            blacklistPerhaps();
        }
        return result;
    }

    /**
     * If there is more than _limit_ exceptions with in a _period_ of time, then
     * stop querying etcd, assuming that it is not healthy
     * @return
     */
    private boolean isBlacklisted() {
        if ( blackout != 0 ) {
            long delta = System.currentTimeMillis()-blackout;
            if ( delta > EXCEPTION_QUIET_TIME ) {
                problemCounter = 0;
                blackout = 0;
            } else if ( problemCounter > EXCEPTION_LIMIT) {
                return true;
            }
        }
        return false;
    }

    /**
     * Noting potential blackout time, and counting problems
     */
    private void blacklistPerhaps() {
        if ( blackout == 0 ) {
            blackout = System.currentTimeMillis();
        }
        problemCounter++;
        if ( problemCounter == EXCEPTION_LIMIT -1 ) {
            log.error("Blacklisting etcd as it is starting to hammer backend which gives too many exceptions.");
        }
    }

    /**
     * Mount a subdirectory from the current etcd structure with etcd
     * @param etcd Connected etcd instance. It may have a prefix
     * @param name Last path element, and name of this connection
     */
    public static SyzygyConfig connectAs(EtcdConnector etcd, String name) {
        return new SyzygyEtcdConfig(etcd, name);
    }
}
