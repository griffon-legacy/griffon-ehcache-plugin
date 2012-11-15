/*
 * Copyright 2012 the original author or authors.
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

import net.sf.ehcache.Cache
import net.sf.ehcache.CacheManager
import net.sf.ehcache.config.CacheConfiguration
import net.sf.ehcache.config.Configuration
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import griffon.core.GriffonApplication
import griffon.util.CallableWithArgs
import griffon.util.ConfigUtils

/**
 * @author Andres Almiray
 */
@Singleton
final class EhcacheConnector implements EhcacheProvider {
    private bootstrap

    private static final Logger LOG = LoggerFactory.getLogger(EhcacheConnector)
    private static final String CLASSPATH_PREFIX = 'classpath://'

    Object withEhcache(String cacheManagerName = 'default', Closure closure) {
        CacheManagerHolder.instance.withEhcache(cacheManagerName, closure)
    }

    public <T> T withEhcache(String cacheManagerName = 'default', CallableWithArgs<T> callable) {
        return CacheManagerHolder.instance.withEhcache(cacheManagerName, callable)
    }

    // ======================================================

    ConfigObject createConfig(GriffonApplication app) {
        ConfigUtils.loadConfigWithI18n('EhcacheConfig')
    }

    private ConfigObject narrowConfig(ConfigObject config, String cacheManagerName) {
        return cacheManagerName == 'default' ? config.cacheManager : config.cacheManagers[cacheManagerName]
    }

    CacheManager connect(GriffonApplication app, ConfigObject config, String cacheManagerName = 'default') {
        if (CacheManagerHolder.instance.isCacheManagerConnected(cacheManagerName)) {
            return CacheManagerHolder.instance.getCacheManager(cacheManagerName)
        }

        config = narrowConfig(config, cacheManagerName)
        app.event('EhcacheConnectStart', [config, cacheManagerName])
        CacheManager cacheManager = startEhcache(app, config)
        CacheManagerHolder.instance.setCacheManager(cacheManagerName, cacheManager)
        bootstrap = app.class.classLoader.loadClass('BootstrapEhcache').newInstance()
        bootstrap.metaClass.app = app
        bootstrap.init(cacheManagerName, cacheManager)
        app.event('EhcacheConnectEnd', [cacheManagerName, cacheManager])
        cacheManager
    }

    void disconnect(GriffonApplication app, ConfigObject config, String cacheManagerName = 'default') {
        if (CacheManagerHolder.instance.isCacheManagerConnected(cacheManagerName)) {
            config = narrowConfig(config, cacheManagerName)
            CacheManager cacheManager = CacheManagerHolder.instance.getCacheManager(cacheManagerName)
            app.event('EhcacheDisconnectStart', [config, cacheManagerName, cacheManager])
            bootstrap.destroy(cacheManagerName, cacheManager)
            stopEhcache(config, cacheManager)
            app.event('EhcacheDisconnectEnd', [config, cacheManagerName])
            CacheManagerHolder.instance.disconnectCacheManager(cacheManagerName)
        }
    }

    private CacheManager startEhcache(GriffonApplication app, ConfigObject config) {
        if (config.url) {
            if (config.url instanceof URL) return new CacheManager(config.url)
            String url = config.url.toString()
            if (url.startsWith(CLASSPATH_PREFIX)) return new CacheManager(app.getResourceAsURL(url.substring(CLASSPATH_PREFIX.length())))
            return new CacheManager(url.toURL())
        }
        Configuration cacheManagerConfig = new Configuration()
        List caches = []
        config.each { key, value ->
            if (key == 'caches') {
                value.each { cacheName, cacheConfigProps ->
                    CacheConfiguration cacheConfiguration = new CacheConfiguration()
                    cacheConfigProps.each { k, v ->
                        cacheConfiguration[k] = v
                    }
                    cacheConfiguration.name = cacheName
                    caches << new Cache(cacheConfiguration)
                }
            } else {
                try {
                    cacheManagerConfig[key] = value
                } catch (MissingPropertyException x) {
                    // ignore ?
                }
            }
        }
        CacheManager cacheManager = new CacheManager(cacheManagerConfig)
        caches.each { cache -> cacheManager.addCache(cache) }
        cacheManager
    }

    private void stopEhcache(ConfigObject config, CacheManager cacheManager) {
        cacheManager.shutdown()
    }
}
