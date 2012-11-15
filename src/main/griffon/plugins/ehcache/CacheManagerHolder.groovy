/*
 * Copyright 2011-2012 the original author or authors.
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
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import griffon.core.GriffonApplication
import griffon.util.ApplicationHolder
import griffon.util.CallableWithArgs
import static griffon.util.GriffonNameUtils.isBlank

/**
 * @author Andres Almiray
 */
@Singleton
class CacheManagerHolder implements EhcacheProvider {
    private static final Logger LOG = LoggerFactory.getLogger(CacheManagerHolder)
    private static final Object[] LOCK = new Object[0]
    private final Map<String, CacheManager> cacheManagers = [:]

    String[] getCacheManagerNames() {
        List<String> cacheManagerNames = new ArrayList().addAll(cacheManagers.keySet())
        cacheManagerNames.toArray(new String[cacheManagerNames.size()])
    }

    CacheManager getCacheManager(String cacheManagerName = 'default') {
        if (isBlank(cacheManagerName)) cacheManagerName = 'default'
        retrieveCacheManager(cacheManagerName)
    }

    void setCacheManager(String cacheManagerName = 'default', CacheManager cacheManager) {
        if (isBlank(cacheManagerName)) cacheManagerName = 'default'
        storeCacheManager(cacheManagerName, cacheManager)
    }

    Object withEhcache(String cacheManagerName = 'default', Closure closure) {
        CacheManager cacheManager = fetchCacheManager(cacheManagerName)
        if (LOG.debugEnabled) LOG.debug("Executing statement on cacheManager '$cacheManagerName'")
        return closure(cacheManagerName, cacheManager)
    }

    public <T> T withEhcache(String cacheManagerName = 'default', CallableWithArgs<T> callable) {
        CacheManager cacheManager = fetchCacheManager(cacheManagerName)
        if (LOG.debugEnabled) LOG.debug("Executing statement on cacheManager '$cacheManagerName'")
        callable.args = [cacheManagerName, cacheManager] as Object[]
        return callable.call()
    }

    boolean isCacheManagerConnected(String cacheManagerName) {
        if (isBlank(cacheManagerName)) cacheManagerName = 'default'
        retrieveCacheManager(cacheManagerName) != null
    }

    void disconnectCacheManager(String cacheManagerName) {
        if (isBlank(cacheManagerName)) cacheManagerName = 'default'
        storeCacheManager(cacheManagerName, null)
    }

    private CacheManager fetchCacheManager(String cacheManagerName) {
        if (isBlank(cacheManagerName)) cacheManagerName = 'default'
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
