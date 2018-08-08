/*
 * Licensed to David Pilato (the "Author") under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Author licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package fr.pilato.elasticsearch.containers;

import org.apache.http.HttpHost;
import org.jetbrains.annotations.Nullable;
import org.junit.rules.ExternalResource;
import org.rnorth.ducttape.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static fr.pilato.elasticsearch.containers.ElasticsearchContainer.ELASTICSEARCH_DEFAULT_BASE_URL;
import static fr.pilato.elasticsearch.containers.ElasticsearchContainer.ELASTICSEARCH_DEFAULT_VERSION;

/**
 * <p>Junit Test Resource for elasticsearch.</p>
 */
public class ElasticsearchResource extends ExternalResource {

    private static final String DEFAULT_RESOURCE_NAME = "elasticsearch.properties";
    static final String FALLBACK_RESOURCE_NAME = "elasticsearch-default.properties";
    private final String baseUrl;
    private final String version;
    private final Path pluginDir;
    private final String password;
    private final List<String> plugins;
    private final Map<String, String> securedSettings;
    @Nullable private ElasticsearchContainer delegate;

    public ElasticsearchResource() {
        this(DEFAULT_RESOURCE_NAME);
    }

    /**
     * Generate a resource programmatically
     * @param baseUrl           If null defaults to ELASTICSEARCH_DEFAULT_BASE_URL
     * @param version           Elasticsearch version to start. If null defaults to ELASTICSEARCH_DEFAULT_VERSION
     * @param pluginDir         Plugin dir which might contain plugins to install. Can be null.
     * @param plugins           Plugins to install. Can be null.
     * @param securedSettings   Map of secured settings (key/value). Can be null.
     * @param password          X-Pack default password. Can be null.
     */
    public ElasticsearchResource(String baseUrl, String version, Path pluginDir, List<String> plugins, Map<String, String> securedSettings,
                                 String password) {
        this.baseUrl = baseUrl == null ? ELASTICSEARCH_DEFAULT_BASE_URL : baseUrl;
        this.version = version == null ? ELASTICSEARCH_DEFAULT_VERSION : version;
        this.pluginDir = pluginDir;
        this.plugins = plugins;
        this.securedSettings = securedSettings;
        this.password = password;
    }

    public ElasticsearchResource(String resourceName) {
        // Find the latest version this project was built with
        String propVersion;
        String propBaseUrl;
        String propPlugins;
        String propPluginDir;
        String propPassword;
        String defaultBaseUrl = null;
        String defaultVersion = null;
        String defaultPlugins = null;
        String defaultPluginDir = null;
        String defaultPassword = null;
        Properties props = new Properties();
        try {
            props.load(ElasticsearchResource.class.getResourceAsStream(FALLBACK_RESOURCE_NAME));
            defaultBaseUrl = props.getProperty("baseUrl");
            defaultVersion = props.getProperty("version");
            defaultPlugins = props.getProperty("plugins");
            defaultPluginDir = props.getProperty("pluginDir");
            defaultPassword = props.getProperty("password");
        } catch (IOException ignored) {
            // This can normally never happen unless someone modifies the JAR file o_O
        }
        try {
            InputStream stream = ElasticsearchResource.class.getResourceAsStream(resourceName);
            if (stream != null) {
                props.load(stream);
                propBaseUrl = props.getProperty("baseUrl", defaultBaseUrl);
                propVersion = props.getProperty("version", defaultVersion);
                propPlugins = props.getProperty("plugins", defaultPluginDir);
                propPluginDir = props.getProperty("pluginDir", defaultPluginDir);
                propPassword = props.getProperty("password", defaultPassword);
            } else {
                propBaseUrl = defaultBaseUrl;
                propVersion = defaultVersion;
                propPlugins = defaultPlugins;
                propPluginDir = defaultPluginDir;
                propPassword = defaultPassword;
            }
        } catch (IOException e) {
            // We might get that exception if the user provides a badly formatted property file
            propBaseUrl = null;
            propVersion = null;
            propPlugins = null;
            propPluginDir = null;
            propPassword = null;
        }
        baseUrl = propBaseUrl;
        version = propVersion;
        plugins = generateFromCommaSeparatedString(propPlugins);
        pluginDir = propPluginDir == null ? null : Paths.get(propPluginDir);
        password = propPassword;
        securedSettings = Collections.emptyMap();
    }

    private List<String> generateFromCommaSeparatedString(String value) {
        List<String> values = new ArrayList<>();
        if (value != null) {
            values.addAll(Arrays.asList(value.split(",")));
        }

        return values;
    }

    @Override
    protected void before() {
        Preconditions.check("baseUrl can't be null", baseUrl != null);
        Preconditions.check("version can't be null", version != null);
        Preconditions.check("plugins can't be null. Should be empty list instead", plugins != null);
        Preconditions.check("securedSettings can't be null. Should be empty map instead", securedSettings != null);
        delegate = new ElasticsearchContainer()
                .withBaseUrl(baseUrl)
                .withVersion(version)
                .withPluginDir(pluginDir);

        for (String plugin : plugins) {
            delegate.withPlugin(plugin);
        }

        for (Map.Entry<String, String> securedSetting : securedSettings.entrySet()) {
            delegate.withSecureSetting(securedSetting.getKey(), securedSetting.getValue());
        }

        if (password != null && !password.isEmpty()) {
            delegate.withEnv("ELASTIC_PASSWORD", password);
        }

        delegate.start();
    }

    @Override
    protected void after() {
        Preconditions.check("delegate must have been created by before()", delegate != null);
        delegate.stop();
    }

    /**
     * Get the HttpHost instance you can use to build an elasticsearch Rest client
     * @return an HttpHost
     */
    public HttpHost getHost() {
        Preconditions.check("delegate must have been created by before()", delegate != null);
        return delegate.getHost();
    }

    /**
     * Get the TcpHost instance you can use to build an elasticsearch Transport client
     * @return a TcpHost
     */
    public TcpHost getTcpHost() {
        Preconditions.check("delegate must have been created by before()", delegate != null);
        return delegate.getTcpHost();
    }

    @Nullable
    public ElasticsearchContainer getContainer() {
        return delegate;
    }

    public String getPassword() {
        return password;
    }
}
