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


import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ElasticsearchResourceWithPluginsDirAndVersionTest extends ElasticsearchResourceBaseTest {
    @ClassRule
    public static ElasticsearchResource elasticsearch = new ElasticsearchResource("elasticsearch-plugins-dir-version.properties");

    @Override
    ElasticsearchResource getElasticsearchResource() {
        return elasticsearch;
    }

    @Test
    public void testPluginsAreInstalled() {
        String list = executeCommandInDocker("bin/elasticsearch-plugin", "list");
        assertThat(list, containsString("ingest-attachment"));
    }

    @Test
    public void testPluginsAreLoaded() throws IOException {
        Response response = restClient.performRequest("GET", "/_cat/plugins");
        assertThat(response.getStatusLine().getStatusCode(), is(200));
        String responseAsString = EntityUtils.toString(response.getEntity());
        assertThat(responseAsString, containsString("ingest-attachment"));
    }

    private String executeCommandInDocker(String... params) {
        try {
            return elasticsearch.getContainer().execInContainer(params).getStdout();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
