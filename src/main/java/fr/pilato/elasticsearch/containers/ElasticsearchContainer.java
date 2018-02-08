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
import org.jetbrains.annotations.NotNull;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static fr.pilato.elasticsearch.containers.ElasticsearchResource.FALLBACK_RESOURCE_NAME;

/**
 * Represents an elasticsearch docker instance which exposes by default port 9200.
 * The docker image is by default fetch from docker.elastic.co/elasticsearch/elasticsearch
 * @author dadoonet
 */
public class ElasticsearchContainer<SELF extends ElasticsearchContainer<SELF>> extends GenericContainer<SELF> {

    private static final int ELASTICSEARCH_DEFAULT_PORT = 9200;
    static final String ELASTICSEARCH_DEFAULT_BASE_URL;
    static final String ELASTICSEARCH_DEFAULT_VERSION;
    static {
        Properties props = new Properties();
        try {
            props.load(ElasticsearchResource.class.getResourceAsStream(FALLBACK_RESOURCE_NAME));
        } catch (IOException ignored) {
        }
        ELASTICSEARCH_DEFAULT_BASE_URL = props.getProperty("baseUrl");
        ELASTICSEARCH_DEFAULT_VERSION = props.getProperty("version");
    }

    private String baseUrl = ELASTICSEARCH_DEFAULT_BASE_URL;
    private String version = ELASTICSEARCH_DEFAULT_VERSION;
    private Path pluginDir = null;
    private List<String> plugins = new ArrayList<>();

    /**
     * Define the elasticsearch version to start
     * @param version  Elasticsearch Version like 5.6.6 or 6.2.1
     * @return this
     */
    public ElasticsearchContainer withVersion(String version) {
        this.version = version;
        return this;
    }

    /**
     * Define the elasticsearch docker registry base url
     * @param baseUrl  defaults to docker.elastic.co/elasticsearch/elasticsearch
     * @return this
     */
    public ElasticsearchContainer withBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }


    /**
     * Plugin name to install. Note that will download the plugin from internet the first time you build the image
     * @param pluginName plugins dir
     * @return this
     */
    public ElasticsearchContainer withPlugin(String pluginName) {
        plugins.add(pluginName);
        return this;
    }

    /**
     * Path to plugin dir which contains plugins that needs to be installed
     * @param pluginDir plugins dir
     * @return this
     */
    public ElasticsearchContainer withPluginDir(Path pluginDir) {
        if (pluginDir == null) {
            return this;
        }

        // When we have a plugin dir, we need to mount it in the docker instance
        this.pluginDir = createVolumeDirectory(true);

        // We create the volume that will be needed for plugins
        addFileSystemBind(this.pluginDir.toString(), "/plugins", BindMode.READ_ONLY);

        logger().debug("Installing plugins from [{}]", pluginDir);
        try {
            Files.list(pluginDir).forEach(path -> {
                logger().trace("File found in [{}]: [{}]", pluginDir, path);
                if (path.toString().endsWith(".zip")) {
                    logger().debug("Copying [{}] to [{}]", path.getFileName(), this.pluginDir.toAbsolutePath().toString());
                    try {
                        Files.copy(path, this.pluginDir.resolve(path.getFileName()));
                        withPlugin("file:///tmp/plugins/" + path.getFileName());
                    } catch (IOException e) {
                        logger().error("Error while copying", e);
                    }
                }
            });
        } catch (IOException e) {
            logger().error("Error listing plugins", e);
        }
        return this;
    }

    @NotNull
    @Override
    protected Set<Integer> getLivenessCheckPorts() {
        Set<Integer> ports = new HashSet<>(super.getLivenessCheckPorts());
        ports.add(getMappedPort(ELASTICSEARCH_DEFAULT_PORT));
        return ports;
    }

    @Override
    protected void configure() {
        logger().info("Starting an elasticsearch container using version [{}] from [{}]", version, baseUrl);
        ImageFromDockerfile dockerImage = new ImageFromDockerfile()
                .withDockerfileFromBuilder(builder -> {
                    builder.from(baseUrl + ":" + version);
                    if (pluginDir != null) {
                        // We need to map the local dir which contains plugins with the container
                        builder.copy("/tmp/plugins", "/tmp/plugins");
                    }
                    for (String plugin : plugins) {
                        logger().debug("Installing plugin [{}]", plugin);
                        builder.run("bin/elasticsearch-plugin install " + plugin);
                    }
                    String s = builder.build();

                    logger().debug("Image generated: {}", s);
                });

        if (pluginDir != null) {
            dockerImage.withFileFromFile("/tmp/plugins", this.pluginDir.toAbsolutePath().toFile());
        }

        setImage(dockerImage);
        addExposedPort(ELASTICSEARCH_DEFAULT_PORT);
    }

    public HttpHost getHost() {
        return new HttpHost(getContainerIpAddress(), getMappedPort(ELASTICSEARCH_DEFAULT_PORT));
    }
}
