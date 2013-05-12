/*
 * Copyright 2012-2013 the original author or authors.
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
    String version = '1.1.0'
    // the version or versions of Griffon the plugin is designed for
    String griffonVersion = '1.3.0 > *'
    // the other plugins this plugin depends on
    Map dependsOn = [lombok: '0.5.0']
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
The Ehcache plugin enables lightweight access to [Ehcache][1] caches
This plugin does NOT provide domain classes nor dynamic finders like GORM does.

Usage
-----
Upon installation the plugin will generate the following artifacts in
`$appdir/griffon-app/conf`:

 * EhcacheConfig.groovy - contains the database definitions.
 * BootstrapEhcache.groovy - defines init/destroy hooks for data to be manipulated
   during app startup/shutdown.

A new dynamic method named `withEhcache` will be injected into all controllers,
giving you access to a `net.sf.ehcache.CacheManager` object, with which you'll
be able to make calls to the repository. Remember to make all repository calls
off the UI thread otherwise your application may appear unresponsive when doing
long computations inside the UI thread.

This method is aware of multiple caches. If no cacheManagerName is specified when
calling it then the default cacheManager will be selected. Here are two example
usages, the first queries against the default cacheManager while the second
queries a cacheManager whose name has been configured as 'internal'

    package sample
    class SampleController {
        def queryAllCaches = {
            withEhcache { cacheManagerName, cacheManager -> ... }
            withEhcache('internal') { cacheManagerName, cacheManaher -> ... }
        }
    }

The following list enumerates all the variants of the injected method

 * `<R> R withEhcache(Closure<R> stmts)`
 * `<R> R withEhcache(CallableWithArgs<R> stmts)`
 * `<R> R withEhcache(String cacheManagerName, Closure<R> stmts)`
 * `<R> R withEhcache(String cacheManagerName, CallableWithArgs<R> stmts)`

These methods are also accessible to any component through the singleton
`griffon.plugins.ehcache.EhcacheConnector`. You can inject these methods to
non-artifacts via metaclasses. Simply grab hold of a particular metaclass and
call `EhcacheEnhancer.enhance(metaClassInstance, ehcacheProviderInstance)`.

Configuration
-------------
### EhcacheAware AST Transformation

The preferred way to mark a class for method injection is by annotating it with
`@griffon.plugins.ehcache.EhcacheAware`. This transformation injects the
`griffon.plugins.ehcache.EhcacheContributionHandler` interface and default
behavior that fulfills the contract.

### Dynamic method injection

Dynamic methods will be added to controllers by default. You can
change this setting by adding a configuration flag in `griffon-app/conf/Config.groovy`

    griffon.ehcache.injectInto = ['controller', 'service']

Dynamic method injection will be skipped for classes implementing
`griffon.plugins.ehcache.EhcacheContributionHandler`.

### Events

The following events will be triggered by this addon

 * EhcacheConnectStart[config, cacheManagerName] - triggered before connecting
   to the cacheManager
 * EhcacheConnectEnd[cacheManagerName, cacheManager] - triggered after connecting
   to the cacheManager
 * EhcacheDisconnectStart[config, cacheManagerName, cacheManager] - triggered
   before disconnecting from the cacheManager
 * EhcacheDisconnectEnd[config, cacheManagerName] - triggered after disconnecting
   from the cacheManager

### Multiple Stores

The config file `EhcacheConfig.groovy` defines a default cacheManager block. As
the name implies this is the cacheManager used by default, however you can
configure named databases by adding a new config block. For example connecting
to a cacheManager whose name is 'internal' can be done in this way

    cacheManagers {
        internal {
            url = 'classpath://ehcache-internal.xml'
        }
    }

This block can be used inside the `environments()` block in the same way as the
default cacheManager block is used.

### Example

A trivial sample application can be found at [https://github.com/aalmiray/griffon_sample_apps/tree/master/persistence/ehcache][2]

Testing
-------

Dynamic methods will not be automatically injected during unit testing, because
addons are simply not initialized for this kind of tests. However you can use
`EhcacheEnhancer.enhance(metaClassInstance, ehcacheProviderInstance)` where
`ehcacheProviderInstance` is of type `griffon.plugins.ehcache.EhcacheProvider`.
The contract for this interface looks like this

    public interface EhcacheProvider {
        <R> R withEhcache(Closure<R> closure);
        <R> R withEhcache(CallableWithArgs<R> callable);
        <R> R withEhcache(String cacheManagerName, Closure<R> closure);
        <R> R withEhcache(String cacheManagerName, CallableWithArgs<R> callable);
    }

It's up to you define how these methods need to be implemented for your tests.
For example, here's an implementation that never fails regardless of the
arguments it receives

    class MyEhcacheProvider implements EhcacheProvider {
        public <R> R withEhcache(Closure<R> closure) { null }
        public <R> R withEhcache(CallableWithArgs<R> callable) { null }
        public <R> R withEhcache(String cacheManagerName, Closure<R> closure) { null }
        public <R> R withEhcache(String cacheManagerName, CallableWithArgs<R> callable) { null }
    }

This implementation may be used in the following way

    class MyServiceTests extends GriffonUnitTestCase {
        void testSmokeAndMirrors() {
            MyService service = new MyService()
            EhcacheEnhancer.enhance(service.metaClass, new MyEhcacheProvider())
            // exercise service methods
        }
    }

On the other hand, if the service is annotated with `@EhcacheAware` then usage
of `EhcacheEnhancer` should be avoided at all costs. Simply set `ehcacheProviderInstance`
on the service instance directly, like so, first the service definition

    @griffon.plugins.ehcache.EhcacheAware
    class MyService {
        def serviceMethod() { ... }
    }

Next is the test

    class MyServiceTests extends GriffonUnitTestCase {
        void testSmokeAndMirrors() {
            MyService service = new MyService()
            service.ehcacheProvider = new MyEhcacheProvider()
            // exercise service methods
        }
    }

Tool Support
------------

### DSL Descriptors

This plugin provides DSL descriptors for Intellij IDEA and Eclipse (provided
you have the Groovy Eclipse plugin installed). These descriptors are found
inside the `griffon-ehcache-compile-x.y.z.jar`, with locations

 * dsdl/ehcache.dsld
 * gdsl/ehcache.gdsl

### Lombok Support

Rewriting Java AST in a similar fashion to Groovy AST transformations is
possible thanks to the [lombok][3] plugin.

#### JavaC

Support for this compiler is provided out-of-the-box by the command line tools.
There's no additional configuration required.

#### Eclipse

Follow the steps found in the [Lombok][3] plugin for setting up Eclipse up to
number 5.

 6. Go to the path where the `lombok.jar` was copied. This path is either found
    inside the Eclipse installation directory or in your local settings. Copy
    the following file from the project's working directory

         $ cp $USER_HOME/.griffon/<version>/projects/<project>/plugins/ehcache-<version>/dist/griffon-ehcache-compile-<version>.jar .

 6. Edit the launch script for Eclipse and tweak the boothclasspath entry so
    that includes the file you just copied

        -Xbootclasspath/a:lombok.jar:lombok-pg-<version>.jar:\
        griffon-lombok-compile-<version>.jar:griffon-ehcache-compile-<version>.jar

 7. Launch Eclipse once more. Eclipse should be able to provide content assist
    for Java classes annotated with `@EhcacheAware`.

#### NetBeans

Follow the instructions found in [Annotation Processors Support in the NetBeans
IDE, Part I: Using Project Lombok][4]. You may need to specify
`lombok.core.AnnotationProcessor` in the list of Annotation Processors.

NetBeans should be able to provide code suggestions on Java classes annotated
with `@EhcacheAware`.

#### Intellij IDEA

Follow the steps found in the [Lombok][3] plugin for setting up Intellij IDEA
up to number 5.

 6. Copy `griffon-ehcache-compile-<version>.jar` to the `lib` directory

         $ pwd
           $USER_HOME/Library/Application Support/IntelliJIdea11/lombok-plugin
         $ cp $USER_HOME/.griffon/<version>/projects/<project>/plugins/ehcache-<version>/dist/griffon-ehcache-compile-<version>.jar lib

 7. Launch IntelliJ IDEA once more. Code completion should work now for Java
    classes annotated with `@EhcacheAware`.


[1]: http://www.ehcache.org
[2]: https://github.com/aalmiray/griffon_sample_apps/tree/master/persistence/ehcache
[3]: /plugin/lombok
[4]: http://netbeans.org/kb/docs/java/annotations-lombok.html
'''
}
