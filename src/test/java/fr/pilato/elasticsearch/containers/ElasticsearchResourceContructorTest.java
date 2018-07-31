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


import org.junit.Ignore;

import java.nio.file.Paths;
import java.util.Collections;

/**
 * We just use this class to help for documenting the project.
 * We disable the test (well, it's not a test technically speaking)
 */
@Ignore
public class ElasticsearchResourceContructorTest {

    // We don't annotate it as a Rule as we just want to have it here for documentation
    public ElasticsearchResource elasticsearch = new ElasticsearchResource(
            "docker.elastic.co/elasticsearch/elasticsearch", // baseUrl (can be null)
            "6.3.2",                                         // version (can be null)
            Paths.get("/path/to/zipped-plugins-dir"),        // pluginsDir (can be null)
            Collections.singletonList("ingest-attachment"),  // standard plugins (can be empty)
            Collections.singletonMap("foo", "bar"),          // Map of secured settings (can be empty)
            "changeme");                                     // X-Pack security password to set (can be null)
}
