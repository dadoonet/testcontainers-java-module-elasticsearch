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
import java.util.Properties;

import static fr.pilato.elasticsearch.containers.ElasticsearchContainer.ELASTICSEARCH_DEFAULT_BASE_URL;

/**
 * <p>Junit Test Resource for elasticsearch.</p>
 */
public class ElasticsearchResource extends ExternalResource {

    private static final String DEFAULT_RESOURCE_NAME = "elasticsearch.properties";
    private static final String FALLBACK_RESOURCE_NAME = "elasticsearch-default.properties";
    private final String baseUrl;
    private final String version;
    @Nullable private ElasticsearchContainer delegate;

    public ElasticsearchResource() {
        this(DEFAULT_RESOURCE_NAME);
    }

    /**
     * Generate a resource programmatically
     * @param baseUrl   If null defaults to ELASTICSEARCH_DEFAULT_BASE_URL
     * @param version   Elasticsearch version to start
     */
    public ElasticsearchResource(String baseUrl, String version) {
        this.baseUrl = baseUrl == null ? ELASTICSEARCH_DEFAULT_BASE_URL : baseUrl;
        this.version = version;
    }

    public ElasticsearchResource(String resourceName) {
        // Find the latest version this project was built with
        String propVersion;
        String propBaseUrl;
        String defaultBaseUrl = null;
        String defaultVersion = null;
        Properties props = new Properties();
        try {
            props.load(ElasticsearchResource.class.getResourceAsStream(FALLBACK_RESOURCE_NAME));
            defaultBaseUrl = props.getProperty("baseUrl");
            defaultVersion = props.getProperty("version");
        } catch (IOException ignored) {
            // This can normally never happen unless someone modifies the JAR file o_O
        }
        try {
            InputStream stream = ElasticsearchResource.class.getResourceAsStream(resourceName);
            if (stream != null) {
                props.load(stream);
                propBaseUrl = props.getProperty("baseUrl", defaultBaseUrl);
                propVersion = props.getProperty("version", defaultVersion);
            } else {
                propBaseUrl = defaultBaseUrl;
                propVersion = defaultVersion;
            }
        } catch (IOException e) {
            // We might get that exception if the user provides a badly formatted property file
            propBaseUrl = null;
            propVersion = null;
        }
        baseUrl = propBaseUrl;
        version = propVersion;
    }

    @Override
    protected void before() throws Throwable {
        Preconditions.check("baseUrl can't be null", baseUrl != null);
        Preconditions.check("version can't be null", version != null);
        delegate = new ElasticsearchContainer()
                .withBaseUrl(baseUrl)
                .withVersion(version);
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
}
