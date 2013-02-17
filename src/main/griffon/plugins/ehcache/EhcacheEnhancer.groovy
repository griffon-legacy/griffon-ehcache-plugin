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

import griffon.util.CallableWithArgs
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Andres Almiray
 */
final class EhcacheEnhancer {
    private static final String DEFAULT = 'default'
    private static final Logger LOG = LoggerFactory.getLogger(EhcacheEnhancer)

    private EhcacheEnhancer() {}
    
    static void enhance(MetaClass mc, EhcacheProvider provider = DefaultEhcacheProvider.instance) {
        if(LOG.debugEnabled) LOG.debug("Enhancing $mc with $provider")
        mc.withEhcache = {Closure closure ->
            provider.withEhcache(DEFAULT, closure)
        }
        mc.withEhcache << {String cacheManagerName, Closure closure ->
            provider.withEhcache(cacheManagerName, closure)
        }
        mc.withEhcache << {CallableWithArgs callable ->
            provider.withEhcache(DEFAULT, callable)
        }
        mc.withEhcache << {String cacheManagerName, CallableWithArgs callable ->
            provider.withEhcache(cacheManagerName, callable)
        }
    }
}