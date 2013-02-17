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

import net.sf.ehcache.CacheManager;

/**
 * @author Andres Almiray
 */
public class DefaultEhcacheProvider extends AbstractEhcacheProvider {
    private static final DefaultEhcacheProvider INSTANCE;

    static {
        INSTANCE = new DefaultEhcacheProvider();
    }

    public static DefaultEhcacheProvider getInstance() {
        return INSTANCE;
    }

    @Override
    protected CacheManager getCacheManager(String cacheManagerName) {
        return CacheManagerHolder.getInstance().fetchCacheManager(cacheManagerName);
    }
}