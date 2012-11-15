cacheManager {
    caches {
        defaultCache {
            maxElementsInMemory = 10000
            eternal = false
            timeToIdleSeconds = 120
            timeToLiveSeconds = 120
            overflowToDisk = true
            diskSpoolBufferSizeMB = 30
            maxElementsOnDisk = 10000000
            diskPersistent = false
            diskExpiryThreadIntervalSeconds = 120
            memoryStoreEvictionPolicy = 'LRU'
        }
    }
}
environments {
    development {
        cacheManager {
            // url = 'classpath://ehcache-dev.xml'
        }
    }
    test {
        cacheManager {
            // url = 'classpath://ehcache-test.xml'
        }
    }
    production {
        cacheManager {
            // url = 'classpath://ehcache-prod.xml'
        }
    }
}
