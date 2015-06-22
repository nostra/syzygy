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

package no.api.syzygy;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Wrapper class when performing lookup in order to retain metadata.
 */
public class SyzygyPayload<T> {

    private final String name;

    private final T value;

    private static final Map<String, AtomicLong> hitMap = new ConcurrentHashMap<>();

    private long hits;

    private final List<String> path;

    private final String doc;

    public SyzygyPayload(String name, T value, List<String> path, String doc) {
        this.name = name;
        this.value = value;
        this.doc = doc;
        this.path = Collections.unmodifiableList(path);
        AtomicLong hit = hitMap.get(name);
        if ( hit == null ) {
            hit = new AtomicLong();
            hitMap.put(name, hit);
        }
        hits = hit.incrementAndGet();
    }

    public String getName() {
        return name;
    }

    public T getValue() {
        return value;
    }

    public long getHits() {
        return hits;
    }

    public List<String> getPath() {
        return path;
    }

    public String getDoc() {
        return doc;
    }
}
