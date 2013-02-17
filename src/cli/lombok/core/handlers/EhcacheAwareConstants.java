/*
 * Copyright 2012-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package lombok.core.handlers;

import lombok.core.BaseConstants;
import lombok.core.util.MethodDescriptor;

import static lombok.core.util.MethodDescriptor.args;
import static lombok.core.util.MethodDescriptor.type;
import static lombok.core.util.MethodDescriptor.typeParams;

/**
 * @author Andres Almiray
 */
public interface EhcacheAwareConstants extends BaseConstants {
    String EHCACHE_PROVIDER_TYPE = "griffon.plugins.ehcache.EhcacheProvider";
    String DEFAULT_EHCACHE_PROVIDER_TYPE = "griffon.plugins.ehcache.DefaultEhcacheProvider";
    String EHCACHE_CONTRIBUTION_HANDLER_TYPE = "griffon.plugins.ehcache.EhcacheContributionHandler";
    String EHCACHE_PROVIDER_FIELD_NAME = "this$ehcacheProvider";
    String METHOD_GET_EHCACHE_PROVIDER = "getEhcacheProvider";
    String METHOD_SET_EHCACHE_PROVIDER = "setEhcacheProvider";
    String METHOD_WITH_EHCACHE = "withEhcache";
    String PROVIDER = "provider";

    MethodDescriptor[] METHODS = new MethodDescriptor[] {
        MethodDescriptor.method(
            type(R),
            typeParams(R),
            METHOD_WITH_EHCACHE,
            args(type(GROOVY_LANG_CLOSURE, R))
        ),
        MethodDescriptor.method(
            type(R),
            typeParams(R),
            METHOD_WITH_EHCACHE,
            args(
                type(JAVA_LANG_STRING),
                type(GROOVY_LANG_CLOSURE, R))
        ),
        MethodDescriptor.method(
            type(R),
            typeParams(R),
            METHOD_WITH_EHCACHE,
            args(type(GRIFFON_UTIL_CALLABLEWITHARGS, R))
        ),
        MethodDescriptor.method(
            type(R),
            typeParams(R),
            METHOD_WITH_EHCACHE,
            args(
                type(JAVA_LANG_STRING),
                type(GRIFFON_UTIL_CALLABLEWITHARGS, R))
        )
    };
}
