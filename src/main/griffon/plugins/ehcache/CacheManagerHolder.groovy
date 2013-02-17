/*
 * Copyright 2012-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package griffon.plugins.ehcache

import net.sf.ehcache.CacheManager

import griffon.core.GriffonApplication
import griffon.util.ApplicationHolder
import static griffon.util.GriffonNameUtils.isBlank

/**
 * @author Andres Almiray
 */
class CacheManagerHolder {
    private static final String DEFAULT = 'default'
    private static final Object[] LOCK = new Object[0]
    private final Map<String, CacheManager> cacheManagers = [:]

    private static final CacheManagerHolder INSTANCE

    static {
        INSTANCE = new CacheManagerHolder()
    }

    static CacheManagerHolder getInstance() {
        INSTANCE
    }

    String[] getCacheManagerNames() {
        List<String> cacheManagerNames = new ArrayList().addAll(cacheManagers.keySet())
        cacheManagerNames.toArray(new String[cacheManagerNames.size()])
    }

    CacheManager getCacheManager(String cacheManagerName = DEFAULT) {
        if (isBlank(cacheManagerName)) cacheManagerName = DEFAULT
        retrieveCacheManager(cacheManagerName)
    }

    void setCacheManager(String cacheManagerName = DEFAULT, CacheManager cacheManager) {
        if (isBlank(cacheManagerName)) cacheManagerName = DEFAULT
        storeCacheManager(cacheManagerName, cacheManager)
    }

    boolean isCacheManagerConnected(String cacheManagerName) {
        if (isBlank(cacheManagerName)) cacheManagerName = DEFAULT
        retrieveCacheManager(cacheManagerName) != null
    }

    void disconnectCacheManager(String cacheManagerName) {
        if (isBlank(cacheManagerName)) cacheManagerName = DEFAULT
        storeCacheManager(cacheManagerName, null)
    }

    CacheManager fetchCacheManager(String cacheManagerName) {
        if (isBlank(cacheManagerName)) cacheManagerName = DEFAULT
        CacheManager cacheManager = retrieveCacheManager(cacheManagerName)
        if (cacheManager == null) {
            GriffonApplication app = ApplicationHolder.application
            ConfigObject config = EhcacheConnector.instance.createConfig(app)
            cacheManager = EhcacheConnector.instance.connect(app, config, cacheManagerName)
        }

        if (cacheManager == null) {
            throw new IllegalArgumentException("No such CacheManager configuration for name $cacheManagerName")
        }
        cacheManager
    }

    private CacheManager retrieveCacheManager(String cacheManagerName) {
        synchronized (LOCK) {
            cacheManagers[cacheManagerName]
        }
    }

    private void storeCacheManager(String cacheManagerName, CacheManager cacheManager) {
        synchronized (LOCK) {
            cacheManagers[cacheManagerName] = cacheManager
        }
    }
}