/*
 * Copyright 2015 Netflix, Inc.
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
package com.netflix.iep.karyon;


import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.netflix.config.ConfigurationManager;
import netflix.admin.AdminConfigImpl;
import netflix.admin.AdminContainerConfig;
import netflix.admin.GlobalModelContextOverride;
import netflix.adminresources.resources.KaryonWebAdminModule;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.swing.text.html.Option;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;


public final class KaryonModule extends AbstractModule {

  private static final Logger LOGGER = LoggerFactory.getLogger(KaryonModule.class);

  private static class OptionalInjections {
    @Inject(optional = true)
    private Configuration config;

    private boolean initialized = false;

    void init() {
      if (!initialized) {
        if (config == null) {
          config = ConfigurationManager.getConfigInstance();
        }
        loadProperties("iep-karyon");
        initialized = true;
      }
    }

    Configuration getConfig() {
      return config;
    }

    private void loadProperties(String name) {
      try {
        ConfigurationManager.loadCascadedPropertiesFromResources(name);
      } catch (IOException e) {
        LOGGER.warn("failed to load properties for '" + name + "'");
      }
    }
  }

  @Override protected void configure() {
    install(new KaryonWebAdminModule());
  }

  @Provides
  @Singleton
  private AdminContainerConfig providesAdminConfig(final OptionalInjections opts) {
    // Need to ensure that the admin config is not created until the v1 configuration object
    // is ready and the karyon properties have been loaded.
    opts.init();
    return new AdminConfigImpl();
  }

  @Provides
  @Singleton
  private GlobalModelContextOverride provideContextOverrides(final OptionalInjections opts) {
    opts.init();
    return new GlobalModelContextOverride() {
      @Override public Properties overrideProperties(Properties properties) {
        Configuration config = opts.getConfig();
        Iterator<String> keys = config.getKeys();
        while (keys.hasNext()) {
          String k = keys.next();
          String v = config.getString(k);
          if (v != null) {
            properties.setProperty(k, config.getString(k));
          } else {
            LOGGER.debug("skipping property '{}' with null value", k);
          }
        }
        return properties;
      }
    };
  }

  @Override public boolean equals(Object obj) {
    return obj != null && getClass().equals(obj.getClass());
  }

  @Override public int hashCode() {
    return getClass().hashCode();
  }
}
