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


import com.github.dockerjava.api.exception.DockerClientException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.junit.After;
import org.junit.Test;
import org.testcontainers.containers.ContainerFetchException;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;

import static fr.pilato.elasticsearch.containers.ElasticsearchContainer.ELASTICSEARCH_DEFAULT_BASE_URL;
import static fr.pilato.elasticsearch.containers.ElasticsearchContainer.ELASTICSEARCH_DEFAULT_VERSION;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assume.assumeTrue;

public class ElasticsearchContainerTest {

    private ElasticsearchContainer container = null;
    private RestClient client = null;

    @After
    public void stopRestClient() throws IOException {
        if (client != null) {
            client.close();
        }
    }

    @After
    public void stopContainer() {
        if (container != null) {
            container.stop();
        }
    }

    @Test
    public void elasticsearchNoVersionTest() throws IOException {
        container = new ElasticsearchContainer();
        container.withEnv("ELASTIC_PASSWORD", "changeme");
        container.start();
        Response response = getClient(container).performRequest("GET", "/");
        assertThat(response.getStatusLine().getStatusCode(), is(200));
    }

    @Test
    public void elasticsearchDefaultTest() throws IOException {
        container = new ElasticsearchContainer();
        container.withVersion(ELASTICSEARCH_DEFAULT_VERSION);
        container.withEnv("ELASTIC_PASSWORD", "changeme");
        container.start();
        Response response = getClient(container).performRequest("GET", "/");
        assertThat(response.getStatusLine().getStatusCode(), is(200));
    }

    @Test
    public void elasticsearchFullTest() throws IOException {
        container = new ElasticsearchContainer();
        container.withVersion(ELASTICSEARCH_DEFAULT_VERSION);
        container.withBaseUrl(ELASTICSEARCH_DEFAULT_BASE_URL);

        // We need to read where we exactly put the files
        Properties props = new Properties();
        try {
            props.load(ElasticsearchResource.class.getResourceAsStream("elasticsearch-plugins-dir.properties"));
            String pluginDir = props.getProperty("pluginDir");
            container.withPluginDir(Paths.get(pluginDir));
        } catch (IOException ignored) {
            // This can normally never happen unless someone modifies the test resources dir o_O
        }

        container.withEnv("ELASTIC_PASSWORD", "changeme");

        container.start();

        Response response = getClient(container).performRequest("GET", "/");
        assertThat(response.getStatusLine().getStatusCode(), is(200));

        response = getClient(container).performRequest("GET", "/_cat/plugins");
        assertThat(response.getStatusLine().getStatusCode(), is(200));
        String responseAsString = EntityUtils.toString(response.getEntity());
        assertThat(responseAsString, containsString("ingest-attachment"));
    }

    @Test
    public void elasticsearchWithPlugin() throws IOException {
        // This test might fail if no internet connection is available
        // As elasticsearch-plugin would download files from internet
        // In which case, we should just ignore the test

        container = new ElasticsearchContainer();
        container.withVersion(ELASTICSEARCH_DEFAULT_VERSION);
        container.withPlugin("discovery-gce");
        container.withEnv("ELASTIC_PASSWORD", "changeme");

        try {
            container.start();
            Response response = getClient(container).performRequest("GET", "/");
            assertThat(response.getStatusLine().getStatusCode(), is(200));

            response = getClient(container).performRequest("GET", "/_cat/plugins");
            assertThat(response.getStatusLine().getStatusCode(), is(200));
            String responseAsString = EntityUtils.toString(response.getEntity());
            assertThat(responseAsString, containsString("discovery-gce"));
        } catch (ContainerFetchException exception) {
            assertThat(exception.getCause(), instanceOf(DockerClientException.class));
            assertThat(exception.getCause().getMessage(), containsString("The command '/bin/sh -c bin/elasticsearch-plugin install discovery-gce' returned a non-zero code: 1"));
            assumeTrue("We can't test this if internet is not available because we can't download elasticsearch plugins.", false);
        }
    }

    private RestClient getClient(ElasticsearchContainer container) {
        if (client == null) {
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials("elastic", "changeme"));

            client = RestClient.builder(container.getHost())
                    .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider))
                    .build();
        }

        return client;
    }
}
