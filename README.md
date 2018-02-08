# TestContainers elasticsearch testing module

[![Build Status](https://travis-ci.org/dadoonet/testcontainers-java-module-elasticsearch.svg?branch=master)](https://travis-ci.org/dadoonet/testcontainers-java-module-elasticsearch)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/fr.pilato.elasticsearch.testcontainers/testcontainers-elasticsearch/badge.svg?style=flat-square)](https://maven-badges.herokuapp.com/maven-central/fr.pilato.elasticsearch.testcontainers/testcontainers-elasticsearch/)

Testcontainers module for [elasticsearch](https://www.elastic.co/products/elasticsearch).

Note that it's based on the [official Docker image](https://www.elastic.co/guide/en/elasticsearch/reference/6.2/docker.html) provided by elastic.

See [testcontainers.org](https://www.testcontainers.org) for more information about Testcontainers.

## Usage example

You can start an elasticsearch container instance from any Java application by using:

```java
// Create the elasticsearch container.
ElasticsearchContainer container = new ElasticsearchContainer();

// Optional but highly recommended: Specify the version you need.
container.withVersion("6.2.0");

// Optional: you can also set what is the Docker registry you want to use with.
container.withBaseUrl("docker.elastic.co/elasticsearch/elasticsearch");

// Optional: define which plugin you would like to install.
// It will download it from internet when building the image
container.withPlugin("discovery-gce");

// Optional: define the plugins directory which should contain plugins ZIP files you want to install.
// Note that if you want to just download an official plugin, use withPlugin(String) instead.
container.withPluginDir(Paths.get("/path/to/zipped-plugins-dir"));

// Optional: you can also change the password for X-Pack (for versions >= 6.1).
container.withEnv("ELASTIC_PASSWORD", "changeme");

// Configure the container (mandatory).
container.configure();

// Start the container. This step might take some time...
container.start();

// Do whatever you want here.
final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("elastic", "changeme"));
RestClient client = RestClient.builder(container.getHost())
        .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider))
        .build();
Response response = client.performRequest("GET", "/");

// Stop the container.
container.stop();
```

## JUnit 4 Usage example

Running elasticsearch as a resource during a test:

```java
public class SomeTest {
    @Rule
    public ElasticsearchResource elasticsearch = new ElasticsearchResource();

    @Test
    public void someTestMethod() {
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials("elastic", "changeme"));

        RestClient client = RestClient.builder(elasticsearch.getHost())
                .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider))
                .build();
        Response response = client.performRequest("GET", "/");
        assertThat(response.getStatusLine().getStatusCode(), is(200));
```

The default version is read from a property file named `elasticsearch.properties` in `fr.pilato.elasticsearch.containers` package.
If you provide an `elasticsearch.properties` in `fr.pilato.elasticsearch.containers` package,
the settings will be read from it:

```properties
baseUrl=docker.elastic.co/elasticsearch/elasticsearch
version=6.2.0
```

You can also define this programmatically with:

```
@Rule
public ElasticsearchResource elasticsearch = new ElasticsearchResource(
    "docker.elastic.co/elasticsearch/elasticsearch", // baseUrl (can be null)
    "6.2.0",                                         // version (can be null)
    Paths.get("/path/to/zipped-plugins-dir"),        // pluginsDir (can be null)
    "changeme");                                     // X-Pack security password to set (can be null)
```

## Running without x-pack

If you prefer to start a Docker image without x-pack plugin, which means with no security or
other advanced features, you can use this baseUrl instead: `docker.elastic.co/elasticsearch/elasticsearch-oss`.

## Dependency information

### Maven

```
<dependency>
    <groupId>fr.pilato.elasticsearch.testcontainers</groupId>
    <artifactId>testcontainers-elasticsearch</artifactId>
    <version>0.1-SNAPSHOT</version>
</dependency>
```

### Gradle

```
compile group: 'fr.pilato.elasticsearch.testcontainers', name: 'testcontainers-elasticsearch', version: '0.1-SNAPSHOT'
```


## License

See [LICENSE](LICENSE).

## Copyright

Copyright (c) 2017, 2018 David Pilato.

