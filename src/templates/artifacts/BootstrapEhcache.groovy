import net.sf.ehcache.CacheManager

class BootstrapEhcache {
    def init = { String cacheManagerName, CacheManager cacheManager ->
    }

    def destroy = { String cacheManagerName, CacheManager cacheManager ->
    }
}