/*
 * Copyright 2011-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author Andres Almiray
 */
 class EhcacheGriffonPlugin {
    // the plugin version
    String version = '0.1'
    // the version or versions of Griffon the plugin is designed for
    String griffonVersion = '1.1.0 > *'
    // the other plugins this plugin depends on
    Map dependsOn = [:]
    // resources that are included in plugin packaging
    List pluginIncludes = []
    // the plugin license
    String license = 'Apache Software License 2.0'
    // Toolkit compatibility. No value means compatible with all
    // Valid values are: swing, javafx, swt, pivot, gtk
    List toolkits = []
    // Platform compatibility. No value means compatible with all
    // Valid values are:
    // linux, linux64, windows, windows64, macosx, macosx64, solaris
    List platforms = []
    // URL where documentation can be found
    String documentation = ''
    // URL where source can be found
    String source = 'https://github.com/griffon/griffon-ehcache-plugin'

    List authors = [
        [
            name: 'Andres Almiray',
            email: 'aalmiray@yahoo.com'
        ]
    ]
    String title = 'Ehcache support'
    String description = '''
The Ehcache plugin enables lightweight access to [Ehcache][1] stores.
This plugin does NOT provide domain classes nor dynamic finders like GORM does.

Usage
-----
Upon installation the plugin will generate the following artifacts in `$appdir/griffon-app/conf`:

 * EhcacheConfig.groovy - contains the store definitions.
 * BootstrapEhcache.groovy - defines init/destroy hooks for data to be manipulated during app startup/shutdown.

A new dynamic method named `withEhcache` will be injected into all controllers,
giving you access to `net.sf.ehcache.CacheManager` and `net.sf.ehcache.Cache` instances, with which you'll be able
to make calls to the store. Remember to make all store calls off the EDT
otherwise your application may appear unresponsive when doing long computations
inside the EDT.

This method is aware of multiple stores. If no cacheManagerName is specified when calling
it then the default store will be selected. Here are two example usages, the first
queries against the default store while the second queries a store whose name has
been configured as 'internal'

    package sample
    class SampleController {
        def queryAllStores = {
            withEhcache { cacheManagerName, client -> ... }
            withEhcache('internal') { cacheManagerName, client -> ... }
        }
    }

This method is also accessible to any component through the singleton `griffon.plugins.ehcache.EhcacheConnector`.
You can inject these methods to non-artifacts via metaclasses. Simply grab hold of a particular metaclass and call
`EhcacheEnhancer.enhance(metaClassInstance, ehcacheProviderInstance)`.

Configuration
-------------
### Dynamic method injection

The `withEhcache()` dynamic method will be added to controllers by default. You can
change this setting by adding a configuration flag in `griffon-app/conf/Config.groovy`

    griffon.ehcache.injectInto = ['controller', 'service']

### Events

The following events will be triggered by this addon

 * EhcacheConnectStart[config, cacheManagerName] - triggered before connecting to the store
 * EhcacheConnectEnd[cacheManagerName, client] - triggered after connecting to the store
 * EhcacheDisconnectStart[config, cacheManagerName, client] - triggered before disconnecting from the store
 * EhcacheDisconnectEnd[config, cacheManagerName] - triggered after disconnecting from the store

### Multiple Stores

The config file `EhcacheConfig.groovy` defines a default client block. As the name
implies this is the client used by default, however you can configure named clients
by adding a new config block. For example connecting to a client whose name is 'internal'
can be done in this way

    stores {
        internal {
            host = 'http://localhost:8090'
        }
    }

This block can be used inside the `environments()` block in the same way as the
default client block is used.

### Example

A trivial sample application can be found at [https://github.com/aalmiray/griffon_sample_apps/tree/master/persistence/ehcache][2]

Testing
-------
The `withEhcache()` dynamic method will not be automatically injected during unit testing, because addons are simply not initialized
for this kind of tests. However you can use `EhcacheEnhancer.enhance(metaClassInstance, ehcacheProviderInstance)` where 
`ehcacheProviderInstance` is of type `griffon.plugins.ehcache.EhcacheProvider`. The contract for this interface looks like this

    public interface EhcacheProvider {
        Object withEhcache(Closure closure);
        Object withEhcache(String cacheManagerName, Closure closure);
        <T> T withEhcache(CallableWithArgs<T> callable);
        <T> T withEhcache(String cacheManagerName, CallableWithArgs<T> callable);
    }

It's up to you define how these methods need to be implemented for your tests. For example, here's an implementation that never
fails regardless of the arguments it receives

    class MyEhcacheProvider implements EhcacheProvider {
        Object withEhcache(String cacheManagerName = 'default', Closure closure) { null }
        public <T> T withEhcache(String cacheManagerName = 'default', CallableWithArgs<T> callable) { null }
    }

This implementation may be used in the following way

    class MyServiceTests extends GriffonUnitTestCase {
        void testSmokeAndMirrors() {
            MyService service = new MyService()
            EhcacheEnhancer.enhance(service.metaClass, new MyEhcacheProvider())
            // exercise service methods
        }
    }


[1]: http://code.google.com/p/ehcache/
[2]: https://github.com/aalmiray/griffon_sample_apps/tree/master/persistence/ehcache
'''
}
