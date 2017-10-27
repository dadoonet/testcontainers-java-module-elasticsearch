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


import org.junit.Rule;

import java.io.IOException;

import static org.junit.Assume.assumeTrue;

public class ElasticsearchResourceDefaultsTest extends ElasticsearchResourceBaseTest {
    @Rule
    public ElasticsearchResource elasticsearch = new ElasticsearchResource();

    @Override
    ElasticsearchResource getElasticsearchResource() {
        return elasticsearch;
    }

    @Override
    public void elasticsearchTest() throws IOException {
        // Let's just ignore this test because with elasticsearch 6.0.0-rc1, we don't have a default
        // User/Password anymore
        assumeTrue("We skip the test with elasticsearch 6.0.0-rc1 for now", false);
    }
}
