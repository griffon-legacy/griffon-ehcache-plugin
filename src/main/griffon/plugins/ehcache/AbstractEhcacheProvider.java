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

package griffon.plugins.ehcache;

import griffon.util.CallableWithArgs;
import groovy.lang.Closure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.ehcache.CacheManager;

import static griffon.util.GriffonNameUtils.isBlank;

/**
 * @author Andres Almiray
 */
public abstract class AbstractEhcacheProvider implements EhcacheProvider {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractEhcacheProvider.class);
    private static final String DEFAULT = "default";

    public <R> R withEhcache(Closure<R> closure) {
        return withEhcache(DEFAULT, closure);
    }

    public <R> R withEhcache(String cacheManagerName, Closure<R> closure) {
        if (isBlank(cacheManagerName)) cacheManagerName = DEFAULT;
        if (closure != null) {
            CacheManager cm = getCacheManager(cacheManagerName);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Executing statement on cacheManager '" + cacheManagerName + "'");
            }
            return closure.call(cacheManagerName, cm);
        }
        return null;
    }

    public <R> R withEhcache(CallableWithArgs<R> callable) {
        return withEhcache(DEFAULT, callable);
    }

    public <R> R withEhcache(String cacheManagerName, CallableWithArgs<R> callable) {
        if (isBlank(cacheManagerName)) cacheManagerName = DEFAULT;
        if (callable != null) {
            CacheManager cm = getCacheManager(cacheManagerName);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Executing statement on cacheManager '" + cacheManagerName + "'");
            }
            callable.setArgs(new Object[]{cacheManagerName, cm});
            return callable.call();
        }
        return null;
    }

    protected abstract CacheManager getCacheManager(String cacheManagerName);
}